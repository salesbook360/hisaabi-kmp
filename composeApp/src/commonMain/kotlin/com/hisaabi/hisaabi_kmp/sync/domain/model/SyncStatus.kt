package com.hisaabi.hisaabi_kmp.sync.domain.model

/**
 * Enum representing sync status of entities
 */
enum class SyncStatus(val value: Int) {
    NONE(1),        // Not synced yet
    SYNCED(2),      // Synced successfully
    UPDATED(3);     // Updated locally, needs to be synced again
    
    companion object {
        // Compile-time constant for use in Room queries
        const val SYNCED_VALUE = 2
        
        fun fromValue(value: Int): SyncStatus {
            return values().find { it.value == value } ?: NONE
        }
    }
}

