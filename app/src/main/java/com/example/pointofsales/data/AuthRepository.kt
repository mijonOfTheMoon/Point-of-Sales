package com.example.pointofsales.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.status.SessionStatus
import com.example.pointofsales.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    suspend fun signUp(name: String, email: String, password: String) = withContext(Dispatchers.IO) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("name", name)
            }
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

    suspend fun getCurrentUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentSessionOrNull()?.user ?: return@withContext null
            client.postgrest.from("profiles")
                .select {
                    filter { eq("id", user.id) }
                }.decodeSingle<UserProfile>()
        } catch (_: Exception) {
            null
        }
    }

    fun getUserEmail(): String {
        return auth.currentSessionOrNull()?.user?.email ?: ""
    }

    suspend fun updateProfile(
        newName: String?,
        newEmail: String?,
        newPassword: String?
    ) = withContext(Dispatchers.IO) {
        val userId = auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("No authenticated user")

        if (!newName.isNullOrBlank()) {
            client.postgrest.from("profiles").update(
                { set("name", newName) }
            ) {
                filter { eq("id", userId) }
            }
        }

        if (!newEmail.isNullOrBlank()) {
            auth.updateUser { email = newEmail }
        }

        if (!newPassword.isNullOrBlank()) {
            auth.updateUser { password = newPassword }
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        val status = auth.sessionStatus.first {
            it is SessionStatus.Authenticated || it is SessionStatus.NotAuthenticated
        }
        return status is SessionStatus.Authenticated
    }
}
