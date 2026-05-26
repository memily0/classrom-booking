package com.classroombooking.backend.room

data class RoomResponse(
    val id: Long,
    val number: String,
    val name: String?,
    val capacity: Int?,
    val description: String?,
    val isActive: Boolean
)