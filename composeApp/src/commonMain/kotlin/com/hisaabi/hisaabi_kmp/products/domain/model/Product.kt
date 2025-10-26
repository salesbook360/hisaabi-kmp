package com.hisaabi.hisaabi_kmp.products.domain.model

data class Product(
    val id: Int = 0,
    val title: String,
    val description: String?,
    val typeId: Int,  // ProductType enum value
    val taxPercentage: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val retailPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val thumbnail: String?,
    val purchasePrice: Double = 0.0,
    val statusId: Int = 1,  // ProductStatus enum value
    val digitalId: String?,
    val baseUnitSlug: String?,
    val defaultUnitSlug: String?,
    val minimumQuantityUnitSlug: String?,
    val openingQuantityUnitSlug: String?,
    val categorySlug: String?,
    val avgPurchasePrice: Double = 0.0,
    val openingQuantityPurchasePrice: Double = 0.0,
    val expiryDate: String?,
    val expiryAlert: String?,
    val manufacturer: String?,
    val slug: String,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
) {
    val productType: ProductType
        get() = ProductType.fromInt(typeId) ?: ProductType.SIMPLE_PRODUCT
    
    val productStatus: ProductStatus
        get() = ProductStatus.fromInt(statusId) ?: ProductStatus.ACTIVE
    
    val displayName: String
        get() = title.ifEmpty { "Unknown Product" }
    
    val isService: Boolean
        get() = typeId == ProductType.SERVICE.type
    
    val isRecipe: Boolean
        get() = typeId == ProductType.RECIPE.type
    
    val isSimpleProduct: Boolean
        get() = typeId == ProductType.SIMPLE_PRODUCT.type
    
    val canBePurchased: Boolean
        get() = !isService  // Services cannot be purchased
    
    val canBeSold: Boolean
        get() = true  // All products can be sold
}

/**
 * Product Types
 * 0 = Simple Product (can be purchased and sold independently)
 * 1 = Service (can only be sold, not purchased)
 * 2 = Recipe/Manufactured Product (composed of other products)
 */
enum class ProductType(val type: Int, val displayName: String) {
    SIMPLE_PRODUCT(1, "Simple Product"),
    SERVICE(2, "Service"),
    RECIPE(3, "Recipe");
    
    companion object {
        fun fromInt(value: Int): ProductType? = entries.find { it.type == value }
    }
}

/**
 * Product Status
 * 1 = Active
 * 2 = Inactive
 * 3 = Deleted
 */
enum class ProductStatus(val status: Int, val displayName: String) {
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive"),
    DELETED(3, "Deleted");
    
    companion object {
        fun fromInt(value: Int): ProductStatus? = entries.find { it.status == value }
    }
}

/**
 * Recipe Ingredient - represents an ingredient in a recipe
 */
data class RecipeIngredient(
    val id: Int = 0,
    val recipeSlug: String,
    val ingredientSlug: String,
    val ingredientTitle: String,  // For display purposes
    val quantity: Double,
    val quantityUnitSlug: String?,
    val quantityUnitTitle: String?,  // For display purposes
    val slug: String?,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * Product with additional details like current quantity
 */
data class ProductWithDetails(
    val product: Product,
    val currentQuantity: Double = 0.0,
    val minimumQuantity: Double = 0.0,
    val ingredients: List<RecipeIngredient> = emptyList()
) {
    val isLowStock: Boolean
        get() = currentQuantity <= minimumQuantity && minimumQuantity > 0
}


