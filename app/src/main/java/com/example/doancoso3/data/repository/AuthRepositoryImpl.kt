package com.example.doancoso3.data.repository

import com.example.doancoso3.data.local.dao.UserDao
import com.example.doancoso3.data.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Đăng ký thất bại"))

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Store user profile in Firestore
            val userMap = hashMapOf(
                "uid" to user.uid,
                "email" to email,
                "displayName" to displayName,
                "familyId" to null,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(userMap).await()

            // Also cache locally
            userDao.insert(
                UserEntity(
                    id = user.uid,
                    displayName = displayName,
                    email = email,
                    familyId = null,
                    createdAt = System.currentTimeMillis()
                )
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Đăng nhập thất bại"))
            
            android.util.Log.d("AuthRepository", "Login successful for user: ${user.uid}")
            // Sync user profile upon login
            val syncResult = syncUserProfile(user.uid)
            android.util.Log.d("AuthRepository", "Initial sync result: ${syncResult.isSuccess}")
            
            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            android.util.Log.d("AuthRepository", "Attempting to send password reset email to: $email")
            firebaseAuth.sendPasswordResetEmail(email).await()
            android.util.Log.d("AuthRepository", "Password reset email sent successfully to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Failed to send password reset email to: $email", e)
            Result.failure(e)
        }
    }

    override fun observeUserProfile(userId: String): Flow<UserEntity?> = callbackFlow {
        // Emit local Room data, and keep it fresh with a real-time Firestore listener so that
        // changes made on other devices (e.g. being approved into a family group, which sets
        // familyId remotely) are reflected locally.
        val roomJob = launch {
            userDao.observeUserById(userId).collect { trySend(it) }
        }

        val registration = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val displayName = snapshot.getString("displayName") ?: ""
                    val email = snapshot.getString("email") ?: ""
                    val familyId = snapshot.getString("familyId")
                    val createdAt = snapshot.getTimestamp("createdAt")?.toDate()?.time
                        ?: System.currentTimeMillis()
                    launch {
                        userDao.insert(
                            UserEntity(
                                id = userId,
                                displayName = displayName,
                                email = email,
                                familyId = familyId,
                                createdAt = createdAt
                            )
                        )
                    }
                }
            }

        awaitClose {
            registration.remove()
            roomJob.cancel()
        }
    }

    override suspend fun syncUserProfile(userId: String): Result<Unit> {
        return try {
            android.util.Log.d("AuthRepository", "Syncing profile for userId: $userId")
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val displayName = document.getString("displayName") ?: ""
                val email = document.getString("email") ?: ""
                val familyId = document.getString("familyId")
                val createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                
                val userEntity = UserEntity(
                    id = userId,
                    displayName = displayName,
                    email = email,
                    familyId = familyId,
                    createdAt = createdAt
                )
                userDao.insert(userEntity)
                android.util.Log.d("AuthRepository", "Profile synced and saved to local DB")
                Result.success(Unit)
            } else {
                android.util.Log.w("AuthRepository", "User profile not found in Firestore for uid: $userId")
                Result.failure(Exception("User profile not found in Firestore"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Sync failed", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(displayName: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))
            
            // 1. Update Firebase Auth Profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // 2. Update Firestore
            firestore.collection("users").document(user.uid)
                .update("displayName", displayName)
                .await()

            // 3. Update Local DB
            userDao.getUserById(user.uid)?.let { currentEntity ->
                userDao.insert(currentEntity.copy(displayName = displayName))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Update profile failed", e)
            Result.failure(e)
        }
    }
}
