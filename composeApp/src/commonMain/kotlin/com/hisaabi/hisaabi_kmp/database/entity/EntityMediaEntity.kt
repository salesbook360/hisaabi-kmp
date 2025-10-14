package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "EntityMedia",
    indices = [Index(value = ["slug"], unique = true)]
)
data class EntityMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entity_name: String?,
    val entity_slug: String?,
    val local_url: String?,
    val url: String?,
    val media_type: String?,
    val action_required: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

