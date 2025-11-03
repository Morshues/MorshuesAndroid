package com.morshues.morshuesandroid.data.model

data class LoginRequest(
    var email: String,
    var password: String,
    var deviceId: String,
)

data class LoginResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
    var user: UserDto,
)

data class RefreshTokenRequest(
    var refreshToken: String,
    var deviceId: String,
)

data class RefreshTokenResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
)