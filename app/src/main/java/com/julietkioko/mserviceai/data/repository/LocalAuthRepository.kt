package com.julietkioko.mserviceai.data.repository

import com.julietkioko.mserviceai.data.local.AuthPreferencesDataStore
import com.julietkioko.mserviceai.domain.model.AuthResult
import com.julietkioko.mserviceai.domain.model.User
import com.julietkioko.mserviceai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/**
 * Concrete local-first implementation of [AuthRepository].
 *
 * Persists user accounts and active sessions locally using Android's
 * Jetpack [DataStore]. Passwords are securely hashed with SHA-256 before
 * storage — never persisted in plain text.
 *
 * This implementation allows the application to work completely offline
 * and seamlessly act as an immediate fallback when Firebase backend services
 * are unconfigured or unavailable.
 */
class LocalAuthRepository(
    private val dataStore: AuthPreferencesDataStore
) : AuthRepository {

    override val isAuthenticated: Flow<Boolean> = dataStore.isLoggedIn

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            val accounts = loadAccounts()
            val normalizedEmail = email.trim().lowercase()

            // Check for existing account with the same email
            if (accounts.any { it.email.lowercase() == normalizedEmail }) {
                return AuthResult.Error("An account with this email already exists. Please sign in.")
            }

            // Create new user account with hashed password
            val user = User(
                uid = "local_" + UUID.randomUUID().toString().take(12),
                name = name.trim(),
                email = email.trim(),
                createdAt = System.currentTimeMillis()
            )

            val record = LocalAccount(
                uid = user.uid,
                name = user.name,
                email = user.email,
                passwordHash = hashPassword(password),
                createdAt = user.createdAt
            )

            accounts.add(record)
            saveAccounts(accounts)

            // Cache active session
            dataStore.saveSession(
                uid = user.uid,
                name = user.name,
                email = user.email
            )

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-up failed. Please try again.")
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val accounts = loadAccounts()
            val normalizedEmail = email.trim().lowercase()

            val account = accounts.find { it.email.lowercase() == normalizedEmail }
                ?: return AuthResult.Error("No account found with this email. Please sign up first.")

            val inputHash = hashPassword(password)
            if (account.passwordHash != inputHash) {
                return AuthResult.Error("Incorrect password. Please try again.")
            }

            val user = User(
                uid = account.uid,
                name = account.name,
                email = account.email,
                createdAt = account.createdAt
            )

            // Cache active session
            dataStore.saveSession(
                uid = user.uid,
                name = user.name,
                email = user.email
            )

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Sign-in failed. Please try again.")
        }
    }

    override suspend fun signOut() {
        dataStore.clearSession()
    }

    override suspend fun getCurrentUser(): User? {
        val uid = dataStore.userUid.first()
        if (uid.isBlank()) return null

        val name = dataStore.userName.first()
        val email = dataStore.userEmail.first()

        return User(
            uid = uid,
            name = name,
            email = email
        )
    }

    // ── Internal Helpers ────────────────────────────────────────────

    private data class LocalAccount(
        val uid: String,
        val name: String,
        val email: String,
        val passwordHash: String,
        val createdAt: Long
    )

    private suspend fun loadAccounts(): MutableList<LocalAccount> {
        val raw = dataStore.registeredAccounts.first()
        if (raw.isBlank()) return mutableListOf()

        val list = mutableListOf<LocalAccount>()
        return try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    LocalAccount(
                        uid = obj.getString("uid"),
                        name = obj.getString("name"),
                        email = obj.getString("email"),
                        passwordHash = obj.getString("passwordHash"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private suspend fun saveAccounts(accounts: List<LocalAccount>) {
        val jsonArray = JSONArray()
        for (account in accounts) {
            val obj = JSONObject().apply {
                put("uid", account.uid)
                put("name", account.name)
                put("email", account.email)
                put("passwordHash", account.passwordHash)
                put("createdAt", account.createdAt)
            }
            jsonArray.put(obj)
        }
        dataStore.saveRegisteredAccounts(jsonArray.toString())
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
