package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "QuantityUnit",
    indices = [Index(value = ["slug"], unique = true)]
)
data class QuantityUnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val sort_order: Int = 0,
    val parent_slug: String?,
    val conversion_factor: Double = 0.0,
    val base_conversion_unit_slug: String?,
    val status_id: Int = 0,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

