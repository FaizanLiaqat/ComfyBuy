package com.muhammadahmedmufii.comfybuy.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// import com.google.firebase.firestore.FirebaseFirestore // REMOVE if not used for other things
// import com.muhammadahmedmufii.comfybuy.data.local.UserDao // Comment out Room
// import com.muhammadahmedmufii.comfybuy.data.local.UserEntity // Comment out Room
import com.muhammadahmedmufii.comfybuy.data.model.RtdbUser
import com.muhammadahmedmufii.comfybuy.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class UserRepository @Inject constructor(
    // private val userDao: UserDao, // Room DAO (Commented out)
    private val firebaseAuth: FirebaseAuth,
    // private val firestore: FirebaseFirestore, // Firestore (Commented out if user data fully moves to RTDB)
    private val rtdb: FirebaseDatabase
) {
    private val TAG = "UserRepository"
    private val usersRtdbRef = rtdb.getReference("users") // Path for user data in RTDB

    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) { Log.e(TAG, "Error converting bitmap to Base64", e); null }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) { Log.e(TAG, "Error converting Base64 to bitmap", e); null }
    }

    // --- Mappers: RTDB Pojo <-> Domain ---
    private fun RtdbUser.toDomainModel(): User {
        return User(
            userId = this.userId,
            fullName = this.fullName,
            email = this.email,
            profileImageBitmap = base64ToBitmap(this.profileImageBase64),
            username = this.username,
            bio = this.bio,
            location = this.location,
            phoneNumber = this.phoneNumber,
            gender = this.gender,
            dateOfBirth = this.dateOfBirth,
            timestamp = this.timestamp
            // Follower/following counts would need to be fetched/calculated if stored separately
        )
    }

    private fun User.toRtdbData(): RtdbUser {
        return RtdbUser(
            userId = this.userId,
            fullName = this.fullName,
            email = this.email,
            username = this.username,
            bio = this.bio,
            location = this.location,
            phoneNumber = this.phoneNumber,
            gender = this.gender,
            dateOfBirth = this.dateOfBirth,
            timestamp = this.timestamp, // Use domain model's timestamp if it's meaningful
            profileImageBase64 = bitmapToBase64(this.profileImageBitmap)
        )
    }

    // --- Data Operations ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUserById(userId: String): Flow<User?> = callbackFlow {
        Log.d(TAG, "getUserById (RTDB): Listening to user $userId")
        val userRef = usersRtdbRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rtdbUser = snapshot.getValue(RtdbUser::class.java)
                val domainUser = rtdbUser?.toDomainModel()
                Log.d(TAG, "getUserById (RTDB): Data for $userId. User: $domainUser")
                trySend(domainUser).isSuccess
                // No Room caching for now
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "getUserById (RTDB): Listener cancelled for $userId", error.toException())
                close(error.toException())
            }
        }
        userRef.addValueEventListener(listener)
        awaitClose {
            Log.d(TAG, "getUserById (RTDB): Closing listener for $userId")
            userRef.removeEventListener(listener)
        }
    }.flowOn(Dispatchers.IO)

    fun getCurrentUser(): Flow<User?> {
        val currentAuthUser = firebaseAuth.currentUser
        return if (currentAuthUser != null) {
            getUserById(currentAuthUser.uid) // Reuses the RTDB listener
        } else {
            Log.d(TAG, "getCurrentUser: No Firebase user logged in.")
            flowOf(null)
        }
    }

    suspend fun fetchUserAndEnsureExistsInRtdb(userId: String, email: String?, displayName: String?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "fetchUserAndEnsureExistsInRtdb: Checking/Creating user $userId in RTDB")
        val userRef = usersRtdbRef.child(userId)
        val snapshot = userRef.get().await()
        if (!snapshot.exists()) {
            Log.w(TAG, "User $userId not found in RTDB. Creating basic record.")
            val newUser = RtdbUser(
                userId = userId,
                email = email,
                fullName = displayName,
                timestamp = System.currentTimeMillis()
                // Other fields default to null
            )
            userRef.setValue(newUser).await()
            Log.d(TAG, "Basic record for user $userId created in RTDB.")
        } else {
            Log.d(TAG, "User $userId already exists in RTDB.")
        }
        // No Room interaction for now
    }

    suspend fun saveUserProfileDetails(userDetails: User, newProfileBitmap: Bitmap?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveUserProfileDetails (RTDB) for UID: ${userDetails.userId}, hasNewBitmap: ${newProfileBitmap != null}")
        try {
            // If a new bitmap is provided, update it on the userDetails domain object first
            val userToSave = if (newProfileBitmap != null) {
                userDetails.copy(profileImageBitmap = newProfileBitmap)
            } else {
                userDetails // Use existing bitmap on domain object (could be null if user cleared it)
            }

            val rtdbData = userToSave.toRtdbData() // This will convert the final profileImageBitmap to Base64

            // Update RTDB (setValue will overwrite the entire node for this user)
            usersRtdbRef.child(userDetails.userId).setValue(rtdbData).await()
            Log.i(TAG, "Saved user profile to RTDB for ${userDetails.userId}")

            // No Room interaction for now
        } catch (e: Exception) {
            Log.e(TAG, "Error in saveUserProfileDetails (RTDB) for ${userDetails.userId}", e)
            throw e
        }
    }

    suspend fun clearUserProfileImageFromRtdb(userId: String) = withContext(Dispatchers.IO) {
        try {
            usersRtdbRef.child(userId).child("profileImageBase64").removeValue().await()
            Log.d(TAG, "Cleared profileImageBase64 from RTDB for $userId")
            // No Room interaction for now
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing profile image from RTDB for $userId", e)
        }
    }
}