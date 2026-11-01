package com.julietkioko.mserviceai.presentation.auth

/**
 * UI state for the Sign Up screen.
 *
 * Mirrors [SignInUiState] with additional fields for name and
 * password confirmation. Validation errors for individual fields
 * are stored separately from the general [errorMessage] to allow
 * inline error display under each text field.
 *
 * @property name               User's full name.
 * @property email              Email address for the new account.
 * @property password           Chosen password.
 * @property confirmPassword    Must match [password] before submission.
 * @property isPasswordVisible  Toggles password visibility for both fields.
 * @property isLoading          True while Firebase processes the registration.
 * @property errorMessage       General error (e.g. network failure, collision).
 * @property nameError          Inline validation error for the name field.
 * @property emailError         Inline validation error for the email field.
 * @property passwordError      Inline validation error for the password field.
 * @property confirmPasswordError Inline validation error for the confirm field.
 * @property isAuthenticated    Flips to true after successful registration.
 */
data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isAuthenticated: Boolean = false
)
