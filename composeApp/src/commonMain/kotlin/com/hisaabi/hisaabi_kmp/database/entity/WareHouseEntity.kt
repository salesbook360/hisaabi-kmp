package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "WareHouse",
    indices = [Index(value = ["slug"], unique = true)]
)
data class WareHouseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val address: String?,
    val description: String?,
    val lat_long: String?,
    val thumbnail: String?,
    val type_id: Int = 0,
    val status_id: Int = 0,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

