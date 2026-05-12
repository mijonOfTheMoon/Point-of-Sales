package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.KasUiState
import com.example.pointofsales.viewmodel.KasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasScreen(
    viewModel: KasViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAdjustDialog by remember { mutableStateOf<Kas?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kas Management") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState) {
                is KasUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is KasUiState.Error -> Text((uiState as KasUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is KasUiState.Success -> {
                    val list = (uiState as KasUiState.Success).kasList
                    LazyColumn {
                        items(list) { kas ->
                            KasItem(
                                kas = kas,
                                onAdjust = { showAdjustDialog = it },
                                onToggle = { viewModel.toggleKasStatus(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    showAdjustDialog?.let { kas ->
        var amount by remember { mutableStateOf("") }
        var reason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAdjustDialog = null },
            title = { Text("Manual Adjustment - ${kas.name}") },
            text = {
                Column {
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (can be negative)") })
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.manualAdjustment(kas.id ?: "", amount.toDoubleOrNull() ?: 0.0, reason)
                    showAdjustDialog = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun KasItem(kas: Kas, onAdjust: (Kas) -> Unit, onToggle: (Kas) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(kas.name, style = MaterialTheme.typography.titleMedium)
                Text("Balance: $${kas.balance}", style = MaterialTheme.typography.bodyLarge)
                Text(if (kas.is_active) "Active" else "Inactive", color = if (kas.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            Button(onClick = { onAdjust(kas) }) { Text("Adjust") }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = kas.is_active, onCheckedChange = { onToggle(kas) })
        }
    }
}
