package com.zavilo.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sku") val sku: String,
    @ColumnInfo(name = "name") val name: String,
    // simple comma-separated keywords/tags (normalize to lowercase)
    @ColumnInfo(name = "keywords") val keywords: String,
    // price in minor units (kobo/cent) to avoid float rounding
    @ColumnInfo(name = "price_minor") val priceMinor: Long,
    @ColumnInfo(name = "delivery_info") val deliveryInfo: String? = null,
    @ColumnInfo(name = "available") val available: Boolean = true,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
