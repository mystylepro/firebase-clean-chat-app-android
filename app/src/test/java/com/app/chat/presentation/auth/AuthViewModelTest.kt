package com.app.chat.presentation.auth

import app.cash.turbine.test
import com.app.chat.domain.model.User
import com.app.chat.domain.usecase.AuthUseCase
import com.app.chat.domain.usecase.AuthValidationResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var authUseCase: AuthUseCase

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val dummyUser = User(uid = "u1", name = "Anil", email = "anil@test.com")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // ✅ Override Main dispatcher
        authUseCase = mockk(relaxed = true)
        viewModel = AuthViewModel(authUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // ✅ Reset to default after each test
    }

    // 🔹 Login Tests

    @Test
    fun `loginWithValidation emits error when validation fails`() = testScope.runTest {
        coEvery { authUseCase.validateLogin("", "123456") } returns
                AuthValidationResult.Invalid("Email is required")

        viewModel.loginWithValidation("", "123456")

        viewModel.validationError.test {
            assertEquals("Email is required", awaitItem())
            cancel()
        }
    }

    @Test
    fun `loginWithValidation emits user on valid input`() = testScope.runTest {
        coEvery { authUseCase.validateLogin("anil@test.com", "123456") } returns AuthValidationResult.Valid
        coEvery { authUseCase.login("anil@test.com", "123456") } returns Result.success(dummyUser)

        viewModel.loginWithValidation("anil@test.com", "123456")

        viewModel.loginEvent.test {
            assertEquals(Result.success(dummyUser), awaitItem())
            cancel()
        }
    }

    // 🔹 Signup Tests

    @Test
    fun `signupWithValidation emits error when validation fails`() = testScope.runTest {
        coEvery {
            authUseCase.validateSignup("", "anil@test.com", "123456")
        } returns AuthValidationResult.Invalid("Name is required")

        viewModel.signupWithValidation("", "anil@test.com", "123456")

        viewModel.validationError.test {
            assertEquals("Name is required", awaitItem())
            cancel()
        }
    }

    @Test
    fun `signupWithValidation emits user on valid input`() = testScope.runTest {
        coEvery {
            authUseCase.validateSignup("Anil", "anil@test.com", "123456")
        } returns AuthValidationResult.Valid
        coEvery {
            authUseCase.signup("Anil", "anil@test.com", "123456")
        } returns Result.success(dummyUser)

        viewModel.signupWithValidation("Anil", "anil@test.com", "123456")

        viewModel.signupEvent.test {
            assertEquals(Result.success(dummyUser), awaitItem())
            cancel()
        }
    }
}
