package com.example.keeiptzuit.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keeiptzuit.features.auth.data.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Submit -> handleLogin()
            is LoginIntent.EmailChanged -> {
                _state.update { it.copy(email = intent.email) }
            }

            is LoginIntent.PasswordChanged -> {
                _state.update { it.copy(password = intent.password) }
            }

            is LoginIntent.PasswordVisibilityChanged -> {
                _state.update { it.copy(isPasswordVisible = intent.isVisible) }
            }

            is LoginIntent.GoogleSignInTokenReceived -> {
                handleGoogleSignIn(intent.idToken)
            }
        }
    }

    private fun handleLogin() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                authRepository.loginWithEmail(state.value.email, state.value.password)
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                        "Incorrect password or email."

                    else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
                }
                _state.update {
                    it.copy(
                        error = errorMessage,
                        password = ""
                    )
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                authRepository.signInWithGoogle(idToken)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}