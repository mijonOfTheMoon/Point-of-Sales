package com.example.pointofsales.data

import com.example.pointofsales.model.TransactionItemInput
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class SalesRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun processSale(
        kasId: String,
        customerId: String?,
        items: List<TransactionItemInput>,
        paid: Double
    ) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "process_sale",
            parameters = buildJsonObject {
                put("p_kas_id", kasId)
                if (customerId != null) {
                    put("p_customer_id", customerId)
                } else {
                    put("p_customer_id", null as String?)
                }
                put("p_items", Json.encodeToJsonElement(items))
                put("p_paid", paid)
            }
        )
    }
}
