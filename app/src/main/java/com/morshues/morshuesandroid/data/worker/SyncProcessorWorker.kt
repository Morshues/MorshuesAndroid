package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.domain.usecase.ProcessSyncQueueUseCase
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
class SyncProcessorWorker(
    context: Context,
    params: WorkerParameters,
    private val syncTaskRepository: SyncTaskRepository,
    private val processSyncQueueUseCase: ProcessSyncQueueUseCase,
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

                val result = processSyncQueueUseCase(maxTasks = availableSlots)
                result.onSuccess { count ->
                    totalProcessed += availableSlots
                    Log.d(TAG, "Enqueued $count tasks (total: $totalProcessed)")
                }.onFailure { e ->
                    Log.e(TAG, "Error processing queue: ${e.message}", e)
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

    companion object {
        private const val TAG = "SyncProcessorWorker"
        const val WORK_NAME = "sync_processor_work"
        const val KEY_MAX_CONCURRENT = "max_concurrent"
        const val DEFAULT_MAX_CONCURRENT = 3

        private const val BATCH_DELAY_MS = 1000L
    }
}
