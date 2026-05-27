package com.classroombooking.backend.booking

import com.classroombooking.backend.room.RoomRepository
import com.classroombooking.backend.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalTime

import com.classroombooking.backend.common.BadRequestException
import com.classroombooking.backend.common.ConflictException
import com.classroombooking.backend.common.NotFoundException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.classroombooking.backend.common.ForbiddenException
import java.time.LocalDateTime
import com.classroombooking.backend.auth.CurrentUserService

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
) {

    fun createBooking(
        request: CreateBookingRequest,
        authorizationHeader: String?
    ): BookingResponse {
        validateTime(request)

        val roomId = request.roomId!!
        val start = request.start!!
        val end = request.end!!

        val room = roomRepository.findByIdAndIsActiveTrue(request.roomId)
            ?: throw NotFoundException("Room not found")

        val user = currentUserService.getCurrentUser(authorizationHeader)

        val conflicts = bookingRepository
            .findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = request.end,
                selectedStart = request.start
            )

        if (conflicts.isNotEmpty()) {
            throw ConflictException("Room is already booked for this time interval")
        }

        val booking = BookingEntity(
            room = room,
            user = user,
            startTime = request.start,
            endTime = request.end,
            status = BookingStatus.ACTIVE
        )

        val savedBooking = bookingRepository.save(booking)

        return BookingResponse(
            id = savedBooking.id,
            roomId = savedBooking.room.id,
            userId = savedBooking.user.id,
            start = savedBooking.startTime,
            end = savedBooking.endTime,
            status = savedBooking.status.name.lowercase()
        )
    }

    private fun validateTime(request: CreateBookingRequest) {
        val start = request.start ?: return
        val end = request.end ?: return

        if (!start.isBefore(end)) {
            throw BadRequestException("Start time must be before end time")
        }

        val workStart = LocalTime.of(8, 0)
        val workEnd = LocalTime.of(20, 0)

        val startTime = start.toLocalTime()
        val endTime = end.toLocalTime()

        if (startTime.isBefore(workStart) || endTime.isAfter(workEnd)) {
            throw BadRequestException("Booking must be inside working hours")
        }

        if (start.toLocalDate() != end.toLocalDate()) {
            throw BadRequestException("Booking must be within one day")
        }
    }

    fun cancelBooking(
        bookingId: Long,
        authorizationHeader: String?
    ): BookingResponse {

        val currentUser = currentUserService.getCurrentUser(authorizationHeader)

        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { NotFoundException("Booking not found") }

        if (booking.user.id != currentUser.id) {
            throw ForbiddenException("You can cancel only your own bookings")
        }

        if (booking.status == BookingStatus.CANCELLED) {
            throw ConflictException("Booking is already cancelled")
        }

        booking.status = BookingStatus.CANCELLED
        booking.updatedAt = LocalDateTime.now()

        val savedBooking = bookingRepository.save(booking)

        return BookingResponse(
            id = savedBooking.id,
            roomId = savedBooking.room.id,
            userId = savedBooking.user.id,
            start = savedBooking.startTime,
            end = savedBooking.endTime,
            status = savedBooking.status.name.lowercase()
        )
    }
}