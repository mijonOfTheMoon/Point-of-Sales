package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.AuthRepository
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
                val role  = repository.getUserRole()
                val email = repository.getUserEmail()
                val name  = repository.getUserName()
                _checkState.value = AuthCheckState.Authenticated(role, email, name)
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
                val role  = repository.getUserRole()
                val email = repository.getUserEmail()
                val name  = repository.getUserName()
                _checkState.value = AuthCheckState.Authenticated(role, email, name)
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
            } catch (_: Exception) {}
        }
    }

    /** Update name/email/password without terminating the session. */
    fun updateProfile(newName: String?, newEmail: String?, newPassword: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                repository.updateProfile(newName, newEmail, newPassword)
                val role  = repository.getUserRole()
                val email = repository.getUserEmail()
                val name  = repository.getUserName()
                _checkState.value = AuthCheckState.Authenticated(role, email, name)
                _uiState.value = AuthUiState.Success("Profile updated successfully!")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
