package com.app.chat.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.app.chat.domain.usecase.ChatUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    val messages = MutableStateFlow<List<Message>>(emptyList())
    val typingStatus = MutableStateFlow(false)

    private var typingJob: Job? = null

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun observeMessages(currentUserId: String, receiverId: String) {
        val chatId = generateChatId(currentUserId, receiverId)

        viewModelScope.launch {
            chatUseCase.observeMessages(chatId).collect { messageList ->
                messages.value = messageList
                markMessagesAsRead(messageList, currentUserId, chatId)
            }
        }
    }

    fun sendMessageWithChat(receiverId: String, messageText: String) {
        val senderId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            chatUseCase.sendMessageWithAutoChat(senderId, receiverId, messageText)
        }
    }

    fun retryMessage(message: Message) {
        viewModelScope.launch {
            val success = chatUseCase.retryMessage(message)
            _toastEvent.emit(
                if (success) "Message sent!" else "Retry failed. Check connection."
            )
        }
    }

    fun onUserTyping(chatId: String, userId: String) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            chatUseCase.updateTypingStatus(chatId, userId, true)
            delay(2000)
            chatUseCase.updateTypingStatus(chatId, userId, false)
        }
    }

    fun updateTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        viewModelScope.launch {
            chatUseCase.updateTypingStatus(chatId, userId, isTyping)
        }
    }


    fun observeTyping(chatId: String, receiverId: String) {
        viewModelScope.launch {
            chatUseCase.observeTypingStatus(chatId, receiverId).collect {
                typingStatus.value = it
            }
        }
    }

    fun updateDelivered(messages: List<Message>, currentUserId: String, chatId: String) {
        viewModelScope.launch {
            messages.filter {
                it.receiverId == currentUserId && it.status == MessageStatus.SENT
            }.forEach {
                chatUseCase.updateMessageStatus(chatId, it.id, MessageStatus.DELIVERED)
            }
        }
    }

    fun markMessagesAsRead(messages: List<Message>, currentUserId: String, chatId: String) {
        viewModelScope.launch {
            messages.filter {
                it.receiverId == currentUserId && it.status == MessageStatus.DELIVERED
            }.forEach {
                chatUseCase.updateMessageStatus(chatId, it.id, MessageStatus.READ)
            }
        }
    }

    fun resetUnreadCount(chatId: String, userId: String) {
        viewModelScope.launch {
            chatUseCase.resetUnreadCount(chatId, userId)
        }
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "$uid1-$uid2" else "$uid2-$uid1"
    }
}
