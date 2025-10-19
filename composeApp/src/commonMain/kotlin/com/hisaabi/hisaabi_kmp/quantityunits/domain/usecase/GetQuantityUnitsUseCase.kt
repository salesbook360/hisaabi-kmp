package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import kotlinx.coroutines.flow.Flow

class GetQuantityUnitsUseCase(
    private val repository: QuantityUnitsRepository
) {
    operator fun invoke(): Flow<List<QuantityUnit>> {
        return repository.getAllUnits()
    }
    
    fun getActiveUnits(): Flow<List<QuantityUnit>> {
        return repository.getActiveUnits()
    }
    
    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnit>> {
        return repository.getUnitsByParent(parentSlug)
    }
}

