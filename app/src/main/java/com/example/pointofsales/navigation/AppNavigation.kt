package com.example.pointofsales.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pointofsales.ui.*
import com.example.pointofsales.viewmodel.*

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val checkState by authViewModel.checkState.collectAsState()

    if (checkState is AuthCheckState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val backStack = rememberNavBackStack(
        when (checkState) {
            is AuthCheckState.Authenticated -> Screen.Dashboard
            else -> Screen.Login
        }
    )

    when (checkState) {
        is AuthCheckState.Authenticated -> {
            if (backStack.isNotEmpty() && backStack.last() !is Screen.Dashboard &&
                backStack.last() !is Screen.Products && backStack.last() !is Screen.Customers &&
                backStack.last() !is Screen.Sales && backStack.last() !is Screen.Kas &&
                backStack.last() !is Screen.Expenses && backStack.last() !is Screen.TransactionHistory) {
                backStack.clear()
                backStack.add(Screen.Dashboard)
            }
        }
        AuthCheckState.Unauthenticated -> {
            if (backStack.isNotEmpty() && (backStack.last() is Screen.Dashboard ||
                backStack.last() is Screen.Products || backStack.last() is Screen.Customers ||
                backStack.last() is Screen.Sales || backStack.last() is Screen.Kas ||
                backStack.last() is Screen.Expenses || backStack.last() is Screen.TransactionHistory)) {
                backStack.clear()
                backStack.add(Screen.Login)
            }
        }
        else -> {}
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Screen.Login> {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { backStack.add(Screen.Register) }
                )
            }
            entry<Screen.Register> {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.Dashboard> {
                val dashboardViewModel: DashboardViewModel = viewModel()
                val customerViewModel: CustomerViewModel = viewModel()
                val kasViewModel: KasViewModel = viewModel()
                val productViewModel: ProductViewModel = viewModel()
                DashboardScreen(
                    authViewModel = authViewModel,
                    dashboardViewModel = dashboardViewModel,
                    customerViewModel = customerViewModel,
                    kasViewModel = kasViewModel,
                    productViewModel = productViewModel,
                    onNavigateToProducts = { backStack.add(Screen.Products) },
                    onNavigateToCustomers = { backStack.add(Screen.Customers) },
                    onNavigateToSales = { backStack.add(Screen.Sales) },
                    onNavigateToKas = { backStack.add(Screen.Kas) },
                    onNavigateToExpenses = { backStack.add(Screen.Expenses) },
                    onLogout = { authViewModel.signOut() }
                )
            }
            entry<Screen.Products> {
                val productViewModel: ProductViewModel = viewModel()
                ProductScreen(
                    viewModel = productViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.Customers> {
                val customerViewModel: CustomerViewModel = viewModel()
                CustomerScreen(
                    viewModel = customerViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.Sales> {
                val salesViewModel: SalesViewModel = viewModel()
                val customerViewModel: CustomerViewModel = viewModel()
                val kasViewModel: KasViewModel = viewModel()
                SalesScreen(
                    salesViewModel = salesViewModel,
                    customerViewModel = customerViewModel,
                    kasViewModel = kasViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToHistory = { backStack.add(Screen.TransactionHistory) }
                )
            }
            entry<Screen.Kas> {
                val kasViewModel: KasViewModel = viewModel()
                KasScreen(
                    viewModel = kasViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.Expenses> {
                val expenseViewModel: ExpenseViewModel = viewModel()
                val kasViewModel: KasViewModel = viewModel()
                ExpenseScreen(
                    expenseViewModel = expenseViewModel,
                    kasViewModel = kasViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Screen.TransactionHistory> {
                val salesViewModel: SalesViewModel = viewModel()
                val kasViewModel: KasViewModel = viewModel()
                TransactionHistoryScreen(
                    salesViewModel = salesViewModel,
                    kasViewModel = kasViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
