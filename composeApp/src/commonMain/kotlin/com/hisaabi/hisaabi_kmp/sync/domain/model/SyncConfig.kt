package com.hisaabi.hisaabi_kmp.sync.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Configuration constants for sync mechanism
 */
object SyncConfig {
    /**
     * Minimum interval between automatic sync operations
     * Default: 2 minutes
     */
    val SYNC_INTERVAL: Duration = 2.minutes
    
    /**
     * Total number of record types to sync up
     */
    const val TOTAL_SYNC_UP_TYPES = 10
    
    /**
     * Total number of record types to sync down
     */
    const val TOTAL_SYNC_DOWN_TYPES = 12
    
    /**
     * Batch size for syncing records
     */
    const val SYNC_BATCH_SIZE = 50
}

