package com.app.chat.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chat.domain.model.User
import com.app.chat.domain.usecase.AuthUseCase
import com.app.chat.domain.usecase.AuthValidationResult
import com.app.chat.presentation.common.UiEvent
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

    val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow

    fun loginWithValidation(email: String, password: String) {
        viewModelScope.launch {
            when (val result = useCase.validateLogin(email, password)) {
                is AuthValidationResult.Valid -> {
                    _eventFlow.emit(UiEvent.ShowLoader)
                    val loginResult = useCase.login(email, password)
                    loginResult.fold(
                        onSuccess = {
                            _eventFlow.emit(UiEvent.HideLoader)
                            _loginEvent.emit(Result.success(it))
                        },
                        onFailure = {
                            _eventFlow.emit(UiEvent.HideLoader)
                            _loginEvent.emit(Result.failure(it))
                        }
                    )
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
                    _eventFlow.emit(UiEvent.ShowLoader)
                    val signupResult = useCase.signup(name, email, password)
                    signupResult.fold(
                        onSuccess = {
                            _eventFlow.emit(UiEvent.HideLoader)
                            _signupEvent.emit(Result.success(it))
                        },
                        onFailure = {
                            _eventFlow.emit(UiEvent.HideLoader)
                            _signupEvent.emit(Result.failure(it))
                        }
                    )
                }
                is AuthValidationResult.Invalid -> {
                    _validationError.emit(result.message)
                }
            }
        }
    }

}