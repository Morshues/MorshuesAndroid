package com.morshues.morshuesandroid.data.model

data class LoginResponse(
    var ok: Boolean,
    var accessToken: String,
    var refreshToken: String,
    var user: UserDto,
)