package com.app.chat.presentation.base

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    // Override to provide the binding inflater
    protected abstract fun bindView(inflater: LayoutInflater, container: ViewGroup?): VB

    // Override to customize input mode (resize, pan, nothing)
    open val softInputMode: Int? = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = bindView(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSoftInputMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSoftInputMode() {
        softInputMode?.let { mode ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                requireActivity().window.setSoftInputMode(mode)
            }
        }
    }

    protected fun <T> collectFlow(
        flow: Flow<T>,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        collector: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(state) {
                flow.collectLatest(collector)
            }
        }
    }

    protected fun showToast(message: String?) {
        if (!message.isNullOrEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
