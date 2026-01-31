package com.runanywhere.startup_hackathon.medicap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.runanywhere.startup_hackathon.medicap.data.model.ExpiryEntity

@Dao
interface ExpiryDao {
    @Query("SELECT * FROM expiries ORDER BY expiryDate ASC")
    suspend fun getAll(): List<ExpiryEntity>

    @Insert
    suspend fun insert(item: ExpiryEntity)

    @Query("SELECT * FROM expiries")
    suspend fun allRaw(): List<ExpiryEntity>
}
