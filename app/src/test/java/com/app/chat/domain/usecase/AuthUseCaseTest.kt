package com.app.chat.domain.usecase

import com.app.chat.domain.model.User
import com.app.chat.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var authUseCase: AuthUseCase

    private val dummyUser = User(uid = "u1", name = "Anil", email = "anil@test.com")

    @Before
    fun setup() {
        repository = mockk()
        authUseCase = AuthUseCase(repository)
    }

    // 🔹 Login Tests

    @Test
    fun `login should return user result`() = runTest {
        coEvery { repository.login("anil@test.com", "123456") } returns Result.success(dummyUser)

        val result = authUseCase.login("anil@test.com", "123456")

        assertEquals(Result.success(dummyUser), result)
        coVerify { repository.login("anil@test.com", "123456") }
    }

    @Test
    fun `validateLogin should return Invalid when email is empty`() {
        val result = authUseCase.validateLogin("", "123456")
        assertEquals(AuthValidationResult.Invalid("Email is required"), result)
    }

    @Test
    fun `validateLogin should return Invalid when email is not valid`() {
        val result = authUseCase.validateLogin("invalid", "123456")
        assertEquals(AuthValidationResult.Invalid("Invalid email format"), result)
    }

    @Test
    fun `validateLogin should return Invalid when password is empty`() {
        val result = authUseCase.validateLogin("anil@test.com", "")
        assertEquals(AuthValidationResult.Invalid("Password is required"), result)
    }

    @Test
    fun `validateLogin should return Invalid when password is less than 6 characters`() {
        val result = authUseCase.validateLogin("anil@test.com", "123")
        assertEquals(AuthValidationResult.Invalid("Password must be at least 6 characters"), result)
    }

    @Test
    fun `validateLogin should return Valid for correct input`() {
        val result = authUseCase.validateLogin("anil@test.com", "123456")
        assertEquals(AuthValidationResult.Valid, result)
    }

    // 🔹 Signup Tests

    @Test
    fun `signup should return user result`() = runTest {
        coEvery { repository.signup("Anil", "anil@test.com", "123456") } returns Result.success(dummyUser)

        val result = authUseCase.signup("Anil", "anil@test.com", "123456")

        assertEquals(Result.success(dummyUser), result)
        coVerify { repository.signup("Anil", "anil@test.com", "123456") }
    }

    @Test
    fun `validateSignup should return Invalid when name is empty`() {
        val result = authUseCase.validateSignup("", "anil@test.com", "123456")
        assertEquals(AuthValidationResult.Invalid("Name is required"), result)
    }

    @Test
    fun `validateSignup should return Invalid when email is invalid`() {
        val result = authUseCase.validateSignup("Anil", "invalid", "123456")
        assertEquals(AuthValidationResult.Invalid("Invalid email format"), result)
    }

    @Test
    fun `validateSignup should return Invalid when password is too short`() {
        val result = authUseCase.validateSignup("Anil", "anil@test.com", "123")
        assertEquals(AuthValidationResult.Invalid("Password must be at least 6 characters"), result)
    }

    @Test
    fun `validateSignup should return Valid when inputs are valid`() {
        val result = authUseCase.validateSignup("Anil", "anil@test.com", "123456")
        assertEquals(AuthValidationResult.Valid, result)
    }
}
