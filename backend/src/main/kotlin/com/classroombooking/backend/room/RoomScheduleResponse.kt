package com.classroombooking.backend.room

import java.time.LocalDate
import java.time.LocalDateTime

data class RoomScheduleResponse(
    val room: RoomDetailsResponse,
    val date: LocalDate,
    val workingHours: WorkingHoursResponse,
    val bookings: List<BookingIntervalResponse>,
    val availableSlots: List<TimeSlotResponse>
)

data class RoomDetailsResponse(
    val id: Long,
    val number: String,
    val name: String?,
    val capacity: Int?,
    val description: String?
)

data class WorkingHoursResponse(
    val start: String,
    val end: String
)

data class BookingIntervalResponse(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val status: String
)

data class TimeSlotResponse(
    val start: LocalDateTime,
    val end: LocalDateTime
)