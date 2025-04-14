package com.app.chat.data.remote

import com.app.chat.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Login failed: User not found")
        firebaseUser.toDomainUser()
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw Exception("Signup failed")
        val newUser = firebaseUser.toDomainUser(name)
        db.collection("users").document(newUser.uid).set(newUser).await()
        newUser
    }

    private fun com.google.firebase.auth.FirebaseUser.toDomainUser(name: String? = null): User {
        return User(
            uid = uid,
            name = name ?: "", // Empty if not provided (login case)
            email = email.orEmpty()
        )
    }
}
