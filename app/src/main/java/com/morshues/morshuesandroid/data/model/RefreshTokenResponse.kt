package com.morshues.morshuesandroid.data.model

data class RefreshTokenResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
)