package com.hisaabi.hisaabi_kmp.paymentmethods.data.repository

import com.hisaabi.hisaabi_kmp.common.Status
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.DeletedRecordsDao
import com.hisaabi.hisaabi_kmp.database.datasource.PaymentMethodLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.DeletedRecordsEntity
import com.hisaabi.hisaabi_kmp.database.entity.PaymentMethodEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class PaymentMethodsRepository(
    private val localDataSource: PaymentMethodLocalDataSource,
    private val deletedRecordsDao: DeletedRecordsDao,
    private val slugGenerator: SlugGenerator,
    private val appSessionManager: AppSessionManager
) {
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>> {
        return localDataSource.getAllPaymentMethods().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getPaymentMethodsByBusiness(businessSlug: String): Flow<List<PaymentMethod>> {
        return localDataSource.getPaymentMethodsByBusiness(businessSlug).map { entities ->
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
            // Get session context for business slug and user slug
            val sessionContext = appSessionManager.getSessionContext()
            if (!sessionContext.isValid) {
                return Result.failure(IllegalStateException("Invalid session context: userSlug or businessSlug is null"))
            }
            
            val businessSlug = sessionContext.businessSlug!!
            val userSlug = sessionContext.userSlug!!
            
            // Soft delete: Update payment method status to DELETED
            val now = getCurrentTimestamp()
            val updatedPaymentMethod = paymentMethod.copy(
                statusId = Status.DELETED.value,
                syncStatus = SyncStatus.NONE.value, // UnSynced
                updatedAt = now
            )
            val updatedEntity = updatedPaymentMethod.toEntity()
            localDataSource.updatePaymentMethod(updatedEntity)
            
            // Add entry to DeletedRecords table
            val deletedRecordSlug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_DELETED_RECORDS)
                ?: return Result.failure(IllegalStateException("Failed to generate slug for deleted record: Invalid session context"))
            
            val deletedRecord = DeletedRecordsEntity(
                id = 0,
                record_slug = paymentMethod.slug,
                record_type = "payment_method",
                deletion_type = "soft",
                slug = deletedRecordSlug,
                business_slug = businessSlug,
                created_by = userSlug,
                sync_status = SyncStatus.NONE.value, // UnSynced
                created_at = now,
                updated_at = now
            )
            
            deletedRecordsDao.insertDeletedRecord(deletedRecord)
            
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

