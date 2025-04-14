package com.app.chat.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.chat.R
import com.app.chat.databinding.FragmentLoginBinding
import com.app.chat.presentation.base.BaseFragment
import com.app.chat.utils.navigateAndClearBackStack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: AuthViewModel by viewModels()

    override val softInputMode: Int?
        get() = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.loginWithValidation(email, password)
        }

        collectFlow(viewModel.loginEvent) { result ->
            result.onSuccess {
                showToast("Login Success!")
                findNavController().navigateAndClearBackStack(R.id.chatListFragment)
            }.onFailure {
                showToast(it.message)
            }
        }

        collectFlow(viewModel.validationError) { message ->
            showToast(message)
        }

        signupText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        collectFlow(viewModel.userState) { result ->
            result?.onSuccess {
                showToast("Login Success!")
                findNavController().navigateAndClearBackStack(R.id.chatListFragment)
            }?.onFailure {
                showToast(it.message)
            }
        }
    }
}
