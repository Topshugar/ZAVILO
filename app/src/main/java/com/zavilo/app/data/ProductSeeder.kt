package com.zavilo.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ProductSeeder {
    fun seedIfEmpty(context: Context) {
        val repo = ProductRepository.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val all = repo.getAll()
            if (all.isEmpty()) {
                val samples = listOf(
                    Product(sku = "SKU001", name = "Blue Widget", keywords = "blue,widget,small", priceMinor = 250000, deliveryInfo = "Delivery ₦500, 2-3 days"),
                    Product(sku = "SKU002", name = "Red Widget", keywords = "red,widget,medium", priceMinor = 350000, deliveryInfo = "Delivery ₦700, 2-4 days"),
                    Product(sku = "SKU003", name = "Green Widget", keywords = "green,widget,large", priceMinor = 450000, deliveryInfo = "Delivery ₦900, 3-5 days")
                )
                for (p in samples) repo.insert(p)
            }
        }
    }
}
