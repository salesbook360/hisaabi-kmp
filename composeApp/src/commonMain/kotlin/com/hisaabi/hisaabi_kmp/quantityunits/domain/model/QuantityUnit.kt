package com.hisaabi.hisaabi_kmp.quantityunits.domain.model

data class QuantityUnit(
    val id: Int = 0,
    val title: String,
    val sortOrder: Int = 0,
    val parentSlug: String?,
    val conversionFactor: Double = 1.0,
    val baseConversionUnitSlug: String?,
    val statusId: Int = 1, // 1=Active, 2=Inactive, 3=Deleted
    val slug: String?,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
) {
    val displayName: String
        get() = title.ifEmpty { "Unknown" }
    
    val isActive: Boolean
        get() = statusId == 1
}

