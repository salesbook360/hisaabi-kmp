package com.hisaabi.hisaabi_kmp.business.data.datasource

import com.hisaabi.hisaabi_kmp.database.dao.UserAuthDao
import kotlinx.coroutines.flow.Flow

/**
 * Data source for managing business-related preferences, such as selected business.
 */
interface BusinessPreferencesDataSource {
    suspend fun getSelectedBusinessSlug(): String?
    suspend fun setSelectedBusinessSlug(businessSlug: String?)
    fun observeSelectedBusinessSlug(): Flow<String?>
}

class BusinessPreferencesDataSourceImpl(
    private val userAuthDao: UserAuthDao
) : BusinessPreferencesDataSource {
    
    // In-memory cache for fast synchronous access in interceptors
    @Volatile private var cachedBusinessSlug: String? = null
    
    override suspend fun getSelectedBusinessSlug(): String? {
        return userAuthDao.getSelectedBusinessSlug()
    }
    
    override suspend fun setSelectedBusinessSlug(businessSlug: String?) {
        userAuthDao.updateSelectedBusinessSlug(businessSlug)
        // Update cache
        cachedBusinessSlug = businessSlug
    }
    
    override fun observeSelectedBusinessSlug(): Flow<String?> {
        return userAuthDao.observeSelectedBusinessSlug()
    }
    
    /**
     * Get business slug synchronously from cache.
     * This is used by request interceptors to avoid blocking.
     */
    fun getSelectedBusinessSlugSync(): String? {
        return cachedBusinessSlug
    }
}

