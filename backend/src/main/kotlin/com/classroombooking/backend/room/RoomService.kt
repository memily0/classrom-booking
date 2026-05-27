package com.classroombooking.backend.room

import com.classroombooking.backend.booking.BookingRepository
import com.classroombooking.backend.booking.BookingStatus
import com.classroombooking.backend.common.BadRequestException
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.classroombooking.backend.common.NotFoundException

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val bookingRepository: BookingRepository
) {

    fun getActiveRooms(): List<RoomResponse> {
        return roomRepository.findAllByIsActiveTrue()
            .map { room ->
                RoomResponse(
                    id = room.id,
                    number = room.number,
                    name = room.name,
                    capacity = room.capacity,
                    description = room.description,
                    buildingName = room.buildingName,
                    floor = room.floor,
                    isActive = room.isActive
                )
            }
    }

    fun getRoomsAvailability(selectedDateTime: LocalDateTime): List<RoomAvailabilityResponse> {
        return roomRepository.findAllByIsActiveTrue()
            .map { room ->
                val currentBookings = bookingRepository
                    .findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId = room.id,
                        status = BookingStatus.ACTIVE,
                        selectedEnd = selectedDateTime.plusNanos(1),
                        selectedStart = selectedDateTime
                    )

                val currentBooking = currentBookings.firstOrNull()
                val schedule = buildSchedule(room.id, selectedDateTime.toLocalDate())

                if (currentBooking != null) {
                    RoomAvailabilityResponse(
                        id = room.id,
                        number = room.number,
                        buildingName = room.buildingName,
                        floor = room.floor,
                        isAvailable = false,
                        status = "busy",
                        availableUntil = null,
                        busyUntil = currentBooking.endTime,
                        schedule = schedule
                    )
                } else {
                    val nextBooking = bookingRepository
                        .findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                            roomId = room.id,
                            status = BookingStatus.ACTIVE,
                            selectedDateTime = selectedDateTime
                        )

                    RoomAvailabilityResponse(
                        id = room.id,
                        number = room.number,
                        buildingName = room.buildingName,
                        floor = room.floor,
                        isAvailable = true,
                        status = "available",
                        availableUntil = nextBooking?.startTime,
                        busyUntil = null,
                        schedule = schedule
                    )
                }
            }
            .sortedWith(
                compareByDescending<RoomAvailabilityResponse> { it.isAvailable }
                    .thenBy { it.number }
            )
    }

    fun getRoomsAvailability(start: LocalDateTime, end: LocalDateTime): List<RoomAvailabilityResponse> {
        if (!start.isBefore(end)) {
            throw BadRequestException("Start time must be before end time")
        }

        return roomRepository.findAllByIsActiveTrue()
            .map { room ->
                val conflicts = bookingRepository
                    .findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId = room.id,
                        status = BookingStatus.ACTIVE,
                        selectedEnd = end,
                        selectedStart = start
                    )
                val schedule = buildSchedule(room.id, start.toLocalDate())

                if (conflicts.isNotEmpty()) {
                    RoomAvailabilityResponse(
                        id = room.id,
                        number = room.number,
                        buildingName = room.buildingName,
                        floor = room.floor,
                        isAvailable = false,
                        status = "busy",
                        availableUntil = null,
                        busyUntil = conflicts.maxOf { it.endTime },
                        schedule = schedule
                    )
                } else {
                    val nextBooking = bookingRepository
                        .findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                            roomId = room.id,
                            status = BookingStatus.ACTIVE,
                            selectedDateTime = start
                        )

                    RoomAvailabilityResponse(
                        id = room.id,
                        number = room.number,
                        buildingName = room.buildingName,
                        floor = room.floor,
                        isAvailable = true,
                        status = "available",
                        availableUntil = nextBooking?.startTime,
                        busyUntil = null,
                        schedule = schedule
                    )
                }
            }
            .sortedWith(
                compareByDescending<RoomAvailabilityResponse> { it.isAvailable }
                    .thenBy { it.number }
            )
    }

    fun getRoomSchedule(roomId: Long, date: LocalDate): RoomScheduleResponse {
        val room = roomRepository.findByIdAndIsActiveTrue(roomId)
            ?: throw NotFoundException("Room not found")

        val workStart = workStart(date)
        val workEnd = workEnd(date)
        val bookings = getActiveBookingsForRoomDate(roomId, date)

        val availableSlots = mutableListOf<TimeSlotResponse>()
        var currentStart = workStart

        for (booking in bookings) {
            if (currentStart < booking.startTime) {
                availableSlots.add(
                    TimeSlotResponse(
                        start = currentStart,
                        end = booking.startTime
                    )
                )
            }

            if (booking.endTime > currentStart) {
                currentStart = booking.endTime
            }
        }

        if (currentStart < workEnd) {
            availableSlots.add(
                TimeSlotResponse(
                    start = currentStart,
                    end = workEnd
                )
            )
        }

        return RoomScheduleResponse(
            room = RoomDetailsResponse(
                id = room.id,
                number = room.number,
                name = room.name,
                capacity = room.capacity,
                description = room.description,
                buildingName = room.buildingName,
                floor = room.floor
            ),
            date = date,
            workingHours = WorkingHoursResponse(
                start = "08:00",
                end = "20:00"
            ),
            bookings = bookings.map { booking ->
                BookingIntervalResponse(
                    id = booking.id,
                    start = booking.startTime,
                    end = booking.endTime,
                    status = booking.status.name.lowercase()
                )
            },
            availableSlots = availableSlots
        )
    }

    private fun buildSchedule(roomId: Long, date: LocalDate): Map<String, String> {
        val workStart = workStart(date)
        val workEnd = workEnd(date)
        val bookings = getActiveBookingsForRoomDate(roomId, date)
        val schedule = linkedMapOf<String, String>()
        var currentStart = workStart

        for (booking in bookings) {
            val bookingStart = if (booking.startTime.isBefore(workStart)) workStart else booking.startTime
            val bookingEnd = if (booking.endTime.isAfter(workEnd)) workEnd else booking.endTime

            if (!bookingEnd.isAfter(workStart) || !bookingStart.isBefore(workEnd)) {
                continue
            }

            if (currentStart.isBefore(bookingStart)) {
                schedule[formatSlot(currentStart, bookingStart)] = "available"
            }

            val busyStart = if (currentStart.isAfter(bookingStart)) currentStart else bookingStart
            if (busyStart.isBefore(bookingEnd)) {
                schedule[formatSlot(busyStart, bookingEnd)] = "busy"
            }

            if (bookingEnd.isAfter(currentStart)) {
                currentStart = bookingEnd
            }
        }

        if (currentStart.isBefore(workEnd)) {
            schedule[formatSlot(currentStart, workEnd)] = "available"
        }

        return schedule
    }

    private fun getActiveBookingsForRoomDate(
        roomId: Long,
        date: LocalDate
    ) = bookingRepository
        .findAllByRoomIdAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
            roomId = roomId,
            status = BookingStatus.ACTIVE,
            dayStart = workStart(date),
            dayEnd = workEnd(date)
        )

    private fun workStart(date: LocalDate): LocalDateTime {
        return date.atTime(8, 0)
    }

    private fun workEnd(date: LocalDate): LocalDateTime {
        return date.atTime(20, 0)
    }

    private fun formatSlot(start: LocalDateTime, end: LocalDateTime): String {
        return "${start.format(timeFormatter)}-${end.format(timeFormatter)}"
    }

    companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
