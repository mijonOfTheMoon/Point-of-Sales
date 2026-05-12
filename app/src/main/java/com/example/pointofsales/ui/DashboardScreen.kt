package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pointofsales.model.DashboardSummary
import com.example.pointofsales.viewmodel.AuthViewModel
import com.example.pointofsales.viewmodel.DashboardUiState
import com.example.pointofsales.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToKas: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("POS Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { dashboardViewModel.loadSummary() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    TextButton(onClick = {
                        authViewModel.signOut()
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (dashboardState) {
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Text(
                        text = (dashboardState as DashboardUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                is DashboardUiState.Success -> {
                    SummaryGrid((dashboardState as DashboardUiState.Success).summary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    MenuCard("Sales", Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primaryContainer, onNavigateToSales)
                }
                item {
                    MenuCard("Products", Icons.Default.Inventory, MaterialTheme.colorScheme.secondaryContainer, onNavigateToProducts)
                }
                item {
                    MenuCard("Customers", Icons.Default.People, MaterialTheme.colorScheme.tertiaryContainer, onNavigateToCustomers)
                }
                item {
                    MenuCard("Kas", Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.surfaceVariant, onNavigateToKas)
                }
                item {
                    MenuCard("Expenses", Icons.Default.Payments, MaterialTheme.colorScheme.errorContainer, onNavigateToExpenses)
                }
            }
        }
    }
}

@Composable
fun SummaryGrid(summary: DashboardSummary) {
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Total Sales", formatter.format(summary.total_sales_value), Icons.AutoMirrored.Filled.TrendingUp, Modifier.weight(1f))
        SummaryCard("Expenses", formatter.format(summary.total_expenses), Icons.AutoMirrored.Filled.TrendingDown, Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Transactions", summary.total_transactions.toString(), Icons.Default.Receipt, Modifier.weight(1f))
        SummaryCard("Cash in Kas", formatter.format(summary.total_cash_active), Icons.Default.Money, Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(title: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.height(100.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
        }
    }
}
