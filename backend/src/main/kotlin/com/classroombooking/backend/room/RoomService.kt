package com.classroombooking.backend.room

import com.classroombooking.backend.booking.BookingRepository
import com.classroombooking.backend.booking.BookingStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDate
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

                if (currentBooking != null) {
                    RoomAvailabilityResponse(
                        id = room.id,
                        number = room.number,
                        isAvailable = false,
                        status = "busy",
                        availableUntil = null,
                        busyUntil = currentBooking.endTime
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
                        isAvailable = true,
                        status = "available",
                        availableUntil = nextBooking?.startTime,
                        busyUntil = null
                    )
                }
            }
            .sortedWith(
                compareByDescending<RoomAvailabilityResponse> { it.isAvailable }
                    .thenBy { it.number }
            )
    }

    fun getRoomSchedule(roomId: Long, date: java.time.LocalDate): RoomScheduleResponse {
        val room = roomRepository.findByIdAndIsActiveTrue(roomId)
            ?: throw NotFoundException("Room not found")

        val workStart = date.atTime(8, 0)
        val workEnd = date.atTime(20, 0)

        val bookings = bookingRepository
            .findAllByRoomIdAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
                roomId = roomId,
                status = BookingStatus.ACTIVE,
                dayStart = workStart,
                dayEnd = workEnd
            )

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
                description = room.description
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
}