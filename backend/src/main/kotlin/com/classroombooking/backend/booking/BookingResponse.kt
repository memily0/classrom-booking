package com.classroombooking.backend.booking

import java.time.LocalDateTime

data class BookingResponse(
    val id: Long,
    val roomId: Long,
    val userId: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val status: String
)