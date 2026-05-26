package com.classroombooking.backend.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): LoginResponse {
        return authService.login(request)
    }

    @GetMapping("/me")
    fun getMe(
        @RequestHeader("Authorization", required = false) authorizationHeader: String?
    ): AuthUserResponse {
        return authService.getMe(authorizationHeader)
    }
}