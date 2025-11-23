package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository

/**
 * CleanupCompletedTasksWorker deletes completed sync tasks from the database.
 * This worker runs daily to keep the database clean and prevent it from growing indefinitely.
 */
class CleanupCompletedTasksWorker(
    context: Context,
    params: WorkerParameters,
    private val syncTaskRepository: SyncTaskRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "CleanupCompletedTasksWorker started")

            syncTaskRepository.clearCompletedTasks()
            syncTaskRepository.clearFailedTasks()

            Log.d(TAG, "CleanupCompletedTasksWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "CleanupCompletedTasksWorker failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "CleanupCompletedTasksWorker"
        const val WORK_NAME = "cleanup_completed_tasks_work"
    }
}
