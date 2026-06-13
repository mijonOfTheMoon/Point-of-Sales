package com.example.pointofsales.data

import com.example.pointofsales.model.Kas
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class KasRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getKas(): List<Kas> = withContext(Dispatchers.IO) {
        postgrest["kas"].select().decodeList<Kas>()
    }

    suspend fun createKas(name: String, initialBalance: Double) = withContext(Dispatchers.IO) {
        postgrest["kas"].insert(
            buildJsonObject {
                put("name", name)
                put("balance", initialBalance)
            }
        )
    }

    suspend fun manualAdjustment(kasId: String, amount: Double, reason: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "manual_kas_adjustment",
            parameters = buildJsonObject {
                put("p_kas_id", kasId)
                put("p_balance_change", amount)
                put("p_description", reason)
            }
        )
    }

    suspend fun activateKas(kasId: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "activate_kas",
            parameters = buildJsonObject {
                put("p_kas_id", kasId)
            }
        )
    }

    suspend fun deactivateKas(kasId: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "deactivate_kas",
            parameters = buildJsonObject {
                put("p_kas_id", kasId)
            }
        )
    }
}
