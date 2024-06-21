package com.example.envilibrary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.envilibrary.screens.CaptureScreen
import com.example.envilibrary.screens.LibraryScreen

@Composable
fun NavGraph(navController: NavHostController, onTabChange: (Int) -> Unit) {
    NavHost(navController = navController, startDestination = "library") {
        composable("capture") {
            CaptureScreen(navController = navController, onTabChange = { onTabChange(0) })
        }
        composable("library") {
            LibraryScreen(navController = navController, onTabChange = { onTabChange(1) })
        }
    }
}
