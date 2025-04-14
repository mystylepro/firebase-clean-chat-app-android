package com.app.chat.domain.util

import com.app.chat.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

object TypingObserver{
    private fun observeTypingStatusForChat(chatId: String, userId: String): Flow<Boolean> = callbackFlow {
        val listener: ListenerRegistration = FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .addSnapshotListener { snapshot, _ ->
                val isTyping = snapshot?.getBoolean("typing_$userId") ?: false
                trySend(isTyping).isSuccess
            }
        awaitClose { listener.remove() }
    }

    fun observeUserWithTyping(user: User, currentUserId: String): Flow<User> {
        val chatId =
            if (currentUserId < user.uid) "$currentUserId-${user.uid}" else "${user.uid}-$currentUserId"
        return observeTypingStatusForChat(
            chatId,
            user.uid
        ).combine(flowOf(user)) { isTyping, baseUser ->
            baseUser.copy(isTyping = isTyping)
        }
    }
}