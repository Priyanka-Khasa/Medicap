package com.runanywhere.startup_hackathon.medicap.data.dao

import androidx.room.*
import com.runanywhere.startup_hackathon.medicap.data.model.HealthProfileEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicalRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // Profile
    @Query("SELECT * FROM health_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): HealthProfileEntity?

    @Query("SELECT * FROM health_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<HealthProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: HealthProfileEntity)

    // Records
    @Query("SELECT * FROM medical_records ORDER BY createdAt DESC")
    fun observeRecords(): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records WHERE id = :id LIMIT 1")
    suspend fun getRecordOnce(id: Long): MedicalRecordEntity?

    @Insert
    suspend fun insertRecord(record: MedicalRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: MedicalRecordEntity)
}
