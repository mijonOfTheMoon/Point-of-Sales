package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.Customer
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    salesViewModel: SalesViewModel,
    customerViewModel: CustomerViewModel,
    kasViewModel: KasViewModel,
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val products by salesViewModel.products.collectAsStateWithLifecycle()
    val cart by salesViewModel.cart.collectAsStateWithLifecycle()
    val uiState by salesViewModel.uiState.collectAsStateWithLifecycle()

    val customersState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val kasState by kasViewModel.uiState.collectAsStateWithLifecycle()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedKas by remember { mutableStateOf<Kas?>(null) }

    val isCheckoutMode = remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val formatter = remember { rupiahFormatter() }
    val total = remember(cart) { cart.sumOf { it.product.price * it.quantity } }
    val totalQuantity = remember(cart) { cart.sumOf { it.quantity.toInt() } }
    val cartQuantityByProductId = remember(cart) {
        cart.mapNotNull { item ->
            item.product.id?.let { id -> id to item.quantity }
        }.toMap()
    }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    if (!isCheckoutMode.value) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0),
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
                                Text("Process Order ->", style = MaterialTheme.typography.titleMedium)
                                Text(formatter.format(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val cs = MaterialTheme.colorScheme
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                        }
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = cs.onPrimary)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.primaryContainer)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Items") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = cs.outlineVariant,
                                    unfocusedContainerColor = cs.surface,
                                    focusedContainerColor = cs.surface
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        items(filteredProducts) { product ->
                            val currentQty = cartQuantityByProductId[product.id] ?: 0.0
                            val qty = currentQty.toInt()
                            val canIncrease = currentQty < product.stock

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cs.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(formatter.format(product.price), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val stockDisplay = if (product.stock % 1.0 == 0.0) product.stock.toInt().toString() else product.stock.toString()
                                        Surface(color = if (product.stock > 0) cs.secondaryContainer else cs.errorContainer, shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = if (product.stock > 0) "Stock left: $stockDisplay" else "Out of Stock",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (product.stock > 0) cs.onSecondaryContainer else cs.onErrorContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.width(128.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .alpha(if (qty > 0) 1f else 0f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(cs.primaryContainer)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    enabled = qty > 0
                                                ) {
                                                    if (qty > 1) salesViewModel.addToCart(product, -1.0)
                                                    else salesViewModel.removeFromCart(product.id ?: "")
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = cs.primary, modifier = Modifier.size(18.dp))
                                        }

                                        Box(
                                            modifier = Modifier.width(36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (qty > 0) {
                                                Text(
                                                    text = qty.toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cs.primary
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (canIncrease) cs.primaryContainer else cs.surfaceVariant)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    enabled = canIncrease
                                                ) { salesViewModel.addToCart(product, 1.0) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = if (canIncrease) cs.primary else cs.onSurface.copy(alpha = 0.38f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payment", style = MaterialTheme.typography.titleMedium)
                            Text(formatter.format(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                selectedKas?.id?.let { kasId ->
                                    salesViewModel.processSale(kasId, selectedCustomer?.id, total)
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
                                Text("Process Payment ->", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val cs = MaterialTheme.colorScheme
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
                        IconButton(onClick = { isCheckoutMode.value = false }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SubHeaderStat("${cart.size} Item${if (cart.size > 1) "s" else ""}", "$totalQuantity pcs", Modifier.weight(1f), cs)
                        SubHeaderStat("Order Total", formatter.format(total), Modifier.weight(1f), cs)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(cs.primaryContainer)
                ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Receive via Kas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (kasState is KasUiState.Success) {
                                val kasList = (kasState as KasUiState.Success).kasList.filter { it.is_active }
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    TextButton(
                                        onClick = { expanded = true },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(selectedKas?.name ?: "Select Kas", style = MaterialTheme.typography.titleMedium)
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        kasList.forEach { kas ->
                                            DropdownMenuItem(text = { Text(kas.name) }, onClick = { selectedKas = kas; expanded = false })
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Customer Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (customersState is CustomerUiState.Success) {
                                        val customerList = (customersState as CustomerUiState.Success).customers.filter { it.is_active }
                                        var expanded by remember { mutableStateOf(false) }
                                        Box {
                                            Text(
                                                text = selectedCustomer?.name ?: "Select",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 2.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Surface(modifier = Modifier.matchParentSize(), color = androidx.compose.ui.graphics.Color.Transparent, onClick = { expanded = true }) {}

                                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                                DropdownMenuItem(text = { Text("None") }, onClick = { selectedCustomer = null; expanded = false })
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

                item {
                    Text("Order Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }

                items(cart) { item ->
                    val canIncrease = item.quantity < item.product.stock

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
                            Spacer(modifier = Modifier.width(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    if (item.quantity > 1) salesViewModel.addToCart(item.product, -1.0)
                                    else salesViewModel.removeFromCart(item.product.id ?: "")
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text(item.quantity.toInt().toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                                IconButton(
                                    onClick = { salesViewModel.addToCart(item.product, 1.0) },
                                    enabled = canIncrease,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatter.format(total), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
                }
            }
        }
    }

    if (uiState is SalesUiState.Success) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
            title = { Text("Transaction Successful", textAlign = TextAlign.Center) },
            text = { Text("Payment of ${formatter.format(total)} has been received.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        salesViewModel.resetUiState()
                        salesViewModel.clearCart()
                        selectedCustomer = null
                        isCheckoutMode.value = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back to Cashier")
                }
            }
        )
    }

    if (uiState is SalesUiState.Error) {
        val errorMessage = (uiState as SalesUiState.Error).message
        AlertDialog(
            onDismissRequest = { salesViewModel.resetUiState() },
            title = { Text("An Error Occurred") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { salesViewModel.resetUiState() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
