package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.Expense
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.ExpenseUiState
import com.example.pointofsales.viewmodel.ExpenseViewModel
import com.example.pointofsales.viewmodel.KasUiState
import com.example.pointofsales.viewmodel.KasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    kasViewModel: KasViewModel,
    onBack: () -> Unit
) {
    val uiState by expenseViewModel.uiState.collectAsState()
    val kasState by kasViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracking") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (uiState) {
                is ExpenseUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ExpenseUiState.Error -> Text((uiState as ExpenseUiState.Error).message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is ExpenseUiState.Success -> {
                    val list = (uiState as ExpenseUiState.Success).expenses
                    LazyColumn {
                        items(list) { expense ->
                            ExpenseItem(expense = expense, onCancel = { expenseViewModel.cancelExpense(it.id ?: "") })
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var description by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var selectedKas by remember { mutableStateOf<Kas?>(null) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Expense") },
            text = {
                Column {
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                    
                    Text("Pay from Kas", style = MaterialTheme.typography.labelMedium)
                    if (kasState is KasUiState.Success) {
                        val kasList = (kasState as KasUiState.Success).kasList.filter { it.is_active }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(selectedKas?.name ?: "Select Kas")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                kasList.forEach { kas ->
                                    DropdownMenuItem(text = { Text(kas.name) }, onClick = { selectedKas = kas; expanded = false })
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedKas?.id?.let {
                        expenseViewModel.createExpense(description, amount.toDoubleOrNull() ?: 0.0, it)
                        showAddDialog = false
                    }
                }, enabled = selectedKas != null) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense, onCancel: (Expense) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.description, style = MaterialTheme.typography.titleMedium)
                Text("Amount: $${expense.amount}", style = MaterialTheme.typography.bodyLarge)
                if (expense.is_cancelled) {
                    Text("CANCELLED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (!expense.is_cancelled) {
                TextButton(onClick = { onCancel(expense) }) { Text("Cancel", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
