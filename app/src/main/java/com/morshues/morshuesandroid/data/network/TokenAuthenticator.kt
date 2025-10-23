package com.morshues.morshuesandroid.data.network

import com.morshues.morshuesandroid.data.SessionStore
import com.morshues.morshuesandroid.data.repository.AuthRepository
import com.morshues.morshuesandroid.utils.JwtUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val originalAuthHeader = response.request.header("Authorization")
        if (originalAuthHeader == null) {
            return null
        }

        // Use synchronized block to prevent multiple concurrent token refreshes
        synchronized(this) {
            return runBlocking {
                try {
                    val currentToken = sessionStore.accessToken.first()
                    val requestToken = originalAuthHeader.removePrefix("Bearer ")

                    // Check if token was already refreshed by another concurrent request
                    if (currentToken != null && currentToken != requestToken) {
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                    }

                    val refreshToken = sessionStore.refreshToken.first()
                        ?: return@runBlocking null
                    val deviceId = sessionStore.getOrCreateDeviceId()

                    val newTokens = authRepository.refresh(refreshToken, deviceId)
                    val expiresAt = JwtUtils.getExpirationTime(newTokens.accessToken)
                    sessionStore.saveTokens(newTokens.accessToken, newTokens.refreshToken, expiresAt)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()

                } catch (e: Exception) {
                    e.printStackTrace()
                    sessionStore.clear()
                    null
                }
            }
        }
    }
}
