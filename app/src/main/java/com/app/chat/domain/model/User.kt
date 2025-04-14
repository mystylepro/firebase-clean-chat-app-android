package com.app.chat.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val lastMessage: String? = null,
    val timestamp: Long? = null,
    val isTyping: Boolean = false,
    val unreadCount: Int = 0,
    val messageStatus: MessageStatus = MessageStatus.PENDING
)
