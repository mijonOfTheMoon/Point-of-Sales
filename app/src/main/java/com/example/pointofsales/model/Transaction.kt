package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val customer_id: String? = null,
    val kas_id: String,
    val sold_at: String,
    val total: Double,
    val paid: Double,
    val change_amount: Double,
    val status: String
)

@Serializable
data class TransactionCustomer(
    val name: String
)

@Serializable
data class TransactionWithItems(
    val id: String,
    val customer_id: String? = null,
    val customer: TransactionCustomer? = null,
    val kas_id: String,
    val sold_at: String,
    val total: Double,
    val paid: Double,
    val change_amount: Double,
    val status: String,
    val transaction_item: List<TransactionItemDetail>
)

@Serializable
data class TransactionItemDetail(
    val id: String,
    val transaction_id: String,
    val product_id: String,
    val product_name: String,
    val unit_price: Double,
    val quantity: Double,
    val subtotal: Double
)
