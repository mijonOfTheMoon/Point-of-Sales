package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.Customer
import com.example.pointofsales.model.Kas
import com.example.pointofsales.model.Product
import com.example.pointofsales.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    salesViewModel: SalesViewModel,
    customerViewModel: CustomerViewModel,
    kasViewModel: KasViewModel,
    onBack: () -> Unit
) {
    val products by salesViewModel.products.collectAsState()
    val cart by salesViewModel.cart.collectAsState()
    val uiState by salesViewModel.uiState.collectAsState()
    
    val customersState by customerViewModel.uiState.collectAsState()
    val kasState by kasViewModel.uiState.collectAsState()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedKas by remember { mutableStateOf<Kas?>(null) }
    var paidAmount by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val total = cart.sumOf { it.product.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sale") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Product List
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Text("Products", style = MaterialTheme.typography.titleLarge)
                LazyColumn {
                    items(products) { product ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name)
                                    Text("$${product.price}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { salesViewModel.addToCart(product) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                }
                            }
                        }
                    }
                }
            }
            
            // Cart
            Column(modifier = Modifier.weight(0.6f).padding(8.dp).fillMaxHeight()) {
                Text("Cart", style = MaterialTheme.typography.titleLarge)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cart) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${item.product.name} x${item.quantity}", modifier = Modifier.weight(1f))
                            Text("$${item.product.price * item.quantity}")
                        }
                    }
                }
                Divider()
                Text("Total: $${total}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showCheckoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = cart.isNotEmpty()
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checkout")
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Checkout") },
            text = {
                Column {
                    Text("Total: $${total}")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Kas Selection
                    Text("Select Kas", style = MaterialTheme.typography.labelMedium)
                    if (kasState is KasUiState.Success) {
                        val kasList = (kasState as KasUiState.Success).kasList.filter { it.is_active }
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(selectedKas?.name ?: "Select Kas")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                kasList.forEach { kas ->
                                    DropdownMenuItem(
                                        text = { Text(kas.name) },
                                        onClick = {
                                            selectedKas = kas
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Customer Selection (Optional)
                    Text("Select Customer (Optional)", style = MaterialTheme.typography.labelMedium)
                    if (customersState is CustomerUiState.Success) {
                        val customerList = (customersState as CustomerUiState.Success).customers.filter { it.is_active }
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(selectedCustomer?.name ?: "No Customer")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(text = { Text("No Customer") }, onClick = { selectedCustomer = null; expanded = false })
                                customerList.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text(customer.name) },
                                        onClick = {
                                            selectedCustomer = customer
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { paidAmount = it },
                        label = { Text("Amount Paid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedKas?.id?.let { kasId ->
                            salesViewModel.processSale(kasId, selectedCustomer?.id, paidAmount.toDoubleOrNull() ?: total)
                            showCheckoutDialog = false
                        }
                    },
                    enabled = selectedKas != null
                ) {
                    Text("Pay")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is SalesUiState.Success) {
            // Optional toast or snackbar
        }
    }
}
