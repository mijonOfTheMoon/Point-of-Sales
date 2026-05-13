package com.example.pointofsales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.pointofsales.viewmodel.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    customerViewModel: CustomerViewModel,
    kasViewModel: KasViewModel,
    productViewModel: ProductViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToKas: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val authState by authViewModel.checkState.collectAsState()
    val customersState by customerViewModel.uiState.collectAsState()
    val kasState by kasViewModel.uiState.collectAsState()
    val productsState by productViewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("POS Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit", tint = MaterialTheme.colorScheme.error)
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
            val role = (authState as? AuthCheckState.Authenticated)?.role ?: ""
            if (role == "admin" || role == "supervisor") {
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
            } else if (role == "cashier") {
                val activeCustomers = (customersState as? CustomerUiState.Success)?.customers?.count { it.is_active } ?: 0
                val availableKas = (kasState as? KasUiState.Success)?.kasList?.count { it.is_active } ?: 0
                CashierInsightGrid(activeCustomers, availableKas)
            } else if (role == "stocker") {
                val totalProducts = (productsState as? ProductUiState.Success)?.products?.size ?: 0
                val lowStock = (productsState as? ProductUiState.Success)?.products?.count { it.stock <= 5.0 } ?: 0
                StockerInsightGrid(totalProducts, lowStock)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (role == "admin" || role == "supervisor" || role == "cashier") {
                    item {
                        MenuCard("Sales", Icons.Default.ShoppingCart, onNavigateToSales)
                    }
                }
                if (role == "admin" || role == "supervisor" || role == "stocker") {
                    item {
                        MenuCard("Products", Icons.Default.Inventory, onNavigateToProducts)
                    }
                }
                if (role == "admin" || role == "supervisor" || role == "cashier") {
                    item {
                        MenuCard("Customers", Icons.Default.People, onNavigateToCustomers)
                    }
                }
                if (role == "admin" || role == "supervisor" || role == "cashier") {
                    item {
                        MenuCard("Kas", Icons.Default.AccountBalanceWallet, onNavigateToKas)
                    }
                    item {
                        MenuCard("Expenses", Icons.Default.Payments, onNavigateToExpenses)
                    }
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

@Composable
fun CashierInsightGrid(activeCustomers: Int, availableKas: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Active Customers", activeCustomers.toString(), Icons.Default.People, Modifier.weight(1f))
        SummaryCard("Available Kas", availableKas.toString(), Icons.Default.AccountBalanceWallet, Modifier.weight(1f))
    }
}

@Composable
fun StockerInsightGrid(totalProducts: Int, lowStock: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Total Products", totalProducts.toString(), Icons.Default.Inventory, Modifier.weight(1f))
        SummaryCard("Low Stock Alert", lowStock.toString(), Icons.Default.Warning, Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.height(100.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
        }
    }
}
