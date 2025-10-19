package com.hisaabi.hisaabi_kmp.paymentmethods.data.repository

import com.hisaabi.hisaabi_kmp.database.datasource.PaymentMethodLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.PaymentMethodEntity
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class PaymentMethodsRepository(
    private val localDataSource: PaymentMethodLocalDataSource
) {
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>> {
        return localDataSource.getAllPaymentMethods().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getActivePaymentMethods(): Flow<List<PaymentMethod>> {
        return localDataSource.getPaymentMethodsByStatus(1).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getPaymentMethodById(id: Int): PaymentMethod? {
        return localDataSource.getPaymentMethodById(id)?.toDomainModel()
    }
    
    suspend fun getPaymentMethodBySlug(slug: String): PaymentMethod? {
        return localDataSource.getPaymentMethodBySlug(slug)?.toDomainModel()
    }
    
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod): Result<Long> {
        return try {
            val entity = paymentMethod.toEntity()
            val id = localDataSource.insertPaymentMethod(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod): Result<Unit> {
        return try {
            val entity = paymentMethod.toEntity()
            localDataSource.updatePaymentMethod(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod): Result<Unit> {
        return try {
            val entity = paymentMethod.toEntity()
            localDataSource.deletePaymentMethod(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePaymentMethodById(id: Int): Result<Unit> {
        return try {
            localDataSource.deletePaymentMethodById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTotalCashInHand(businessSlug: String): Double {
        return localDataSource.getTotalCashInHand(businessSlug)
    }
    
    // Extension functions for mapping
    private fun PaymentMethodEntity.toDomainModel(): PaymentMethod {
        return PaymentMethod(
            id = id,
            title = title,
            description = description,
            amount = amount,
            openingAmount = opening_amount,
            statusId = status_id,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    private fun PaymentMethod.toEntity(): PaymentMethodEntity {
        return PaymentMethodEntity(
            id = id,
            title = title,
            description = description ?: "",
            amount = amount,
            opening_amount = openingAmount,
            status_id = statusId,
            slug = slug,
            business_slug = businessSlug,
            created_by = createdBy,
            sync_status = syncStatus,
            created_at = createdAt,
            updated_at = updatedAt ?: Clock.System.now().toString()
        )
    }
}

