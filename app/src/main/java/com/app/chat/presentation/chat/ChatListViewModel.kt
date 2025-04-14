package com.app.chat.presentation.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chat.domain.model.User
import com.app.chat.domain.usecase.UserUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun loadChats() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userUseCase.getUsersYouChattedWithWithTyping(uid).collect { userList ->
                val updated = userList.map { it.copy() }
                _users.value = updated
            }
        }
    }

    fun loadUsersExcludingSelf() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userUseCase.getAllUsersExceptCurrent(uid).collect { userList ->
                _users.value = userList.map { it.copy() }
            }
        }
    }
}
