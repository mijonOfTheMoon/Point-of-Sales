package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.ExpenseRepository
import com.example.pointofsales.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExpenseUiState {
    data object Loading : ExpenseUiState
    data class Success(val expenses: List<Expense>) : ExpenseUiState
    data class Error(val message: String) : ExpenseUiState
}

class ExpenseViewModel(private val repository: ExpenseRepository = ExpenseRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = ExpenseUiState.Loading
            try {
                val list = repository.getExpenses()
                _uiState.value = ExpenseUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to load expenses")
            }
        }
    }

    fun createExpense(description: String, amount: Double, kasId: String) {
        if (kasId.isBlank()) {
            _uiState.value = ExpenseUiState.Error("Kas id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.createExpense(description, amount, kasId)
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to create expense")
            }
        }
    }

    fun cancelExpense(id: String) {
        if (id.isBlank()) {
            _uiState.value = ExpenseUiState.Error("Expense id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.cancelExpense(id)
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "Failed to cancel expense")
            }
        }
    }
}
