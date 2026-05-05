package com.example.keeiptzuit.features.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.keeiptzuit.features.main.navigation.Main
import com.example.keeiptzuit.features.auth.presentation.welcome.WelcomeScreen
import com.example.keeiptzuit.features.auth.presentation.login.LoginScreen
import com.example.keeiptzuit.features.auth.presentation.register.RegisterScreen
import com.example.keeiptzuit.features.auth.presentation.resetPassword.ResetPasswordScreen
import kotlinx.serialization.Serializable

@Serializable
data object Auth

@Serializable
data object Welcome

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object ResetPassword

fun NavGraphBuilder.authNavGraph(navController: NavController) {
    navigation<Auth>(startDestination = Welcome) {
        composable<Welcome> {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Login)
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Main) {
                        popUpTo(Auth) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Main) {
                        popUpTo(Auth) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Login)
                }
            )
        }
        composable<ResetPassword> {
            ResetPasswordScreen()
        }
    }
}