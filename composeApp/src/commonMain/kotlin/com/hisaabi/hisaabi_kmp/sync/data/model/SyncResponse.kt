package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic response wrapper for sync operations
 */
@Serializable
data class SyncResponse<T>(
    val data: SyncData<T>? = null,
    @SerialName("status")
    val statusCode: Int? = null,
    val message: String? = null,
    val timestamp: String? = null
) {
    fun isSuccess(): Boolean = statusCode == 200
}

@Serializable
data class SyncData<T>(
    val resultCount: String? = null,
    val list: List<T>? = null
)

