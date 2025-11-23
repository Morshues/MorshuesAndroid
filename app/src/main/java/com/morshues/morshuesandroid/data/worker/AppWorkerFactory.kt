package com.morshues.morshuesandroid.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository
import com.morshues.morshuesandroid.data.repository.SyncTaskRepository
import com.morshues.morshuesandroid.data.repository.SyncingFolderRepository
import com.morshues.morshuesandroid.domain.usecase.ProcessSyncQueueUseCase
import com.morshues.morshuesandroid.domain.usecase.SyncFolderUseCase

/**
 * Custom WorkerFactory that provides proper dependency injection for Workers.
 * This replaces the direct AppModule access in Worker classes.
 *
 * Note: processSyncQueueUseCaseProvider is a lazy provider to break circular dependency:
 * AppModule -> ProcessSyncQueueUseCase -> SyncTaskEnqueuer -> WorkManager -> AppWorkerFactory
 */
class AppWorkerFactory(
    private val syncingFolderRepository: SyncingFolderRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
    private val syncTaskRepository: SyncTaskRepository,
    private val syncFolderUseCase: SyncFolderUseCase,
    private val processSyncQueueUseCaseProvider: () -> ProcessSyncQueueUseCase,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            FolderScanWorker::class.java.name -> FolderScanWorker(
                appContext,
                workerParameters,
                syncingFolderRepository,
                syncFolderUseCase,
            )
            SyncProcessorWorker::class.java.name -> SyncProcessorWorker(
                appContext,
                workerParameters,
                syncTaskRepository,
                processSyncQueueUseCaseProvider(), // Lazy evaluation breaks circular dependency
            )
            FileUploadWorker::class.java.name -> {
                FileUploadWorker(
                    context = appContext,
                    params = workerParameters,
                    remoteFileRepository = remoteFileRepository,
                    syncTaskRepository = syncTaskRepository
                )
            }
            FileDownloadWorker::class.java.name -> {
                FileDownloadWorker(
                    context = appContext,
                    params = workerParameters,
                    remoteFileRepository = remoteFileRepository,
                    localFileRepository = localFileRepository,
                    syncTaskRepository = syncTaskRepository
                )
            }
            CleanupCompletedTasksWorker::class.java.name -> {
                CleanupCompletedTasksWorker(
                    context = appContext,
                    params = workerParameters,
                    syncTaskRepository = syncTaskRepository
                )
            }
            else -> null // Return null to delegate to default factory
        }
    }
}
