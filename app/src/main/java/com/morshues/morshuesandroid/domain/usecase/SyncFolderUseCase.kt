package com.morshues.morshuesandroid.domain.usecase

import com.morshues.morshuesandroid.data.db.entity.SyncStatus
import com.morshues.morshuesandroid.data.db.entity.SyncTask
import com.morshues.morshuesandroid.data.db.entity.SyncType
import com.morshues.morshuesandroid.data.model.FileEntry
import com.morshues.morshuesandroid.data.model.FileItem
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.utils.MediaFileHelper
import java.io.File

/**
 * Use case for scanning a folder and creating sync tasks.
 * Shared logic between FolderScanWorker and FileSyncViewModel.
 */
class SyncFolderUseCase(
    private val localFileRepository: LocalFileRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val syncTaskRepository: SyncTaskRepository,
) {

    /**
     * Scans a folder and creates sync tasks for files that need to be synced.
     *
     * @param folderPath Path to the folder to scan
     * @return Number of tasks created
     */
    suspend operator fun invoke(folderPath: String): Result<Int> {
        return try {
            val tasksCreated = scanAndCreateTasks(folderPath)
            Result.success(tasksCreated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun scanAndCreateTasks(folderPath: String): Int {
        val localFiles = localFileRepository.listFiles(folderPath)
            .filterIsInstance<FileItem>()

        val remoteCompareResult = remoteFileRepository.compareFolder(
            folderPath = folderPath,
            entries = localFiles.map {
                FileEntry(
                    name = it.name,
                    modifiedAt = it.lastModified,
                    size = it.sizeBytes,
                )
            }
        )

        val tasksToCreate = mutableListOf<SyncTask>()
        remoteCompareResult.download
            .filter { MediaFileHelper.isMediaFile(it.name) }
            .forEach { fileEntry ->
                tasksToCreate.add(
                    SyncTask(
                        folderPath = folderPath,
                        fileName = fileEntry.name,
                        filePath = File(folderPath, fileEntry.name).path,
                        syncType = SyncType.DOWNLOAD,
                        status = SyncStatus.PENDING,
                        fileSize = fileEntry.size
                    )
                )
            }
        remoteCompareResult.upload.forEach { fileEntry ->
            tasksToCreate.add(
                SyncTask(
                    folderPath = folderPath,
                    fileName = fileEntry.name,
                    filePath = File(folderPath, fileEntry.name).path,
                    syncType = SyncType.UPLOAD,
                    status = SyncStatus.PENDING,
                    fileSize = fileEntry.size
                )
            )
        }

        if (tasksToCreate.isNotEmpty()) {
            syncTaskRepository.addTasks(tasksToCreate)
        }

        return tasksToCreate.size
    }
}
