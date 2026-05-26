package com.classroombooking.backend.auth

data class AuthUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val surname: String
)