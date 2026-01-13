package com.morshues.morshuesandroid.di.hilt

import com.morshues.morshuesandroid.BuildConfig
import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.api.AuthApiService
import com.morshues.morshuesandroid.data.network.DynamicUrlInterceptor
import com.morshues.morshuesandroid.data.network.TokenAuthenticator
import com.morshues.morshuesandroid.data.network.TokenInterceptor
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.settings.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ProtectedClient

/**
 * Hilt module for network-related components.
 * Provides OkHttpClients and Retrofit API services.
 */
@Module
@InstallIn(SingletonComponent::class)
object HiltNetworkModule {

    /**
     * OkHttpClient for non-authenticated endpoints (login, refresh).
     * Does NOT include TokenAuthenticator to avoid circular dependency.
     */
    @Provides
    @Singleton
    @AuthClient
    fun provideAuthOkHttpClient(
        settingsManager: SettingsManager
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(DynamicUrlInterceptor(settingsManager))
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }.build()
    }

    /**
     * OkHttpClient for protected endpoints with automatic token refresh.
     * Includes TokenAuthenticator which uses authRepository.
     */
    @Provides
    @Singleton
    @ProtectedClient
    fun provideProtectedOkHttpClient(
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
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }.build()
    }

    /**
     * Creates API service for authentication endpoints (login, refresh).
     * Uses a client WITHOUT TokenAuthenticator to avoid circular dependency.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(
        @AuthClient okHttpClient: OkHttpClient
    ): AuthApiService {
        return Retrofit.Builder()
            .baseUrl(SettingsManager.DEFAULT_SERVER_PATH)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    /**
     * Creates API service for protected endpoints (file operations, etc.).
     * Uses a client WITH TokenAuthenticator for automatic token refresh.
     */
    @Provides
    @Singleton
    fun provideApiService(
        @ProtectedClient okHttpClient: OkHttpClient
    ): ApiService {
        return Retrofit.Builder()
            .baseUrl(SettingsManager.DEFAULT_SERVER_PATH)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
