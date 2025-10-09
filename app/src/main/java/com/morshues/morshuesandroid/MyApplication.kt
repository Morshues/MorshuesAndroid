package com.morshues.morshuesandroid

import android.app.Application
import com.morshues.morshuesandroid.di.AppModule

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}