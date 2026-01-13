package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * FolderScanWorker scans all syncing folders and creates sync tasks in the database.
 * This worker runs periodically to check for new files that need to be synced.
 */
@HiltWorker
class FolderScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncingFolderRepository: SyncingFolderRepository,
    private val syncFolderUseCase: SyncFolderUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "FolderScanWorker started")

            val syncingFolders = syncingFolderRepository.getSyncingFolders().first()
            if (syncingFolders.isEmpty()) {
                Log.d(TAG, "No syncing folders found")
                return Result.success()
            }

            var totalTasksCreated = 0
            syncingFolders.forEach { folder ->
                try {
                    val result = syncFolderUseCase(folder.path)
                    result.onSuccess { tasksCreated ->
                        totalTasksCreated += tasksCreated
                        Log.d(TAG, "Created $tasksCreated tasks for folder: ${folder.path}")
                    }.onFailure { e ->
                        Log.e(TAG, "Error scanning folder ${folder.path}: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error scanning folder ${folder.path}: ${e.message}", e)
                }
            }

            Log.d(TAG, "FolderScanWorker completed. Total tasks created: $totalTasksCreated")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "FolderScanWorker failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "FolderScanWorker"
        const val WORK_NAME = "folder_scan_work"
    }
}
