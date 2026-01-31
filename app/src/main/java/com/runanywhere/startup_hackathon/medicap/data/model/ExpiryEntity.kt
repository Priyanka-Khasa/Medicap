package com.runanywhere.startup_hackathon.medicap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expiries")
data class ExpiryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineName: String,
    val expiryDate: String, // YYYY-MM-DD
    val notes: String? = null
)
