package com.hisaabi.hisaabi_kmp.warehouses.data.repository

import com.hisaabi.hisaabi_kmp.database.datasource.WareHouseLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.WareHouseEntity
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class WarehousesRepository(
    private val localDataSource: WareHouseLocalDataSource
) {
    fun getAllWarehouses(): Flow<List<Warehouse>> {
        return localDataSource.getAllWareHouses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getActiveWarehouses(): Flow<List<Warehouse>> {
        return localDataSource.getAllWareHouses().map { entities ->
            entities.filter { it.status_id == 1 }.map { it.toDomainModel() }
        }
    }
    
    fun getWarehousesByType(typeId: Int): Flow<List<Warehouse>> {
        return localDataSource.getWareHousesByType(typeId).map { entities ->
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
            val entity = warehouse.toEntity()
            localDataSource.deleteWareHouse(entity)
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

