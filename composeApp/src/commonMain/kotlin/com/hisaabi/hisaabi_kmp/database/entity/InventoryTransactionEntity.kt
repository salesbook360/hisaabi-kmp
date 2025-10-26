package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "InventoryTransaction",
    indices = [Index(value = ["slug"], unique = true)]
)
data class InventoryTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customer_slug: String?,
    val parent_slug: String?,
    val total_bill: Double = 0.0,
    val total_paid: Double = 0.0,
    val timestamp: String?,
    val discount: Double = 0.0,
    val payment_method_to_slug: String?,
    val payment_method_from_slug: String?,
    val transaction_type: Int = 0,
    val price_type_id: Int = 0,
    val additional_charges_slug: String?,
    val additional_charges_desc: String?,
    val additional_charges: Double = 0.0,
    val discount_type_id: Int = 0,
    val tax_type_id: Int = 0,
    val tax: Double = 0.0,
    val description: String?,
    val shipping_address: String?,
    val status_id: Int = 0, // 0=Active, 2=Deleted
    val state_id: Int = 0,
    val remind_at_milliseconds: Long = 0,
    val ware_house_slug_from: String?,
    val ware_house_slug_to: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

