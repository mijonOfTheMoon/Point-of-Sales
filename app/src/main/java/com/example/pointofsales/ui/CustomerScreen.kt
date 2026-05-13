package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.Customer
import com.example.pointofsales.viewmodel.CustomerUiState
import com.example.pointofsales.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState) {
                is CustomerUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is CustomerUiState.Error -> Text(
                    text = (uiState as CustomerUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is CustomerUiState.Success -> {
                    val customers = (uiState as CustomerUiState.Success).customers
                    LazyColumn {
                        items(customers) { customer ->
                            CustomerItem(
                                customer = customer,
                                onEdit = { customerToEdit = it },
                                onToggleStatus = { viewModel.toggleCustomerStatus(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CustomerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone ->
                viewModel.registerCustomer(name, phone)
                showAddDialog = false
            }
        )
    }

    customerToEdit?.let { customer ->
        CustomerDialog(
            customer = customer,
            onDismiss = { customerToEdit = null },
            onConfirm = { name, phone ->
                viewModel.updateCustomer(customer.id ?: "", name, phone)
                customerToEdit = null
            }
        )
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onEdit: (Customer) -> Unit,
    onToggleStatus: (Customer) -> Unit
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
                Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Phone: ${customer.phone}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = if (customer.is_active) "Active" else "Inactive",
                    color = if (customer.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = { onEdit(customer) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            Switch(
                checked = customer.is_active,
                onCheckedChange = { onToggleStatus(customer) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDialog(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (customer == null) "Register Customer" else "Update Customer") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, phone) }) {
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
