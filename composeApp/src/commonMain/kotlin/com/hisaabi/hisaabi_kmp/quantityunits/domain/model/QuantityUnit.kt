package com.hisaabi.hisaabi_kmp.quantityunits.domain.model

data class QuantityUnit(
    val id: Int = 0,
    val title: String,
    val sortOrder: Int = 0,
    val parentSlug: String?,
    val conversionFactor: Double = 1.0,
    val baseConversionUnitSlug: String?,
    val statusId: Int = 0, // 0=Active, 2=Deleted
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
        get() = statusId != 2
    
    /**
     * Returns true if this is a parent unit type (e.g., Weight, Quantity, Liquid, Length)
     * Parent unit types have parent_slug = null
     */
    val isParentUnitType: Boolean
        get() = parentSlug == null
    
    /**
     * Returns true if this is a child unit (e.g., KG, MG, Ton under Weight)
     * Child units have a parent_slug pointing to a parent unit type
     */
    val isChildUnit: Boolean
        get() = !parentSlug.isNullOrBlank()
    
    /**
     * Returns the conversion factor string (e.g., "1 KG = 1000 g")
     */
    fun conversionFactorStr(baseUnitTitle: String?): String {
        return if (baseUnitTitle != null && conversionFactor != 1.0) {
            "1 $title = $conversionFactor $baseUnitTitle"
        } else {
            ""
        }
    }
}

