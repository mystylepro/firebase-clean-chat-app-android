package com.app.chat.domain.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val isRead: Boolean = false,
    val status: MessageStatus = MessageStatus.PENDING
)
