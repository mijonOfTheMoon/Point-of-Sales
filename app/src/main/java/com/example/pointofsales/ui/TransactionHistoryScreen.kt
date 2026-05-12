package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build()).apply { maximumFractionDigits = 0 }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(transactions) { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val dateTime = try {
                                    ZonedDateTime.parse(transaction.sold_at).withZoneSameInstant(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
                                } catch (_: Exception) {
                                    transaction.sold_at
                                }
                                Text(dateTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(
                                    color = if (transaction.status == "completed") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = transaction.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            transaction.transaction_item.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.quantity.toInt()}x ${item.product_name}", style = MaterialTheme.typography.bodyMedium)
                                    Text(formatter.format(item.subtotal), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(formatter.format(transaction.total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
