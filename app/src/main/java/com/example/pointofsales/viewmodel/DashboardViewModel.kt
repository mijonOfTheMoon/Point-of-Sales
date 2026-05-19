package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.DashboardRepository
import com.example.pointofsales.model.DashboardSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Idle : DashboardUiState
    data object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class DashboardViewModel(private val repository: DashboardRepository = DashboardRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val summary = repository.getDashboardSummary()
                _uiState.value = DashboardUiState.Success(summary)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard summary")
            }
        }
    }

    fun clearSummary() {
        _uiState.value = DashboardUiState.Idle
    }
}
