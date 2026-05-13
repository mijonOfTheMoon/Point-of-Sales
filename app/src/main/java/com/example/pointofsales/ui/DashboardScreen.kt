package com.example.pointofsales.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

    val role = (authState as? AuthCheckState.Authenticated)?.role  ?: ""
    val name = (authState as? AuthCheckState.Authenticated)?.name  ?: ""

    val cs = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primary)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting(),
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onPrimary.copy(alpha = 0.55f)
                        )
                        Text(
                            text = if (name.isNotBlank()) name else "POS Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
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
                            text = (if (name.isNotBlank()) name else role).take(2).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            HeaderStatItem("Active Customers", abbreviateNumber(activeCust), Icons.Default.People),
                            HeaderStatItem("Available Kas",    abbreviateNumber(availableKas), Icons.Default.AccountBalanceWallet),
                            cs
                        )
                    }
                    "stocker" -> {
                        val total    = (productsState as? ProductUiState.Success)?.products?.size ?: 0
                        val lowStock = (productsState as? ProductUiState.Success)?.products?.count { it.stock <= 5.0 } ?: 0
                        HeaderStatRow(
                            HeaderStatItem("Total Products", abbreviateNumber(total), Icons.Default.Inventory),
                            HeaderStatItem("Low Stock",      abbreviateNumber(lowStock), Icons.Default.Warning),
                            cs
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.primaryContainer)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "MAIN MENU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = cs.onSurface.copy(alpha = 0.45f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val menuItems = buildList {
                    if (role in listOf("admin", "supervisor", "cashier")) {
                        add(MenuDef("Sales", "Manage transactions", Icons.Default.ShoppingCart, cs.primaryContainer, cs.inversePrimary, onNavigateToSales))
                    }
                    if (role in listOf("admin", "supervisor", "stocker")) {
                        add(MenuDef("Products", "Manage inventory", Icons.Default.Inventory, Color(0xFFE1F5EE), Color(0xFF0F6E56), onNavigateToProducts))
                    }
                    if (role in listOf("admin", "supervisor", "cashier")) {
                        add(MenuDef("Customers", "Customer records", Icons.Default.People, cs.primaryContainer, cs.primary, onNavigateToCustomers))
                        add(MenuDef("Kas", "Cash management", Icons.Default.AccountBalanceWallet, Color(0xFFFAEEDA), Color(0xFF854F0B), onNavigateToKas))
                        add(MenuDef("Expenses", "Record expenses", Icons.Default.Payments, cs.errorContainer, cs.error, onNavigateToExpenses))
                    }
                }

                menuItems.chunked(2).forEachIndexed { idx, row ->
                    if (idx > 0) Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                MenuCard(title = item.title, subtitle = item.subtitle, icon = item.icon, iconBg = item.iconBg, iconTint = item.iconTint, onClick = item.onClick)
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun greeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11  -> "Good morning,"
        in 12..14 -> "Good afternoon,"
        in 15..17 -> "Good evening,"
        else      -> "Good night,"
    }
}

private data class HeaderStatItem(val label: String, val value: String, val icon: ImageVector)
private data class MenuDef(val title: String, val subtitle: String, val icon: ImageVector, val iconBg: Color, val iconTint: Color, val onClick: () -> Unit)

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
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cs.onPrimary.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 14.dp)
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
                Text(item.label, fontSize = 11.sp, color = cs.onPrimary.copy(alpha = 0.55f), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = cs.onPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun AdminStatGrid(summary: DashboardSummary, cs: ColorScheme) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderStatCard(HeaderStatItem("Total Sales",  "Rp${abbreviateNumber(summary.total_sales_value)}", Icons.AutoMirrored.Filled.TrendingUp),   cs, Modifier.weight(1f))
            HeaderStatCard(HeaderStatItem("Expenses",     "Rp${abbreviateNumber(summary.total_expenses)}",     Icons.AutoMirrored.Filled.TrendingDown), cs, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeaderStatCard(HeaderStatItem("Transactions", abbreviateNumber(summary.total_transactions), Icons.Default.Receipt), cs, Modifier.weight(1f))
            HeaderStatCard(HeaderStatItem("Cash in Kas",  "Rp${abbreviateNumber(summary.total_cash_active)}", Icons.Default.Money),  cs, Modifier.weight(1f))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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