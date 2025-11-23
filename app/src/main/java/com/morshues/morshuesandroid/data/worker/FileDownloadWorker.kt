package com.morshues.morshuesandroid.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import java.io.File

class FileDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncTaskRepository: SyncTaskRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        val folderPath = inputData.getString(KEY_FOLDER_PATH) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val targetPath = File(folderPath, fileName).absolutePath

        return try {
            Log.d(TAG, "Downloading file: $fileName")

            val remoteFileResult = remoteFileRepository.downloadFile(folderPath, fileName)
            localFileRepository.writeFile(targetPath, remoteFileResult)

            // Mark task as completed if taskId is provided
            if (taskId != -1L) {
                syncTaskRepository.markTaskCompleted(taskId)
            }

            Log.d(TAG, "Download completed: $fileName")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Download failed (attempt ${runAttemptCount + 1}/3): $fileName - ${e.message}", e)

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                if (taskId != -1L) {
                    syncTaskRepository.markTaskFailed(taskId, e.message ?: "Download failed after 3 attempts")
                }
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "FileDownloadWorker"
        const val KEY_TASK_ID = "task_id"
        const val KEY_FOLDER_PATH = "folder_path"
        const val KEY_FILE_NAME = "file_name"
    }
}
