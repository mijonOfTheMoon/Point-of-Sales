package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String? = null,
    val name: String,
    val phone: String,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)
