package com.julietkioko.mserviceai.domain.model

/**
 * Sealed interface representing the outcome of an authentication operation.
 *
 * Using a sealed interface instead of exceptions gives the ViewModel
 * exhaustive `when` handling and makes error states explicit in the
 * type system — no uncaught surprises.
 *
 * Flow:
 * ```
 * ViewModel calls repository.signIn(email, password)
 *     ↓
 * Repository returns AuthResult.Success(user) or AuthResult.Error(message)
 *     ↓
 * ViewModel updates UiState based on the sealed branch
 * ```
 */
sealed interface AuthResult {

    /** Authentication succeeded — contains the authenticated [User]. */
    data class Success(val user: User) : AuthResult

    /** Authentication failed — contains a human-readable [message]. */
    data class Error(val message: String) : AuthResult
}
