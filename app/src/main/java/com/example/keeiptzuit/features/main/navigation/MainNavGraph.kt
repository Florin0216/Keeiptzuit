package com.example.keeiptzuit.features.main.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.keeiptzuit.features.main.presentation.MainScreen
import com.example.keeiptzuit.features.scanner.navigation.Scanner
import kotlinx.serialization.Serializable

@Serializable
data object Main

fun NavGraphBuilder.mainNavGraph(navController: NavController) {
    composable<Main> {
        MainScreen(
            onNavigateToScanner = {
                navController.navigate(Scanner)
            }
        )
    }
}