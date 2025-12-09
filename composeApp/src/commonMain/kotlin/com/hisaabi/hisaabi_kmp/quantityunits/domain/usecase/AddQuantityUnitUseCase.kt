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
    
    /**
     * Add a child unit under a parent unit type
     */
    suspend operator fun invoke(
        title: String,
        sortOrder: Int = 0,
        conversionFactor: Double = 1.0,
        parentSlug: String? = null,
        baseConversionUnitSlug: String? = null,
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
            parentSlug = parentSlug,
            conversionFactor = conversionFactor,
            baseConversionUnitSlug = baseConversionUnitSlug,
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
    
    /**
     * Add a new parent unit type (e.g., Weight, Quantity, Liquid, Length)
     */
    suspend fun addParentUnitType(
        title: String,
        sortOrder: Int = 0,
        businessSlug: String?,
        createdBy: String?
    ): Result<String> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Unit type name cannot be empty"))
        }
        
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_QUANTITY_UNIT)
            ?: return Result.failure(IllegalStateException("Failed to generate slug: Invalid session context"))
        
        // Get current timestamp in ISO 8601 format
        val now = getCurrentTimestamp()
        
        val unit = QuantityUnit(
            title = title,
            sortOrder = sortOrder,
            parentSlug = "0", // Parent units have parent_slug = "0"
            conversionFactor = 1.0,
            baseConversionUnitSlug = null,
            statusId = 0, // Active
            slug = slug,
            businessSlug = businessSlug,
            createdBy = createdBy,
            syncStatus = 1, // Needs sync
            createdAt = now,
            updatedAt = now
        )
        
        return repository.insertUnit(unit).map { slug }
    }
    
    /**
     * Update base conversion unit for a parent unit type
     */
    suspend fun updateBaseConversionUnit(parentSlug: String, baseUnitSlug: String): Result<Unit> {
        return repository.updateBaseConversionUnit(parentSlug, baseUnitSlug)
    }
}

