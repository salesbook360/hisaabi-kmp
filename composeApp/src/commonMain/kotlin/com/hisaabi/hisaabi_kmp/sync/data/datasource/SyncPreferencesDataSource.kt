package com.hisaabi.hisaabi_kmp.sync.data.datasource

import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Data source for managing sync-related preferences
 */
interface SyncPreferencesDataSource {
    suspend fun getLastSyncTime(businessSlug: String, userSlug: String): Instant?
    suspend fun setLastSyncTime(businessSlug: String, userSlug: String, time: Instant)
    fun observeLastSyncTime(businessSlug: String, userSlug: String): Flow<Instant?>
    suspend fun shouldSync(businessSlug: String, userSlug: String, intervalMillis: Long): Boolean
}

class SyncPreferencesDataSourceImpl(
    private val preferencesManager: PreferencesManager
) : SyncPreferencesDataSource {
    
    companion object {
        private const val LAST_SYNC_TIME_PREFIX = "last_sync_time"
        
        private fun getSyncTimeKey(businessSlug: String, userSlug: String): String {
            return "${LAST_SYNC_TIME_PREFIX}_${userSlug}_${businessSlug}"
        }
    }
    
    override suspend fun getLastSyncTime(businessSlug: String, userSlug: String): Instant? {
        val key = getSyncTimeKey(businessSlug, userSlug)
        val timeMillis = preferencesManager.getLong(key, 0L)
        return if (timeMillis > 0) {
            Instant.fromEpochMilliseconds(timeMillis)
        } else {
            null
        }
    }
    
    override suspend fun setLastSyncTime(businessSlug: String, userSlug: String, time: Instant) {
        val key = getSyncTimeKey(businessSlug, userSlug)
        preferencesManager.setLong(key, time.toEpochMilliseconds())
    }
    
    override fun observeLastSyncTime(businessSlug: String, userSlug: String): Flow<Instant?> {
        val key = getSyncTimeKey(businessSlug, userSlug)
        return preferencesManager.observeLong(key, 0L).map { timeMillis ->
            if (timeMillis > 0) {
                Instant.fromEpochMilliseconds(timeMillis)
            } else {
                null
            }
        }
    }
    
    override suspend fun shouldSync(
        businessSlug: String,
        userSlug: String,
        intervalMillis: Long
    ): Boolean {
        val lastSyncTime = getLastSyncTime(businessSlug, userSlug) ?: return true
        val now = Clock.System.now()
        val elapsedMillis = (now - lastSyncTime).inWholeMilliseconds
        return elapsedMillis >= intervalMillis
    }
}

