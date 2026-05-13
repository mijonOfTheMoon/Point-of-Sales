package com.example.pointofsales.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object Products : Screen

    @Serializable
    data object Customers : Screen

    @Serializable
    data object Sales : Screen

    @Serializable
    data object Kas : Screen

    @Serializable
    data object Expenses : Screen

    @Serializable
    data object TransactionHistory : Screen

    @Serializable
    data object Profile : Screen
}
