package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse

class DeleteWarehouseUseCase(
    private val repository: WarehousesRepository
) {
    suspend operator fun invoke(warehouse: Warehouse): Result<Unit> {
        return repository.deleteWarehouse(warehouse)
    }
    
    suspend fun deleteById(id: Int): Result<Unit> {
        return repository.deleteWarehouseById(id)
    }
}

