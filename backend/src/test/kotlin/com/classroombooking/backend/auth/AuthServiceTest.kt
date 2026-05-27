package com.classroombooking.backend.auth

import com.classroombooking.backend.common.BadRequestException
import com.classroombooking.backend.user.UserEntity
import com.classroombooking.backend.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthServiceTest {

    private val userRepository: UserRepository = mock()
    private val jwtService: JwtService = mock()
    private val currentUserService: CurrentUserService = mock()
    private val authService = AuthService(userRepository, jwtService, currentUserService)
    private val passwordEncoder = BCryptPasswordEncoder()

    @Test
    fun `login should return login response when email and password are correct`() {
        val user = user(passwordHash = encodedPassword("password123"))
        whenever(userRepository.findByEmail("student@test.com")).thenReturn(user)
        whenever(jwtService.generateToken(user)).thenReturn("jwt-token")

        val response = authService.login(
            LoginRequest(
                email = "student@test.com",
                password = "password123"
            )
        )

        assertEquals("jwt-token", response.accessToken)
        assertEquals(user.id, response.user.id)
        assertEquals(user.email, response.user.email)
        assertEquals(user.name, response.user.name)
        assertEquals(user.surname, response.user.surname)
    }

    @Test
    fun `login should throw bad request when user is not found`() {
        whenever(userRepository.findByEmail("missing@test.com")).thenReturn(null)

        assertThrows(BadRequestException::class.java) {
            authService.login(
                LoginRequest(
                    email = "missing@test.com",
                    password = "password123"
                )
            )
        }
    }

    @Test
    fun `login should throw bad request when password is invalid`() {
        val user = user(passwordHash = encodedPassword("password123"))
        whenever(userRepository.findByEmail("student@test.com")).thenReturn(user)

        assertThrows(BadRequestException::class.java) {
            authService.login(
                LoginRequest(
                    email = "student@test.com",
                    password = "wrong-password"
                )
            )
        }
    }

    @Test
    fun `getMe should return current user response`() {
        val user = user()
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(user)

        val response = authService.getMe("Bearer token")

        assertEquals(user.id, response.id)
        assertEquals(user.email, response.email)
        assertEquals(user.name, response.name)
        assertEquals(user.surname, response.surname)
        verify(currentUserService).getCurrentUser("Bearer token")
    }

    private fun user(
        id: Long = 1L,
        email: String = "student@test.com",
        passwordHash: String = encodedPassword("password123"),
        name: String = "Test",
        surname: String = "Student"
    ) = UserEntity(
        id = id,
        email = email,
        passwordHash = passwordHash,
        name = name,
        surname = surname
    )

    private fun encodedPassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)!!
    }
}
