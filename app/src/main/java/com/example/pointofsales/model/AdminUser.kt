package com.example.pointofsales.model

data class AdminUser(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val created_at: String? = null,
    val updated_at: String? = null,
    val last_sign_in_at: String? = null
)
