package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import kotlinx.coroutines.flow.Flow

class GetQuantityUnitsUseCase(
    private val repository: QuantityUnitsRepository
) {

    fun getUnitsByParent(parentSlug: String): Flow<List<QuantityUnit>> {
        return repository.getUnitsByParent(parentSlug)
    }
    
    suspend fun getUnitsByParentSuspend(parentSlug: String): List<QuantityUnit> {
        return repository.getUnitsByParentSuspend(parentSlug)
    }
    
    // Get all parent unit types (Unit Types like Weight, Quantity, Liquid, Length)
    fun getParentUnitTypes(businessSlug: String): Flow<List<QuantityUnit>> {
        return repository.getParentUnitTypes(businessSlug)
    }

    suspend fun getUnitBySlug(slug: String): QuantityUnit? {
        return repository.getUnitBySlug(slug)
    }
}

