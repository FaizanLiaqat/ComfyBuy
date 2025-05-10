package com.muhammadahmedmufii.comfybuy.worker // Example package name

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.muhammadahmedmufii.comfybuy.data.local.AppDatabase
import com.muhammadahmedmufii.comfybuy.data.repository.ProductRepository
import com.muhammadahmedmufii.comfybuy.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

// Worker class responsible for synchronizing data between Room and Firebase
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Initialize your repositories and Firebase instances
    // In a real app, you would likely use dependency injection (like Hilt or Koin)
    // to provide these dependencies. For now, we'll manually get instances.
    private val database = AppDatabase.getDatabase(appContext)
    private val userDao = database.userDao()
    private val productDao = database.productDao()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDatabase = FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app")
    val firebaseDatabase =FirebaseDatabase.getInstance("https://messamfaizanahmed-default-rtdb.asia-southeast1.firebasedatabase.app") // Get RTDB instance

    private val userRepository = UserRepository(userDao, firebaseAuth, firestore, firebaseDatabase)
    private val productRepository = ProductRepository(productDao, firestore, realtimeDatabase)

    // Define a key for storing the last sync timestamp in SharedPreferences
    private val LAST_SYNC_TIMESTAMP_PREF = "last_sync_timestamp_pref"
    private val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("SyncWorker", "Starting data synchronization...")

        // Get the timestamp of the last successful sync
        val lastSyncTimestamp = getLastSyncTimestamp()

        try {
            // --- Pull Sync: Fetch data from remote (Firebase) and save to local (Room) ---
            Log.d("SyncWorker", "Performing pull sync from remote...")
            productRepository.syncProductsFromRemote()
            // TODO: Implement pull sync for users if needed (e.g., fetching data of other users)
            // For current user, fetchAndSaveUser is typically called after login/signup.

            // --- Push Sync: Push local changes (new, updated, deleted) to remote (Firebase) ---
            Log.d("SyncWorker", "Performing push sync to remote...")
            productRepository.syncProductsToRemote(lastSyncTimestamp)
            // TODO: Implement push sync for user data if user can update their profile offline

            // --- Update Last Sync Timestamp ---
            setLastSyncTimestamp(System.currentTimeMillis())
            Log.d("SyncWorker", "Data synchronization completed successfully.")

            Result.success() // Indicate that the work finished successfully

        } catch (e: Exception) {
            Log.e("SyncWorker", "Data synchronization failed", e)
            // Indicate that the work failed. WorkManager can retry based on your WorkRequest settings.
            Result.retry() // Use Result.retry() to indicate the work should be retried
        }
    }

    // Helper function to get the last sync timestamp from SharedPreferences
    private fun getLastSyncTimestamp(): Long {
        val sharedPrefs = applicationContext.getSharedPreferences(LAST_SYNC_TIMESTAMP_PREF, Context.MODE_PRIVATE)
        // Default to 0 if no timestamp is found (will sync all data on first run)
        return sharedPrefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    }

    // Helper function to set the last sync timestamp in SharedPreferences
    private fun setLastSyncTimestamp(timestamp: Long) {
        val sharedPrefs = applicationContext.getSharedPreferences(LAST_SYNC_TIMESTAMP_PREF, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp)
            apply()
        }
    }
}
