package com.app.chat.di

import android.content.Context
import androidx.room.Room
import com.app.chat.data.local.ChatDatabase
import com.app.chat.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context // ✅ Fix: Add this annotation
    ): ChatDatabase {
        return Room.databaseBuilder(
            appContext,
            ChatDatabase::class.java,
            "chat_db"
        ).build()
    }

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao = db.messageDao()
}
