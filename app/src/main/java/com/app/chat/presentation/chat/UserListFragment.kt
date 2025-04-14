package com.app.chat.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.chat.R
import com.app.chat.databinding.FragmentChatListBinding
import com.app.chat.presentation.base.BaseFragment
import com.app.chat.presentation.chat.adapter.ChatListAdapter
import com.app.chat.presentation.chat.adapter.UserListAdapter
import com.app.chat.utils.VerticalSpaceItemDecoration
import com.app.chat.utils.navigateWithPopUp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListFragment : BaseFragment<FragmentChatListBinding>() {

    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var adapter: UserListAdapter

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?): FragmentChatListBinding {
        return FragmentChatListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.loadUsersExcludingSelf()

        collectFlow(viewModel.users) { userList ->
            adapter.submitList(userList)
            noConversationView.visibility = if (userList.isEmpty()) View.VISIBLE else View.GONE
            rvUsers.visibility = if (userList.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupRecyclerView() = with(binding) {
        adapter = UserListAdapter { user ->
            val action = UserListFragmentDirections.actionUserListToChat(user.uid, user.name)
            findNavController().navigateWithPopUp(action, popUpTo = R.id.userListFragment)
        }
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
        val space = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
        rvUsers.addItemDecoration(VerticalSpaceItemDecoration(space))
    }
}
