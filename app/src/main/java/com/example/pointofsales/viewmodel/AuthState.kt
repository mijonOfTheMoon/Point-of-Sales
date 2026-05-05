package com.example.pointofsales.viewmodel

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

sealed class AuthCheckState {
    object Loading : AuthCheckState()
    object Authenticated : AuthCheckState()
    object Unauthenticated : AuthCheckState()
}
