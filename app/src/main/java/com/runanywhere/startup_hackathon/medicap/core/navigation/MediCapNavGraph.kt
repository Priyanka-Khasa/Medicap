package com.runanywhere.startup_hackathon.medicap.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.runanywhere.startup_hackathon.medicap.ui.screens.AddExpiryScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.DeliveryHubScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.ExpiryVaultScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.HomeScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.MedicineDetailsScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.ModelsScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.ScanScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.SearchScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.EmergencyScreen
import com.runanywhere.startup_hackathon.medicap.ui.screens.HealthRecordsScreen



@Composable
fun MediCapNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onSearch = { navController.navigate(Routes.SEARCH) },
                onScan = { navController.navigate(Routes.SCAN) },
                onExpiryVault = { navController.navigate(Routes.EXPIRY_VAULT) },
                onDeliveryHub = { navController.navigate(Routes.DELIVERY_HUB) },
                onEmergency = { navController.navigate(Routes.EMERGENCY) },
                onHealthRecords = { navController.navigate(Routes.HEALTH_RECORDS) }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onOpenDetails = { medicineId ->
                    navController.navigate("${Routes.DETAILS}/$medicineId")
                }
            )
        }

        composable(Routes.HEALTH_RECORDS) {
            HealthRecordsScreen(onBack = { navController.popBackStack() })
        }


        composable(Routes.SCAN) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onResultPick = { medicineId ->
                    navController.navigate("${Routes.DETAILS}/$medicineId")
                },
                onManualSearch = { navController.navigate(Routes.SEARCH) },
                autoOpenBestMatch = true
            )
        }

        composable(
            route = "${Routes.DETAILS}/{medicineId}",
            arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("medicineId") ?: -1L

            MedicineDetailsScreen(
                medicineId = id,
                onBack = { navController.popBackStack() },
                onAddExpiry = { navController.navigate(Routes.ADD_EXPIRY) },
                onOpenModels = { navController.navigate(Routes.MODELS) }
            )
        }

        composable(Routes.EXPIRY_VAULT) {
            ExpiryVaultScreen(
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Routes.ADD_EXPIRY) }
            )
        }

        composable(Routes.EMERGENCY) {
            EmergencyScreen(onBack = { navController.popBackStack() })
        }


        composable(Routes.DELIVERY_HUB) {
            DeliveryHubScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_EXPIRY) {
            AddExpiryScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.MODELS) {
            ModelsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
