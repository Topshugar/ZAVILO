package com.zavilo.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getBySku(sku: String): Product?

    // Basic keyword search using LIKE on the keywords column
    @Query("SELECT * FROM products WHERE available = 1 AND keywords LIKE '%' || :keyword || '%' LIMIT 1")
    suspend fun findByKeyword(keyword: String): Product?

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAll(): List<Product>
}
