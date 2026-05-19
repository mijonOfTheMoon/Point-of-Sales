package com.example.pointofsales.data

import com.example.pointofsales.BuildConfig
import com.example.pointofsales.model.AdminUser
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UserRepository {
    private val client = SupabaseClientProvider.client
    private val auth = client.auth

    suspend fun getUsers(): List<AdminUser> = withContext(Dispatchers.IO) {
        val json = request(JSONObject().put("action", "list"))
        val users = json.optJSONArray("users") ?: return@withContext emptyList()
        List(users.length()) { index -> users.getJSONObject(index).toAdminUser() }
    }

    suspend fun createUser(name: String, email: String, password: String, role: String): AdminUser = withContext(Dispatchers.IO) {
        request(
            JSONObject()
                .put("action", "create")
                .put("name", name)
                .put("email", email)
                .put("password", password)
                .put("role", role)
        ).getJSONObject("user").toAdminUser()
    }

    suspend fun updateUser(id: String, name: String, email: String, password: String?, role: String): AdminUser = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("action", "update")
            .put("id", id)
            .put("name", name)
            .put("email", email)
            .put("role", role)
        if (!password.isNullOrBlank()) payload.put("password", password)
        request(payload).getJSONObject("user").toAdminUser()
    }

    suspend fun updateOwnProfile(name: String?, email: String?, password: String?): AdminUser = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("action", "self_update")
        if (!name.isNullOrBlank()) payload.put("name", name)
        if (!email.isNullOrBlank()) payload.put("email", email)
        if (!password.isNullOrBlank()) payload.put("password", password)
        request(payload).getJSONObject("user").toAdminUser()
    }

    suspend fun deleteUser(id: String) = withContext(Dispatchers.IO) {
        request(JSONObject().put("action", "delete").put("id", id))
    }

    private fun request(payload: JSONObject): JSONObject {
        val session = auth.currentSessionOrNull() ?: throw IllegalStateException("No authenticated user")
        val connection = URL("${BuildConfig.SUPABASE_URL}/functions/v1/admin-users").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY)
        connection.setRequestProperty("Authorization", "Bearer ${session.accessToken}")

        connection.outputStream.use { stream ->
            stream.write(payload.toString().toByteArray(Charsets.UTF_8))
        }

        val responseText = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }

        val json = if (responseText.isBlank()) JSONObject() else JSONObject(responseText)
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException(json.optString("error", "Request failed"))
        }
        return json
    }

    private fun JSONObject.toAdminUser(): AdminUser {
        return AdminUser(
            id = getString("id"),
            email = optString("email"),
            name = optString("name"),
            role = optString("role", "cashier"),
            created_at = nullableString("created_at"),
            updated_at = nullableString("updated_at"),
            last_sign_in_at = nullableString("last_sign_in_at")
        )
    }

    private fun JSONObject.nullableString(key: String): String? {
        return if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    }
}
