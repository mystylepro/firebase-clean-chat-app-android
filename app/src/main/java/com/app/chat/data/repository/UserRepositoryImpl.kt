package com.app.chat.data.repository

import android.util.Log
import com.app.chat.domain.model.User
import com.app.chat.domain.repository.UserRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUsersYouChattedWith(currentUserId: String): Flow<List<User>> = callbackFlow {

        fun fetchAndEnrichUsers(
            currentUserId: String,
            chatMeta: Map<String, ChatMeta>
        ) {
            val userIds = chatMeta.keys.toList()

            if (userIds.isEmpty()) {
                trySend(emptyList())
                return
            }

            val fetchTasks = userIds.chunked(10).map { chunk ->
                firestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
            }

            com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(fetchTasks)
                .addOnSuccessListener { results ->
                    val enrichedUsers = results.flatMap { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            val uid = doc.id
                            val meta = chatMeta[uid] ?: return@mapNotNull null

                            User(
                                uid = uid,
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                profileImageUrl = doc.getString("profileImageUrl"),
                                lastMessage = meta.lastMessage,
                                timestamp = meta.timestamp,
                                isTyping = meta.isTyping,
                                unreadCount = meta.unreadCount
                            )
                        }
                    }.sortedByDescending { it.timestamp ?: 0L }

                    trySend(enrichedUsers)
                }
                .addOnFailureListener {
                    Log.e("UserRepo", "Error enriching users: ${it.message}")
                    close(it)
                }
        }

        val chatListener = firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chatDocs = snapshot.documents.sortedByDescending {
                    it.getTimestamp("timestamp")?.toDate()?.time ?: it.getLong("fallbackTime") ?: 0L
                }

                if (chatDocs.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val chatMetaMap = mutableMapOf<String, ChatMeta>()

                chatDocs.forEach { doc ->
                    val participants = doc.get("participants") as? List<*> ?: return@forEach
                    val otherUserId = participants.filterIsInstance<String>().firstOrNull { it != currentUserId } ?: return@forEach

                    chatMetaMap[otherUserId] = ChatMeta(
                        lastMessage = doc.getString("lastMessage"),
                        timestamp = doc.getTimestamp("timestamp")?.toDate()?.time,
                        isTyping = doc.getBoolean("typing_$otherUserId") ?: false,
                        unreadCount = doc.getLong("unread_$currentUserId")?.toInt() ?: 0
                    )
                }

                fetchAndEnrichUsers(currentUserId, chatMetaMap)
            }

        awaitClose { chatListener.remove() }
    }

    override fun getAllUsersExceptCurrent(currentUid: String): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val users = snapshot.documents.mapNotNull {
                    val id = it.id
                    val name = it.getString("name") ?: ""
                    val email = it.getString("email") ?: ""
                    val profileImageUrl = it.getString("profileImageUrl")

                    User(uid = id, name = name, email = email, profileImageUrl = profileImageUrl)
                }.filterNot { it.uid == currentUid }

                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    private data class ChatMeta(
        val lastMessage: String?,
        val timestamp: Long?,
        val isTyping: Boolean,
        val unreadCount: Int
    )
}
