package com.runanywhere.startup_hackathon.medicap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,          // SNOMED/NDHM code
    val display: String        // e.g., "Aspirin 75 mg oral tablet"
)

