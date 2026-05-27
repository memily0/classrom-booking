package com.classroombooking.backend.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.classroombooking.backend.common.BadRequestException
import com.classroombooking.backend.user.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val currentUserService: CurrentUserService
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BadRequestException("Invalid email or password")

        val passwordMatches = passwordEncoder.matches(
            request.password,
            user.passwordHash
        )

        if (!passwordMatches) {
            throw BadRequestException("Invalid email or password")
        }

        return LoginResponse(
            accessToken = jwtService.generateToken(user),
            user = AuthUserResponse(
                id = user.id,
                email = user.email,
                name = user.name,
                surname = user.surname
            )
        )
    }

    fun getMe(authorizationHeader: String?): AuthUserResponse {
        val user = currentUserService.getCurrentUser(authorizationHeader)

        return AuthUserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            surname = user.surname
        )
    }
}