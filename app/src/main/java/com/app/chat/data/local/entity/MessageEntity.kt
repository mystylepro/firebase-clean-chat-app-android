package com.app.chat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.google.firebase.Timestamp

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val status: String
) {
    fun toDomain(): Message = Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        timestamp = Timestamp(timestamp / 1000, ((timestamp % 1000) * 1_000_000).toInt()),
        isRead = isRead,
        status = try {
            MessageStatus.valueOf(status)
        } catch (e: Exception) {
            MessageStatus.FAILED
        }
    )

    companion object {
        fun fromDomain(message: Message): MessageEntity = MessageEntity(
            id = message.id,
            chatId = message.chatId,
            senderId = message.senderId,
            receiverId = message.receiverId,
            text = message.text,
            timestamp = message.timestamp?.toDate()?.time ?: 0L,
            isRead = message.isRead,
            status = message.status.name
        )
    }
}
