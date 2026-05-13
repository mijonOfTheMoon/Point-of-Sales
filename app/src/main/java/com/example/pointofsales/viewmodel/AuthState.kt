package com.example.pointofsales.viewmodel

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface AuthCheckState {
    data object Loading : AuthCheckState
    data class Authenticated(val role: String, val email: String = "") : AuthCheckState
    data object Unauthenticated : AuthCheckState
}
