package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp

class AddWarehouseUseCase(
    private val repository: WarehousesRepository,
    private val slugGenerator: SlugGenerator
) {
    suspend operator fun invoke(
        title: String,
        address: String?,
        description: String?,
        typeId: Int,
        businessSlug: String?,
        createdBy: String?
    ): Result<Long> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_WAREHOUSE)
            ?: return Result.failure(IllegalStateException("Failed to generate slug: Invalid session context"))
        
        // Get current timestamp in ISO 8601 format
        val now = getCurrentTimestamp()
        
        val warehouse = Warehouse(
            title = title,
            address = address,
            description = description,
            latLong = null,
            thumbnail = null,
            typeId = typeId,
            statusId = 0, // Active
            slug = slug,
            businessSlug = businessSlug,
            createdBy = createdBy,
            syncStatus = 1, // Needs sync
            createdAt = now,
            updatedAt = now
        )
        
        return repository.insertWarehouse(warehouse)
    }
}

