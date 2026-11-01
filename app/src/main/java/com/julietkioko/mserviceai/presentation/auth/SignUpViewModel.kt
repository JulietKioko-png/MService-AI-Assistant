package com.julietkioko.mserviceai.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.julietkioko.mserviceai.domain.model.AuthResult
import com.julietkioko.mserviceai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Sign Up screen.
 *
 * Responsibilities:
 * 1. Holds and mutates [SignUpUiState] via [MutableStateFlow].
 * 2. Validates all four fields with per-field error messages.
 * 3. Delegates account creation to [AuthRepository].
 * 4. Maps [AuthResult] outcomes back to UI state.
 *
 * Validation rules:
 * - Name: ≥ 2 characters
 * - Email: valid format (Android [Patterns.EMAIL_ADDRESS])
 * - Password: ≥ 6 characters (Firebase minimum)
 * - Confirm Password: must match Password
 *
 * ```
 * SignUpScreen (observes uiState)
 *       │
 *       ▼
 * SignUpViewModel (validates → calls repo → updates state)
 *       │
 *       ▼
 * AuthRepository.signUp() → Firebase Auth → Firestore → DataStore
 * ```
 */
class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    // ── Input Handlers ──────────────────────────────────────────────

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Sign Up Action ──────────────────────────────────────────────

    fun onSignUp() {
        val state = _uiState.value

        // Per-field validation — all errors shown simultaneously
        var hasError = false
        var nameErr: String? = null
        var emailErr: String? = null
        var passwordErr: String? = null
        var confirmErr: String? = null

        if (state.name.isBlank() || state.name.trim().length < 2) {
            nameErr = "Name must be at least 2 characters."
            hasError = true
        }

        if (state.email.isBlank()) {
            emailErr = "Email is required."
            hasError = true
        } else if (!isValidEmail(state.email.trim())) {
            emailErr = "Please enter a valid email address."
            hasError = true
        }

        if (state.password.length < 6) {
            passwordErr = "Password must be at least 6 characters."
            hasError = true
        }

        if (state.confirmPassword != state.password) {
            confirmErr = "Passwords do not match."
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = nameErr,
                    emailError = emailErr,
                    passwordError = passwordErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        // All validations passed — fire the async sign-up request
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signUp(
                name = state.name.trim(),
                email = state.email.trim(),
                password = state.password
            )) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return runCatching {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }.getOrElse {
            email.contains("@") && email.contains(".") && email.length >= 5
        }
    }

    // ── Factory ─────────────────────────────────────────────────────

    companion object {
        fun provideFactory(
            authRepository: AuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignUpViewModel(authRepository) as T
            }
        }
    }
}
