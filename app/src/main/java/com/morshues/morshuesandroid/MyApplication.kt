package com.morshues.morshuesandroid

import android.app.Application
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.video.VideoFrameDecoder
import com.morshues.morshuesandroid.data.sync.PeriodicSyncScheduler
import com.morshues.morshuesandroid.data.worker.AppWorkerFactory
import com.morshues.morshuesandroid.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            AppModule.syncTaskRepository.resetActiveTasks()
        }

        PeriodicSyncScheduler.init(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                AppWorkerFactory(
                    syncingFolderRepository = AppModule.syncingFolderRepository,
                    remoteFileRepository = AppModule.remoteFileRepository,
                    localFileRepository = AppModule.localFileRepository,
                    syncTaskRepository = AppModule.syncTaskRepository,
                    syncFolderUseCase = AppModule.syncFolderUseCase,
                    // Use provider function to break circular dependency
                    syncTaskEnqueuerProvider = { AppModule.syncTaskEnqueuer },
                )
            )
            .build()
}