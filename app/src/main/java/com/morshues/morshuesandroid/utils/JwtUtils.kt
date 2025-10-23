package com.morshues.morshuesandroid.utils

import com.auth0.android.jwt.JWT

object JwtUtils {
    /**
     * Extracts the expiration timestamp (in milliseconds) from a JWT token.
     *
     * @param token The JWT token string
     * @return The expiration timestamp in milliseconds, or null if unable to decode
     */
    fun getExpirationTime(token: String): Long? {
        return try {
            val jwt = JWT(token)
            jwt.expiresAt?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a token is expiring soon (within the specified threshold).
     *
     * @param token The JWT token string
     * @param thresholdMillis Time before expiration to consider "expiring soon" (default: 2 minutes)
     * @return true if the token will expire within the threshold, false otherwise
     */
    fun isTokenExpiringSoon(token: String, thresholdMillis: Long = 2 * 60 * 1000): Boolean {
        val expiresAt = getExpirationTime(token) ?: return true
        val now = System.currentTimeMillis()
        return expiresAt - now < thresholdMillis
    }
}
