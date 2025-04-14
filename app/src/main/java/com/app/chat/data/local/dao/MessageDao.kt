package com.app.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.chat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE senderId = :user OR receiverId = :user ORDER BY timestamp ASC")
    fun getMessagesForUser(user: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE status = 'FAILED'")
    suspend fun getFailedMessages(): List<MessageEntity>

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
