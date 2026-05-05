package com.example.keeiptzuit.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keeiptzuit.features.auth.data.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun processIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.Submit -> handleRegister()
            is RegisterIntent.EmailChanged -> {
                _state.update { it.copy(email = intent.email) }
            }

            is RegisterIntent.PasswordChanged -> {
                _state.update { it.copy(password = intent.password) }
            }

            is RegisterIntent.FirstNameChanged -> {
                _state.update { it.copy(firstName = intent.firstName) }
            }

            is RegisterIntent.LastNameChanged -> {
                _state.update { it.copy(lastName = intent.lastName) }
            }

            is RegisterIntent.ConfirmedPasswordChanged -> {
                _state.update { it.copy(confirmPassword = intent.confirmPassword) }
            }

            is RegisterIntent.ConfirmPasswordVisibilityChanged -> {
                _state.update { it.copy(isConfirmPasswordVisible = intent.isVisible) }
            }

            is RegisterIntent.PasswordVisibilityChanged -> {
                _state.update { it.copy(isPasswordVisible = intent.isVisible) }
            }

            is RegisterIntent.GoogleSignInTokenReceived -> {
                handleGoogleSignIn(intent.idToken)
            }
        }
    }

    private fun handleRegister() {
        viewModelScope.launch {
            if (state.value.password != state.value.confirmPassword) {
                _state.update { it.copy(error = "Passwords do not match") }
                return@launch
            }
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                authRepository.createAccountWithEmail(state.value.firstName, state.value.lastName, state.value.email, state.value.password)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                when (e) {
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                        _state.update {
                            it.copy(
                                error = "Password is too weak. Use at least 6 characters.",
                                password = "",
                                confirmPassword = ""
                            )
                        }
                    }

                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                        _state.update { it.copy(error = "This email is already registered.") }
                    }

                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                        _state.update { it.copy(error = "Invalid email format.") }
                    }

                    else -> {
                        _state.update {
                            it.copy(
                                error = e.localizedMessage ?: "An unexpected error occurred"
                            )
                        }
                    }
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