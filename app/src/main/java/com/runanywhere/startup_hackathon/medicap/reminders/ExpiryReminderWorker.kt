package com.runanywhere.startup_hackathon.medicap.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ExpiryReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val items = db.expiryDao().allRaw()

        val soon = items.filter { e ->
            runCatching {
                val d = LocalDate.parse(e.expiryDate)
                val days = ChronoUnit.DAYS.between(LocalDate.now(), d)
                days in 0..7
            }.getOrDefault(false)
        }

        if (soon.isNotEmpty()) {
            NotificationHelper.showExpiryNotification(
                applicationContext,
                title = "MediCap: Expiry alert",
                message = "You have ${soon.size} medicine(s) expiring within 7 days."
            )
        }

        return Result.success()
    }
}
