package com.app.chat.data.repository

import android.content.Context
import android.util.Log
import com.app.chat.data.local.dao.MessageDao
import com.app.chat.data.local.entity.MessageEntity
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.app.chat.domain.repository.ChatRepository
import com.app.chat.utils.NetworkUtils.isNetworkAvailable
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messageDao: MessageDao,
    @ApplicationContext private val context: Context
) : ChatRepository {

    override fun observeMessages(chatId: String): Flow<List<Message>> {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Message(
                            id = doc.id,
                            chatId = chatId,
                            senderId = data["senderId"] as? String ?: "",
                            receiverId = data["receiverId"] as? String ?: "",
                            text = data["text"] as? String ?: "",
                            timestamp = data["timestamp"] as? com.google.firebase.Timestamp,
                            isRead = data["isRead"] as? Boolean ?: false,
                            status = try {
                                MessageStatus.valueOf(data["status"] as? String ?: "PENDING")
                            } catch (e: Exception) {
                                MessageStatus.PENDING
                            }
                        )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        messageDao.insertAll(messages.map { MessageEntity.fromDomain(it) })
                    }
                } else if (error != null) {
                    Log.e("observeMessages", "Firestore error", error)
                }
            }

        return messageDao.getMessagesForChat(chatId).map { it.map { msg -> msg.toDomain() } }
    }

    override suspend fun sendMessage(chatId: String, message: Message) {
        val localMessage = message.copy(
            id = message.id.ifEmpty { UUID.randomUUID().toString() },
            chatId = chatId,
            status = MessageStatus.PENDING
        )

        messageDao.insertMessage(MessageEntity.fromDomain(localMessage))

        if (!isNetworkAvailable(context)) {
            val failedMessage = localMessage.copy(status = MessageStatus.FAILED)
            messageDao.insertMessage(MessageEntity.fromDomain(failedMessage))
            Log.w("sendMessage", "No internet. Marked as FAILED.")
            throw IllegalStateException("No network available")
        }

        try {
            val docRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(localMessage.id)

            docRef.set(localMessage).await()
            docRef.update("status", MessageStatus.SENT.name).await()

            val sentMessage = localMessage.copy(status = MessageStatus.SENT)
            messageDao.insertMessage(MessageEntity.fromDomain(sentMessage))

        } catch (e: Exception) {
            val failedMessage = localMessage.copy(status = MessageStatus.FAILED)
            messageDao.insertMessage(MessageEntity.fromDomain(failedMessage))
            Log.e("sendMessage", "Firestore failed: ${e.message}")
            throw e
        }
    }

    override suspend fun setTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        val field = "typing_$userId"
        val chatRef = firestore.collection("chats").document(chatId)
        chatRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) chatRef.update(field, isTyping)
            else chatRef.set(mapOf(field to isTyping))
        }
    }

    override fun observeTypingStatus(chatId: String, receiverId: String): Flow<Boolean> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.getBoolean("typing_$receiverId") ?: false)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update("status", status.name)
    }

    override suspend fun chatExists(chatId: String): Boolean {
        val doc = firestore.collection("chats").document(chatId).get().await()
        return doc.exists()
    }

    override suspend fun createChat(chatId: String, senderId: String, receiverId: String) {
        val chatRef = firestore.collection("chats").document(chatId)
        val snapshot = chatRef.get().await()
        val participants = listOf(senderId, receiverId).sorted()

        if (snapshot.exists()) {
            if (!snapshot.data?.containsKey("participants")!!) {
                chatRef.update("participants", participants)
            }
        } else {
            val chatData = mapOf(
                "participants" to participants,
                "lastMessage" to "",
                "timestamp" to FieldValue.serverTimestamp(),
                "typing_${senderId}" to false,
                "typing_${receiverId}" to false,
                "unread_${senderId}" to 0,
                "unread_${receiverId}" to 0
            )
            chatRef.set(chatData).await()
        }
    }

    override suspend fun updateLastMessage(chatId: String, lastMessage: String) {
        firestore.collection("chats")
            .document(chatId)
            .update(
                mapOf(
                    "lastMessage" to lastMessage,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "fallbackTime" to System.currentTimeMillis()
                )
            ).addOnFailureListener {
                Log.e("updateLastMessage", "Failed: ${it.message}")
            }
    }

    override suspend fun incrementUnreadCount(chatId: String, userId: String) {
        firestore.collection("chats")
            .document(chatId)
            .update("unread_$userId", FieldValue.increment(1))
    }

    override suspend fun resetUnreadCount(chatId: String, userId: String) {
        firestore.collection("chats")
            .document(chatId)
            .update("unread_$userId", 0)
    }

    override suspend fun ensureParticipantsPresent(chatId: String, uid1: String, uid2: String) {
        val chatRef = firestore.collection("chats").document(chatId)
        val snapshot = chatRef.get().await()
        val sorted = listOf(uid1, uid2).sorted()
        if (snapshot.exists() && !snapshot.contains("participants")) {
            chatRef.update("participants", sorted)
        }
    }

    override suspend fun clearLocalCache() {
        messageDao.clearAll()
    }
}
