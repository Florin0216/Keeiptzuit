package com.example.keeiptzuit.features.auth.data.repository

import com.example.keeiptzuit.features.auth.domain.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun createAccountWithEmail(firstName: String, lastName: String, email: String, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("User ID is null")
        val userData = User(userId, firstName, lastName, email)
        firestore.collection("users").document(userId).set(userData).await()
    }

    suspend fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val userId = authResult.user?.uid ?: throw Exception("User ID is null")
        val userData = User(uid = userId, email = authResult.user?.email)
        firestore.collection("users").document(userId).set(userData).await()
    }
}