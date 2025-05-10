package com.muhammadahmedmufii.comfybuy.data.repository // Example package name

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.muhammadahmedmufii.comfybuy.data.local.UserDao
import com.muhammadahmedmufii.comfybuy.data.local.UserEntity
import com.muhammadahmedmufii.comfybuy.domain.model.User // CORRECT: Import your domain User model
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull // Import firstOrNull
import kotlinx.coroutines.tasks.await // For converting Firebase Tasks to suspend functions
import kotlinx.coroutines.flow.flowOf // Import flowOf for returning a flow with a single value (null)
import javax.inject.Inject // If using dependency injection
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.withContext // Import withContext
import kotlinx.coroutines.flow.distinctUntilChanged // Import distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import java.io.ByteArrayOutputStream


// Repository for managing user data from local Room and remote Firestore
// This class acts as a single source of truth for user data.
class UserRepository @Inject constructor( // Use @Inject if using dependency injection
    private val userDao: UserDao, // Room DAO for local user data
    private val firebaseAuth: FirebaseAuth, // Firebase Auth for current user info
    private val firestore: FirebaseFirestore, // Firebase Firestore for remote user data
    private val rtdb: FirebaseDatabase
) {

    private val usersCollection = firestore.collection("users") // Reference to the "users" collection in Firestore

    private val userProfileImagesRtdbRef: DatabaseReference = rtdb.getReference("user_profile_images")

    // --- Conversion Helpers (can be moved to a utility class) ---
    private fun bitmapToBase64(bitmap: Bitmap?): String? {
        if (bitmap == null) return null
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Adjust quality
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP) // Use NO_WRAP for RTDB
        } catch (e: Exception) {
            Log.e("UserRepository", "Error converting bitmap to Base64", e)
            null
        }
    }

    private fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String == null) return null
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error converting Base64 to bitmap", e)
            null
        }
    }

    /**
     * Gets the current user's data.
     * Prioritizes local data and assumes a separate sync mechanism will update local data from remote.
     * Includes an attempt to fetch from Firestore if not found locally initially.
     * @return A Flow of the current User domain model, or null if no user is logged in.
     */
    fun getCurrentUser(): Flow<User?> {
        val currentAuthUser = firebaseAuth.currentUser
        if (currentAuthUser == null) {
            Log.d("UserRepository", "getCurrentUser: No Firebase user logged in.")
            return flowOf(null)
        }
        val userId = currentAuthUser.uid
        Log.d("UserRepository", "getCurrentUser: Observing local user data for UID: $userId")

        // Combine Firestore metadata flow with RTDB image flow
        return userDao.getUserById(userId).distinctUntilChanged().flatMapLatest { entity ->
            if (entity == null) {
                Log.d("UserRepository", "getCurrentUser: UserEntity null for $userId from Room. Fetching from remote.")
                // Optionally trigger a fetch from remote if local is null, then re-observe,
                // or just return null for now and let fetchAndSaveUser handle it.
                // For simplicity now, if entity is null, we won't try to get image from RTDB yet.
                // fetchAndSaveUser should populate it.
                flowOf(null)
            } else {
                // User entity exists, now try to get image Base64 from Room
                // The entity.profileImageBase64 should have been populated by fetchAndSaveUser
                Log.d("UserRepository", "getCurrentUser: UserEntity found for $userId. ImageBase64 from Room: ${entity.profileImageBase64 != null}")
                flowOf(entity.toDomainModel()) // toDomainModel will convert Base64 to Bitmap
            }
        }
    }

    /**
     * **NEW:** Gets a specific user's data by their ID from the local Room database.
     * This is used by the ProductDetailViewModel to get the seller's information.
     * @param userId The UID of the user to fetch.
     * @return A Flow of the User domain model, or null if not found locally.
     */
    fun getUserById(userId: String): Flow<User?> {
        Log.d("UserRepository", "getUserById: Observing local user data for UID: $userId")
        return userDao.getUserById(userId).map { userEntity ->
            Log.d("UserRepository", "getUserById: Local DB emitted userEntity: $userEntity for UID: $userId")
            userEntity?.toDomainModel()
        }.distinctUntilChanged()
        // Note: This only fetches from the local DB. A separate mechanism (like the sync worker
        // or fetching when viewing a product) should ensure the seller's data is in the local DB.
    }


    /**
     * Fetches user data from Firestore for a given user ID and saves it to Room.
     * This is typically used during initial sync or when a user logs in.
     * @param userId The UID of the user to fetch.
     */
    suspend fun fetchAndSaveUser(userId: String) = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "fetchAndSaveUser: Attempting for UID: $userId")
        try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            if (documentSnapshot.exists()) {
                var tempUserEntity: UserEntity? = documentSnapshot.toObject(UserEntity::class.java)
                val finalUserEntity: UserEntity = if (tempUserEntity == null) {
                    Log.w("UserRepository", "fetchAndSaveUser: toObject returned null for $userId, using createEntityFromRawData.")
                    createEntityFromRawData(documentSnapshot, userId) // createEntityFromRawData will now include new fields
                } else {
                    tempUserEntity
                }

                var entityWithImage = finalUserEntity
                try {
                    val rtdbSnapshot = userProfileImagesRtdbRef.child(userId).get().await()
                    val imageBase64FromRtdb = rtdbSnapshot.getValue(String::class.java)
                    entityWithImage = finalUserEntity.copy(profileImageBase64 = imageBase64FromRtdb) // It will be null if not in RTDB
                } catch (e: Exception) {
                    Log.e("UserRepository", "fetchAndSaveUser: Error fetching image from RTDB for $userId", e)
                    entityWithImage = finalUserEntity.copy(profileImageBase64 = null)
                }
                userDao.insertUser(entityWithImage)
                Log.d("UserRepository", "fetchAndSaveUser: Saved user to Room: $entityWithImage")
            } else {
                Log.w("UserRepository", "fetchAndSaveUser: User doc not in Firestore for UID: $userId. Creating basic.")
                createBasicUserInFirestore(userId, firebaseAuth.currentUser?.email)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "fetchAndSaveUser: Error for UID: $userId", e)
        }
    }

    private fun createEntityFromRawData(snapshot: DocumentSnapshot, userId: String): UserEntity {
        return UserEntity(
            userId = userId,
            fullName = snapshot.getString("fullName"),
            email = snapshot.getString("email"),
            username = snapshot.getString("username"),
            bio = snapshot.getString("bio"),
            location = snapshot.getString("location"),
            phoneNumber = snapshot.getString("phoneNumber"), // NEW
            gender = snapshot.getString("gender"),           // NEW
            dateOfBirth = snapshot.getString("dateOfBirth"), // NEW
            timestamp = snapshot.getLong("timestamp") ?: System.currentTimeMillis(),
            profileImageBase64 = null // Image base64 comes from RTDB in fetchAndSaveUser
        )
    }

    /**
     * Creates a basic user document in Firestore if it doesn't exist.
     * Useful for users created via Auth but without initial Firestore data.
     */
    private suspend fun createBasicUserInFirestore(userId: String, email: String?): Unit = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "createBasicUserInFirestore for UID: $userId")
        try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            if (!documentSnapshot.exists()) {
                Log.d("UserRepository", "Basic user data not found in Firestore for $userId, creating default entry.")
                val basicUserFirestore = hashMapOf<String, Any?>(
                    "userId" to userId,
                    "email" to email,
                    "fullName" to firebaseAuth.currentUser?.displayName,
                    "username" to null,
                    "bio" to null,
                    "location" to null,
                    "phoneNumber" to null, // NEW
                    "gender" to null,      // NEW
                    "dateOfBirth" to null, // NEW
                    "timestamp" to System.currentTimeMillis()
                )
                usersCollection.document(userId).set(basicUserFirestore, SetOptions.merge()).await()
                Log.d("UserRepository", "Basic user metadata created in Firestore for UID: $userId")
                userProfileImagesRtdbRef.child(userId).removeValue().await()
                Log.d("UserRepository", "Ensured no old image in RTDB for new user $userId")
                fetchAndSaveUser(userId)
            } else { /* ... */ }
        } catch (e: Exception) { /* ... */ }
    }






    /**
     * Saves user details and profile image (as Base64) to Room and remote databases.
     */
    // Renamed for clarity, or you can overload saveUserWithProfileImage
    suspend fun saveUserProfileDetails(userDetails: User, newImageBitmap: Bitmap?) = withContext(Dispatchers.IO) {
        Log.d("UserRepository", "saveUserProfileDetails for UID: ${userDetails.userId}, hasNewBitmap: ${newImageBitmap != null}")
        try {
            var finalImageBase64: String?
            val existingUserEntity = userDao.getUserById(userDetails.userId).firstOrNull() // Get current state from Room

            if (newImageBitmap != null) {
                finalImageBase64 = bitmapToBase64(newImageBitmap)
                if (finalImageBase64 != null) {
                    userProfileImagesRtdbRef.child(userDetails.userId).setValue(finalImageBase64).await()
                    Log.d("UserRepository", "NEW image saved to RTDB for ${userDetails.userId}")
                } else {
                    finalImageBase64 = existingUserEntity?.profileImageBase64 // Keep existing if new one fails
                    Log.e("UserRepository", "New image Base64 conversion failed. Keeping existing.")
                }
            } else {
                finalImageBase64 = existingUserEntity?.profileImageBase64 // No new image, keep existing
                Log.d("UserRepository", "No new image selected. Kept existing image Base64: ${finalImageBase64 != null}")
            }

            val userEntity = UserEntity(
                userId = userDetails.userId,
                fullName = userDetails.fullName,
                email = userDetails.email, // Be cautious if allowing email edit
                username = userDetails.username,
                bio = userDetails.bio,
                location = userDetails.location,
                phoneNumber = userDetails.phoneNumber, // NEW
                gender = userDetails.gender,           // NEW
                dateOfBirth = userDetails.dateOfBirth, // NEW
                profileImageBase64 = finalImageBase64,
                timestamp = System.currentTimeMillis()
            )
            userDao.insertUser(userEntity)
            Log.d("UserRepository", "Saved/Updated user in Room: $userEntity")

            val userFirestoreMap = hashMapOf<String, Any?>(
                "userId" to userDetails.userId,
                "fullName" to userDetails.fullName,
                "email" to userDetails.email,
                "username" to userDetails.username,
                "bio" to userDetails.bio,
                "location" to userDetails.location,
                "phoneNumber" to userDetails.phoneNumber, // NEW
                "gender" to userDetails.gender,           // NEW
                "dateOfBirth" to userDetails.dateOfBirth, // NEW
                "timestamp" to System.currentTimeMillis()
            )
            usersCollection.document(userDetails.userId).set(userFirestoreMap, SetOptions.merge()).await()
            Log.d("UserRepository", "Saved/Updated user metadata in Firestore for ${userDetails.userId}")

        } catch (e: Exception) {
            Log.e("UserRepository", "Error in saveUserProfileDetails for ${userDetails.userId}", e)
            throw e
        }
    }

    // You might need functions for:
    // - Deleting a user (less common in this app context)
    // - Syncing logic (can be triggered by WorkManager, the repository provides the methods WorkManager will call)

    // --- Helper functions to convert between domain model and entity ---
    // You should define your domain.model.User data class in a separate file

    /**
     * Converts a local UserEntity to a domain User model.
     */
    fun UserEntity.toDomainModel(): User {
        return User(
            userId = userId,
            fullName = fullName,
            email = email,
            profileImageBitmap = base64ToBitmap(profileImageBase64),
            username = username,
            bio = bio,
            location = location,
            phoneNumber = phoneNumber, // NEW
            gender = gender,           // NEW
            dateOfBirth = dateOfBirth  // NEW
        )
    }


    /**
     * Converts a domain User model to a local UserEntity.
     */
    fun User.toEntity(): UserEntity { // Removed imageBase64 param, as domain User has Bitmap
        return UserEntity(
            userId = userId,
            fullName = fullName,
            email = email,
            // profileImageBase64 will be set from profileImageBitmap before saving
            profileImageBase64 = null, // Placeholder, should be set by calling code if image changes
            username = username,
            bio = bio,
            location = location,
            phoneNumber = phoneNumber, // NEW
            gender = gender,           // NEW
            dateOfBirth = dateOfBirth, // NEW
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun clearUserProfileImageFromRtdb(userId: String) = withContext(Dispatchers.IO) {
        try {
            userProfileImagesRtdbRef.child(userId).removeValue().await()
            Log.d("UserRepository", "Cleared profile image from RTDB for $userId")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error clearing profile image from RTDB for $userId", e)
        }
    }
}
