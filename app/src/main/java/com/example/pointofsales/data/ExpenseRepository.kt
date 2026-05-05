package com.example.pointofsales.data

import com.example.pointofsales.model.Expense
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ExpenseRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getExpenses(): List<Expense> = withContext(Dispatchers.IO) {
        postgrest["expense"].select().decodeList<Expense>()
    }

    suspend fun createExpense(description: String, amount: Double, kasId: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "create_expense",
            parameters = buildJsonObject {
                put("p_description", description)
                put("p_amount", amount)
                put("p_kas_id", kasId)
            }
        )
    }

    suspend fun cancelExpense(expenseId: String) = withContext(Dispatchers.IO) {
        postgrest.rpc(
            function = "cancel_expense",
            parameters = buildJsonObject {
                put("p_expense_id", expenseId)
            }
        )
    }
}
