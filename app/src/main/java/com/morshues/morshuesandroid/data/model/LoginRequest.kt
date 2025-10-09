package com.morshues.morshuesandroid.data.model

data class LoginRequest(
    var email: String,
    var password: String,
    var deviceId: String,
)