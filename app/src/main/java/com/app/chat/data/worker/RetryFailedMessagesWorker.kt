package com.app.chat.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.app.chat.data.local.dao.MessageDao
import com.app.chat.domain.model.MessageStatus
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class RetryFailedMessagesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageDao: MessageDao,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val failedMessages = messageDao.getFailedMessages()

        failedMessages.forEach { entity ->
            try {
                retryMessage(entity)
            } catch (e: Exception) {
                Log.e("RetryWorker", "Failed to resend message ${entity.id}: ${e.message}")
            }
        }

        return Result.success()
    }

    private suspend fun retryMessage(entity: com.app.chat.data.local.entity.MessageEntity) {
        val message = entity.toDomain().copy(status = MessageStatus.SENT)

        firestore.collection("chats")
            .document(message.chatId)
            .collection("messages")
            .document(message.id)
            .set(message)
            .await()

        messageDao.insertMessage(entity.copy(status = MessageStatus.SENT.name))
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RetryFailedMessagesWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
