package com.runanywhere.startup_hackathon.medicap.core

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.runanywhere.startup_hackathon.medicap.core.auth.SessionStore
import com.runanywhere.startup_hackathon.medicap.core.navigation.MediCapNavGraph
import com.runanywhere.startup_hackathon.medicap.data.AppDatabase
import com.runanywhere.startup_hackathon.medicap.data.seed.NdhmSeeder
import com.runanywhere.startup_hackathon.medicap.ui.screens.LoginScreen
import kotlinx.coroutines.launch

@Composable
fun MediCapApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Seed DB only once safely
    LaunchedEffect(Unit) {
        val db = AppDatabase.get(context)
        NdhmSeeder.seedIfEmpty(context, db)
    }

    val isLoggedIn by SessionStore.isLoggedInFlow(context).collectAsState(initial = false)

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                scope.launch { SessionStore.setLoggedIn(context, true) }
            }
        )
    } else {
        MediCapNavGraph(navController = navController)
    }
}
