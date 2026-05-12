package com.example.pointofsales.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String? = null,
    val description: String,
    @SerialName("total")
    val amount: Double,
    val kas_id: String,
    val status: String = "active",
    val created_at: String? = null
) {
    val is_cancelled: Boolean
        get() = status == "cancelled"
}
