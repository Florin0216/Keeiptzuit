package com.example.keeiptzuit.features.auth.presentation.login

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data class PasswordVisibilityChanged(val isVisible: Boolean) : LoginIntent()
    data object Submit : LoginIntent()
    data class GoogleSignInTokenReceived(val idToken: String) : LoginIntent()
}