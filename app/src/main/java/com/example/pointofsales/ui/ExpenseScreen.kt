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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pointofsales.model.Expense
import com.example.pointofsales.model.Kas
import com.example.pointofsales.viewmodel.ExpenseUiState
import com.example.pointofsales.viewmodel.ExpenseViewModel
import com.example.pointofsales.viewmodel.KasUiState
import com.example.pointofsales.viewmodel.KasViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    kasViewModel: KasViewModel,
    onBack: () -> Unit
) {
    val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
    val kasState by kasViewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Recent") }
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
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
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
                    Text("Expenses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val expenses = (uiState as? ExpenseUiState.Success)?.expenses ?: emptyList()
                val totalActive = expenses.filter { !it.is_cancelled }.sumOf { it.amount }
                val count = expenses.count { !it.is_cancelled }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Records", abbreviateNumber(count), Modifier.weight(1f), cs)
                    SubHeaderStat("Total Spent", "Rp${abbreviateNumber(totalActive)}", Modifier.weight(1f), cs)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is ExpenseUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = cs.primary)
                    }
                    is ExpenseUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as ExpenseUiState.Error).message, color = cs.error)
                    }
                    is ExpenseUiState.Success -> {
                        val list = (uiState as ExpenseUiState.Success).expenses
                            .filter { it.description.contains(searchQuery, true) || it.status.contains(searchQuery, true) }
                            .let { list ->
                                when (sortBy) {
                                    "A-Z" -> list.sortedBy { it.description.lowercase() }
                                    "Amount" -> list.sortedByDescending { it.amount }
                                    "Status" -> list.sortedWith(compareBy<Expense> { it.status }.thenByDescending { it.created_at.orEmpty() })
                                    else -> list.sortedByDescending { it.created_at.orEmpty() }
                                }
                            }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            item {
                                SearchSortBar(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    sortLabel = sortBy,
                                    sortOptions = listOf("Recent", "A-Z", "Amount", "Status"),
                                    onSortChange = { sortBy = it },
                                    placeholder = "Search expenses"
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            items(list) { expense ->
                                ExpenseItem(expense = expense, fmt = fmt, onCancel = { expenseViewModel.cancelExpense(it.id ?: "") })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        val kasList = (kasState as? KasUiState.Success)?.kasList?.filter { it.is_active } ?: emptyList()
        ExpenseSheet(
            kasList = kasList,
            onDismiss = { showAddSheet = false },
            onConfirm = { description, amount, kasId ->
                expenseViewModel.createExpense(description, amount, kasId)
                showAddSheet = false
            }
        )
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    fmt: NumberFormat,
    onCancel: (Expense) -> Unit
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
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (expense.is_cancelled) cs.errorContainer.copy(alpha = 0.4f) else cs.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = if (expense.is_cancelled) cs.error.copy(alpha = 0.4f) else cs.error, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (expense.is_cancelled) cs.onSurface.copy(alpha = 0.4f) else cs.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    fmt.format(expense.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (expense.is_cancelled) cs.onSurface.copy(alpha = 0.35f) else cs.error
                )
                if (expense.is_cancelled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = cs.errorContainer, shape = RoundedCornerShape(4.dp)) {
                        Text("Cancelled", style = MaterialTheme.typography.labelSmall, color = cs.onErrorContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            if (!expense.is_cancelled) {
                TextButton(onClick = { onCancel(expense) }) {
                    Text("Cancel", color = cs.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSheet(
    kasList: List<Kas>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedKas by remember { mutableStateOf<Kas?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("New Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = cs.primary)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedKas?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pay from Kas") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    kasList.forEach { kas ->
                        DropdownMenuItem(
                            text = { Text(kas.name) },
                            onClick = { selectedKas = kas; expanded = false }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { selectedKas?.id?.let { onConfirm(description, amount.toDoubleOrNull() ?: 0.0, it) } },
                enabled = selectedKas != null && description.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Record Expense", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
