package com.muhammadahmedmufii.comfybuy // Example package name

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.muhammadahmedmufii.comfybuy.worker.SyncWorker // Import your SyncWorker

import java.util.concurrent.TimeUnit

object SyncWorkScheduler {

    private const val SYNC_WORK_NAME = "ComfyBuySyncWork"

    fun schedulePeriodicSync(context: Context) {
        // Define constraints for when the work should run
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requires network connection
            // Add other constraints if needed (e.g., device charging, device idle)
            // .setRequiresCharging(true)
            // .setRequiresDeviceIdle(true)
            .build()

        // Create a PeriodicWorkRequest
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Repeat every 15 minutes
            // Add flex interval if needed: , 5, TimeUnit.MINUTES // Run within the last 5 mins of the interval
        )
            .setConstraints(constraints) // Apply constraints
            // Add backoff policy if retries are needed (Result.retry() in worker)
            // .setBackoffCriteria(
            //     BackoffPolicy.LINEAR, // or EXPONENTIAL
            //     PeriodicWorkRequest.MIN_BACKOFF_MILLIS, // Minimum backoff delay
            //     TimeUnit.MILLISECONDS
            // )
            .build()

        // Enqueue the work
        // Use ExistingPeriodicWorkPolicy.KEEP to keep the existing work if it's already scheduled
        // Use ExistingPeriodicWorkPolicy.REPLACE to cancel and replace the existing work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Log.d("SyncWorkScheduler", "Periodic sync work scheduled.")
    }

    // You might also want a function to cancel the work
    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Log.d("SyncWorkScheduler", "Periodic sync work cancelled.")
    }

    // You can also define and enqueue OneTimeWorkRequests for immediate sync needs
    fun triggerOneTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeSyncRequest)
        Log.d("SyncWorkScheduler", "One-time sync work enqueued.")
    }
}