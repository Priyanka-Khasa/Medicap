package com.runanywhere.startup_hackathon.medicap.data.model

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = MedicineEntity::class)
@Entity(tableName = "medicines_fts")
data class MedicineFtsEntity(
    val code: String,
    val display: String
)

