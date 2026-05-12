package com.example.keeiptzuit.features.scanner.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.keeiptzuit.features.main.navigation.Main
import com.example.keeiptzuit.features.scanner.presentation.analysis.AnalysisScreen
import com.example.keeiptzuit.features.scanner.presentation.camera.CameraScreen
import com.example.keeiptzuit.features.scanner.presentation.confirmation.ConfirmationScreen
import com.example.keeiptzuit.features.scanner.presentation.shared.SharedViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Scanner

@Serializable
data object Camera

@Serializable
data object Confirmation

@Serializable
data object Analysis

fun NavGraphBuilder.scannerNavGraph(navController: NavController) {
    navigation<Scanner>(startDestination = Camera) {
        composable<Camera> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<Scanner>()
            }
            val sharedViewModel: SharedViewModel = hiltViewModel(parentEntry)
            CameraScreen(
                onNavigateToResult = {
                    navController.navigate(Confirmation)
                },
                sharedViewModel = sharedViewModel
            )
        }
        composable<Confirmation> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<Scanner>()
            }
            val sharedViewModel: SharedViewModel = hiltViewModel(parentEntry)
            ConfirmationScreen(
                onRetake = {
                    navController.popBackStack()
                },
                onConfirm = {
                    navController.navigate(Analysis)
                },
                sharedViewModel = sharedViewModel
            )
        }
        composable<Analysis> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<Scanner>()
            }
            val sharedViewModel: SharedViewModel = hiltViewModel(parentEntry)

            AnalysisScreen(
                sharedViewModel = sharedViewModel,
                onExitToMain = {
                    navController.navigate(Main) {
                        popUpTo(Scanner) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}