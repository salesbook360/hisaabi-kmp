package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TransactionDetail",
    indices = [Index(value = ["transaction_slug", "product_slug", "recipe_slug"], unique = true)]
)
data class TransactionDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val transaction_slug: String?,
    val flat_tax: Double = 0.0,
    val tax_type: Int = 0,
    val flat_discount: Double = 0.0,
    val discount_type: Int = 0,
    val product_slug: String?,
    val description: String?,
    val recipe_slug: String = "0",
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val quantity_unit_slug: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

