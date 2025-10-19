package com.hisaabi.hisaabi_kmp.business.data.repository

import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.database.datasource.BusinessLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class BusinessRepository(
    private val localDataSource: BusinessLocalDataSource
) {
    fun getAllBusinesses(): Flow<List<Business>> {
        return localDataSource.getAllBusinesses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getBusinessById(id: Int): Business? {
        return localDataSource.getBusinessById(id)?.toDomainModel()
    }
    
    suspend fun getBusinessBySlug(slug: String): Business? {
        return localDataSource.getBusinessBySlug(slug)?.toDomainModel()
    }
    
    suspend fun insertBusiness(business: Business): Result<Long> {
        return try {
            val entity = business.toEntity()
            val id = localDataSource.insertBusiness(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBusiness(business: Business): Result<Unit> {
        return try {
            val entity = business.toEntity()
            localDataSource.updateBusiness(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteBusiness(business: Business): Result<Unit> {
        return try {
            val entity = business.toEntity()
            localDataSource.deleteBusiness(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteBusinessById(id: Int): Result<Unit> {
        return try {
            localDataSource.deleteBusinessById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension functions for mapping
    private fun BusinessEntity.toDomainModel(): Business {
        return Business(
            id = id,
            title = title ?: "",
            email = email,
            address = address,
            phone = phone,
            logo = logo,
            slug = slug
        )
    }
    
    private fun Business.toEntity(): BusinessEntity {
        return BusinessEntity(
            id = id,
            title = title,
            email = email,
            address = address,
            phone = phone,
            logo = logo,
            slug = slug
        )
    }
}

