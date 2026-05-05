package com.example.keeiptzuit.features.auth.presentation.register

sealed class RegisterIntent {
    data class EmailChanged(val email: String) : RegisterIntent()
    data class ConfirmedPasswordChanged(val confirmPassword: String) : RegisterIntent()
    data class PasswordChanged(val password: String) : RegisterIntent()
    data class FirstNameChanged(val firstName: String) : RegisterIntent()
    data class LastNameChanged(val lastName: String) : RegisterIntent()
    data class PasswordVisibilityChanged(val isVisible: Boolean) : RegisterIntent()
    data class ConfirmPasswordVisibilityChanged(val isVisible: Boolean) : RegisterIntent()
    data object Submit : RegisterIntent()
    data class GoogleSignInTokenReceived(val idToken: String) : RegisterIntent()
}