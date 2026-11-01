package com.julietkioko.mserviceai.presentation.auth

/**
 * UI state for the Sign In screen.
 *
 * Every field in this data class maps directly to a visual element
 * on screen — the ViewModel produces new instances via [copy] and
 * the Composable renders them without side effects.
 *
 * @property email           Current text in the email input field.
 * @property password        Current text in the password input field.
 * @property isPasswordVisible Whether the password field shows clear text.
 * @property isLoading       True while Firebase Auth is processing — disables buttons, shows spinner.
 * @property errorMessage    Non-null when an error should be displayed (inline or Snackbar).
 * @property isAuthenticated Flips to true after successful sign-in — triggers navigation to Home.
 */
data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)
