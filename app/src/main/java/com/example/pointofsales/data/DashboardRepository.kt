package com.example.pointofsales.data

import com.example.pointofsales.model.DashboardSummary
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getDashboardSummary(): DashboardSummary = withContext(Dispatchers.IO) {
        postgrest.rpc("get_dashboard_summary").decodeSingle<DashboardSummary>()
    }
}
