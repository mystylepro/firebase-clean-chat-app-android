package com.app.chat.domain.usecase

import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.app.chat.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatUseCaseTest {

    private lateinit var repository: ChatRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var chatUseCase: ChatUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        chatUseCase = ChatUseCase(repository, firebaseAuth)
    }

    @Test
    fun `observeMessages returns expected flow`() = runTest {
        val messages = listOf(Message(id = "1", text = "Hi", senderId = "a", receiverId = "b"))
        coEvery { repository.observeMessages("a-b") } returns flowOf(messages)

        val result = chatUseCase.observeMessages("a-b").first()
        assertEquals(messages, result)
    }

    @Test
    fun `observeTypingStatus returns expected flow`() = runTest {
        coEvery { repository.observeTypingStatus("chat123", "receiver") } returns flowOf(true)

        val result = chatUseCase.observeTypingStatus("chat123", "receiver").first()
        assertTrue(result)
    }

    @Test
    fun `sendMessageWithAutoChat creates chat and sends message`() = runTest {
        coEvery { repository.chatExists("a-b") } returns false
        coEvery { repository.createChat(any(), any(), any()) } just Runs
        coEvery { repository.sendMessage(any(), any()) } just Runs
        coEvery { repository.updateLastMessage(any(), any()) } just Runs
        coEvery { repository.incrementUnreadCount(any(), any()) } just Runs

        val result = chatUseCase.sendMessageWithAutoChat("a", "b", "Hello")

        assertTrue(result)
        coVerify { repository.createChat("a-b", "a", "b") }
        coVerify { repository.sendMessage("a-b", match { it.text == "Hello" }) }
    }

    @Test
    fun `retryMessage sends message using fallback chatId`() = runTest {
        val message = Message(id = "1", senderId = "a", receiverId = "b", chatId = "")
        coEvery { repository.sendMessage(any(), any()) } just Runs

        val result = chatUseCase.retryMessage(message)

        assertTrue(result)
        coVerify { repository.sendMessage("a-b", match { it.status == MessageStatus.PENDING }) }
    }

    @Test
    fun `updateTypingStatus delegates to repository`() = runTest {
        chatUseCase.updateTypingStatus("chat1", "u1", true)
        coVerify { repository.setTypingStatus("chat1", "u1", true) }
    }

    @Test
    fun `updateMessageStatus delegates to repository`() = runTest {
        chatUseCase.updateMessageStatus("chatId", "m1", MessageStatus.READ)
        coVerify { repository.updateMessageStatus("chatId", "m1", MessageStatus.READ) }
    }

    @Test
    fun `resetUnreadCount delegates to repository`() = runTest {
        chatUseCase.resetUnreadCount("chatId", "u1")
        coVerify { repository.resetUnreadCount("chatId", "u1") }
    }

    @Test
    fun `logout clears cache and signs out`() = runTest {
        chatUseCase.logout()
        coVerify { repository.clearLocalCache() }
        coVerify { firebaseAuth.signOut() }
    }
}
