package com.morshues.morshuesandroid.di

import com.morshues.morshuesandroid.BuildConfig
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.api.AuthApiService
import com.morshues.morshuesandroid.data.network.DynamicUrlInterceptor
import com.morshues.morshuesandroid.data.network.TokenAuthenticator
import com.morshues.morshuesandroid.data.network.TokenInterceptor
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.settings.SettingsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dependency injection module for network-related components.
 * Manages OkHttpClients and Retrofit API services.
 */
object NetworkModule {

    /**
     * OkHttpClient for non-authenticated endpoints (login, refresh).
     * Does NOT include TokenAuthenticator to avoid circular dependency.
     */
    private fun createAuthOkHttpClient(settingsManager: SettingsManager): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(DynamicUrlInterceptor(settingsManager))
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }.build()
    }

    /**
     * OkHttpClient for protected endpoints with automatic token refresh.
     * Includes TokenAuthenticator which uses authRepository.
     */
    private fun createProtectedOkHttpClient(
        settingsManager: SettingsManager,
        sessionStore: SessionStore,
        authRepository: AuthRepository
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(DynamicUrlInterceptor(settingsManager))
            addInterceptor(TokenInterceptor(sessionStore, authRepository))
            authenticator(TokenAuthenticator(sessionStore, authRepository))
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }.build()
    }

    /**
     * Creates API service for authentication endpoints (login, refresh).
     * Uses a client WITHOUT TokenAuthenticator to avoid circular dependency.
     */
    fun createAuthApiService(settingsManager: SettingsManager): AuthApiService {
        val client = createAuthOkHttpClient(settingsManager)
        return Retrofit.Builder()
            .baseUrl(SettingsManager.DEFAULT_SERVER_PATH)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    /**
     * Creates API service for protected endpoints (file operations, etc.).
     * Uses a client WITH TokenAuthenticator for automatic token refresh.
     */
    fun createApiService(
        settingsManager: SettingsManager,
        sessionStore: SessionStore,
        authRepository: AuthRepository
    ): ApiService {
        val client = createProtectedOkHttpClient(settingsManager, sessionStore, authRepository)
        return Retrofit.Builder()
            .baseUrl(SettingsManager.DEFAULT_SERVER_PATH)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
