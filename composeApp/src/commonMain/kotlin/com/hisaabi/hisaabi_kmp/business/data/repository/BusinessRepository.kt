package com.hisaabi.hisaabi_kmp.business.data.repository

import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessRemoteDataSource
import com.hisaabi.hisaabi_kmp.business.data.model.toRequest
import com.hisaabi.hisaabi_kmp.business.data.model.toDomainModel
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BusinessRepository(
    private val remoteDataSource: BusinessRemoteDataSource
) {
    fun getAllBusinesses(): Flow<List<Business>> = flow {
        try {
            val response = remoteDataSource.getAllBusinesses()
            
            // Check for error response
            if (response.statusCode != null) {
                val errorMessage = response.message ?: "Failed to fetch businesses"
                throw Exception(errorMessage)
            }
            
            // Extract businesses from response
            val businesses = response.data?.list?.map { it.toDomainModel() } ?: emptyList()
            emit(businesses)
        } catch (e: ResponseException) {
            println("Get All Businesses ResponseException: ${e.message}")
            throw Exception(e.message ?: "Failed to fetch businesses")
        } catch (e: Exception) {
            println("Get All Businesses Exception: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    suspend fun getBusinessById(id: Int): Business? {
        return try {
            val response = remoteDataSource.getAllBusinesses()
            response.data?.list?.find { it.id == id }?.toDomainModel()
        } catch (e: Exception) {
            println("Get Business By ID Exception: ${e.message}")
            null
        }
    }
    
    suspend fun getBusinessBySlug(slug: String): Business? {
        return try {
            val response = remoteDataSource.getAllBusinesses()
            response.data?.list?.find { it.slug == slug }?.toDomainModel()
        } catch (e: Exception) {
            println("Get Business By Slug Exception: ${e.message}")
            null
        }
    }
    
    suspend fun insertBusiness(business: Business): Result<Long> {
        return try {
            val request = business.toRequest()
            val response = remoteDataSource.createBusiness(request)
            
            // Check for error response
            if (response.statusCode != null) {
                val errorMessage = response.message ?: "Failed to create business"
                return Result.failure(Exception(errorMessage))
            }
            
            // Get the created business ID from response
            val createdBusiness = response.data?.list?.firstOrNull()
            if (createdBusiness != null) {
                Result.success(createdBusiness.id.toLong())
            } else {
                Result.failure(Exception("No business data received from server"))
            }
        } catch (e: ResponseException) {
            println("Create Business ResponseException: ${e.message}")
            Result.failure(Exception(e.message ?: "Failed to create business"))
        } catch (e: Exception) {
            println("Create Business Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun updateBusiness(business: Business): Result<Unit> {
        return try {
            val request = business.toRequest()
            val response = remoteDataSource.updateBusiness(request)
            
            // Check for error response
            if (response.statusCode != null) {
                val errorMessage = response.message ?: "Failed to update business"
                return Result.failure(Exception(errorMessage))
            }
            
            Result.success(Unit)
        } catch (e: ResponseException) {
            println("Update Business ResponseException: ${e.message}")
            Result.failure(Exception(e.message ?: "Failed to update business"))
        } catch (e: Exception) {
            println("Update Business Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun deleteBusiness(business: Business): Result<Unit> {
        return try {
            // Slug is required for deletion
            if (business.slug.isNullOrBlank()) {
                return Result.failure(Exception("Business slug is required for deletion"))
            }
            
            val response = remoteDataSource.deleteBusiness(business.slug)
            
            // Check for error response
            if (response.statusCode != null) {
                val errorMessage = response.message ?: "Failed to delete business"
                return Result.failure(Exception(errorMessage))
            }
            
            Result.success(Unit)
        } catch (e: ResponseException) {
            println("Delete Business ResponseException: ${e.message}")
            Result.failure(Exception(e.message ?: "Failed to delete business"))
        } catch (e: Exception) {
            println("Delete Business Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun deleteBusinessById(id: Int): Result<Unit> {
        return try {
            // First get the business to find its slug
            val business = getBusinessById(id)
            if (business == null) {
                return Result.failure(Exception("Business not found"))
            }
            
            if (business.slug.isNullOrBlank()) {
                return Result.failure(Exception("Business slug is required for deletion"))
            }
            
            deleteBusiness(business)
        } catch (e: Exception) {
            println("Delete Business By ID Exception: ${e.message}")
            Result.failure(e)
        }
    }
}

