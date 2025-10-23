package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.Serializable

/**
 * Generic response wrapper for sync operations
 */
@Serializable
data class SyncResponse<T>(
    val data: SyncData<T>? = null,
    val status: String,
    val message: String? = null,
    val timestamp: String? = null
)

@Serializable
data class SyncData<T>(
    val resultCount: String? = null,
    val list: List<T>? = null
)

