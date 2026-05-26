package com.classroombooking.backend.room

import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<RoomEntity, Long> {

    fun findAllByIsActiveTrue(): List<RoomEntity>

    fun findByIdAndIsActiveTrue(id: Long): RoomEntity?
}