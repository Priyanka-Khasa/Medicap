package com.runanywhere.startup_hackathon.medicap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_profile")
data class HealthProfileEntity(
    @PrimaryKey val id: Int = 1, // always 1 row
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val bloodGroup: String = "",
    val allergies: String = "",
    val conditions: String = "",
    val emergencyContact: String = ""
)
