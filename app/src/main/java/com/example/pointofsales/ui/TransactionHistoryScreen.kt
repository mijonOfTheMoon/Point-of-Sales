package com.example.pointofsales.ui

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
import java.text.NumberFormat
import java.util.Locale
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.example.pointofsales.model.TransactionWithItems
import com.example.pointofsales.viewmodel.SalesViewModel
import com.example.pointofsales.viewmodel.KasViewModel
import com.example.pointofsales.viewmodel.KasUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    salesViewModel: SalesViewModel,
    kasViewModel: KasViewModel,
    onBack: () -> Unit
) {
    val transactions by salesViewModel.transactions.collectAsState()
    val kasState by kasViewModel.uiState.collectAsState()

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    LaunchedEffect(kasState) {
        if (kasState is KasUiState.Success) {
            val kasList = (kasState as KasUiState.Success).kasList.filter { it.is_active }
            if (kasList.isNotEmpty()) {
                salesViewModel.loadTransactions(kasList.first().id ?: "")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Belum ada transaksi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Transaksi yang berhasil akan muncul di sini",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(transactions) { transaction ->
                    TransactionCard(transaction = transaction, formatter = formatter)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
