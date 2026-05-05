package com.example.keeiptzuit.features.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.keeiptzuit.features.main.presentation.MainScreen
import kotlinx.serialization.Serializable

@Serializable
data object Main

fun NavGraphBuilder.mainNavGraph() {
    composable<Main> {
        MainScreen()
    }
}