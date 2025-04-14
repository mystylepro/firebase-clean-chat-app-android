package com.app.chat.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.app.chat.data.worker.RetryFailedMessagesWorker

object NetworkMonitor {

    fun register(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .build()

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                RetryFailedMessagesWorker.enqueue(context)
            }
        })
    }
}
