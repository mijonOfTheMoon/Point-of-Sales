package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.CustomerRepository
import com.example.pointofsales.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CustomerUiState {
    data object Loading : CustomerUiState
    data class Success(val customers: List<Customer>) : CustomerUiState
    data class Error(val message: String) : CustomerUiState
}

class CustomerViewModel(private val repository: CustomerRepository = CustomerRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomerUiState>(CustomerUiState.Loading)
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomerUiState.Loading
            try {
                val customers = repository.getCustomers()
                _uiState.value = CustomerUiState.Success(customers)
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Failed to load customers")
            }
        }
    }

    fun registerCustomer(name: String, phone: String) {
        viewModelScope.launch {
            try {
                repository.registerCustomer(name, phone)
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Failed to register customer")
            }
        }
    }

    fun updateCustomer(id: String, name: String, phone: String) {
        if (id.isBlank()) {
            _uiState.value = CustomerUiState.Error("Customer id is missing")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateCustomer(id, name, phone)
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Failed to update customer")
            }
        }
    }

    fun toggleCustomerStatus(customer: Customer) {
        val customerId = customer.id
        if (customerId.isNullOrBlank()) {
            _uiState.value = CustomerUiState.Error("Customer id is missing")
            return
        }
        viewModelScope.launch {
            try {
                if (customer.is_active) {
                    repository.deactivateCustomer(customerId)
                } else {
                    repository.activateCustomer(customerId)
                }
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = CustomerUiState.Error(e.message ?: "Failed to update customer status")
            }
        }
    }
}
