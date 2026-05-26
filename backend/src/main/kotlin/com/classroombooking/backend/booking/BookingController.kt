package com.classroombooking.backend.booking

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingService: BookingService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBooking(
        @Valid @RequestBody request: CreateBookingRequest,
        @RequestHeader("Authorization", required = false) authorizationHeader: String?
    ): BookingResponse {
        return bookingService.createBooking(request, authorizationHeader)
    }

    @PatchMapping("/{bookingId}/cancel")
    fun cancelBooking(
        @PathVariable bookingId: Long,
        @RequestHeader("Authorization", required = false) authorizationHeader: String?
    ): BookingResponse {
        return bookingService.cancelBooking(bookingId, authorizationHeader)
    }
}