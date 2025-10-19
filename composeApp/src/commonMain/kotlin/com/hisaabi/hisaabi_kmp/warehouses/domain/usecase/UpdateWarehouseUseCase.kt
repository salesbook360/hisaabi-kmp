package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.datetime.Clock

class UpdateWarehouseUseCase(
    private val repository: WarehousesRepository
) {
    suspend operator fun invoke(warehouse: Warehouse): Result<Unit> {
        // Validate input
        if (warehouse.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        
        val now = Clock.System.now().toString()
        
        val updatedWarehouse = warehouse.copy(
            syncStatus = 1, // Needs sync
            updatedAt = now
        )
        
        return repository.updateWarehouse(updatedWarehouse)
    }
}

