package com.morshues.morshuesandroid.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.morshues.morshuesandroid.data.repository.LocalFileRepository
import com.morshues.morshuesandroid.data.repository.RemoteFileRepository

/**
 * Custom WorkerFactory that provides proper dependency injection for Workers.
 * This replaces the direct AppModule access in Worker classes.
 */
class AppWorkerFactory(
    private val remoteFileRepository: RemoteFileRepository,
    private val localFileRepository: LocalFileRepository,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            FileUploadWorker::class.java.name -> {
                FileUploadWorker(
                    context = appContext,
                    params = workerParameters,
                    remoteFileRepository = remoteFileRepository
                )
            }
            FileDownloadWorker::class.java.name -> {
                FileDownloadWorker(
                    context = appContext,
                    params = workerParameters,
                    remoteFileRepository = remoteFileRepository,
                    localFileRepository = localFileRepository
                )
            }
            else -> null // Return null to delegate to default factory
        }
    }
}
