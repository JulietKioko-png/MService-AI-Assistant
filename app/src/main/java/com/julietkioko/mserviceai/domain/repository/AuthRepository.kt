package com.julietkioko.mserviceai.domain.repository

import com.julietkioko.mserviceai.domain.model.AuthResult
import com.julietkioko.mserviceai.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contract for authentication operations.
 *
 * This interface sits in the **domain layer** and knows nothing about
 * Firebase, DataStore, or any other implementation detail. The data
 * layer provides concrete implementations ([FirebaseAuthRepository]).
 *
 * All long-running operations are `suspend` functions so they can be
 * called from a coroutine scope inside ViewModels.
 *
 * Architecture:
 * ```
 * ┌──────────────┐      ┌────────────────────┐      ┌───────────────┐
 * │  ViewModel   │─────▶│  AuthRepository     │◀─────│  Firebase     │
 * │  (uses)      │      │  (domain interface) │      │  (implements) │
 * └──────────────┘      └────────────────────┘      └───────────────┘
 * ```
 */
interface AuthRepository {

    /**
     * Registers a new user with email and password.
     * On success, creates a user profile document in Firestore.
     */
    suspend fun signUp(name: String, email: String, password: String): AuthResult

    /**
     * Authenticates an existing user with email and password.
     * On success, returns the user profile from Firestore.
     */
    suspend fun signIn(email: String, password: String): AuthResult

    /**
     * Signs out the current user and clears the local session.
     */
    suspend fun signOut()

    /**
     * Returns the currently authenticated [User], or null if no session exists.
     * Checks Firebase Auth state first — not a network call if cached.
     */
    suspend fun getCurrentUser(): User?

    /**
     * Observable stream of the current authentication state.
     * Emits `true` when a user is signed in, `false` otherwise.
     *
     * The navigation graph observes this to decide the start destination
     * (auth screens vs. home screen).
     */
    val isAuthenticated: Flow<Boolean>
}
