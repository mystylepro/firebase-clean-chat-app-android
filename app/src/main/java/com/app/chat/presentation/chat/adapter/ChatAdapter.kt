package com.app.chat.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.chat.R
import com.app.chat.databinding.ItemMessageReceivedBinding
import com.app.chat.databinding.ItemMessageSentBinding
import com.app.chat.domain.model.Message
import com.app.chat.domain.model.MessageStatus
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val currentUserId: String,
    private val retryCallback: (Message) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1

        val DiffCallback = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
                SentViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(getItem(position))
            is ReceivedViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class SentViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.tvSentMessage.text = message.text
            binding.tvSentTime.text = formatTime(message.timestamp)

            binding.imgRetry.setOnClickListener { retryCallback(message) }

            when (message.status) {
                MessageStatus.FAILED -> {
                    binding.imgRetry.visibility = View.VISIBLE
                    binding.imgMessageStatus.visibility = View.GONE
                }
                else -> {
                    binding.imgRetry.visibility = View.GONE
                    val statusIcon = when (message.status) {
                        MessageStatus.SENT -> R.drawable.ic_sent
                        MessageStatus.DELIVERED -> R.drawable.ic_delivered
                        MessageStatus.READ -> R.drawable.ic_read
                        MessageStatus.PENDING -> R.drawable.ic_pending
                        else -> 0
                    }
                    if (statusIcon != 0) {
                        binding.imgMessageStatus.setImageResource(statusIcon)
                        binding.imgMessageStatus.visibility = View.VISIBLE
                    } else {
                        binding.imgMessageStatus.visibility = View.GONE
                    }
                }
            }
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvReceivedMessage.text = message.text
            binding.tvReceivedTime.text = formatTime(message.timestamp)
        }
    }

    private fun formatTime(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
        } ?: ""
    }
}
