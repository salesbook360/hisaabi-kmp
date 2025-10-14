package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.BusinessDao
import com.hisaabi.hisaabi_kmp.database.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

interface BusinessLocalDataSource {
    fun getAllBusinesses(): Flow<List<BusinessEntity>>
    suspend fun getBusinessById(id: Int): BusinessEntity?
    suspend fun getBusinessBySlug(slug: String): BusinessEntity?
    suspend fun insertBusiness(business: BusinessEntity): Long
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)
    suspend fun updateBusiness(business: BusinessEntity)
    suspend fun deleteBusiness(business: BusinessEntity)
    suspend fun deleteBusinessById(id: Int)
    suspend fun deleteAllBusinesses()
}

class BusinessLocalDataSourceImpl(
    private val businessDao: BusinessDao
) : BusinessLocalDataSource {
    override fun getAllBusinesses(): Flow<List<BusinessEntity>> = businessDao.getAllBusinesses()
    
    override suspend fun getBusinessById(id: Int): BusinessEntity? = businessDao.getBusinessById(id)
    
    override suspend fun getBusinessBySlug(slug: String): BusinessEntity? = businessDao.getBusinessBySlug(slug)
    
    override suspend fun insertBusiness(business: BusinessEntity): Long = 
        businessDao.insertBusiness(business)
    
    override suspend fun insertBusinesses(businesses: List<BusinessEntity>) = 
        businessDao.insertBusinesses(businesses)
    
    override suspend fun updateBusiness(business: BusinessEntity) = 
        businessDao.updateBusiness(business)
    
    override suspend fun deleteBusiness(business: BusinessEntity) = 
        businessDao.deleteBusiness(business)
    
    override suspend fun deleteBusinessById(id: Int) = 
        businessDao.deleteBusinessById(id)
    
    override suspend fun deleteAllBusinesses() = 
        businessDao.deleteAllBusinesses()
}

