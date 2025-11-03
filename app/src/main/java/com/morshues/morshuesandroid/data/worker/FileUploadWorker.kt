package com.morshues.morshuesandroid.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository

class FileUploadWorker(
    context: Context,
    params: WorkerParameters,
    private val remoteFileRepository: RemoteFileRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val folderPath = inputData.getString(KEY_FOLDER_PATH) ?: return Result.failure()
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()

        return try {
            remoteFileRepository.uploadFile(folderPath, filePath)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_FOLDER_PATH = "folder_path"
        const val KEY_FILE_PATH = "file_path"
    }
}
