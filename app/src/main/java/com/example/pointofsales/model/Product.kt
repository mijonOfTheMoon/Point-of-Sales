package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String? = null,
    val name: String,
    val price: Double,
    val stock: Double,
    val is_active: Boolean = true,
    val created_at: String? = null
)
