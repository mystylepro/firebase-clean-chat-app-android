package com.app.chat.domain.usecase

import android.util.Log
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.app.chat.domain.repository.ChatRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatUseCase @Inject constructor(
    private val repository: ChatRepository,
    private val firebaseAuth: FirebaseAuth
) {

    fun observeMessages(chatId: String): Flow<List<Message>> =
        repository.observeMessages(chatId)

    fun observeTypingStatus(chatId: String, receiverId: String): Flow<Boolean> =
        repository.observeTypingStatus(chatId, receiverId)

    suspend fun updateTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        repository.setTypingStatus(chatId, userId, isTyping)
    }

    suspend fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        repository.updateMessageStatus(chatId, messageId, status)
    }

    suspend fun resetUnreadCount(chatId: String, userId: String) {
        repository.resetUnreadCount(chatId, userId)
    }

    suspend fun logout() {
        repository.clearLocalCache()
        firebaseAuth.signOut()
    }

    suspend fun sendMessageWithAutoChat(senderId: String, receiverId: String, messageText: String): Boolean {
        return try {
            val chatId = generateChatId(senderId, receiverId)

            if (!repository.chatExists(chatId)) {
                repository.createChat(chatId, senderId, receiverId)
            } else {
                repository.ensureParticipantsPresent(chatId, senderId, receiverId)
            }

            val message = Message(
                text = messageText,
                senderId = senderId,
                receiverId = receiverId,
                timestamp = Timestamp.now(),
                status = MessageStatus.PENDING
            )

            repository.sendMessage(chatId, message)
            repository.updateLastMessage(chatId, messageText)
            repository.incrementUnreadCount(chatId, receiverId)

            true
        } catch (e: Exception) {
            Log.e("ChatUseCase", "sendMessageWithAutoChat failed: ${e.message}", e)
            false
        }
    }

    suspend fun retryMessage(message: Message): Boolean {
        return try {
            val chatId = message.chatId.ifEmpty {
                generateChatId(message.senderId, message.receiverId)
            }

            repository.sendMessage(chatId, message.copy(status = MessageStatus.PENDING))
            true
        } catch (e: Exception) {
            Log.e("ChatUseCase", "Retry failed: ${e.message}")
            false
        }
    }

    private fun generateChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "$uid1-$uid2" else "$uid2-$uid1"
    }
}
