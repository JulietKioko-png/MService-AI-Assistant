package com.julietkioko.mserviceai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore wrapper for persisting lightweight user preferences and session state.
 *
 * **What lives here vs. Firestore:**
 * - DataStore  → session flag, cached display name, theme preference (fast, local, tiny)
 * - Firestore  → authoritative user profile, account data (source of truth, synced)
 *
 * Uses Jetpack DataStore (Preferences) which is the modern replacement for
 * SharedPreferences, providing:
 * - Coroutine-based async reads/writes (never blocks the main thread)
 * - Flow-based observation for reactive UI updates
 * - Atomic read-modify-write via [edit] transactions
 *
 * The [Context.dataStore] extension property creates a singleton DataStore
 * scoped to the application, stored at:
 * `/data/data/com.julietkioko.mserviceai/files/datastore/mservice_preferences.preferences_pb`
 */

/** Singleton DataStore instance — one per app process. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mservice_preferences"
)

class AuthPreferencesDataStore(private val context: Context) {

    // ── Preference Keys ─────────────────────────────────────────────

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_UID = stringPreferencesKey("user_uid")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val REGISTERED_ACCOUNTS = stringPreferencesKey("registered_accounts_data")
    }

    // ── Reads (Flow-based, reactive) ────────────────────────────────

    /**
     * Emits `true` when a local session exists, `false` otherwise.
     * Observed by the navigation graph to determine the start destination.
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    /** Cached user display name for fast UI rendering without a Firestore read. */
    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: ""
    }

    /** Cached user email. */
    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_EMAIL] ?: ""
    }

    /** Cached Firebase UID. */
    val userUid: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_UID] ?: ""
    }

    /** Stored registered accounts data for offline/local-first authentication. */
    val registeredAccounts: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.REGISTERED_ACCOUNTS] ?: ""
    }

    // ── Writes (suspend, atomic) ────────────────────────────────────

    /**
     * Saves session data after a successful sign-in or sign-up.
     * Called by [FirebaseAuthRepository] after Firebase Auth + Firestore succeed.
     */
    suspend fun saveSession(uid: String, name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.USER_UID] = uid
            prefs[Keys.USER_NAME] = name
            prefs[Keys.USER_EMAIL] = email
        }
    }

    /**
     * Persists accounts data for local credential validation.
     */
    suspend fun saveRegisteredAccounts(data: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REGISTERED_ACCOUNTS] = data
        }
    }

    /**
     * Clears session data on sign-out while preserving registered accounts.
     * After this, [isLoggedIn] emits `false` and navigation redirects to sign-in.
     */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.USER_UID] = ""
            prefs[Keys.USER_NAME] = ""
            prefs[Keys.USER_EMAIL] = ""
        }
    }
}
