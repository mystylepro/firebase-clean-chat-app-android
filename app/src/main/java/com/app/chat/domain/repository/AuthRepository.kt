package com.app.chat.domain.repository

import com.app.chat.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(name: String, email: String, password: String): Result<User>
}
