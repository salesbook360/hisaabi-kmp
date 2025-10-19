package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit

class DeleteQuantityUnitUseCase(
    private val repository: QuantityUnitsRepository
) {
    suspend operator fun invoke(unit: QuantityUnit): Result<Unit> {
        return repository.deleteUnit(unit)
    }
    
    suspend fun deleteById(id: Int): Result<Unit> {
        return repository.deleteUnitById(id)
    }
}

