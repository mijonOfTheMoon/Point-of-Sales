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

    fun createKas(name: String, initialBalance: Double) {
        if (name.isBlank()) {
            _uiState.value = KasUiState.Error("Kas name is required")
            return
        }
        viewModelScope.launch {
            try {
                repository.createKas(name.trim(), initialBalance)
                loadKas()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Failed to create kas")
            }
        }
    }

    fun updateKasName(kasId: String, name: String) {
        if (kasId.isBlank()) {
            _uiState.value = KasUiState.Error("Kas id is missing")
            return
        }
        if (name.isBlank()) {
            _uiState.value = KasUiState.Error("Kas name is required")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateKasName(kasId, name.trim())
                loadKas()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Failed to update kas name")
            }
        }
    }

    fun deleteKas(kasId: String) {
        if (kasId.isBlank()) {
            _uiState.value = KasUiState.Error("Kas id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteKas(kasId)
                loadKas()
            } catch (e: Exception) {
                val message = e.message.orEmpty()
                val friendly = if (message.contains("foreign key", true) || message.contains("violates", true) || message.contains("23503")) {
                    "This kas can't be deleted because it has transaction history. Deactivate it instead."
                } else {
                    message.ifBlank { "Failed to delete kas" }
                }
                _uiState.value = KasUiState.Error(friendly)
            }
        }
    }

    fun manualAdjustment(kasId: String, amount: Double, reason: String) {
        if (kasId.isBlank()) {
            _uiState.value = KasUiState.Error("Kas id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.manualAdjustment(kasId, amount, reason)
                loadKas()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Failed to adjust kas")
            }
        }
    }

    fun toggleKasStatus(kas: Kas) {
        val kasId = kas.id
        if (kasId.isNullOrBlank()) {
            _uiState.value = KasUiState.Error("Kas id is missing")
            return
        }
        viewModelScope.launch {
            try {
                if (kas.is_active) {
                    repository.deactivateKas(kasId)
                } else {
                    repository.activateKas(kasId)
                }
                loadKas()
            } catch (e: Exception) {
                _uiState.value = KasUiState.Error(e.message ?: "Failed to update kas status")
            }
        }
    }
}
