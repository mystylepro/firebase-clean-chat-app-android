package com.app.chat.data.repository

import com.app.chat.data.remote.FirebaseService
import com.app.chat.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService
) : AuthRepository {

    override suspend fun login(email: String, password: String) =
        firebaseService.login(email, password)

    override suspend fun signup(name: String, email: String, password: String) =
        firebaseService.signup(name, email, password)
}
