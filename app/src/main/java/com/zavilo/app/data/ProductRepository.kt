package com.zavilo.app.data

import android.content.Context

class ProductRepository private constructor(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.productDao()

    suspend fun insert(product: Product) = dao.insert(product)
    suspend fun update(product: Product) = dao.update(product)
    suspend fun delete(id: Int) = dao.deleteById(id)
    suspend fun getBySku(sku: String) = dao.getBySku(sku)
    suspend fun findByKeyword(keyword: String) = dao.findByKeyword(keyword)
    suspend fun getAll() = dao.getAll()

    companion object {
        @Volatile private var INSTANCE: ProductRepository? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) { INSTANCE ?: ProductRepository(context.applicationContext).also { INSTANCE = it } }
    }
}
