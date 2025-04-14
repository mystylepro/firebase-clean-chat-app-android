package com.app.chat.domain.repository

import com.app.chat.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsersExceptCurrent(currentUid: String): Flow<List<User>>
    fun getUsersYouChattedWith(currentUserId: String): Flow<List<User>>
}
