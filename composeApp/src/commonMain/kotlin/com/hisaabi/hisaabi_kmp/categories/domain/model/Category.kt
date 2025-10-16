package com.hisaabi.hisaabi_kmp.categories.domain.model

data class Category(
    val id: Int = 0,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val typeId: Int,  // 1=Products, 2=Area, 3=Customer Category
    val slug: String,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
) {
    val displayName: String
        get() = title.ifEmpty { "Unknown" }
}

enum class CategoryType(val type: Int, val displayName: String) {
    PRODUCTS(1, "Product Category"),
    AREA(2, "Area"),
    CUSTOMER_CATEGORY(3, "Customer Category");
    
    companion object {
        fun fromInt(value: Int): CategoryType? = entries.find { it.type == value }
    }
}

