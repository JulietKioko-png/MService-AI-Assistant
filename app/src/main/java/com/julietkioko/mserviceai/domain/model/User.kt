package com.julietkioko.mserviceai.domain.model

/**
 * Domain representation of an authenticated user.
 *
 * This model is backend-agnostic — it contains only the essential
 * identity fields that every auth provider exposes. Additional
 * profile fields stored in Firestore (e.g. phone, county) can be
 * added here as the app evolves.
 *
 * @property uid       Unique identifier assigned by the auth provider (Firebase UID).
 * @property name      User's display name provided during sign-up.
 * @property email     Email address used for authentication.
 * @property createdAt Epoch millis when the account was created (Firestore server timestamp).
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
