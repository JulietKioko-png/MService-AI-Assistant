package com.julietkioko.mserviceai.presentation.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeAuthRepository()
        viewModel = SignUpViewModel(fakeRepository)
    }

    @Test
    fun `initial state has empty fields and null errors`() {
        val state = viewModel.uiState.value
        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
        assertFalse(state.isAuthenticated)
    }

    @Test
    fun `field changes update state and clear corresponding errors`() {
        viewModel.onNameChanged("Alice")
        assertEquals("Alice", viewModel.uiState.value.name)
        assertNull(viewModel.uiState.value.nameError)

        viewModel.onEmailChanged("alice@example.com")
        assertEquals("alice@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)

        viewModel.onPasswordChanged("password123")
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)

        viewModel.onConfirmPasswordChanged("password123")
        assertEquals("password123", viewModel.uiState.value.confirmPassword)
        assertNull(viewModel.uiState.value.confirmPasswordError)
    }

    @Test
    fun `onTogglePasswordVisibility flips flag`() {
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.uiState.value.isPasswordVisible)
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun `onSignUp with empty fields sets errors on all fields simultaneously`() {
        viewModel.onSignUp()

        val state = viewModel.uiState.value
        assertNotNull(state.nameError)
        assertEquals("Name must be at least 2 characters.", state.nameError)
        assertNotNull(state.emailError)
        assertEquals("Email is required.", state.emailError)
        assertNotNull(state.passwordError)
        assertEquals("Password must be at least 6 characters.", state.passwordError)
    }

    @Test
    fun `onSignUp with mismatched passwords sets confirm error`() {
        viewModel.onNameChanged("Bob Dylan")
        viewModel.onEmailChanged("bob@example.com")
        viewModel.onPasswordChanged("securePass1")
        viewModel.onConfirmPasswordChanged("differentPass2")

        viewModel.onSignUp()

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertEquals("Passwords do not match.", state.confirmPasswordError)
    }

    @Test
    fun `onSignUp with invalid email format sets email error`() {
        viewModel.onNameChanged("Charlie")
        viewModel.onEmailChanged("invalid-email")
        viewModel.onPasswordChanged("mypassword")
        viewModel.onConfirmPasswordChanged("mypassword")

        viewModel.onSignUp()

        assertEquals("Please enter a valid email address.", viewModel.uiState.value.emailError)
    }
}
