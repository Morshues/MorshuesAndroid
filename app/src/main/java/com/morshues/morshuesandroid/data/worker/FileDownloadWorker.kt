package com.morshues.morshuesandroid.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import java.io.File

class FileDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val folderPath = inputData.getString(KEY_FOLDER_PATH) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val targetPath = File(folderPath, fileName).absolutePath

        return try {
            val remoteFileResult = remoteFileRepository.downloadFile(folderPath, fileName)
            localFileRepository.writeFile(targetPath, remoteFileResult)

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
        const val KEY_FILE_NAME = "file_name"
    }
}
