package com.morshues.morshuesandroid.di

import android.content.Context
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.settings.SettingsManager

/**
 * A simple manual dependency injector to provide singleton instances of our services.
 * In a larger app, this would be replaced by Hilt.
 */
object AppModule {
    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    val settingsManager: SettingsManager by lazy {
        SettingsManager(applicationContext)
    }

    val sessionStore: SessionStore by lazy {
        SessionStore(applicationContext)
    }

    private val authApiService by lazy {
        NetworkModule.createAuthApiService(settingsManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApiService)
    }

    private val apiService by lazy {
        NetworkModule.createApiService(settingsManager, sessionStore, authRepository)
    }
}