package com.classroombooking.backend.booking

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface BookingRepository : JpaRepository<BookingEntity, Long> {

    // будет искать пересекающиеся брони
    fun findAllByRoomIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
        roomId: Long,
        status: BookingStatus,
        selectedEnd: LocalDateTime,
        selectedStart: LocalDateTime
    ): List<BookingEntity>

    // будет брать брони кабинета за выбранную дату
    fun findAllByRoomIdAndStatusAndStartTimeBetweenOrderByStartTimeAsc(
        roomId: Long,
        status: BookingStatus,
        dayStart: LocalDateTime,
        dayEnd: LocalDateTime
    ): List<BookingEntity>

    // если кабинет свободен сейчас, мы найдем, когда у него следующая бронь
    fun findFirstByRoomIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
        roomId: Long,
        status: BookingStatus,
        selectedDateTime: LocalDateTime
    ): BookingEntity?
}