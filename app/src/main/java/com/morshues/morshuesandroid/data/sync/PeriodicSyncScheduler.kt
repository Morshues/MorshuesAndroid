package com.morshues.morshuesandroid.data.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.morshues.morshuesandroid.data.worker.CleanupCompletedTasksWorker
import com.morshues.morshuesandroid.data.worker.FolderScanWorker
import com.morshues.morshuesandroid.data.worker.SyncProcessorWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PeriodicSyncScheduler manages the scheduling of periodic sync operations.
 * It schedules three types of workers:
 * 1. FolderScanWorker - Scans folders and creates sync tasks
 * 2. SyncProcessorWorker - Processes pending sync tasks
 * 3. CleanupCompletedTasksWorker - Cleans up completed tasks daily
 */
@Singleton
class PeriodicSyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Initialize periodic sync with default settings.
     * This should be called when the app starts (e.g., in Application.onCreate()).
     */
    fun scheduleAll() {
        Log.d(TAG, "Initializing periodic sync")
        scheduleFolderScan()
        scheduleProcessor()
        scheduleCleanup()
    }

    /**
     * Schedule the FolderScanWorker to run periodically.
     * This worker scans all syncing folders and creates sync tasks.
     *
     * @param intervalMinutes How often to run (minimum 15 minutes)
     */
    private fun scheduleFolderScan(
        intervalMinutes: Long = FOLDER_SCAN_INTERVAL_MINUTES
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val folderScanRequest = PeriodicWorkRequestBuilder<FolderScanWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WORKER_TAG_PERIODIC_SYNC)
            .addTag(WORKER_TAG_FOLDER_SCAN)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FolderScanWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            folderScanRequest
        )

        Log.d(TAG, "FolderScan scheduled to run every $intervalMinutes minutes")
    }

    /**
     * Schedule the SyncProcessorWorker to run periodically.
     * This worker processes pending tasks from the database.
     *
     * @param intervalMinutes How often to run (minimum 15 minutes)
     * @param maxConcurrent Maximum number of concurrent upload/download tasks
     */
    private fun scheduleProcessor(
        intervalMinutes: Long = PROCESSOR_INTERVAL_MINUTES,
        maxConcurrent: Int = MAX_CONCURRENT
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val processorRequest = PeriodicWorkRequestBuilder<SyncProcessorWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(SyncProcessorWorker.KEY_MAX_CONCURRENT to maxConcurrent)
            )
            .addTag(WORKER_TAG_PERIODIC_SYNC)
            .addTag(WORKER_TAG_PROCESSOR)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncProcessorWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            processorRequest
        )

        Log.d(TAG, "SyncProcessor scheduled to run every $intervalMinutes minutes (max $maxConcurrent concurrent)")
    }

    /**
     * Schedule the CleanupCompletedTasksWorker to run periodically.
     * This worker deletes completed and failed sync tasks from the database.
     *
     * @param intervalHours How often to run (minimum 1 hour, default 24 hours)
     */
    private fun scheduleCleanup(
        intervalHours: Long = CLEANUP_INTERVAL_HOURS
    ) {
        val cleanupRequest = PeriodicWorkRequestBuilder<CleanupCompletedTasksWorker>(
            repeatInterval = intervalHours,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .addTag(WORKER_TAG_PERIODIC_SYNC)
            .addTag(WORKER_TAG_CLEANUP)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CleanupCompletedTasksWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )

        Log.d(TAG, "Cleanup scheduled to run every $intervalHours hours")
    }

    companion object {
        private const val TAG = "PeriodicSyncScheduler"

        private const val FOLDER_SCAN_INTERVAL_MINUTES = 30L
        private const val PROCESSOR_INTERVAL_MINUTES = 15L
        private const val CLEANUP_INTERVAL_HOURS = 24L
        private const val MAX_CONCURRENT = 3

        private const val WORKER_TAG_PERIODIC_SYNC = "periodic_sync"
        private const val WORKER_TAG_FOLDER_SCAN = "folder_scan"
        private const val WORKER_TAG_PROCESSOR = "processor"
        private const val WORKER_TAG_CLEANUP = "cleanup"
    }
}
