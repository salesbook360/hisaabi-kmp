package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DeletedRecords",
    indices = [Index(value = ["slug"], unique = true)]
)
data class DeletedRecordsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val record_slug: String?,
    val record_type: String?,
    val deletion_type: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

