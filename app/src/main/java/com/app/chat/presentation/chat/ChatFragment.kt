package com.app.chat.presentation.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.chat.MainActivity
import com.app.chat.databinding.FragmentChatBinding
import com.app.chat.presentation.base.BaseFragment
import com.app.chat.presentation.chat.adapter.ChatAdapter
import com.app.chat.utils.applyKeyboardInsetAsMargin
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>() {

    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    @Inject lateinit var firebaseAuth: FirebaseAuth

    override val softInputMode: Int
        get() = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

    private val currentUserId by lazy {
        firebaseAuth.currentUser?.uid.orEmpty()
    }
    private val receiverId by lazy { args.receiverId }
    private val receiverName by lazy { args.receiverName }
    private val chatId by lazy {
        if (currentUserId < receiverId) "$currentUserId-$receiverId" else "$receiverId-$currentUserId"
    }

    private lateinit var chatAdapter: ChatAdapter

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentChatBinding {
        return FragmentChatBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupChatAdapter()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        (requireActivity() as? MainActivity)?.setToolbarTitle(receiverName)
    }

    private fun FragmentChatBinding.setupChatAdapter() {
        messageInputContainer.applyKeyboardInsetAsMargin()

        chatAdapter = ChatAdapter(currentUserId) { failedMessage ->
            viewModel.retryMessage(failedMessage)
        }

        rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = chatAdapter

            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    post { scrollToPosition(chatAdapter.itemCount - 1) }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.observeMessages(currentUserId, receiverId)
        viewModel.observeTyping(chatId, receiverId)

        collectFlow(viewModel.messages) { messages ->
            chatAdapter.submitList(messages)
            binding.rvMessages.scrollToPosition(messages.size - 1)

            viewModel.updateDelivered(messages, currentUserId, chatId)
            viewModel.markMessagesAsRead(messages, currentUserId, chatId)
            viewModel.resetUnreadCount(chatId, currentUserId)
        }

        collectFlow(viewModel.typingStatus) { isTyping ->
            (requireActivity() as? MainActivity)?.setTypingSubtitle(
                isTyping
            )
        }

        collectFlow(viewModel.toastEvent) {
            showToast(it)
        }
    }

    private fun FragmentChatBinding.setupListeners() {
        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessageWithChat(receiverId, message)
                etMessage.text.clear()
            }
        }

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onUserTyping(chatId, currentUserId)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateTypingStatus(chatId, currentUserId, false)
    }

}
