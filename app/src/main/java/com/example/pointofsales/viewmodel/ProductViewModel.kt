package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.ProductRepository
import com.example.pointofsales.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class ProductViewModel(private val repository: ProductRepository = ProductRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val products = repository.getProducts()
                _uiState.value = ProductUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Failed to load products")
            }
        }
    }

    fun addProduct(name: String, price: Double, stock: Double) {
        viewModelScope.launch {
            try {
                repository.addProduct(Product(name = name, price = price, stock = stock))
                loadProducts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                loadProducts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(id)
                loadProducts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
