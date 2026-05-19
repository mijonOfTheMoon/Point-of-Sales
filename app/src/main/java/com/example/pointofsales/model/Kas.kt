package com.example.pointofsales.model

import kotlinx.serialization.Serializable

@Serializable
data class Kas(
    val id: String? = null,
    val name: String,
    val balance: Double = 0.0,
    val is_active: Boolean = true,
    val created_at: String? = null
)
