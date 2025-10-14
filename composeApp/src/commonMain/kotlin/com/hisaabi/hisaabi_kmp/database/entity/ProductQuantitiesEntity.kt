package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ProductQuantities",
    indices = [Index(value = ["product_slug", "warehouse_slug"], unique = true)]
)
data class ProductQuantitiesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val product_slug: String?,
    val warehouse_slug: String?,
    val opening_quantity: Double = 0.0,
    val current_quantity: Double = 0.0,
    val minimum_quantity: Double = 0.0,
    val maximum_quantity: Double = 0.0,
    val business_slug: String?,
    val sync_status: Int = 0
)

