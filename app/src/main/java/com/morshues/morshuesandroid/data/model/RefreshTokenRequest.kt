package com.morshues.morshuesandroid.data.model

data class RefreshTokenRequest(
    var refreshToken: String,
    var deviceId: String,
)