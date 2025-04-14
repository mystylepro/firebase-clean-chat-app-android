package com.app.chat.domain.repository

import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun observeMessages(chatId: String): Flow<List<Message>>

    suspend fun sendMessage(chatId: String, message: Message)

    suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean)

    fun observeTypingStatus(chatId: String, receiverId: String): Flow<Boolean>

    suspend fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus)

    suspend fun chatExists(chatId: String): Boolean

    suspend fun createChat(chatId: String, senderId: String, receiverId: String)

    suspend fun incrementUnreadCount(chatId: String, userId: String)

    suspend fun updateLastMessage(chatId: String, lastMessage: String)

    suspend fun ensureParticipantsPresent(chatId: String, uid1: String, uid2: String)

    suspend fun resetUnreadCount(chatId: String, userId: String)

    suspend fun clearLocalCache()
}
