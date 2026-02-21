package com.dpi.changer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dpi.changer.ui.screens.MainScreen
import com.dpi.changer.ui.screens.PresetScreen

@Composable
fun DPINavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToPresets = { navController.navigate("presets") }
            )
        }
        composable("presets") {
            PresetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}