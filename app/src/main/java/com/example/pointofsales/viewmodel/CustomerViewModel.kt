package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.CustomerRepository
import com.example.pointofsales.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CustomerUiState {
    object Loading : CustomerUiState()
    data class Success(val customers: List<Customer>) : CustomerUiState()
    data class Error(val message: String) : CustomerUiState()
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
                // Handle error
            }
        }
    }

    fun updateCustomer(id: String, name: String, phone: String) {
        viewModelScope.launch {
            try {
                repository.updateCustomer(id, name, phone)
                loadCustomers()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleCustomerStatus(customer: Customer) {
        viewModelScope.launch {
            try {
                if (customer.is_active) {
                    repository.deactivateCustomer(customer.id ?: "")
                } else {
                    repository.activateCustomer(customer.id ?: "")
                }
                loadCustomers()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
