package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionItemInput(
    val product_id: String,
    val quantity: Double,
    val price_at_sale: Double
)
