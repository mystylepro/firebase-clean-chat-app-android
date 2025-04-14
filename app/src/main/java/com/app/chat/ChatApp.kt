package com.app.chat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.app.chat.data.worker.RetryFailedMessagesWorker
import com.app.chat.utils.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        RetryFailedMessagesWorker.enqueue(applicationContext)
        NetworkMonitor.register(this) // ✅ Safe from API 24+
    }
}