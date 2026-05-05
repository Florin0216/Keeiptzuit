package com.example.keeiptzuit.features.main.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.keeiptzuit.features.profile.navigation.Profile
import com.example.keeiptzuit.features.profile.navigation.profileNavGraph
import com.example.keeiptzuit.features.scanner.navigation.Scanner
import com.example.keeiptzuit.features.scanner.navigation.scannerNavGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            MainBottomBar(
                hierarchy = navController.currentBackStackEntryAsState().value?.destination?.hierarchy,
                onNavigateToScanner = { navController.navigate(Scanner) },
                onNavigateToProfile = { navController.navigate(Profile) }
            )
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            startDestination = Scanner
        ) {
            scannerNavGraph()
            profileNavGraph()
        }
    }
}

@Composable
private fun MainBottomBar(
    hierarchy: Sequence<NavDestination>?,
    onNavigateToScanner: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = hierarchy?.any { it.hasRoute(Scanner::class) } == true,
            onClick = { onNavigateToScanner() },
            label = { (Text(text = "Scanner")) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Scanner,
                    contentDescription = "Scanner"
                )
            }
        )
        NavigationBarItem(
            selected = hierarchy?.any { it.hasRoute(Profile::class) } == true,
            onClick = { onNavigateToProfile() },
            label = { (Text(text = "Profile")) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile"
                )
            }
        )
    }
}