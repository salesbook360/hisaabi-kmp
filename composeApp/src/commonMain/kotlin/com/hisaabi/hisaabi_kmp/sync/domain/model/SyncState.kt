package com.hisaabi.hisaabi_kmp.sync.domain.model

import kotlinx.datetime.Instant

/**
 * Represents the current state of synchronization
 */
sealed class SyncState {
    data object Idle : SyncState()
    data object InProgress : SyncState()
    data class Progress(
        val progress: SyncProgress
    ) : SyncState()
    data class Success(
        val lastSyncTime: Instant
    ) : SyncState()
    data class Error(
        val message: String,
        val syncType: SyncType? = null
    ) : SyncState()
}

