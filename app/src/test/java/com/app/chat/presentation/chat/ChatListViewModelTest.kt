package com.app.chat.presentation.chat

import app.cash.turbine.test
import com.app.chat.domain.model.User
import com.app.chat.domain.usecase.UserUseCase
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private lateinit var userUseCase: UserUseCase
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ChatListViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val dummyUserList = listOf(
        User(uid = "u1", name = "Anil"),
        User(uid = "u2", name = "Kumar")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        userUseCase = mockk()
        auth = mockk()
        viewModel = ChatListViewModel(userUseCase, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_loadChats_emitsUpdatedUsersList() = testScope.runTest {
        // Given
        every { auth.currentUser?.uid } returns "123"
        coEvery { userUseCase.getUsersYouChattedWithWithTyping("123") } returns flowOf(dummyUserList)

        // When
        viewModel.loadChats()

        // ⏳ Let coroutine finish
        testScheduler.advanceUntilIdle()

        // Then
        viewModel.users.test {
            val result = awaitItem()
            assertEquals(dummyUserList, result)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun test_loadUsersExcludingSelf_emitsUpdatedUsersList() = testScope.runTest {
        // Given
        every { auth.currentUser?.uid } returns "456"
        coEvery { userUseCase.getAllUsersExceptCurrent("456") } returns flowOf(dummyUserList)

        // When
        viewModel.loadUsersExcludingSelf()
        testScheduler.advanceUntilIdle()

        // Then
        viewModel.users.test {
            val result = awaitItem()
            assertEquals(dummyUserList, result)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
