package com.classroombooking.backend.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.classroombooking.backend.common.NotFoundException
import com.classroombooking.backend.common.UnauthorizedException
import com.classroombooking.backend.user.UserEntity
import com.classroombooking.backend.user.UserRepository
import org.springframework.stereotype.Service

@Service
class CurrentUserService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) {

    fun getCurrentUser(authorizationHeader: String?): UserEntity {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw UnauthorizedException("Missing or invalid Authorization header")
        }

        val token = authorizationHeader.removePrefix("Bearer ").trim()

        val userId = try {
            jwtService.extractUserId(token)
        } catch (ex: JWTVerificationException) {
            throw UnauthorizedException("Invalid or expired token")
        }

        return userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }
    }
}