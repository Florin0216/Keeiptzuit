package com.example.keeiptzuit.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.keeiptzuit.features.main.navigation.mainNavGraph
import com.example.keeiptzuit.features.auth.navigation.authNavGraph
import com.example.keeiptzuit.features.auth.navigation.Auth
import com.example.keeiptzuit.features.main.navigation.Main
import com.example.keeiptzuit.features.scanner.navigation.scannerNavGraph
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if(Firebase.auth.currentUser != null) Main else Auth
    ) {
        authNavGraph(navController)
        mainNavGraph(navController)
        scannerNavGraph(navController)
    }
}