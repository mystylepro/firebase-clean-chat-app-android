package com.app.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.chat.data.local.dao.MessageDao
import com.app.chat.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}