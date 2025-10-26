package com.hisaabi.hisaabi_kmp.quantityunits.domain.usecase

import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import kotlinx.datetime.Clock

class AddQuantityUnitUseCase(
    private val repository: QuantityUnitsRepository
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
        
        // Generate slug from title
        val slug = generateSlug(title)
        
        // Check if unit with same slug exists
        val existing = repository.getUnitBySlug(slug)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Unit with this name already exists"))
        }
        
        val now = Clock.System.now().toString()
        
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
    
    private fun generateSlug(title: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "${title.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-$timestamp"
    }
}

