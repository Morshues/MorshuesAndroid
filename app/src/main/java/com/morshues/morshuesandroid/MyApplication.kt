package com.morshues.morshuesandroid

import android.app.Application
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.video.VideoFrameDecoder
import com.morshues.morshuesandroid.data.worker.AppWorkerFactory
import com.morshues.morshuesandroid.di.AppModule

class MyApplication : Application(), SingletonImageLoader.Factory, Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
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
                    remoteFileRepository = AppModule.remoteFileRepository,
                    localFileRepository = AppModule.localFileRepository
                )
            )
            .build()
}