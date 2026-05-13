package com.example.pointofsales.data

import com.example.pointofsales.model.Customer
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CustomerRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getCustomers(): List<Customer> = withContext(Dispatchers.IO) {
        postgrest["customer"].select().decodeList<Customer>()
    }

    suspend fun registerCustomer(name: String, phone: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "register_customer",
            parameters = buildJsonObject {
                put("p_name", name)
                put("p_phone", phone)
            }
        )
    }

    suspend fun updateCustomer(id: String, name: String, phone: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "update_customer_profile",
            parameters = buildJsonObject {
                put("p_customer_id", id)
                put("p_name", name)
                put("p_phone", phone)
            }
        )
    }

    suspend fun deactivateCustomer(id: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "deactivate_customer",
            parameters = buildJsonObject {
                put("p_customer_id", id)
            }
        )
    }

    suspend fun activateCustomer(id: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "activate_customer",
            parameters = buildJsonObject {
                put("p_customer_id", id)
            }
        )
    }
}
