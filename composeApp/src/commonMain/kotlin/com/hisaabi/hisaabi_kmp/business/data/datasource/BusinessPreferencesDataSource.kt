package com.hisaabi.hisaabi_kmp.business.data.datasource

import com.hisaabi.hisaabi_kmp.database.dao.UserAuthDao
import kotlinx.coroutines.flow.Flow

/**
 * Data source for managing business-related preferences, such as selected business.
 */
interface BusinessPreferencesDataSource {
    suspend fun getSelectedBusinessId(): Int?
    suspend fun setSelectedBusinessId(businessId: Int?)
    fun observeSelectedBusinessId(): Flow<Int?>
}

class BusinessPreferencesDataSourceImpl(
    private val userAuthDao: UserAuthDao
) : BusinessPreferencesDataSource {
    
    override suspend fun getSelectedBusinessId(): Int? {
        return userAuthDao.getSelectedBusinessId()
    }
    
    override suspend fun setSelectedBusinessId(businessId: Int?) {
        userAuthDao.updateSelectedBusinessId(businessId)
    }
    
    override fun observeSelectedBusinessId(): Flow<Int?> {
        return userAuthDao.observeSelectedBusinessId()
    }
}

