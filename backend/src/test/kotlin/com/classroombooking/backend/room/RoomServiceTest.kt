package com.classroombooking.backend.room

import com.classroombooking.backend.booking.BookingEntity
import com.classroombooking.backend.booking.BookingRepository
import com.classroombooking.backend.booking.BookingStatus
import com.classroombooking.backend.common.NotFoundException
import com.classroombooking.backend.user.UserEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class RoomServiceTest {

    private val roomRepository: RoomRepository = mock()
    private val bookingRepository: BookingRepository = mock()
    private val roomService = RoomService(roomRepository, bookingRepository)

    @Test
    fun `getActiveRooms should return active rooms from repository`() {
        val rooms = listOf(
            room(id = 1L, number = "101"),
            room(id = 2L, number = "204")
        )
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(rooms)

        val result = roomService.getActiveRooms()

        assertEquals(2, result.size)
        assertEquals(listOf("101", "204"), result.map { it.number })
        assertEquals(listOf("Main Building", "Main Building"), result.map { it.buildingName })
        assertEquals(listOf(1, 1), result.map { it.floor })
        assertTrue(result.all { it.isActive })
    }

    @Test
    fun `getRoomsAvailability should return room as available when there is no active booking at selected time`() {
        val selectedDateTime = LocalDateTime.of(2026, 5, 27, 10, 0)
        val room = room(id = 1L, number = "101")
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(listOf(room))
        wheneverSchedule(room.id, selectedDateTime.toLocalDate())
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = selectedDateTime.plusNanos(1),
                selectedStart = selectedDateTime
            )
        ).thenReturn(emptyList())
        whenever(
            bookingRepository.findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedDateTime = selectedDateTime
            )
        ).thenReturn(null)

        val result = roomService.getRoomsAvailability(selectedDateTime)

        assertEquals(1, result.size)
        assertTrue(result.single().isAvailable)
        assertEquals("available", result.single().status)
        assertNull(result.single().availableUntil)
        assertNull(result.single().busyUntil)
        assertEquals(mapOf("08:00-20:00" to "available"), result.single().schedule)
    }

    @Test
    fun `getRoomsAvailability should return room as busy when there is active booking at selected time`() {
        val selectedDateTime = LocalDateTime.of(2026, 5, 27, 10, 30)
        val room = room(id = 1L, number = "101")
        val booking = booking(
            id = 1L,
            room = room,
            start = LocalDateTime.of(2026, 5, 27, 10, 0),
            end = LocalDateTime.of(2026, 5, 27, 11, 0)
        )
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(listOf(room))
        wheneverSchedule(room.id, selectedDateTime.toLocalDate(), listOf(booking))
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = selectedDateTime.plusNanos(1),
                selectedStart = selectedDateTime
            )
        ).thenReturn(listOf(booking))

        val result = roomService.getRoomsAvailability(selectedDateTime)

        assertEquals(1, result.size)
        assertFalse(result.single().isAvailable)
        assertEquals("busy", result.single().status)
        assertNull(result.single().availableUntil)
        assertEquals(booking.endTime, result.single().busyUntil)
        assertEquals("busy", result.single().schedule["10:00-11:00"])
    }

    @Test
    fun `getRoomsAvailability should calculate available until for available room`() {
        val selectedDateTime = LocalDateTime.of(2026, 5, 27, 9, 0)
        val room = room(id = 1L, number = "101")
        val nextBooking = booking(
            id = 1L,
            room = room,
            start = LocalDateTime.of(2026, 5, 27, 11, 0),
            end = LocalDateTime.of(2026, 5, 27, 12, 0)
        )
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(listOf(room))
        wheneverSchedule(room.id, selectedDateTime.toLocalDate(), listOf(nextBooking))
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = selectedDateTime.plusNanos(1),
                selectedStart = selectedDateTime
            )
        ).thenReturn(emptyList())
        whenever(
            bookingRepository.findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedDateTime = selectedDateTime
            )
        ).thenReturn(nextBooking)

        val result = roomService.getRoomsAvailability(selectedDateTime)

        assertTrue(result.single().isAvailable)
        assertEquals(nextBooking.startTime, result.single().availableUntil)
        assertNull(result.single().busyUntil)
    }

    @Test
    fun `getRoomsAvailability should return room as busy when booking overlaps selected interval`() {
        val start = LocalDateTime.of(2026, 5, 27, 10, 30)
        val end = LocalDateTime.of(2026, 5, 27, 11, 30)
        val room = room(id = 1L, number = "101")
        val booking = booking(
            id = 1L,
            room = room,
            start = LocalDateTime.of(2026, 5, 27, 10, 0),
            end = LocalDateTime.of(2026, 5, 27, 11, 0)
        )
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(listOf(room))
        wheneverSchedule(room.id, start.toLocalDate(), listOf(booking))
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = end,
                selectedStart = start
            )
        ).thenReturn(listOf(booking))

        val result = roomService.getRoomsAvailability(start, end)

        assertFalse(result.single().isAvailable)
        assertEquals("busy", result.single().status)
        assertEquals(booking.endTime, result.single().busyUntil)
        assertEquals("busy", result.single().schedule["10:00-11:00"])
    }

    @Test
    fun `getRoomsAvailability should return room as available when booking does not overlap selected interval`() {
        val start = LocalDateTime.of(2026, 5, 27, 11, 0)
        val end = LocalDateTime.of(2026, 5, 27, 12, 0)
        val room = room(id = 1L, number = "101")
        val earlierBooking = booking(
            id = 1L,
            room = room,
            start = LocalDateTime.of(2026, 5, 27, 9, 0),
            end = LocalDateTime.of(2026, 5, 27, 10, 0)
        )
        whenever(roomRepository.findAllByIsActiveTrue()).thenReturn(listOf(room))
        wheneverSchedule(room.id, start.toLocalDate(), listOf(earlierBooking))
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedEnd = end,
                selectedStart = start
            )
        ).thenReturn(emptyList())
        whenever(
            bookingRepository.findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                selectedDateTime = start
            )
        ).thenReturn(null)

        val result = roomService.getRoomsAvailability(start, end)

        assertTrue(result.single().isAvailable)
        assertEquals("available", result.single().status)
        assertNull(result.single().busyUntil)
        assertTrue(result.single().schedule.isNotEmpty())
    }

    @Test
    fun `getRoomSchedule should return schedule and calculate available slots`() {
        val date = LocalDate.of(2026, 5, 27)
        val room = room(id = 1L, number = "101")
        val firstBooking = booking(
            id = 1L,
            room = room,
            start = date.atTime(10, 0),
            end = date.atTime(11, 0)
        )
        val secondBooking = booking(
            id = 2L,
            room = room,
            start = date.atTime(13, 0),
            end = date.atTime(14, 30)
        )
        whenever(roomRepository.findByIdAndIsActiveTrue(room.id)).thenReturn(room)
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
                roomId = room.id,
                status = BookingStatus.ACTIVE,
                dayStart = date.atTime(8, 0),
                dayEnd = date.atTime(20, 0)
            )
        ).thenReturn(listOf(firstBooking, secondBooking))

        val result = roomService.getRoomSchedule(room.id, date)

        assertEquals(room.id, result.room.id)
        assertEquals(room.buildingName, result.room.buildingName)
        assertEquals(room.floor, result.room.floor)
        assertEquals(date, result.date)
        assertEquals("08:00", result.workingHours.start)
        assertEquals("20:00", result.workingHours.end)
        assertEquals(listOf(1L, 2L), result.bookings.map { it.id })
        assertEquals(
            listOf(
                TimeSlotResponse(date.atTime(8, 0), date.atTime(10, 0)),
                TimeSlotResponse(date.atTime(11, 0), date.atTime(13, 0)),
                TimeSlotResponse(date.atTime(14, 30), date.atTime(20, 0))
            ),
            result.availableSlots
        )
    }

    @Test
    fun `getRoomSchedule should throw not found when room is missing`() {
        val date = LocalDate.of(2026, 5, 27)
        whenever(roomRepository.findByIdAndIsActiveTrue(99L)).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            roomService.getRoomSchedule(99L, date)
        }
    }

    private fun room(
        id: Long,
        number: String,
        name: String = "Room $number",
        capacity: Int = 25,
        buildingName: String = "Main Building",
        floor: Int = 1
    ) = RoomEntity(
        id = id,
        number = number,
        name = name,
        capacity = capacity,
        description = "Test room",
        buildingName = buildingName,
        floor = floor,
        isActive = true
    )

    private fun wheneverSchedule(
        roomId: Long,
        date: LocalDate,
        bookings: List<BookingEntity> = emptyList()
    ) {
        whenever(
            bookingRepository.findAllByRoomIdAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
                roomId = roomId,
                status = BookingStatus.ACTIVE,
                dayStart = date.atTime(8, 0),
                dayEnd = date.atTime(20, 0)
            )
        ).thenReturn(bookings)
    }

    private fun booking(
        id: Long,
        room: RoomEntity,
        start: LocalDateTime,
        end: LocalDateTime,
        status: BookingStatus = BookingStatus.ACTIVE
    ) = BookingEntity(
        id = id,
        room = room,
        user = user(),
        startTime = start,
        endTime = end,
        status = status
    )

    private fun user() = UserEntity(
        id = 1L,
        email = "student@test.com",
        passwordHash = "hash",
        name = "Test",
        surname = "Student"
    )
}
