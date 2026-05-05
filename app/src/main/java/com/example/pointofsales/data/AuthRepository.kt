package com.example.pointofsales.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    suspend fun signUp(email: String, password: String) = withContext(Dispatchers.IO) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        auth.signOut()
    }

    fun getCurrentSession() = auth.currentSessionOrNull()

    fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }
}
