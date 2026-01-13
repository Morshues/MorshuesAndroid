package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.sync.SyncTaskEnqueuer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * SyncProcessorWorker processes pending sync tasks from the database.
 * It picks up pending tasks and enqueues upload/download workers for them.
 * This worker runs periodically to process the task queue.
 *
 * Processing strategy:
 * - Continuously processes tasks in batches until queue is empty or max concurrent reached
 * - Waits between batches to allow workers to start and update their status
 * - Ensures all pending tasks are processed without waiting for next periodic run
 */
@HiltWorker
class SyncProcessorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncTaskEnqueuer: SyncTaskEnqueuer,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "SyncProcessorWorker started")

            val maxConcurrent = inputData.getInt(KEY_MAX_CONCURRENT, DEFAULT_MAX_CONCURRENT)
            var totalProcessed = 0

            // Process tasks in a loop until queue is empty
            while (true) {
                val inProgressCount = syncTaskRepository.getActiveTaskCount().first()
                val pendingCount = syncTaskRepository.getPendingTaskCount().first()

                Log.d(TAG, "In progress: $inProgressCount, Pending: $pendingCount, Total processed: $totalProcessed")

                if (pendingCount == 0) {
                    Log.d(TAG, "No more pending tasks")
                    break
                }

                val availableSlots = maxConcurrent - inProgressCount
                if (availableSlots <= 0) {
                    Log.d(TAG, "Max concurrent tasks reached ($inProgressCount/$maxConcurrent), waiting...")
                    delay(BATCH_DELAY_MS)
                    continue
                }

                val successCount = processSyncQueue(maxTasks = availableSlots)
                if (successCount >= 0) {
                    totalProcessed += successCount
                    Log.d(TAG, "Enqueued $successCount tasks (total: $totalProcessed)")
                } else {
                    Log.e(TAG, "Error processing queue")
                }

                delay(BATCH_DELAY_MS)
            }

            Log.d(TAG, "SyncProcessorWorker completed. Total processed: $totalProcessed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncProcessorWorker failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Process pending sync tasks from the queue.
     * Picks up pending tasks and enqueues upload/download workers for them.
     *
     * @param maxTasks Maximum number of tasks to process
     * @return Number of tasks successfully enqueued, or -1 on error
     */
    private suspend fun processSyncQueue(maxTasks: Int): Int {
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
            successCount
        } catch (e: Exception) {
            Log.e(TAG, "processSyncQueue failed: ${e.message}", e)
            -1
        }
    }

    companion object {
        private const val TAG = "SyncProcessorWorker"
        const val WORK_NAME = "sync_processor_work"
        const val KEY_MAX_CONCURRENT = "max_concurrent"
        const val DEFAULT_MAX_CONCURRENT = 3

        private const val BATCH_DELAY_MS = 1000L
    }
}
