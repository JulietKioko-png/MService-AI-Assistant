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
 * ViewModel for the Sign In screen.
 *
 * Responsibilities:
 * 1. Holds and mutates [SignInUiState] via [MutableStateFlow].
 * 2. Validates email/password format before calling the repository.
 * 3. Delegates authentication to [AuthRepository] (Firebase under the hood).
 * 4. Maps [AuthResult] outcomes back to UI state changes.
 *
 * The ViewModel does NOT reference any Android framework classes (no Context,
 * no Activity) — it depends only on the domain-layer [AuthRepository] interface.
 * This makes it fully unit-testable with a fake repository.
 *
 * ```
 * SignInScreen (observes uiState)
 *       │
 *       ▼
 * SignInViewModel (validates → calls repo → updates state)
 *       │
 *       ▼
 * AuthRepository (Firebase Auth + Firestore + DataStore)
 * ```
 */
class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    // ── Input Handlers ──────────────────────────────────────────────

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Sign In Action ──────────────────────────────────────────────

    fun onSignIn() {
        val state = _uiState.value

        // Client-side validation before hitting the network
        if (state.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }
        if (!isValidEmail(state.email.trim())) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your password.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        // Fire the async sign-in request
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.signIn(state.email.trim(), state.password)) {
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
        /**
         * Factory that injects [AuthRepository] into the ViewModel.
         * Used until the project adopts Hilt or Koin for DI.
         */
        fun provideFactory(
            authRepository: AuthRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignInViewModel(authRepository) as T
            }
        }
    }
}
