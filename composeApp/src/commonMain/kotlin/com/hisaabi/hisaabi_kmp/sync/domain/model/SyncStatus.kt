package com.hisaabi.hisaabi_kmp.sync.domain.model

/**
 * Enum representing sync status of entities
 */
enum class SyncStatus(val value: Int) {
    NONE(1),        // Not synced yet
    SYNCED(2),      // Synced successfully
    UPDATED(3);     // Updated locally, needs to be synced again
    
    companion object {
        fun fromValue(value: Int): SyncStatus {
            return values().find { it.value == value } ?: NONE
        }
    }
}

