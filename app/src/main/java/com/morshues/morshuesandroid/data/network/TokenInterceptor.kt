package com.morshues.morshuesandroid.data.network

import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.utils.JwtUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TokenInterceptor(
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository
) : Interceptor {
    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking {
            getValidAccessToken()
        }

        // If no token available, return 401 response without making the request
        if (token == null) {
            return Response.Builder()
                .request(originalRequest)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized - No access token available")
                .body("".toResponseBody())
                .build()
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }

    private suspend fun getValidAccessToken(): String? {
        val currentToken = sessionStore.accessToken.first()
            ?: return null

        val expiresAt = sessionStore.tokenExpiresAt.first()
        if (expiresAt != null) {
            val now = System.currentTimeMillis()

            if (expiresAt - now < REFRESH_THRESHOLD) {
                refreshMutex.withLock {
                    // Double-check after acquiring lock (another coroutine might have refreshed)
                    val latestToken = sessionStore.accessToken.first()
                    val latestExpiry = sessionStore.tokenExpiresAt.first()

                    if (latestExpiry != null && latestExpiry - System.currentTimeMillis() < REFRESH_THRESHOLD) {
                        val refreshToken = sessionStore.refreshToken.first()
                            ?: throw IllegalStateException("No refresh token available")
                        val deviceId = sessionStore.getOrCreateDeviceId()

                        val newTokens = authRepository.refresh(refreshToken, deviceId)
                        val newExpiresAt = JwtUtils.getExpirationTime(newTokens.accessToken)
                        sessionStore.saveTokens(
                            newTokens.accessToken,
                            newTokens.refreshToken,
                            newExpiresAt,
                        )

                        return newTokens.accessToken
                    }

                    return latestToken ?: throw IllegalStateException("No access token available")
                }
            }
        }

        return currentToken
    }

    companion object {
        private const val REFRESH_THRESHOLD = 2 * 60 * 1000
    }
}
