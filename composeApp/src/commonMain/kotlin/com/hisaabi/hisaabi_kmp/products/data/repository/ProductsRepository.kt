package com.hisaabi.hisaabi_kmp.products.data.repository

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.RecipeIngredientsDao
import com.hisaabi.hisaabi_kmp.database.dao.QuantityUnitDao
import com.hisaabi.hisaabi_kmp.database.datasource.ProductLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import com.hisaabi.hisaabi_kmp.database.entity.RecipeIngredientsEntity
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.model.RecipeIngredient
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.first

interface ProductsRepository {
    suspend fun getProducts(
        businessSlug: String,
        productType: ProductType? = null
    ): List<Product>
    
    suspend fun getProductBySlug(slug: String): Product?
    
    suspend fun getProductBySlugAnyStatus(slug: String): Product?
    
    suspend fun addProduct(
        product: Product,
        businessSlug: String,
        userSlug: String
    ): String
    
    suspend fun updateProduct(product: Product): String
    
    suspend fun deleteProduct(productSlug: String)
    
    suspend fun getRecipeIngredients(recipeSlug: String): List<RecipeIngredient>
    
    suspend fun addRecipeIngredient(
        ingredient: RecipeIngredient,
        businessSlug: String,
        userSlug: String
    ): String
    
    suspend fun deleteRecipeIngredient(ingredientSlug: String)
}

class ProductsRepositoryImpl(
    private val productDataSource: ProductLocalDataSource,
    private val recipeIngredientsDao: RecipeIngredientsDao,
    private val quantityUnitDao: QuantityUnitDao,
    private val slugGenerator: SlugGenerator
) : ProductsRepository {
    
    override suspend fun getProducts(
        businessSlug: String,
        productType: ProductType?
    ): List<Product> {
        val products = productDataSource.getProductsByBusinessList(businessSlug)
            .map { it.toDomainModel() }
        
        // Filter by product type if specified
        return if (productType != null) {
            products.filter { it.typeId == productType.type }
        } else {
            products
        }
    }
    
    override suspend fun getProductBySlug(slug: String): Product? {
        return productDataSource.getProductBySlug(slug)?.toDomainModel()
    }
    
    override suspend fun getProductBySlugAnyStatus(slug: String): Product? {
        return productDataSource.getProductBySlugAnyStatus(slug)?.toDomainModel()
    }
    
    override suspend fun addProduct(
        product: Product,
        businessSlug: String,
        userSlug: String
    ): String {
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_PRODUCT)
            ?: throw IllegalStateException("Failed to generate slug: Invalid session context")
        
        // Get current timestamp for both created_at and updated_at
        val now = getCurrentTimestamp()
        
        // Create entity with generated slug, business info, and timestamps
        val entity = product.toEntity().copy(
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug,
            created_at = now,
            updated_at = now
        )
        
        productDataSource.insertProduct(entity)
        return slug
    }
    
    override suspend fun updateProduct(product: Product): String {
        // Update only the updated_at timestamp, preserve created_at
        val entity = product.toEntity().copy(
            updated_at = getCurrentTimestamp()
        )
        productDataSource.updateProduct(entity)
        return product.slug
    }
    
    override suspend fun deleteProduct(productSlug: String) {
        val product = productDataSource.getProductBySlug(productSlug)
        if (product != null) {
            productDataSource.deleteProduct(product)
        }
    }
    
    override suspend fun getRecipeIngredients(recipeSlug: String): List<RecipeIngredient> {
        return recipeIngredientsDao.getIngredientsByRecipe(recipeSlug)
            .first()
            .map { entity ->
                // Load the product details for each ingredient
                val ingredientProduct = productDataSource.getProductBySlug(entity.ingredient_slug ?: "")
                
                // Load the quantity unit title
                val quantityUnitTitle = entity.quantity_unit_slug?.let { unitSlug ->
                    quantityUnitDao.getUnitBySlug(unitSlug)?.title
                }
                
                entity.toDomainModel().copy(
                    ingredientTitle = ingredientProduct?.title ?: "Unknown",
                    quantityUnitTitle = quantityUnitTitle
                )
            }
    }
    
    override suspend fun addRecipeIngredient(
        ingredient: RecipeIngredient,
        businessSlug: String,
        userSlug: String
    ): String {
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_RECIPE_INGREDIENTS)
            ?: throw IllegalStateException("Failed to generate slug: Invalid session context")
        
        // Get current timestamp for both created_at and updated_at
        val now = getCurrentTimestamp()
        
        // Create entity with generated slug, business info, and timestamps
        val entity = ingredient.toEntity().copy(
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug,
            created_at = now,
            updated_at = now
        )
        
        recipeIngredientsDao.insertRecipeIngredient(entity)
        return slug
    }
    
    override suspend fun deleteRecipeIngredient(ingredientSlug: String) {
        recipeIngredientsDao.deleteRecipeIngredientBySlug(ingredientSlug)
    }
}

// Extension function to convert entity to domain model
fun ProductEntity.toDomainModel(): Product {
    return Product(
        id = id,
        title = title ?: "",
        description = description,
        typeId = type_id,
        taxPercentage = tax_percentage,
        discountPercentage = discount_percentage,
        retailPrice = retail_price,
        wholesalePrice = wholesale_price,
        thumbnail = thumbnail,
        purchasePrice = purchase_price,
        statusId = status_id,
        digitalId = digital_id,
        baseUnitSlug = base_unit_slug,
        defaultUnitSlug = default_unit_slug,
        minimumQuantityUnitSlug = minimum_quantity_unit_slug,
        openingQuantityUnitSlug = opening_quantity_unit_slug,
        categorySlug = category_slug,
        avgPurchasePrice = avg_purchase_price,
        openingQuantityPurchasePrice = opening_quantity_purchase_price,
        expiryDate = expiry_date,
        expiryAlert = expiry_alert,
        manufacturer = manufacturer,
        slug = slug ?: "",
        businessSlug = business_slug,
        createdBy = created_by,
        syncStatus = sync_status,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

// Extension function to convert domain model to entity
fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        description = description,
        type_id = typeId,
        tax_percentage = taxPercentage,
        discount_percentage = discountPercentage,
        retail_price = retailPrice,
        wholesale_price = wholesalePrice,
        thumbnail = thumbnail,
        purchase_price = purchasePrice,
        status_id = statusId,
        digital_id = digitalId,
        base_unit_slug = baseUnitSlug,
        default_unit_slug = defaultUnitSlug,
        minimum_quantity_unit_slug = minimumQuantityUnitSlug,
        opening_quantity_unit_slug = openingQuantityUnitSlug,
        category_slug = categorySlug,
        avg_purchase_price = avgPurchasePrice,
        opening_quantity_purchase_price = openingQuantityPurchasePrice,
        expiry_date = expiryDate,
        expiry_alert = expiryAlert,
        manufacturer = manufacturer,
        slug = slug,
        business_slug = businessSlug,
        created_by = createdBy,
        sync_status = syncStatus,
        created_at = createdAt,
        updated_at = updatedAt
    )
}

// Extension functions for RecipeIngredient
fun RecipeIngredientsEntity.toDomainModel(): RecipeIngredient {
    return RecipeIngredient(
        id = id,
        recipeSlug = recipe_slug ?: "",
        ingredientSlug = ingredient_slug ?: "",
        ingredientTitle = "", // Will be populated by use case
        quantity = quantity ?: 0.0,
        quantityUnitSlug = quantity_unit_slug,
        quantityUnitTitle = null,
        slug = slug,
        businessSlug = business_slug,
        createdBy = created_by,
        syncStatus = sync_status,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

fun RecipeIngredient.toEntity(): RecipeIngredientsEntity {
    return RecipeIngredientsEntity(
        id = id,
        recipe_slug = recipeSlug,
        ingredient_slug = ingredientSlug,
        quantity = quantity,
        quantity_unit_slug = quantityUnitSlug,
        slug = slug,
        business_slug = businessSlug,
        created_by = createdBy,
        sync_status = syncStatus,
        created_at = createdAt,
        updated_at = updatedAt
    )
}

