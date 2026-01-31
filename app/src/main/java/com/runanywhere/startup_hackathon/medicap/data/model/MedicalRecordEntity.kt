package com.runanywhere.startup_hackathon.medicap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,                 // Prescription / Lab / Scan / Discharge / Vaccine / Other
    val title: String,
    val date: String,                 // store as yyyy-MM-dd (simple)
    val doctorOrHospital: String? = null,
    val uriString: String,            // content://... or file://...
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
