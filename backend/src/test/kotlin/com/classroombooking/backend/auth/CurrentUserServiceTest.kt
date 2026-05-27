package com.classroombooking.backend.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.classroombooking.backend.common.NotFoundException
import com.classroombooking.backend.common.UnauthorizedException
import com.classroombooking.backend.user.UserEntity
import com.classroombooking.backend.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class CurrentUserServiceTest {

    private val jwtService: JwtService = mock()
    private val userRepository: UserRepository = mock()
    private val currentUserService = CurrentUserService(jwtService, userRepository)

    @Test
    fun `getCurrentUser should throw unauthorized when authorization header is missing`() {
        assertThrows(UnauthorizedException::class.java) {
            currentUserService.getCurrentUser(null)
        }
    }

    @Test
    fun `getCurrentUser should throw unauthorized when authorization header is not bearer`() {
        assertThrows(UnauthorizedException::class.java) {
            currentUserService.getCurrentUser("Basic token")
        }
    }

    @Test
    fun `getCurrentUser should return user when jwt is valid and user exists`() {
        val user = user()
        whenever(jwtService.extractUserId("valid-token")).thenReturn(user.id)
        whenever(userRepository.findById(user.id)).thenReturn(Optional.of(user))

        val result = currentUserService.getCurrentUser("Bearer valid-token")

        assertEquals(user, result)
    }

    @Test
    fun `getCurrentUser should throw unauthorized when jwt is invalid`() {
        whenever(jwtService.extractUserId("invalid-token"))
            .thenThrow(JWTVerificationException("Invalid token"))

        assertThrows(UnauthorizedException::class.java) {
            currentUserService.getCurrentUser("Bearer invalid-token")
        }
    }

    @Test
    fun `getCurrentUser should throw not found when user from token is missing`() {
        whenever(jwtService.extractUserId("valid-token")).thenReturn(99L)
        whenever(userRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            currentUserService.getCurrentUser("Bearer valid-token")
        }
    }

    private fun user(
        id: Long = 1L,
        email: String = "student@test.com"
    ) = UserEntity(
        id = id,
        email = email,
        passwordHash = "hash",
        name = "Test",
        surname = "Student"
    )
}
