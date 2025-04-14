package com.app.chat.domain.usecase

import com.app.chat.domain.model.User
import com.app.chat.domain.repository.UserRepository
import com.app.chat.domain.util.TypingObserver.observeUserWithTyping
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class UserUseCase @Inject constructor(
    private val repo: UserRepository
) {

    fun getUsersYouChattedWithWithTyping(currentUid: String): Flow<List<User>> {
        return repo.getUsersYouChattedWith(currentUid).flatMapLatest { userList ->
            if (userList.isEmpty()) {
                flowOf(emptyList())
            } else {
                val typingFlows = userList.map { observeUserWithTyping(it, currentUid) }
                combine(typingFlows) { enrichedUsers ->
                    enrichedUsers.toList()
                }
            }
        }
    }

    fun getAllUsersExceptCurrent(currentUid: String): Flow<List<User>> {
        return repo.getAllUsersExceptCurrent(currentUid)
    }
}
