package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.AuthRepository
import com.example.pointofsales.model.AdminUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _checkState = MutableStateFlow<AuthCheckState>(AuthCheckState.Loading)
    val checkState: StateFlow<AuthCheckState> = _checkState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            if (repository.isUserLoggedIn()) {
                refreshAuthenticatedState()
            } else {
                _checkState.value = AuthCheckState.Unauthenticated
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.signUp(name, email, password)
                _uiState.value = AuthUiState.Success("Registration successful! Please check your email.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.signIn(email, password)
                refreshAuthenticatedState()
                _uiState.value = AuthUiState.Success("Login successful!")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
                _checkState.value = AuthCheckState.Unauthenticated
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Logout failed")
            }
        }
    }

    /** Update name/email/password without terminating the session. */
    fun updateProfile(newName: String?, newEmail: String?, newPassword: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.updateProfile(newName, newEmail, newPassword)
                applyCurrentUserSnapshot(user)
                _uiState.value = AuthUiState.Success("Profile updated successfully!")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun refreshSessionProfile() {
        viewModelScope.launch {
            refreshAuthenticatedState()
        }
    }

    fun applyCurrentUserSnapshot(user: AdminUser) {
        _checkState.value = AuthCheckState.Authenticated(
            role = user.role,
            email = user.email,
            name = user.name,
            id = user.id
        )
    }

    private suspend fun refreshAuthenticatedState() {
        val profile = repository.getCurrentUserProfile()
        _checkState.value = AuthCheckState.Authenticated(
            role = profile?.role ?: "cashier",
            email = repository.getUserEmail(),
            name = profile?.name.orEmpty(),
            id = repository.getUserId()
        )
    }
}
