package com.example.keeiptzuit.features.scanner.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.keeiptzuit.features.scanner.presentation.scannerCamera.ScannerCameraScreen
import com.example.keeiptzuit.features.scanner.presentation.scannerResult.ScannerResultScreen
import kotlinx.serialization.Serializable

@Serializable
data object Scanner

@Serializable
data object ScannerCamera

@Serializable
data object ScannerResult

fun NavGraphBuilder.scannerNavGraph() {
    navigation<Scanner>(startDestination = ScannerCamera) {
        composable<ScannerCamera> {
            ScannerCameraScreen()
        }
        composable<ScannerResult> {
            ScannerResultScreen()
        }
    }
}