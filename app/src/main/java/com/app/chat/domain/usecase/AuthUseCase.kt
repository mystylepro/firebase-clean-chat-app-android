package com.app.chat.domain.usecase

import com.app.chat.domain.model.User
import com.app.chat.domain.repository.AuthRepository
import javax.inject.Inject

sealed class AuthValidationResult {
    data object Valid : AuthValidationResult()
    data class Invalid(val message: String) : AuthValidationResult()
}

class AuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend fun login(email: String, password: String): Result<User> =
        repository.login(email, password)

    suspend fun signup(name: String, email: String, password: String): Result<User> =
        repository.signup(name, email, password)

    fun validateLogin(email: String, password: String): AuthValidationResult {
        return when {
            email.isBlank() -> AuthValidationResult.Invalid("Email is required")
            !Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})").matches(email) ->
                AuthValidationResult.Invalid("Invalid email format")
            password.isBlank() -> AuthValidationResult.Invalid("Password is required")
            password.length < 6 -> AuthValidationResult.Invalid("Password must be at least 6 characters")
            else -> AuthValidationResult.Valid
        }
    }

    fun validateSignup(name: String, email: String, password: String): AuthValidationResult {
        return when {
            name.isBlank() -> AuthValidationResult.Invalid("Name is required")
            email.isBlank() -> AuthValidationResult.Invalid("Email is required")
            !Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})").matches(email) ->
                AuthValidationResult.Invalid("Invalid email format")
            password.isBlank() -> AuthValidationResult.Invalid("Password is required")
            password.length < 6 -> AuthValidationResult.Invalid("Password must be at least 6 characters")
            else -> AuthValidationResult.Valid
        }
    }
}
