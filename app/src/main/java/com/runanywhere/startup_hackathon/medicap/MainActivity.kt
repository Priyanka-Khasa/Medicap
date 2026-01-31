package com.runanywhere.startup_hackathon.medicap

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.runanywhere.startup_hackathon.medicap.core.MediCapApp
import com.runanywhere.startup_hackathon.medicap.ui.MediCapTheme
import com.runanywhere.startup_hackathon.medicap.reminders.ReminderScheduler

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Schedule daily expiry reminder check
        ReminderScheduler.scheduleDailyExpiryCheck(this)

        setContent {
            MediCapTheme(darkTheme = false) {
                MediCapApp()
            }
        }
    }
}
