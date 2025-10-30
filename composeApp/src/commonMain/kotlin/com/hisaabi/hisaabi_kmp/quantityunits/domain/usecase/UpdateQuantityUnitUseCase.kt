package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import kotlinx.datetime.Clock

class UpdateQuantityUnitUseCase(
    private val repository: QuantityUnitsRepository
) {
    suspend operator fun invoke(unit: QuantityUnit): Result<Unit> {
        // Validate input
        if (unit.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Unit name cannot be empty"))
        }
        
        if (unit.conversionFactor <= 0) {
            return Result.failure(IllegalArgumentException("Conversion factor must be greater than 0"))
        }
        
        // Get current timestamp in ISO 8601 format
        val now = getCurrentTimestamp()
        
        val updatedUnit = unit.copy(
            syncStatus = 1, // Needs sync
            updatedAt = now
        )
        
        return repository.updateUnit(updatedUnit)
    }
}

