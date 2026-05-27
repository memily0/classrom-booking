package com.classroombooking.backend.room

import com.classroombooking.backend.common.BadRequestException
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
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        datetime: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        start: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        end: LocalDateTime?
    ): List<RoomAvailabilityResponse> {
        if (start != null && end != null) {
            return roomService.getRoomsAvailability(start, end)
        }

        if (datetime != null && start == null && end == null) {
            return roomService.getRoomsAvailability(datetime)
        }

        throw BadRequestException("Provide either datetime or both start and end")
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
