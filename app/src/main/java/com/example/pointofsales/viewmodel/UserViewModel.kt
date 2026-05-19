package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.UserRepository
import com.example.pointofsales.model.AdminUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val users: List<AdminUser>) : UserUiState
    data class Error(val message: String) : UserUiState
}

class UserViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                _uiState.value = UserUiState.Success(repository.getUsers())
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to load users")
            }
        }
    }

    fun createUser(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            try {
                repository.createUser(name, email, password, role)
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to create user")
            }
        }
    }

    fun updateUser(id: String, name: String, email: String, password: String?, role: String) {
        if (id.isBlank()) {
            _uiState.value = UserUiState.Error("User id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateUser(id, name, email, password, role)
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to update user")
            }
        }
    }

    /**
     * Updates the currently logged-in admin's own record.
     * Provides an [onSuccess] callback so the caller can trigger re-authentication
     * after the Admin API invalidates the current session token.
     */
    fun updateSelf(
        id: String,
        name: String,
        email: String,
        password: String?,
        role: String,
        onSuccess: () -> Unit
    ) {
        if (id.isBlank()) {
            _uiState.value = UserUiState.Error("User id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateUser(id, name, email, password, role)
                onSuccess()
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun deleteUser(id: String) {
        if (id.isBlank()) {
            _uiState.value = UserUiState.Error("User id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteUser(id)
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Failed to delete user")
            }
        }
    }
}
