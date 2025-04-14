package com.app.chat.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.chat.R
import com.app.chat.databinding.FragmentChatListBinding
import com.app.chat.presentation.chat.adapter.ChatListAdapter
import com.app.chat.utils.VerticalSpaceItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var chatListAdapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupListeners()
        observeChatUsers()

        viewModel.loadChats()
    }

    private fun setupRecyclerView() = with(binding) {
        chatListAdapter = ChatListAdapter { user ->
            val action = ChatListFragmentDirections.actionChatListToChat(user.uid, user.name)
            findNavController().navigate(action)
        }
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = chatListAdapter

        val space = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
        rvUsers.addItemDecoration(VerticalSpaceItemDecoration(space))
    }

    private fun setupListeners() = with(binding) {
        btnChatPeople.setOnClickListener {
            findNavController().navigate(R.id.action_chatList_to_userList)
        }
    }

    private fun observeChatUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collectLatest { userList ->
                    chatListAdapter.submitList(userList)
                    binding.noConversationView.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvUsers.visibility = if (userList.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
