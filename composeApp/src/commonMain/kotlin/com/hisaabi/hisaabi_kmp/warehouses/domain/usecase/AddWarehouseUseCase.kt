package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.datetime.Clock

class AddWarehouseUseCase(
    private val repository: WarehousesRepository
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
        
        // Generate slug from title
        val slug = generateSlug(title)
        
        // Check if warehouse with same slug exists
        val existing = repository.getWarehouseBySlug(slug)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Warehouse with this title already exists"))
        }
        
        val now = Clock.System.now().toString()
        
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
    
    private fun generateSlug(title: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "${title.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-$timestamp"
    }
}

