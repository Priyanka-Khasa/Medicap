package com.runanywhere.startup_hackathon.medicap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.runanywhere.startup_hackathon.medicap.data.dao.ExpiryDao
import com.runanywhere.startup_hackathon.medicap.data.dao.HealthDao
import com.runanywhere.startup_hackathon.medicap.data.dao.MedicineDao
import com.runanywhere.startup_hackathon.medicap.data.model.ExpiryEntity
import com.runanywhere.startup_hackathon.medicap.data.model.HealthProfileEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicalRecordEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineEntity
import com.runanywhere.startup_hackathon.medicap.data.model.MedicineFtsEntity

@Database(
    entities = [
        MedicineEntity::class,
        MedicineFtsEntity::class,
        ExpiryEntity::class,
        HealthProfileEntity::class,
        MedicalRecordEntity::class
    ],
    version = 3, // ✅ bump this whenever schema changes
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun expiryDao(): ExpiryDao
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "medicap.db"

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration() // ✅ dev-safe
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
