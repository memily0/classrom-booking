package com.classroombooking.backend.auth

data class LoginResponse(
    val accessToken: String,
    val user: AuthUserResponse
)
