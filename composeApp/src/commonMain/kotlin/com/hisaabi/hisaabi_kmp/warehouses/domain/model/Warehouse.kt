package com.hisaabi.hisaabi_kmp.warehouses.domain.model

data class Warehouse(
    val id: Int = 0,
    val title: String,
    val address: String?,
    val description: String?,
    val latLong: String?,
    val thumbnail: String?,
    val typeId: Int = 1, // 1=Main, 2=Branch, 3=Virtual
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

enum class WarehouseType(val typeId: Int, val displayName: String) {
    MAIN(1, "Main Warehouse"),
    BRANCH(2, "Branch Warehouse"),
    VIRTUAL(3, "Virtual Warehouse");
    
    companion object {
        fun fromInt(value: Int): WarehouseType? = entries.find { it.typeId == value }
    }
}

enum class WarehouseStatus(val statusId: Int, val displayName: String) {
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive"),
    DELETED(3, "Deleted");
    
    companion object {
        fun fromInt(value: Int): WarehouseStatus? = entries.find { it.statusId == value }
    }
}

