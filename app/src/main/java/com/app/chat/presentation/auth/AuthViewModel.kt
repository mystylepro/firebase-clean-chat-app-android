package com.app.chat.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chat.domain.model.User
import com.app.chat.domain.usecase.AuthUseCase
import com.app.chat.domain.usecase.AuthValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val useCase: AuthUseCase
) : ViewModel() {

    private val _userState = MutableStateFlow<Result<User>?>(null)
    val userState: StateFlow<Result<User>?> = _userState

    private val _loginEvent = MutableSharedFlow<Result<User>>()
    val loginEvent: SharedFlow<Result<User>> = _loginEvent

    private val _validationError = MutableSharedFlow<String>()
    val validationError: SharedFlow<String> = _validationError

    private val _signupEvent = MutableSharedFlow<Result<User>>()
    val signupEvent: SharedFlow<Result<User>> = _signupEvent


    fun loginWithValidation(email: String, password: String) {
        viewModelScope.launch {
            when (val result = useCase.validateLogin(email, password)) {
                is AuthValidationResult.Valid -> {
                    val loginResult = useCase.login(email, password)
                    _loginEvent.emit(loginResult)
                }
                is AuthValidationResult.Invalid -> {
                    _validationError.emit(result.message)
                }
            }
        }
    }

    fun signupWithValidation(name: String, email: String, password: String) {
        viewModelScope.launch {
            when (val result = useCase.validateSignup(name, email, password)) {
                is AuthValidationResult.Valid -> {
                    val signupResult = useCase.signup(name, email, password)
                    _signupEvent.emit(signupResult)
                }
                is AuthValidationResult.Invalid -> {
                    _validationError.emit(result.message)
                }
            }
        }
    }
}