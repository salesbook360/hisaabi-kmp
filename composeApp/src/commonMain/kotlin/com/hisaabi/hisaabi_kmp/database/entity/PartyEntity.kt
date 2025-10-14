package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Party",
    indices = [Index(value = ["slug"], unique = true)]
)
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String?,
    val phone: String?,
    val address: String?,
    val balance: Double = 0.0,
    val opening_balance: Double = 0.0,
    val thumbnail: String?,
    val role_id: Int = 0,
    val person_status: Int = 1,
    val digital_id: String?,
    val lat_long: String?,
    val area_slug: String?,
    val category_slug: String?,
    val email: String?,
    val description: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

