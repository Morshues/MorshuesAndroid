package com.morshues.morshuesandroid.domain.usecase

import android.util.Log
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer

/**
 * Use case for processing pending sync tasks from the queue.
 * Picks up pending tasks and enqueues upload/download workers for them.
 */
class ProcessSyncQueueUseCase(
    private val syncTaskRepository: SyncTaskRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
) {

    /**
     * Process pending sync tasks.
     *
     * @param maxTasks Maximum number of tasks to process
     * @return Number of tasks successfully enqueued
     */
    suspend operator fun invoke(maxTasks: Int = 3): Result<Int> {
        return try {
            val tasks = syncTaskRepository.getPendingTasks(limit = maxTasks)

            var successCount = 0
            tasks.forEach { task ->
                try {
                    val workerId = syncTaskEnqueuer.enqueueTask(task)
                    syncTaskRepository.markTaskStartedWithWorker(task.id, workerId.toString())
                    successCount++
                    Log.d(TAG, "Enqueued ${task.syncType} worker for: ${task.fileName}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error enqueueing task ${task.id}: ${e.message}", e)
                    syncTaskRepository.markTaskFailed(task.id, e.message ?: "Enqueue failed")
                }
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Log.e(TAG, "ProcessSyncQueueUseCase failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "ProcessSyncQueueUseCase"
    }
}
