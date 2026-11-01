package com.julietkioko.mserviceai.presentation.auth

import com.julietkioko.mserviceai.domain.model.AuthResult
import com.julietkioko.mserviceai.domain.model.User
import com.julietkioko.mserviceai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of [AuthRepository] for unit testing ViewModels.
 * Avoids any Android/Firebase/DataStore dependencies.
 */
class FakeAuthRepository(
    initialIsAuthenticated: Boolean = false,
    var shouldFail: Boolean = false,
    var failureMessage: String = "Authentication failed."
) : AuthRepository {

    private val _isAuthenticated = MutableStateFlow(initialIsAuthenticated)
    override val isAuthenticated: Flow<Boolean> = _isAuthenticated.asStateFlow()

    var currentUser: User? = if (initialIsAuthenticated) {
        User(uid = "test-uid", name = "Test User", email = "test@example.com")
    } else null

    override suspend fun signUp(name: String, email: String, password: String): AuthResult {
        return if (shouldFail) {
            AuthResult.Error(failureMessage)
        } else {
            val user = User(uid = "new-uid", name = name, email = email)
            currentUser = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return if (shouldFail) {
            AuthResult.Error(failureMessage)
        } else {
            val user = User(uid = "signed-in-uid", name = "Existing User", email = email)
            currentUser = user
            _isAuthenticated.value = true
            AuthResult.Success(user)
        }
    }

    override suspend fun signOut() {
        currentUser = null
        _isAuthenticated.value = false
    }

    override suspend fun getCurrentUser(): User? = currentUser
}
