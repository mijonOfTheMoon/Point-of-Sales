package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import com.example.pointofsales.model.Customer
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    salesViewModel: SalesViewModel,
    customerViewModel: CustomerViewModel,
    kasViewModel: KasViewModel,
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    val products by salesViewModel.products.collectAsState()
    val cart by salesViewModel.cart.collectAsState()
    val uiState by salesViewModel.uiState.collectAsState()
    
    val customersState by customerViewModel.uiState.collectAsState()
    val kasState by kasViewModel.uiState.collectAsState()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedKas by remember { mutableStateOf<Kas?>(null) }

    val isCheckoutMode = remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build()).apply { maximumFractionDigits = 0 } }
    val total = cart.sumOf { it.product.price * it.quantity }

    if (!isCheckoutMode.value) {
        // --- 1. PRODUCT MENU MODE ---
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Menu Kasir", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (onLogout != null) {
                            IconButton(onClick = { showLogoutDialog.value = true }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (cart.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 16.dp
                    ) {
                        Button(
                            onClick = { isCheckoutMode.value = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Proses Pesanan →", style = MaterialTheme.typography.titleMedium)
                                Text(formatter.format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Bar Placeholder
                item {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        placeholder = { Text("Cari Menu") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(products) { product ->
                    val cartItem = cart.find { it.product.id == product.id }
                    val qty = cartItem?.quantity?.toInt() ?: 0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (qty > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                             else MaterialTheme.colorScheme.surface,
                        ),
                        border = if (qty > 0) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Info Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatter.format(product.price), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val stockDisplay = if (product.stock % 1.0 == 0.0) product.stock.toInt().toString() else product.stock.toString()
                                Surface(
                                    color = if (product.stock > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (product.stock > 0) "Sisa Stok: $stockDisplay" else "Stok Habis",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (product.stock > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Controls Row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (qty > 0) {
                                    IconButton(
                                        onClick = {
                                            if (qty > 1) salesViewModel.addToCart(product, -1.0)
                                            else salesViewModel.removeFromCart(product.id ?: "")
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }

                                    Text(
                                        text = qty.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { salesViewModel.addToCart(product, 1.0) },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- 2. CHECKOUT MODE ---
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Pesanan Checkout", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { isCheckoutMode.value = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pembayaran", style = MaterialTheme.typography.titleMedium)
                            Text(formatter.format(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                // Simplified: if kas is selected auto-pay, else show simple validation toast (to be handled)
                                selectedKas?.id?.let { kasId ->
                                    salesViewModel.processSale(kasId, selectedCustomer?.id, total) // default auto paid full
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = cart.isNotEmpty() && selectedKas != null && uiState !is SalesUiState.Loading
                        ) {
                            if (uiState is SalesUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Proses Pembayaran →", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section Summary Cards
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Kas Selection Outline
                            Text("Kas Penerima", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (kasState is KasUiState.Success) {
                                val kasList = (kasState as KasUiState.Success).kasList.filter { it.is_active }
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    TextButton(
                                        onClick = { expanded = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(selectedKas?.name ?: "Pilih Kas", style = MaterialTheme.typography.titleMedium)
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        kasList.forEach { kas ->
                                            DropdownMenuItem(text = { Text(kas.name) }, onClick = { selectedKas = kas; expanded = false })
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // Customer Selection
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Nama Customer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (customersState is CustomerUiState.Success) {
                                        val customerList = (customersState as CustomerUiState.Success).customers.filter { it.is_active }
                                        var expanded by remember { mutableStateOf(false) }
                                        Box {
                                            Text(
                                                text = selectedCustomer?.name ?: "Pilih (Opsional)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 2.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            // Make invisible button overlay for click
                                            Surface(modifier = Modifier.matchParentSize(), color = androidx.compose.ui.graphics.Color.Transparent, onClick = { expanded = true }) {}

                                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                                DropdownMenuItem(text = { Text("Kosongkan") }, onClick = { selectedCustomer = null; expanded = false })
                                                DropdownMenuItem(
                                                    text = { Text("Tambah Pelanggan Baru...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) },
                                                    onClick = { showAddCustomerDialog = true; expanded = false }
                                                )
                                                customerList.forEach { customer ->
                                                    DropdownMenuItem(text = { Text(customer.name) }, onClick = { selectedCustomer = customer; expanded = false })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Detail Menu
                item {
                    Text("Menu Pesanan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }

                items(cart) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatter.format(item.product.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (item.quantity > 1) salesViewModel.addToCart(item.product, -1.0)
                                    else salesViewModel.removeFromCart(item.product.id ?: "")
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text(item.quantity.toInt().toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { salesViewModel.addToCart(item.product, 1.0) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detail Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatter.format(total), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(100.dp)) // padding for bottom bar
                }
            }
        }
    }

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari Kasir?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog.value = false
                        onLogout?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAddCustomerDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Pelanggan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Nama Pelanggan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCustomerPhone,
                        onValueChange = { newCustomerPhone = it },
                        label = { Text("Nomor Telepon (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerName.isNotBlank()) {
                            customerViewModel.registerCustomer(newCustomerName, newCustomerPhone)
                            newCustomerName = ""
                            newCustomerPhone = ""
                            // Note: customerViewModel.loadCustomers() will run and selectedCustomer can be assigned later manually by the cashier.
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Simpan", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Batal")
                }
            }
        )
    }

    if (uiState is SalesUiState.Success) {
        AlertDialog(
            onDismissRequest = { }, // Force click button to clear
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
            title = { Text("Transaksi Berhasil", textAlign = TextAlign.Center) },
            text = { Text("Pembayaran sebesar ${formatter.format(total)} telah diterima.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        salesViewModel.resetUiState()
                        salesViewModel.clearCart() // explicitly clearing cart just to be safe
                        selectedCustomer = null
                        isCheckoutMode.value = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Kembali ke Kasir")
                }
            }
        )
    }

    if (uiState is SalesUiState.Error) {
        AlertDialog(
            onDismissRequest = { salesViewModel.resetUiState() },
            title = { Text("Terjadi Kesalahan") },
            text = { Text("Silakan coba lagi nanti.") },
            confirmButton = {
                Button(
                    onClick = { salesViewModel.resetUiState() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}
