package com.classroombooking.backend.room

import java.time.LocalDateTime

data class RoomAvailabilityResponse(
    val id: Long,
    val number: String,
    val buildingName: String?,
    val floor: Int?,
    val isAvailable: Boolean,
    val status: String,
    val availableUntil: LocalDateTime?,
    val busyUntil: LocalDateTime?,
    val schedule: Map<String, String>
)
