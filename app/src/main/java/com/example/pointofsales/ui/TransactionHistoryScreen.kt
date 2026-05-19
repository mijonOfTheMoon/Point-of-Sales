package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.example.pointofsales.model.TransactionWithItems
import com.example.pointofsales.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    salesViewModel: SalesViewModel,
    onBack: () -> Unit
) {
    val transactions by salesViewModel.transactions.collectAsStateWithLifecycle()

    val formatter = remember {
        rupiahFormatter()
    }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Recent") }

    val filteredTransactions = remember(transactions, searchQuery, sortBy) {
        transactions
            .filter { transaction ->
                val customerMatch = transaction.customer?.name?.contains(searchQuery, true) == true
                val itemMatch = transaction.transaction_item.any { it.product_name.contains(searchQuery, true) }
                searchQuery.isBlank() || customerMatch || itemMatch || transaction.status.contains(searchQuery, true)
            }
            .let { list ->
                when (sortBy) {
                    "A-Z" -> list.sortedBy { it.customer?.name.orEmpty().lowercase() }
                    "Total" -> list.sortedByDescending { it.total }
                    "Status" -> list.sortedWith(compareBy<TransactionWithItems> { it.status }.thenByDescending { recentTimestamp(it.updated_at, it.created_at, it.sold_at) })
                    else -> list.sortedByDescending { recentTimestamp(it.updated_at, it.created_at, it.sold_at) }
                }
            }
    }

    LaunchedEffect(Unit) {
        salesViewModel.loadTransactions()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
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
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = cs.onPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transaction History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = cs.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val total = transactions.filter { it.status == "completed" }.sumOf { it.total }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubHeaderStat("Transactions", abbreviateNumber(transactions.size), Modifier.weight(1f), cs)
                    SubHeaderStat("Total Sales", "Rp${abbreviateNumber(total)}", Modifier.weight(1f), cs)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SearchSortBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        sortLabel = sortBy,
                        sortOptions = listOf("Recent", "A-Z", "Total", "Status"),
                        onSortChange = { sortBy = it },
                        placeholder = "Search history"
                    )
                }
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (transactions.isEmpty()) "No transactions yet" else "No matching transactions",
                                style = MaterialTheme.typography.titleMedium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredTransactions) { transaction ->
                        TransactionCard(transaction = transaction, formatter = formatter)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: TransactionWithItems,
    formatter: NumberFormat
) {
    val dateTime = remember(transaction.sold_at) {
        try {
            ZonedDateTime.parse(transaction.sold_at)
                .withZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
        } catch (_: Exception) {
            transaction.sold_at
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = transaction.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (transaction.customer != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.customer.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                transaction.transaction_item.forEach { item ->
                    val qty = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString()
                              else item.quantity.toString()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${qty}x ${item.product_name}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatter.format(item.subtotal),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatter.format(transaction.total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
