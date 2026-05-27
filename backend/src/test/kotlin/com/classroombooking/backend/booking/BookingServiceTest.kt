package com.classroombooking.backend.booking

import com.classroombooking.backend.auth.CurrentUserService
import com.classroombooking.backend.common.BadRequestException
import com.classroombooking.backend.common.ConflictException
import com.classroombooking.backend.common.ForbiddenException
import com.classroombooking.backend.common.NotFoundException
import com.classroombooking.backend.room.RoomEntity
import com.classroombooking.backend.room.RoomRepository
import com.classroombooking.backend.user.UserEntity
import com.classroombooking.backend.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional

class BookingServiceTest {

    private val bookingRepository: BookingRepository = mock()
    private val roomRepository: RoomRepository = mock()
    private val userRepository: UserRepository = mock()
    private val currentUserService: CurrentUserService = mock()
    private val bookingService = BookingService(
        bookingRepository,
        roomRepository,
        userRepository,
        currentUserService
    )

    @Test
    fun `createBooking should create booking when room is available`() {
        val room = room()
        val user = user()
        val request = bookingRequest()
        whenever(roomRepository.findByIdAndIsActiveTrue(room.id)).thenReturn(room)
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(user)
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = request.end!!,
                selectedStart = request.start!!
            )
        ).thenReturn(emptyList())
        whenever(bookingRepository.save(any())).thenAnswer { invocation ->
            val booking = invocation.getArgument<BookingEntity>(0)
            BookingEntity(
                id = 10L,
                room = booking.room,
                user = booking.user,
                startTime = booking.startTime,
                endTime = booking.endTime,
                status = booking.status
            )
        }

        val response = bookingService.createBooking(request, "Bearer token")

        assertEquals(10L, response.id)
        assertEquals(room.id, response.roomId)
        assertEquals(user.id, response.userId)
        assertEquals(request.start, response.start)
        assertEquals(request.end, response.end)
        assertEquals("active", response.status)
    }

    @Test
    fun `createBooking should throw bad request when start is not before end`() {
        val start = LocalDateTime.of(2026, 5, 27, 10, 0)
        val request = bookingRequest(start = start, end = start)

        assertThrows(BadRequestException::class.java) {
            bookingService.createBooking(request, "Bearer token")
        }

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `createBooking should throw bad request when time is outside working hours`() {
        val request = bookingRequest(
            start = LocalDateTime.of(2026, 5, 27, 7, 30),
            end = LocalDateTime.of(2026, 5, 27, 8, 30)
        )

        assertThrows(BadRequestException::class.java) {
            bookingService.createBooking(request, "Bearer token")
        }

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `createBooking should throw not found when room is missing`() {
        val request = bookingRequest()
        whenever(roomRepository.findByIdAndIsActiveTrue(request.roomId!!)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            bookingService.createBooking(request, "Bearer token")
        }
    }

    @Test
    fun `createBooking should throw conflict when active booking overlaps`() {
        val room = room()
        val user = user()
        val request = bookingRequest()
        val conflictingBooking = booking(room = room, user = user)
        whenever(roomRepository.findByIdAndIsActiveTrue(room.id)).thenReturn(room)
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(user)
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = request.end!!,
                selectedStart = request.start!!
            )
        ).thenReturn(listOf(conflictingBooking))

        assertThrows(ConflictException::class.java) {
            bookingService.createBooking(request, "Bearer token")
        }

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `cancelBooking should cancel owner booking`() {
        val owner = user(id = 1L)
        val booking = booking(user = owner)
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(owner)
        whenever(bookingRepository.findById(booking.id)).thenReturn(Optional.of(booking))
        whenever(bookingRepository.save(booking)).thenReturn(booking)

        val response = bookingService.cancelBooking(booking.id, "Bearer token")

        assertEquals(booking.id, response.id)
        assertEquals(owner.id, response.userId)
        assertEquals("cancelled", response.status)
        assertEquals(BookingStatus.CANCELLED, booking.status)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `cancelBooking should throw forbidden when user cancels another user booking`() {
        val currentUser = user(id = 1L)
        val owner = user(id = 2L, email = "owner@test.com")
        val booking = booking(user = owner)
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(currentUser)
        whenever(bookingRepository.findById(booking.id)).thenReturn(Optional.of(booking))

        assertThrows(ForbiddenException::class.java) {
            bookingService.cancelBooking(booking.id, "Bearer token")
        }

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `cancelBooking should throw conflict when booking is already cancelled`() {
        val owner = user(id = 1L)
        val booking = booking(user = owner, status = BookingStatus.CANCELLED)
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(owner)
        whenever(bookingRepository.findById(booking.id)).thenReturn(Optional.of(booking))

        assertThrows(ConflictException::class.java) {
            bookingService.cancelBooking(booking.id, "Bearer token")
        }

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `cancelBooking should throw not found when booking is missing`() {
        whenever(currentUserService.getCurrentUser("Bearer token")).thenReturn(user())
        whenever(bookingRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            bookingService.cancelBooking(99L, "Bearer token")
        }
    }

    private fun bookingRequest(
        roomId: Long = 1L,
        start: LocalDateTime = LocalDateTime.of(2026, 5, 27, 10, 0),
        end: LocalDateTime = LocalDateTime.of(2026, 5, 27, 11, 0)
    ) = CreateBookingRequest(
        roomId = roomId,
        start = start,
        end = end
    )

    private fun booking(
        id: Long = 1L,
        room: RoomEntity = room(),
        user: UserEntity = user(),
        start: LocalDateTime = LocalDateTime.of(2026, 5, 27, 10, 0),
        end: LocalDateTime = LocalDateTime.of(2026, 5, 27, 11, 0),
        status: BookingStatus = BookingStatus.ACTIVE
    ) = BookingEntity(
        id = id,
        room = room,
        user = user,
        startTime = start,
        endTime = end,
        status = status
    )

    private fun room(
        id: Long = 1L,
        number: String = "101"
    ) = RoomEntity(
        id = id,
        number = number,
        name = "Room $number",
        capacity = 25,
        description = "Test room",
        isActive = true
    )

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
