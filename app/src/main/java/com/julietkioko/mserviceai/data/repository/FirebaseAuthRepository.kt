package com.julietkioko.mserviceai.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.julietkioko.mserviceai.data.local.AuthPreferencesDataStore
import com.julietkioko.mserviceai.domain.model.AuthResult
import com.julietkioko.mserviceai.domain.model.User
import com.julietkioko.mserviceai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * Concrete implementation of [AuthRepository] backed by:
 * - **Firebase Authentication** — handles email/password sign-in/sign-up.
 * - **Cloud Firestore** — stores user profile documents at `users/{uid}`.
 * - **DataStore** — caches session state locally for instant app-launch checks.
 *
 * Data flow on sign-up:
 * ```
 * 1. FirebaseAuth.createUserWithEmailAndPassword() → creates auth account
 * 2. FirebaseAuth.updateProfile() → sets displayName on the auth record
 * 3. Firestore.collection("users").document(uid).set() → persists profile
 * 4. DataStore.saveSession() → caches session locally for next launch
 * ```
 *
 * Data flow on sign-in:
 * ```
 * 1. FirebaseAuth.signInWithEmailAndPassword() → authenticates
 * 2. Firestore.collection("users").document(uid).get() → fetches profile
 * 3. DataStore.saveSession() → updates local cache
 * ```
 *
 * Error handling:
 * Firebase exceptions are caught and mapped to human-readable messages
 * via [AuthResult.Error], so the UI layer never sees raw exceptions.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val dataStore: AuthPreferencesDataStore
) : AuthRepository {

    companion object {
        /** Firestore collection where user profile documents are stored. */
        private const val USERS_COLLECTION = "users"
    }

    // ── AuthRepository contract ─────────────────────────────────────

    override val isAuthenticated: Flow<Boolean> = dataStore.isLoggedIn

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            // 1. Create the Firebase Auth account
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Account creation failed. Please try again.")

            // 2. Set the display name on the Auth profile
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Build the domain user
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                createdAt = System.currentTimeMillis()
            )

            // 4. Save the profile document to Firestore
            saveUserToFirestore(user)

            // 5. Cache session locally
            dataStore.saveSession(
                uid = user.uid,
                name = user.name,
                email = user.email
            )

            AuthResult.Success(user)
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Please use at least 6 characters.")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("An account with this email already exists. Please sign in.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid email format. Please check and try again.")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-up failed. Please try again.")
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            // 1. Authenticate with Firebase
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return AuthResult.Error("Sign-in failed. Please try again.")

            // 2. Fetch profile from Firestore
            val user = getUserFromFirestore(firebaseUser.uid)
                ?: User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email
                )

            // 3. Cache session locally
            dataStore.saveSession(
                uid = user.uid,
                name = user.name,
                email = user.email
            )

            AuthResult.Success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email. Please sign up first.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Incorrect password. Please try again.")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-in failed. Please try again.")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        dataStore.clearSession()
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return getUserFromFirestore(firebaseUser.uid)
            ?: User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            )
    }

    // ── Firestore helpers ───────────────────────────────────────────

    /**
     * Persists a user profile document to Firestore at `users/{uid}`.
     *
     * Uses [set] (not [add]) so the document ID matches the Firebase Auth UID,
     * making lookups O(1) by UID.
     */
    private suspend fun saveUserToFirestore(user: User) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "createdAt" to user.createdAt
        )
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(userData)
            .await()
    }

    /**
     * Fetches a user profile from Firestore by UID.
     *
     * Returns null if the document doesn't exist (e.g. legacy accounts
     * created before Firestore profile storage was added).
     */
    private suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (doc.exists()) {
                User(
                    uid = doc.getString("uid") ?: uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null // Gracefully degrade — auth still works without profile
        }
    }
}
