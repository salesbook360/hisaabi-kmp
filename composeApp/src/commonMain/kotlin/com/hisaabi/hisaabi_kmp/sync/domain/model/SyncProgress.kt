package com.hisaabi.hisaabi_kmp.sync.domain.model

/**
 * Represents sync progress for UI updates
 */
data class SyncProgress(
    val recordType: String,
    val completed: Int,
    val total: Int,
    val syncType: SyncType
) {
    val percentage: Float
        get() = if (total > 0) (completed.toFloat() / total.toFloat()) * 100 else 0f
}

enum class SyncType {
    SYNC_UP,    // Local to cloud
    SYNC_DOWN   // Cloud to local
}

