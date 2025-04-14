package com.app.chat.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.chat.R
import com.app.chat.databinding.ItemUserBinding
import com.app.chat.domain.model.User
import com.bumptech.glide.Glide

class UserListAdapter(
    private val onClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserName.text = user.name

            Glide.with(binding.imgProfile.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .circleCrop()
                .into(binding.imgProfile)

            binding.root.setOnClickListener { onClick(user) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
        }
    }
}
