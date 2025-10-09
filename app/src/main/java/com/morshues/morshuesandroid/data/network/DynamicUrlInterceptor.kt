package com.morshues.morshuesandroid.data.network

import com.morshues.morshuesandroid.settings.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class DynamicUrlInterceptor(private val settingsManager: SettingsManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // runBlocking is used here because Interceptors are synchronous.
        val currentBaseUrl = runBlocking { settingsManager.getServerPath().first() }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(currentBaseUrl.toHttpUrl().scheme)
            .host(currentBaseUrl.toHttpUrl().host)
            .port(currentBaseUrl.toHttpUrl().port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}