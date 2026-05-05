package com.example.pointofsales.data

import com.example.pointofsales.model.Product
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    private val postgrest = SupabaseClientProvider.client.postgrest

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        postgrest["product"].select().decodeList<Product>()
    }

    suspend fun addProduct(product: Product) = withContext(Dispatchers.IO) {
        postgrest["product"].insert(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        postgrest["product"].update(product) {
            filter {
                eq("id", product.id ?: "")
            }
        }
    }

    suspend fun deleteProduct(id: String) = withContext(Dispatchers.IO) {
        postgrest["product"].delete {
            filter {
                eq("id", id)
            }
        }
    }
}
