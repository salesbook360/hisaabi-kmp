package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Product",
    indices = [Index(value = ["slug"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val type_id: Int = 0,
    val tax_percentage: Double = 0.0,
    val discount_percentage: Double = 0.0,
    val retail_price: Double = 0.0,
    val wholesale_price: Double = 0.0,
    val thumbnail: String?,
    val purchase_price: Double = 0.0,
    val status_id: Int = 0,
    val digital_id: String?,
    val base_unit_slug: String?,
    val default_unit_slug: String?,
    val minimum_quantity_unit_slug: String?,
    val opening_quantity_unit_slug: String?,
    val category_slug: String?,
    val avg_purchase_price: Double = 0.0,
    val opening_quantity_purchase_price: Double = 0.0,
    val expiry_date: String?,
    val expiry_alert: String?,
    val manufacturer: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

