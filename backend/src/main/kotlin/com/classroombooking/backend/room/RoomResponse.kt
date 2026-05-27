package com.classroombooking.backend.room

data class RoomResponse(
    val id: Long,
    val number: String,
    val name: String?,
    val capacity: Int?,
    val description: String?,
    val buildingName: String?,
    val floor: Int?,
    val isActive: Boolean
)
