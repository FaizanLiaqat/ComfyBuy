package com.muhammadahmedmufii.comfybuy.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.muhammadahmedmufii.comfybuy.FollowUser
import com.muhammadahmedmufii.comfybuy.data.model.Follow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    private val TAG = "FirebaseRepository"

    // Collection references
    private val usersCollection = firestore.collection("users")
    private val followsCollection = firestore.collection("follows")

    // Get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Search users
    suspend fun searchUsers(query: String): List<FollowUser> = withContext(Dispatchers.IO) {
        try {
            val result = mutableListOf<FollowUser>()

            // Search by fullName
            val nameQuery = usersCollection
                .whereGreaterThanOrEqualTo("fullName", query)
                .whereLessThanOrEqualTo("fullName", query + '\uf8ff')
                .limit(20)
                .get()
                .await()

            // Search by username
            val usernameQuery = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + '\uf8ff')
                .limit(20)
                .get()
                .await()

            // Combine results (removing duplicates)
            val userIds = mutableSetOf<String>()
            val currentUserId = getCurrentUserId()

            for (document in nameQuery.documents) {
                val userId = document.getString("userId") ?: continue
                if (userIds.contains(userId)) continue

                userIds.add(userId)
                val fullName = document.getString("fullName") ?: ""
                val username = document.getString("username") ?: ""

                // Check if current user is following this user
                val isFollowing = if (currentUserId != null) {
                    isFollowing(currentUserId, userId)
                } else {
                    false
                }

                result.add(FollowUser(
                    userId = userId,
                    name = fullName,
                    username = username,
                    profilePicUrl = "",
                    followerCount = document.getLong("followerCount")?.toInt() ?: 0,
                    followingCount = document.getLong("followingCount")?.toInt() ?: 0,
                    bio = document.getString("bio") ?: "",
                    isFollowing = isFollowing
                ))
            }

            for (document in usernameQuery.documents) {
                val userId = document.getString("userId") ?: continue
                if (userIds.contains(userId)) continue

                userIds.add(userId)
                val fullName = document.getString("fullName") ?: ""
                val username = document.getString("username") ?: ""

                // Check if current user is following this user
                val isFollowing = if (currentUserId != null) {
                    isFollowing(currentUserId, userId)
                } else {
                    false
                }

                result.add(FollowUser(
                    userId = userId,
                    name = fullName,
                    username = username,
                    profilePicUrl = "",
                    followerCount = document.getLong("followerCount")?.toInt() ?: 0,
                    followingCount = document.getLong("followingCount")?.toInt() ?: 0,
                    bio = document.getString("bio") ?: "",
                    isFollowing = isFollowing
                ))
            }

            return@withContext result
        } catch (e: Exception) {
            Log.e(TAG, "Error searching users: ${e.message}")
            return@withContext emptyList()
        }
    }

    // Follow operations
    suspend fun followUser(followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: return@withContext false

            // Check if already following
            val existingFollow = followsCollection
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            if (!existingFollow.isEmpty) {
                // Already following
                return@withContext true
            }

            // Create follow relationship
            val follow = Follow(
                followerId = currentUserId,
                followingId = followingId,
                timestamp = Date()
            )

            // Add to follows collection
            followsCollection.add(follow).await()

            // Update follower count for the followed user
            usersCollection.document(followingId)
                .update("followerCount", FieldValue.increment(1))
                .await()

            // Update following count for the current user
            usersCollection.document(currentUserId)
                .update("followingCount", FieldValue.increment(1))
                .await()

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error following user: ${e.message}")
            return@withContext false
        }
    }

    suspend fun unfollowUser(followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId() ?: return@withContext false

            // Find the follow document
            val querySnapshot = followsCollection
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // Not following
                return@withContext true
            }

            // Delete the follow document
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
            }

            // Update follower count for the unfollowed user
            usersCollection.document(followingId)
                .update("followerCount", FieldValue.increment(-1))
                .await()

            // Update following count for the current user
            usersCollection.document(currentUserId)
                .update("followingCount", FieldValue.increment(-1))
                .await()

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing user: ${e.message}")
            return@withContext false
        }
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = followsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            return@withContext !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking follow status: ${e.message}")
            return@withContext false
        }
    }

    // Get followers as a Flow
    fun getFollowers(userId: String): Flow<List<FollowUser>> = flow {
        try {
            // Get all followers
            val querySnapshot = followsCollection
                .whereEqualTo("followingId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val result = mutableListOf<FollowUser>()
            val currentUserId = getCurrentUserId()

            // Get user details for each follower
            for (document in querySnapshot.documents) {
                val followerId = document.getString("followerId") ?: continue

                // Get user details from Firestore
                val userDoc = usersCollection.document(followerId).get().await()
                if (!userDoc.exists()) continue

                val fullName = userDoc.getString("fullName") ?: ""
                val username = userDoc.getString("username") ?: ""

                // Check if current user is following this follower
                val isFollowing = if (currentUserId != null) {
                    isFollowing(currentUserId, followerId)
                } else {
                    false
                }

                result.add(FollowUser(
                    userId = followerId,
                    name = fullName,
                    username = username,
                    profilePicUrl = "",
                    followerCount = userDoc.getLong("followerCount")?.toInt() ?: 0,
                    followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                    bio = userDoc.getString("bio") ?: "",
                    isFollowing = isFollowing
                ))
            }

            emit(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting followers: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Get following as a Flow
    fun getFollowing(userId: String): Flow<List<FollowUser>> = flow {
        try {
            // Get all following
            val querySnapshot = followsCollection
                .whereEqualTo("followerId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val result = mutableListOf<FollowUser>()
            val currentUserId = getCurrentUserId()

            // Get user details for each following
            for (document in querySnapshot.documents) {
                val followingId = document.getString("followingId") ?: continue

                // Get user details from Firestore
                val userDoc = usersCollection.document(followingId).get().await()
                if (!userDoc.exists()) continue

                val fullName = userDoc.getString("fullName") ?: ""
                val username = userDoc.getString("username") ?: ""

                // Always true since the user is following them
                val isFollowing = true

                result.add(FollowUser(
                    userId = followingId,
                    name = fullName,
                    username = username,
                    profilePicUrl = "",
                    followerCount = userDoc.getLong("followerCount")?.toInt() ?: 0,
                    followingCount = userDoc.getLong("followingCount")?.toInt() ?: 0,
                    bio = userDoc.getString("bio") ?: "",
                    isFollowing = isFollowing
                ))
            }

            emit(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting following: ${e.message}")
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Initialize user follow counts if they don't exist
    suspend fun initializeUserFollowCounts(userId: String) = withContext(Dispatchers.IO) {
        try {
            val userDoc = usersCollection.document(userId).get().await();

            if (userDoc.exists()) {
                val updates = hashMapOf<String, Any>()

                if (!userDoc.contains("followerCount")) {
                    updates["followerCount"] = 0
                }
                else if (!userDoc.contains("followingCount")) {
                    updates["followingCount"] = 0
                }
                else if (updates.isNotEmpty()) {
                    usersCollection.document(userId).set(updates, SetOptions.merge()).await()
                    Log.d(TAG, "Initialized follow counts for user $userId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing follow counts: ${e.message}")
        }
    }

    // Get follower count
    suspend fun getFollowerCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val userDoc = usersCollection.document(userId).get().await()
            return@withContext userDoc.getLong("followerCount")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting follower count: ${e.message}")
            return@withContext 0
        }
    }

    // Get following count
    suspend fun getFollowingCount(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            val userDoc = usersCollection.document(userId).get().await()
            return@withContext userDoc.getLong("followingCount")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting following count: ${e.message}")
            return@withContext 0
        }
    }

    // Data class for Follow
    data class Follow(
        val followerId: String = "",
        val followingId: String = "",
        val timestamp: Date = Date()
    )
}