package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.KasRepository
import com.example.pointofsales.model.Kas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface KasUiState {
    data object Loading : KasUiState
    data class Success(val kasList: List<Kas>) : KasUiState
    data class Error(val message: String) : KasUiState
}

class KasViewModel(private val repository: KasRepository = KasRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<KasUiState>(KasUiState.Loading)
    val uiState: StateFlow<KasUiState> = _uiState.asStateFlow()

    init {
        loadKas()
    }

    fun loadKas() {
        viewModelScope.launch {
            _uiState.value = KasUiState.Loading
            try {
                val list = repository.getKas()
                _uiState.value = KasUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Failed to load kas")
            }
        }
    }

    fun manualAdjustment(kasId: String, amount: Double, reason: String) {
        viewModelScope.launch {
            try {
                repository.manualAdjustment(kasId, amount, reason)
                loadKas()
            } catch (_: Exception) {}
        }
    }

    fun toggleKasStatus(kas: Kas) {
        viewModelScope.launch {
            try {
                if (kas.is_active) {
                    repository.deactivateKas(kas.id ?: "")
                } else {
                    repository.activateKas(kas.id ?: "")
                }
                loadKas()
            } catch (_: Exception) {}
        }
    }
}
