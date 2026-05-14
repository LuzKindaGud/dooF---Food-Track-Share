package com.example.doancoso3.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {

    /** Flow emitting the current authenticated user, or null if not logged in. */
    val currentUser: Flow<FirebaseUser?>

    /** Returns the currently logged-in user synchronously, or null. */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Register a new user with email and password.
     * Also stores user profile in Firestore /users/{userId}.
     */
    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser>

    /**
     * Login with email and password.
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser>

    /**
     * Logout the current user.
     */
    fun logout()

    /**
     * Send a password reset email to the given address.
     */
    suspend fun resetPassword(email: String): Result<Unit>
}
