package com.classroombooking.backend.room

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "rooms")
class RoomEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val number: String,

    val name: String? = null,

    val capacity: Int? = null,

    val description: String? = null,

    @Column(name = "building_name")
    val buildingName: String? = null,

    val floor: Int? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true
)
