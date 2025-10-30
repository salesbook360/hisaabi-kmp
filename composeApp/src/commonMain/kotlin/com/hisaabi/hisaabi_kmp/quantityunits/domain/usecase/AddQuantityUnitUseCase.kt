package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp

class AddQuantityUnitUseCase(
    private val repository: QuantityUnitsRepository,
    private val slugGenerator: SlugGenerator
) {
    suspend operator fun invoke(
        title: String,
        sortOrder: Int = 0,
        conversionFactor: Double = 1.0,
        businessSlug: String?,
        createdBy: String?
    ): Result<Long> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Unit name cannot be empty"))
        }
        
        if (conversionFactor <= 0) {
            return Result.failure(IllegalArgumentException("Conversion factor must be greater than 0"))
        }
        
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_QUANTITY_UNIT)
            ?: return Result.failure(IllegalStateException("Failed to generate slug: Invalid session context"))
        
        // Get current timestamp in ISO 8601 format
        val now = getCurrentTimestamp()
        
        val unit = QuantityUnit(
            title = title,
            sortOrder = sortOrder,
            parentSlug = null,
            conversionFactor = conversionFactor,
            baseConversionUnitSlug = null,
            statusId = 0, // Active
            slug = slug,
            businessSlug = businessSlug,
            createdBy = createdBy,
            syncStatus = 1, // Needs sync
            createdAt = now,
            updatedAt = now
        )
        
        return repository.insertUnit(unit)
    }
}

