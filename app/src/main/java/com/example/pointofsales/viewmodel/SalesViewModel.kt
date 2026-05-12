package com.example.pointofsales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pointofsales.data.ProductRepository
import com.example.pointofsales.data.SalesRepository
import com.example.pointofsales.model.Product
import com.example.pointofsales.model.TransactionItemInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Double
)

sealed interface SalesUiState {
    data object Idle : SalesUiState
    data object Loading : SalesUiState
    data class Success(val message: String) : SalesUiState
    data class Error(val message: String) : SalesUiState
}

class SalesViewModel(
    private val salesRepository: SalesRepository = SalesRepository(),
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SalesUiState>(SalesUiState.Idle)
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                _products.value = productRepository.getProducts().filter { it.is_active }
            } catch (e: Exception) {
                _uiState.value = SalesUiState.Error(e.message ?: "Failed to load products")
            }
        }
    }

    fun addToCart(product: Product, quantity: Double = 1.0) {
        val currentCart = _cart.value.toMutableList()
        val existingItemIndex = currentCart.indexOfFirst { it.product.id == product.id }
        if (existingItemIndex != -1) {
            val existingItem = currentCart[existingItemIndex]
            currentCart[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentCart.add(CartItem(product, quantity))
        }
        _cart.value = currentCart
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun processSale(kasId: String, customerId: String?, paid: Double) {
        viewModelScope.launch {
            _uiState.value = SalesUiState.Loading
            try {
                val items = _cart.value.map {
                    TransactionItemInput(
                        product_id = it.product.id ?: "",
                        quantity = it.quantity,
                        price_at_sale = it.product.price
                    )
                }
                salesRepository.processSale(kasId, customerId, items, paid)
                _uiState.value = SalesUiState.Success("Sale processed successfully!")
                loadProducts()
            } catch (e: Exception) {
                _uiState.value = SalesUiState.Error(e.message ?: "Failed to process sale")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = SalesUiState.Idle
    }
}
