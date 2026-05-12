package com.example.pointofsales.data

import com.example.pointofsales.model.TransactionItemInput
import com.example.pointofsales.model.TransactionWithItems
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class SalesRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getTransactions(kasId: String): List<TransactionWithItems> = withContext(Dispatchers.IO) {
        postgrest["transaction"].select(columns = Columns.raw("*, transaction_item(*)")) {
            filter {
                eq("kas_id", kasId)
            }
            order("sold_at", Order.DESCENDING)
        }.decodeList<TransactionWithItems>()
    }

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
