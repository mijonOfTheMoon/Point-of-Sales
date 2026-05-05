package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.Product
import com.example.pointofsales.viewmodel.ProductUiState
import com.example.pointofsales.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Management") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState) {
                is ProductUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ProductUiState.Error -> Text(
                    text = (uiState as ProductUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is ProductUiState.Success -> {
                    val products = (uiState as ProductUiState.Success).products
                    LazyColumn {
                        items(products) { product ->
                            ProductItem(
                                product = product,
                                onEdit = { productToEdit = it },
                                onDelete = { viewModel.deleteProduct(it.id ?: "") }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, stock ->
                viewModel.addProduct(name, price, stock)
                showAddDialog = false
            }
        )
    }

    productToEdit?.let { product ->
        ProductDialog(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { name, price, stock ->
                viewModel.updateProduct(product.copy(name = name, price = price, stock = stock))
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Price: $${product.price}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(product) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(product) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(name, price.toDoubleOrNull() ?: 0.0, stock.toDoubleOrNull() ?: 0.0)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
