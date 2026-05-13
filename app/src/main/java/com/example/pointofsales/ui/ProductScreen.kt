package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.Product
import com.example.pointofsales.viewmodel.ProductUiState
import com.example.pointofsales.viewmodel.ProductViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    val cs = MaterialTheme.colorScheme
    val fmt = remember { rupiahFormatter() }

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
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
                    Text("Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val total = (uiState as? ProductUiState.Success)?.products?.size ?: 0
                val lowStock = (uiState as? ProductUiState.Success)?.products?.count { it.stock <= 5.0 } ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Total Items", abbreviateNumber(total), Modifier.weight(1f), cs)
                    SubHeaderStat("Low Stock", abbreviateNumber(lowStock), Modifier.weight(1f), cs)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is ProductUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                    is ProductUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as ProductUiState.Error).message, color = cs.error)
                    }
                    is ProductUiState.Success -> {
                        val products = (uiState as ProductUiState.Success).products
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(products) { product ->
                                ProductItem(
                                    product = product,
                                    fmt = fmt,
                                    onEdit = { productToEdit = it },
                                    onDelete = { viewModel.deleteProduct(it.id ?: "") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        ProductSheet(
            onDismiss = { showAddSheet = false },
            onConfirm = { name, price, stock -> viewModel.addProduct(name, price, stock); showAddSheet = false }
        )
    }
    productToEdit?.let { product ->
        ProductSheet(
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { name, price, stock -> viewModel.updateProduct(product.copy(name = name, price = price, stock = stock)); productToEdit = null }
        )
    }
}

@Composable
fun ProductItem(
    product: Product,
    fmt: NumberFormat,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
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
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = cs.primary, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = cs.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(fmt.format(product.price), style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.55f))
                Spacer(modifier = Modifier.height(4.dp))
                val stockInt = if (product.stock % 1.0 == 0.0) product.stock.toInt().toString() else product.stock.toString()
                Surface(
                    color = if (product.stock > 5) cs.secondaryContainer else cs.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (product.stock > 0) "Stock: $stockInt" else "Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.stock > 5) cs.onSecondaryContainer else cs.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = { onEdit(product) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = cs.primary)
            }
            IconButton(onClick = { onDelete(product) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = cs.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(if (product == null) "Add Product" else "Edit Product", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, leadingIcon = { Icon(Icons.Default.Inventory, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, leadingIcon = { Icon(Icons.Default.Payments, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, leadingIcon = { Icon(Icons.Default.Numbers, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, stock.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Text(if (product == null) "Add Product" else "Save Changes", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
