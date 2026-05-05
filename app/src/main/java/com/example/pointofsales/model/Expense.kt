package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String? = null,
    val description: String,
    val amount: Double,
    val kas_id: String,
    val is_cancelled: Boolean = false,
    val created_at: String? = null
)
