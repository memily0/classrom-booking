package com.classroombooking.backend.room

import java.time.LocalDateTime

data class RoomAvailabilityResponse(
    val id: Long,
    val number: String,
    val isAvailable: Boolean,
    val status: String,
    val availableUntil: LocalDateTime?,
    val busyUntil: LocalDateTime?
)