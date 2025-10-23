package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.Serializable

/**
 * Generic request wrapper for sync operations
 */
@Serializable
data class SyncRequest<T>(
    val list: List<T>
)

