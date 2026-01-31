package com.runanywhere.startup_hackathon.medicap.reminders

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val UNIQUE_NAME = "medicap_expiry_daily_check"

    fun scheduleDailyExpiryCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<ExpiryReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
