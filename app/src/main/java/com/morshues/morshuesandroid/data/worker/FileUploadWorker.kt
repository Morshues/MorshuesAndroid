package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val remoteFileRepository: RemoteFileRepository,
    private val syncTaskRepository: SyncTaskRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        val folderPath = inputData.getString(KEY_FOLDER_PATH) ?: return Result.failure()
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()

        if (!File(filePath).exists()) {
            Log.d(TAG, "Uploading file not exists: $filePath")
            syncTaskRepository.markTaskFailed(taskId, "Uploading file not exists")
            return Result.failure()
        }

        return try {
            Log.d(TAG, "Uploading file: $filePath")

            remoteFileRepository.uploadFile(folderPath, filePath)

            // Mark task as completed if taskId is provided
            if (taskId != -1L) {
                syncTaskRepository.markTaskCompleted(taskId)
            }

            Log.d(TAG, "Upload completed: $filePath")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed (attempt ${runAttemptCount + 1}/3): $filePath - ${e.message}", e)

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                if (taskId != -1L) {
                    syncTaskRepository.markTaskFailed(taskId, e.message ?: "Upload failed after 3 attempts")
                }
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "FileUploadWorker"
        const val KEY_TASK_ID = "task_id"
        const val KEY_FOLDER_PATH = "folder_path"
        const val KEY_FILE_PATH = "file_path"
    }
}
