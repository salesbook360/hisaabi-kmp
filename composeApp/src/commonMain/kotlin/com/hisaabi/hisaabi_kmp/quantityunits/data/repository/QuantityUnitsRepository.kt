package com.hisaabi.hisaabi_kmp.quantityunits.data.repository

import com.hisaabi.hisaabi_kmp.database.datasource.QuantityUnitLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.QuantityUnitEntity
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class QuantityUnitsRepository(
    private val localDataSource: QuantityUnitLocalDataSource
) {

    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnit>> {
        return localDataSource.getUnitsByParent(parentSlug).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getUnitsByParentSuspend(parentSlug: String): List<QuantityUnit> {
        return localDataSource.getUnitsByParentSuspend(parentSlug).map { it.toDomainModel() }
    }
    
    // Get all parent unit types (Unit Types like Weight, Quantity, Liquid, Length)
    fun getParentUnitTypes(businessSlug: String): Flow<List<QuantityUnit>> {
        return localDataSource.getParentUnitTypes(businessSlug).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getUnitBySlug(slug: String): QuantityUnit? {
        return localDataSource.getUnitBySlug(slug)?.toDomainModel()
    }
    
    suspend fun insertUnit(unit: QuantityUnit): Result<Long> {
        return try {
            val entity = unit.toEntity()
            val id = localDataSource.insertUnit(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUnit(unit: QuantityUnit): Result<Unit> {
        return try {
            val entity = unit.toEntity()
            localDataSource.updateUnit(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUnit(unit: QuantityUnit): Result<Unit> {
        return try {
            val entity = unit.toEntity()
            localDataSource.deleteUnit(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUnitById(id: Int): Result<Unit> {
        return try {
            localDataSource.deleteUnitById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBaseConversionUnit(parentSlug: String, baseUnitSlug: String): Result<Unit> {
        return try {
            localDataSource.updateBaseConversionUnit(parentSlug, baseUnitSlug)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Extension functions for mapping
    private fun QuantityUnitEntity.toDomainModel(): QuantityUnit {
        return QuantityUnit(
            id = id,
            title = title,
            sortOrder = sort_order,
            parentSlug = parent_slug,
            conversionFactor = conversion_factor,
            baseConversionUnitSlug = base_conversion_unit_slug,
            statusId = status_id,
            slug = slug,
            businessSlug = business_slug,
            createdBy = created_by,
            syncStatus = sync_status,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
    
    private fun QuantityUnit.toEntity(): QuantityUnitEntity {
        return QuantityUnitEntity(
            id = id,
            title = title,
            sort_order = sortOrder,
            parent_slug = parentSlug,
            conversion_factor = conversionFactor,
            base_conversion_unit_slug = baseConversionUnitSlug,
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

