package com.morshues.morshuesandroid.di

import android.content.Context
import com.morshues.morshuesandroid.BuildConfig
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.network.DynamicUrlInterceptor
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.settings.SettingsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(DynamicUrlInterceptor(settingsManager))
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }.build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService)
    }
}