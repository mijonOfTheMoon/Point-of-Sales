package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pointofsales.model.DashboardSummary
import com.example.pointofsales.viewmodel.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale


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
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val dashboardState  by dashboardViewModel.uiState.collectAsState()
    val authState       by authViewModel.checkState.collectAsState()
    val customersState  by customerViewModel.uiState.collectAsState()
    val kasState        by kasViewModel.uiState.collectAsState()
    val productsState   by productViewModel.uiState.collectAsState()

    val role = (authState as? AuthCheckState.Authenticated)?.role ?: ""

    val cs = MaterialTheme.colorScheme

    // The header spans behind the status bar; only the body uses innerPadding at the bottom.
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            // ── Dark header (extends behind status bar) ───────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting(),
                            fontSize = 12.sp,
                            color = cs.onPrimary.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "POS Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(cs.inversePrimary)
                            .clickable(onClick = onNavigateToProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = role.take(2).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (role) {
                    "admin", "supervisor" -> {
                        when (val state = dashboardState) {
                            is DashboardUiState.Loading ->
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(80.dp),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator(color = cs.onPrimary) }

                            is DashboardUiState.Error ->
                                Text(
                                    text = state.message,
                                    color = cs.onPrimary.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )

                            is DashboardUiState.Success ->
                                AdminStatGrid(state.summary, cs)
                        }
                    }
                    "cashier" -> {
                        val activeCust   = (customersState as? CustomerUiState.Success)?.customers?.count { it.is_active } ?: 0
                        val availableKas = (kasState as? KasUiState.Success)?.kasList?.count { it.is_active } ?: 0
                        HeaderStatRow(
                            HeaderStatItem("Active Customers", activeCust.toString(),   Icons.Default.People),
                            HeaderStatItem("Available Kas",    availableKas.toString(), Icons.Default.AccountBalanceWallet),
                            cs
                        )
                    }
                    "stocker" -> {
                        val total    = (productsState as? ProductUiState.Success)?.products?.size ?: 0
                        val lowStock = (productsState as? ProductUiState.Success)?.products?.count { it.stock <= 5.0 } ?: 0
                        HeaderStatRow(
                            HeaderStatItem("Total Products", total.toString(),    Icons.Default.Inventory),
                            HeaderStatItem("Low Stock",      lowStock.toString(), Icons.Default.Warning),
                            cs
                        )
                    }
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cs.primaryContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = "MAIN MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = cs.onSurface.copy(alpha = 0.45f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (role in listOf("admin", "supervisor", "cashier")) {
                        item {
                            MenuCard(
                                title    = "Sales",
                                subtitle = "Manage transactions",
                                icon     = Icons.Default.ShoppingCart,
                                iconBg   = cs.primaryContainer,
                                iconTint = cs.inversePrimary,
                                onClick  = onNavigateToSales
                            )
                        }
                    }
                    if (role in listOf("admin", "supervisor", "stocker")) {
                        item {
                            MenuCard(
                                title    = "Products",
                                subtitle = "Manage inventory",
                                icon     = Icons.Default.Inventory,
                                iconBg   = Color(0xFFE1F5EE),
                                iconTint = Color(0xFF0F6E56),
                                onClick  = onNavigateToProducts
                            )
                        }
                    }
                    if (role in listOf("admin", "supervisor", "cashier")) {
                        item {
                            MenuCard(
                                title    = "Customers",
                                subtitle = "Customer records",
                                icon     = Icons.Default.People,
                                iconBg   = cs.primaryContainer,
                                iconTint = cs.primary,
                                onClick  = onNavigateToCustomers
                            )
                        }
                        item {
                            MenuCard(
                                title    = "Kas",
                                subtitle = "Cash management",
                                icon     = Icons.Default.AccountBalanceWallet,
                                iconBg   = Color(0xFFFAEEDA),
                                iconTint = Color(0xFF854F0B),
                                onClick  = onNavigateToKas
                            )
                        }
                        item {
                            MenuCard(
                                title    = "Expenses",
                                subtitle = "Record expenses",
                                icon     = Icons.Default.Payments,
                                iconBg   = cs.errorContainer,
                                iconTint = cs.error,
                                onClick  = onNavigateToExpenses
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun greeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Good morning 👋"
        in 12..14 -> "Good afternoon 👋"
        in 15..17 -> "Good evening 👋"
        else      -> "Good night 👋"
    }
}

private data class HeaderStatItem(val label: String, val value: String, val icon: ImageVector)

@Composable
private fun HeaderStatRow(left: HeaderStatItem, right: HeaderStatItem, cs: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HeaderStatCard(left,  cs, Modifier.weight(1f))
        HeaderStatCard(right, cs, Modifier.weight(1f))
    }
}

@Composable
private fun HeaderStatCard(item: HeaderStatItem, cs: ColorScheme, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cs.onPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = cs.onPrimary.copy(alpha = 0.55f)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(item.label, fontSize = 11.sp, color = cs.onPrimary.copy(alpha = 0.55f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = cs.onPrimary)
        }
    }
}

@Composable
private fun AdminStatGrid(summary: DashboardSummary, cs: ColorScheme) {
    val fmt = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderStatCard(HeaderStatItem("Total Sales",  fmt.format(summary.total_sales_value), Icons.AutoMirrored.Filled.TrendingUp),   cs, Modifier.weight(1f))
            HeaderStatCard(HeaderStatItem("Expenses",     fmt.format(summary.total_expenses),     Icons.AutoMirrored.Filled.TrendingDown), cs, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderStatCard(HeaderStatItem("Transactions", summary.total_transactions.toString(), Icons.Default.Receipt), cs, Modifier.weight(1f))
            HeaderStatCard(HeaderStatItem("Cash in Kas",  fmt.format(summary.total_cash_active), Icons.Default.Money),  cs, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuCard(
    title:    String,
    subtitle: String,
    icon:     ImageVector,
    iconBg:   Color,
    iconTint: Color,
    onClick:  () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = iconTint)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title,    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = cs.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurface.copy(alpha = 0.5f))
        }
    }
}