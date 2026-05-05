package com.example.keeiptzuit.features.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.keeiptzuit.features.profile.presentation.profileDetails.ProfileDetailsScreen
import kotlinx.serialization.Serializable

@Serializable
data object Profile

fun NavGraphBuilder.profileNavGraph() {
    composable<Profile> {
        ProfileDetailsScreen()
    }
}