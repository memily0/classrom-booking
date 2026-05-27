package com.classroombooking.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.classroombooking.backend.user.UserEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration-ms}")
    private val expirationMs: Long
) {

    private fun algorithm(): Algorithm {
        return Algorithm.HMAC256(secret)
    }

    fun generateToken(user: UserEntity): String {
        val now = Date()
        val expiresAt = Date(now.time + expirationMs)

        return JWT.create()
            .withSubject(user.email)
            .withClaim("userId", user.id)
            .withClaim("email", user.email)
            .withClaim("name", user.name)
            .withClaim("surname", user.surname)
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm())
    }

    fun extractUserId(token: String): Long {
        val verifier = JWT.require(algorithm()).build()

        val decodedJwt = verifier.verify(token)

        return decodedJwt.getClaim("userId").asLong()
    }
}