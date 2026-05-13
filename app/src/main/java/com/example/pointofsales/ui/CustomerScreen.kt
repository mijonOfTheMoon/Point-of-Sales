package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.Customer
import com.example.pointofsales.viewmodel.CustomerUiState
import com.example.pointofsales.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    val cs = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = cs.primary,
                contentColor = cs.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Customers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val count = (uiState as? CustomerUiState.Success)?.customers?.size ?: 0
                val active = (uiState as? CustomerUiState.Success)?.customers?.count { it.is_active } ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Total", abbreviateNumber(count), Modifier.weight(1f), cs)
                    SubHeaderStat("Active", abbreviateNumber(active), Modifier.weight(1f), cs)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is CustomerUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                    is CustomerUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as CustomerUiState.Error).message, color = cs.error)
                    }
                    is CustomerUiState.Success -> {
                        val customers = (uiState as CustomerUiState.Success).customers
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
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
    }

    if (showAddSheet) {
        CustomerSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, phone -> viewModel.registerCustomer(name, phone); showAddSheet = false }
        )
    }
    customerToEdit?.let { customer ->
        CustomerSheet(
            customer = customer,
            onDismiss = { customerToEdit = null },
            onConfirm = { name, phone -> viewModel.updateCustomer(customer.id ?: "", name, phone); customerToEdit = null }
        )
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onEdit: (Customer) -> Unit,
    onToggleStatus: (Customer) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = cs.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = cs.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.55f))
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (customer.is_active) cs.secondaryContainer else cs.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (customer.is_active) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (customer.is_active) cs.onSecondaryContainer else cs.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = { onEdit(customer) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = cs.primary)
            }
            Switch(checked = customer.is_active, onCheckedChange = { onToggleStatus(customer) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSheet(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(if (customer == null) "Register Customer" else "Edit Customer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onConfirm(name, phone) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Text(if (customer == null) "Register" else "Save Changes", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
