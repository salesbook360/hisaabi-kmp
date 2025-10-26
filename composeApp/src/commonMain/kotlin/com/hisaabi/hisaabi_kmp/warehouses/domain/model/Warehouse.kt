package com.hisaabi.hisaabi_kmp.warehouses.domain.model

data class Warehouse(
    val id: Int = 0,
    val title: String,
    val address: String?,
    val description: String?,
    val latLong: String?,
    val thumbnail: String?,
    val typeId: Int = 1, // 1=Main, 2=Branch, 3=Virtual
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
        get() = statusId == 0
}

enum class WarehouseType(val typeId: Int, val displayName: String) {
    MAIN(1, "Main Warehouse"),
    BRANCH(2, "Branch Warehouse"),
    VIRTUAL(3, "Virtual Warehouse");
    
    companion object {
        fun fromInt(value: Int): WarehouseType? = entries.find { it.typeId == value }
    }
}
