package com.runanywhere.startup_hackathon.medicap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity

@Dao
interface MedicineDao {

    @Query("SELECT COUNT(*) FROM medicines")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<MedicineEntity>)

    // FTS search (fast + offline)
    @Query("""
        SELECT medicines.*
        FROM medicines
        JOIN medicines_fts
        ON medicines.id = medicines_fts.rowid
        WHERE medicines_fts MATCH :ftsQuery
        LIMIT :limit
    """)
    suspend fun searchFts(ftsQuery: String, limit: Int = 50): List<MedicineEntity>

    @Query("SELECT * FROM medicines ORDER BY display ASC LIMIT :limit")
    suspend fun getTop(limit: Int = 50): List<MedicineEntity>

    // ✅ ADD THIS
    @Query("SELECT * FROM medicines WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MedicineEntity?
}

