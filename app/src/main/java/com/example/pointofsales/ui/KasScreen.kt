package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.KasUiState
import com.example.pointofsales.viewmodel.KasViewModel
import java.text.NumberFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    viewModel: KasViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAdjustSheet by remember { mutableStateOf<Kas?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("A-Z") }
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
                Icon(Icons.Default.Add, contentDescription = "Add Kas")
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
                    Text("Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val totalBalance = (uiState as? KasUiState.Success)?.kasList?.filter { it.is_active }?.sumOf { it.balance } ?: 0.0
                val activeCount = (uiState as? KasUiState.Success)?.kasList?.count { it.is_active } ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Active Kas", abbreviateNumber(activeCount), Modifier.weight(1f), cs)
                    SubHeaderStat("Total Balance", "Rp${abbreviateNumber(totalBalance)}", Modifier.weight(1f), cs)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is KasUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                    is KasUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as KasUiState.Error).message, color = cs.error)
                    }
                    is KasUiState.Success -> {
                        val list = (uiState as KasUiState.Success).kasList
                            .filter { it.name.contains(searchQuery, true) }
                            .let { list ->
                                when (sortBy) {
                                    "Recent" -> list.sortedByDescending { recentTimestamp(it.updated_at, it.created_at) }
                                    "Balance" -> list.sortedByDescending { it.balance }
                                    "Status" -> list.sortedWith(compareByDescending<Kas> { it.is_active }.thenBy { it.name.lowercase() })
                                    else -> list.sortedBy { it.name.lowercase() }
                                }
                            }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            item {
                                SearchSortBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    sortLabel = sortBy,
                                    sortOptions = listOf("A-Z", "Recent", "Balance", "Status"),
                                    onSortChange = { sortBy = it },
                                    placeholder = "Search kas"
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            items(list) { kas ->
                                KasItem(
                                    kas = kas,
                                    fmt = fmt,
                                    onAdjust = { showAdjustSheet = it },
                                    onToggle = { viewModel.toggleKasStatus(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showAddSheet.let { show ->
        if (show) {
            KasAddSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { name, initialBalance ->
                    viewModel.createKas(name, initialBalance)
                    showAddSheet = false
                }
            )
        }
    }

    showAdjustSheet?.let { kas ->
        KasAdjustSheet(
            kas = kas,
            onDismiss = { showAdjustSheet = null },
            onSave = { newName, newBalance, reason ->
                if (newName != kas.name) {
                    viewModel.updateKasName(kas.id ?: "", newName)
                }
                if (abs(newBalance - kas.balance) > 0.000001) {
                    val description = reason.ifBlank {
                        "Balance edited from ${kas.balance.toAmountInput()} to ${newBalance.toAmountInput()}"
                    }
                    viewModel.manualAdjustment(kas.id ?: "", newBalance - kas.balance, description)
                }
                showAdjustSheet = null
            },
            onDelete = {
                viewModel.deleteKas(kas.id ?: "")
                showAdjustSheet = null
            }
        )
    }
}

@Composable
fun KasItem(
    kas: Kas,
    fmt: NumberFormat,
    onAdjust: (Kas) -> Unit,
    onToggle: (Kas) -> Unit
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
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAEEDA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF854F0B), modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(kas.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = cs.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(fmt.format(kas.balance), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = cs.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (kas.is_active) cs.secondaryContainer else cs.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (kas.is_active) "Active" else "Inactive",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (kas.is_active) cs.onSecondaryContainer else cs.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            OutlinedButton(
                onClick = { onAdjust(kas) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Adjust", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = kas.is_active, onCheckedChange = { onToggle(kas) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasAdjustSheet(
    kas: Kas,
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(kas.id) { mutableStateOf(kas.name) }
    var amount by remember(kas.id, kas.balance) { mutableStateOf(kas.balance.toAmountInput()) }
    var reason by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val newBalance = amount.toDoubleOrNull()
    val trimmedName = name.trim()
    val nameChanged = trimmedName.isNotBlank() && trimmedName != kas.name
    val balanceChanged = newBalance != null && newBalance >= 0.0 && abs(newBalance - kas.balance) > 0.000001
    val canSave = trimmedName.isNotBlank() && newBalance != null && newBalance >= 0.0 && (nameChanged || balanceChanged)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Edit - ${kas.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Update the name or balance for this kas.", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { newBalance?.let { onSave(trimmedName, it, reason) } },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Changes", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Kas", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Kas") },
            text = { Text("Are you sure you want to delete \"${kas.name}\"? This action can't be undone. Kas with transaction history can't be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete", color = cs.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasAddSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    val initialBalance = amount.toDoubleOrNull() ?: 0.0
    val isValid = name.isNotBlank() && initialBalance >= 0.0

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Add Kas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Create a new kas account.", style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Initial Balance") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name.trim(), initialBalance) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Create Kas", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

private fun Double.toAmountInput(): String {
    return if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
}
