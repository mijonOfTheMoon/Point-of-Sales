package com.example.pointofsales.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import com.example.pointofsales.model.UserProfile
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

    suspend fun getUserRole(): String {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentSessionOrNull()?.user ?: return@withContext "cashier"
                val profile = client.postgrest.from("profiles")
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }.decodeSingle<UserProfile>()
                profile.role
            } catch (e: Exception) {
                "cashier"
            }
        }
    }

    fun getCurrentSession() = auth.currentSessionOrNull()

    fun isUserLoggedIn(): Boolean {
        return auth.currentSessionOrNull() != null
    }
}
