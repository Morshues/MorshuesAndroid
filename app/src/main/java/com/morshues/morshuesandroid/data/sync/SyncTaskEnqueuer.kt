package com.morshues.morshuesandroid.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.morshues.morshuesandroid.data.db.entity.SyncTask
import com.morshues.morshuesandroid.data.db.entity.SyncType
import com.morshues.morshuesandroid.data.worker.FileDownloadWorker
import com.morshues.morshuesandroid.data.worker.FileUploadWorker
import com.morshues.morshuesandroid.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Helper class to enqueue upload/download workers for sync tasks.
 * Shared between FileSyncViewModel and SyncProcessorWorker.
 */
class SyncTaskEnqueuer(
    private val context: Context,
    private val workManager: WorkManager,
) {

    /**
     * Enqueue an upload or download worker for the given task.
     * @return WorkManager UUID for tracking
     */
    fun enqueueTask(task: SyncTask): UUID {
        return when (task.syncType) {
            SyncType.UPLOAD -> enqueueUploadWorker(task)
            SyncType.DOWNLOAD -> enqueueDownloadWorker(task)
        }
    }

    private fun getNetworkType(): NetworkType {
        val settingsManager = SettingsManager(context)
        val networkTypeString = runBlocking {
            settingsManager.getSyncNetworkType().first()
        }
        return when (networkTypeString) {
            SettingsManager.NETWORK_TYPE_ANY -> NetworkType.CONNECTED
            SettingsManager.NETWORK_TYPE_WIFI_ONLY -> NetworkType.UNMETERED
            else -> NetworkType.UNMETERED
        }
    }

    private fun enqueueUploadWorker(task: SyncTask): UUID {
        val uploadData = workDataOf(
            FileUploadWorker.KEY_TASK_ID to task.id,
            FileUploadWorker.KEY_FOLDER_PATH to task.folderPath,
            FileUploadWorker.KEY_FILE_PATH to task.filePath
        )

        val uploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setInputData(uploadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(getNetworkType())
                    .build()
            )
            .addTag(TAG_PREFIX_UPLOAD)
            .addTag("$TAG_PREFIX_UPLOAD${task.folderPath}")
            .addTag("$TAG_PREFIX_TASK${task.id}")
            .build()

        workManager.enqueue(uploadRequest)
        return uploadRequest.id
    }

    private fun enqueueDownloadWorker(task: SyncTask): UUID {
        val downloadData = workDataOf(
            FileDownloadWorker.KEY_TASK_ID to task.id,
            FileDownloadWorker.KEY_FOLDER_PATH to task.folderPath,
            FileDownloadWorker.KEY_FILE_NAME to task.fileName
        )

        val downloadRequest = OneTimeWorkRequestBuilder<FileDownloadWorker>()
            .setInputData(downloadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(getNetworkType())
                    .build()
            )
            .addTag(TAG_PREFIX_DOWNLOAD)
            .addTag("$TAG_PREFIX_DOWNLOAD${task.folderPath}")
            .addTag("$TAG_PREFIX_TASK${task.id}")
            .build()

        workManager.enqueue(downloadRequest)
        return downloadRequest.id
    }

    fun cancelWorkersByFolder(folderPath: String) {
        workManager.cancelAllWorkByTag("$TAG_PREFIX_UPLOAD$folderPath")
        workManager.cancelAllWorkByTag("$TAG_PREFIX_DOWNLOAD$folderPath")
    }

    fun cancelAllUploadWorkers() {
        workManager.cancelAllWorkByTag(TAG_PREFIX_UPLOAD)
    }

    fun cancelAllDownloadWorkers() {
        workManager.cancelAllWorkByTag(TAG_PREFIX_DOWNLOAD)
    }

    companion object {
        private const val TAG_PREFIX_UPLOAD = "upload_"
        private const val TAG_PREFIX_DOWNLOAD = "download_"
        private const val TAG_PREFIX_TASK = "task_"
    }
}
