package com.hisaabi.hisaabi_kmp.warehouses.domain.usecase

import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.Flow

class GetWarehousesUseCase(
    private val repository: WarehousesRepository
) {
    operator fun invoke(): Flow<List<Warehouse>> {
        return repository.getAllWarehouses()
    }
    
    fun getActiveWarehouses(): Flow<List<Warehouse>> {
        return repository.getActiveWarehouses()
    }
    
    fun getWarehousesByType(typeId: Int): Flow<List<Warehouse>> {
        return repository.getWarehousesByType(typeId)
    }
}

