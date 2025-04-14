package com.app.chat.presentation.chat

import app.cash.turbine.test
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.app.chat.domain.usecase.ChatUseCase
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private lateinit var chatUseCase: ChatUseCase
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: ChatViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val dummyMessages = listOf(
        Message(id = "1", senderId = "u1", receiverId = "u2", text = "Hi", status = MessageStatus.DELIVERED),
        Message(id = "2", senderId = "u1", receiverId = "u2", text = "Hey", status = MessageStatus.SENT),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        chatUseCase = mockk(relaxed = true)
        auth = mockk()
        viewModel = ChatViewModel(chatUseCase, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_observeMessages_updatesMessagesAndMarksRead() = testScope.runTest {
        val uid = "u2"
        every { auth.currentUser?.uid } returns uid
        coEvery { chatUseCase.observeMessages("u1-u2") } returns flowOf(dummyMessages)

        viewModel.observeMessages("u2", "u1")
        testScheduler.advanceUntilIdle()

        viewModel.messages.test {
            assertEquals(dummyMessages, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_sendMessageWithChat_callsUseCase() = testScope.runTest {
        every { auth.currentUser?.uid } returns "u1"

        viewModel.sendMessageWithChat("u2", "Hello")
        testScheduler.advanceUntilIdle()

        coVerify { chatUseCase.sendMessageWithAutoChat("u1", "u2", "Hello") }
    }

    @Test
    fun test_retryMessage_emitsSuccessToast() = testScope.runTest {
        val message = dummyMessages[0]
        coEvery { chatUseCase.retryMessage(message) } returns true

        viewModel.toastEvent.test {
            viewModel.retryMessage(message)
            testScheduler.advanceUntilIdle()

            assertEquals("Message sent!", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_onUserTyping_updatesTypingStatus() = testScope.runTest {
        viewModel.onUserTyping("chat1", "u1")
        testScheduler.advanceTimeBy(2500)

        coVerify(exactly = 1) { chatUseCase.updateTypingStatus("chat1", "u1", true) }
        coVerify(exactly = 1) { chatUseCase.updateTypingStatus("chat1", "u1", false) }
    }

    @Test
    fun test_observeTyping_updatesTypingStatus() = testScope.runTest {
        coEvery { chatUseCase.observeTypingStatus("chat1", "u2") } returns flowOf(true)

        viewModel.observeTyping("chat1", "u2")
        testScheduler.advanceUntilIdle()

        viewModel.typingStatus.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun test_updateDelivered_marksDeliveredMessages() = testScope.runTest {
        val chatId = "u1-u2"
        viewModel.updateDelivered(dummyMessages, "u2", chatId)
        testScheduler.advanceUntilIdle()

        coVerify {
            chatUseCase.updateMessageStatus(chatId, "2", MessageStatus.DELIVERED)
        }
    }

    @Test
    fun test_markMessagesAsRead_marksReadMessages() = testScope.runTest {
        val chatId = "u1-u2"
        viewModel.markMessagesAsRead(dummyMessages, "u2", chatId)
        testScheduler.advanceUntilIdle()

        coVerify {
            chatUseCase.updateMessageStatus(chatId, "1", MessageStatus.READ)
        }
    }

    @Test
    fun test_resetUnreadCount_callsUseCase() = testScope.runTest {
        viewModel.resetUnreadCount("chat123", "u1")
        testScheduler.advanceUntilIdle()

        coVerify { chatUseCase.resetUnreadCount("chat123", "u1") }
    }
}
