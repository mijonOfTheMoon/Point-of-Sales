package com.example.pointofsales.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class DashboardSummary(
    val total_transactions: Long = 0,
    val total_sales_value: Double = 0.0,
    @SerialName("total_active_expenses")
    val total_expenses: Double = 0.0,
    @SerialName("total_active_kas_balance")
    val total_cash_active: Double = 0.0,
    val total_active_products: Long = 0
)
