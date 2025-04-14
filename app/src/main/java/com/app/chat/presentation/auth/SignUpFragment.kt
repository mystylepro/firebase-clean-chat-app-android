package com.app.chat.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.app.chat.R
import com.app.chat.databinding.FragmentSignUpBinding
import com.app.chat.presentation.base.BaseFragment
import com.app.chat.utils.navigateAndClearBackStack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : BaseFragment<FragmentSignUpBinding>() {

    private val viewModel: AuthViewModel by viewModels()

    override val softInputMode: Int?
        get() = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentSignUpBinding {
        return FragmentSignUpBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            viewModel.signupWithValidation(name, email, password)
        }

        binding.tvSignIn.setOnClickListener {
            findNavController().navigateAndClearBackStack(R.id.loginFragment)
        }

        collectFlow(viewModel.signupEvent) { result ->
            result.onSuccess {
                showToast("Signup Success!")
                findNavController().navigateAndClearBackStack(R.id.chatListFragment)
            }.onFailure {
                showToast(it.message)
            }
        }

        collectFlow(viewModel.validationError) { message ->
            showToast(message)
        }
    }
}
