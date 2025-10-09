package com.morshues.morshuesandroid.data.repository

import com.morshues.morshuesandroid.data.api.ApiService
import com.morshues.morshuesandroid.data.model.LoginRequest
import com.morshues.morshuesandroid.data.model.LoginResponse
import com.morshues.morshuesandroid.data.model.RefreshTokenRequest
import com.morshues.morshuesandroid.data.model.RefreshTokenResponse

class AuthRepository(private val api: ApiService) {
    suspend fun login(email: String, password: String, deviceId: String): LoginResponse {
        return api.login(LoginRequest(email = email, password = password, deviceId = deviceId))
    }

    suspend fun refresh(refreshToken: String, deviceId: String): RefreshTokenResponse {
        return api.refresh(RefreshTokenRequest(refreshToken = refreshToken, deviceId = deviceId))
    }
}