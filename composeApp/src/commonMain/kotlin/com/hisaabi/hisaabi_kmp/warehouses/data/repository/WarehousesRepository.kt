package com.hisaabi.hisaabi_kmp.warehouses.data.repository

import com.hisaabi.hisaabi_kmp.common.Status
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.DeletedRecordsDao
import com.hisaabi.hisaabi_kmp.database.datasource.WareHouseLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.DeletedRecordsEntity
import com.hisaabi.hisaabi_kmp.database.entity.WareHouseEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class WarehousesRepository(
    private val localDataSource: WareHouseLocalDataSource,
    private val deletedRecordsDao: DeletedRecordsDao,
    private val slugGenerator: SlugGenerator,
    private val appSessionManager: AppSessionManager
) {
    fun getAllWarehouses(): Flow<List<Warehouse>> {
        return localDataSource.getAllWareHouses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getActiveWarehouses(): Flow<List<Warehouse>> {
        return localDataSource.getAllWareHouses().map { entities ->
            entities.filter { it.status_id == 0 }.map { it.toDomainModel() }
        }
    }
    
    fun getWarehousesByType(typeId: Int): Flow<List<Warehouse>> {
        return localDataSource.getWareHousesByType(typeId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getWarehousesByBusiness(businessSlug: String): Flow<List<Warehouse>> {
        return localDataSource.getWareHousesByBusiness(businessSlug).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getWarehouseById(id: Int): Warehouse? {
        return localDataSource.getWareHouseById(id)?.toDomainModel()
    }
    
    suspend fun getWarehouseBySlug(slug: String): Warehouse? {
        return localDataSource.getWareHouseBySlug(slug)?.toDomainModel()
    }
    
    suspend fun insertWarehouse(warehouse: Warehouse): Result<Long> {
        return try {
            val entity = warehouse.toEntity()
            val id = localDataSource.insertWareHouse(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateWarehouse(warehouse: Warehouse): Result<Unit> {
        return try {
            val entity = warehouse.toEntity()
            localDataSource.updateWareHouse(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteWarehouse(warehouse: Warehouse): Result<Unit> {
        return try {
            // Get session context for business slug and user slug
            val sessionContext = appSessionManager.getSessionContext()
            if (!sessionContext.isValid) {
                return Result.failure(IllegalStateException("Invalid session context: userSlug or businessSlug is null"))
            }
            
            val businessSlug = sessionContext.businessSlug!!
            val userSlug = sessionContext.userSlug!!
            
            // Soft delete: Update warehouse status to DELETED
            val now = getCurrentTimestamp()
            val updatedWarehouse = warehouse.copy(
                statusId = Status.DELETED.value,
                syncStatus = SyncStatus.NONE.value, // UnSynced
                updatedAt = now
            )
            val updatedEntity = updatedWarehouse.toEntity()
            localDataSource.updateWareHouse(updatedEntity)
            
            // Add entry to DeletedRecords table
            val deletedRecordSlug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_DELETED_RECORDS)
                ?: return Result.failure(IllegalStateException("Failed to generate slug for deleted record: Invalid session context"))
            
            val deletedRecord = DeletedRecordsEntity(
                id = 0,
                record_slug = warehouse.slug,
                record_type = "warehouse",
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
    
    suspend fun deleteWarehouseById(id: Int): Result<Unit> {
        return try {
            localDataSource.deleteWareHouseById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension functions for mapping
    private fun WareHouseEntity.toDomainModel(): Warehouse {
        return Warehouse(
            id = id,
            title = title ?: "",
            address = address,
            description = description,
            latLong = lat_long,
            thumbnail = thumbnail,
            typeId = type_id,
            statusId = status_id,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    private fun Warehouse.toEntity(): WareHouseEntity {
        return WareHouseEntity(
            id = id,
            title = title,
            address = address,
            description = description,
            lat_long = latLong,
            thumbnail = thumbnail,
            type_id = typeId,
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

