package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Business",
    indices = [Index(value = ["slug"], unique = true)]
)
data class BusinessEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val email: String?,
    val address: String?,
    val phone: String?,
    val logo: String?,
    val slug: String?
)

