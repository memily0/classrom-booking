package com.classroombooking.backend.booking

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateBookingRequest(

    @field:NotNull(message = "Room id must not be null")
    val roomId: Long?,

    @field:NotNull(message = "Start time must not be null")
    val start: LocalDateTime?,

    @field:NotNull(message = "End time must not be null")
    val end: LocalDateTime?
)