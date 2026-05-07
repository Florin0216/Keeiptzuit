package com.example.keeiptzuit.features.scanner.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.keeiptzuit.features.scanner.presentation.scannerCamera.ScannerCameraScreen
import com.example.keeiptzuit.features.scanner.presentation.scannerResult.ScannerResultScreen
import com.example.keeiptzuit.features.scanner.presentation.shared.ScannerViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Scanner

@Serializable
data object ScannerCamera

@Serializable
data object ScannerResult

fun NavGraphBuilder.scannerNavGraph(navController: NavController) {
    navigation<Scanner>(startDestination = ScannerCamera) {
        composable<ScannerCamera> { backStackEntry ->
            val viewModel: ScannerViewModel = hiltViewModel(backStackEntry)
            ScannerCameraScreen(
                onNavigateToResult = {
                    navController.navigate(ScannerResult)
                },
                viewModel = viewModel
            )
        }
        composable<ScannerResult> { backStackEntry ->
            val viewModel: ScannerViewModel =
                if (navController.previousBackStackEntry != null) hiltViewModel(
                    navController.previousBackStackEntry!!
                ) else hiltViewModel()
            ScannerResultScreen(
                viewModel = viewModel
            )
        }
    }
}