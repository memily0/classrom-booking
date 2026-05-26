package com.classroombooking.backend.room

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService
) {

    @GetMapping
    fun getRooms(): List<RoomResponse> {
        return roomService.getActiveRooms()
    }

    @GetMapping("/availability")
    fun getRoomsAvailability(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        datetime: LocalDateTime
    ): List<RoomAvailabilityResponse> {
        return roomService.getRoomsAvailability(datetime)
    }

    @GetMapping("/{roomId}/schedule")
    fun getRoomSchedule(
        @PathVariable roomId: Long,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate
    ): RoomScheduleResponse {
        return roomService.getRoomSchedule(roomId, date)
    }
}