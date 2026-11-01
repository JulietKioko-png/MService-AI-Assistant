package com.julietkioko.mserviceai.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignInViewModelTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: SignInViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeAuthRepository()
        viewModel = SignInViewModel(fakeRepository)
    }

    @Test
    fun `initial state has empty fields, not loading, not authenticated`() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun `onEmailChanged updates email and clears error`() {
        viewModel.onEmailChanged("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() {
        viewModel.onPasswordChanged("secret123")
        assertEquals("secret123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onTogglePasswordVisibility flips visibility flag`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `onSignIn with blank email produces error message`() {
        viewModel.onSignIn()
        assertEquals("Please enter your email address.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSignIn with invalid email format produces error message`() {
        viewModel.onEmailChanged("not-an-email")
        viewModel.onSignIn()
        assertEquals("Please enter a valid email address.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSignIn with blank password produces error message`() {
        viewModel.onEmailChanged("user@domain.com")
        viewModel.onSignIn()
        assertEquals("Please enter your password.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onSignIn with short password produces error message`() {
        viewModel.onEmailChanged("user@domain.com")
        viewModel.onPasswordChanged("123")
        viewModel.onSignIn()
        assertEquals("Password must be at least 6 characters.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError resets errorMessage to null`() {
        viewModel.onSignIn()
        assertEquals("Please enter your email address.", viewModel.uiState.value.errorMessage)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
