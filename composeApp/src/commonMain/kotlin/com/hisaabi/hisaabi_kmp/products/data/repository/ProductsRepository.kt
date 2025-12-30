package com.hisaabi.hisaabi_kmp.products.data.repository

import com.hisaabi.hisaabi_kmp.common.Status
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.DeletedRecordsDao
import com.hisaabi.hisaabi_kmp.database.dao.RecipeIngredientsDao
import com.hisaabi.hisaabi_kmp.database.dao.QuantityUnitDao
import com.hisaabi.hisaabi_kmp.database.datasource.ProductLocalDataSource
import com.hisaabi.hisaabi_kmp.database.datasource.ProductQuantitiesLocalDataSource
import com.hisaabi.hisaabi_kmp.database.entity.DeletedRecordsEntity
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import com.hisaabi.hisaabi_kmp.database.entity.RecipeIngredientsEntity
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.domain.model.RecipeIngredient
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
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
    
    suspend fun softDeleteProduct(product: Product): Result<Unit>
    
    suspend fun deleteProduct(productSlug: String)
    
    suspend fun getRecipeIngredients(recipeSlug: String): List<RecipeIngredient>
    
    suspend fun addRecipeIngredient(
        ingredient: RecipeIngredient,
        businessSlug: String,
        userSlug: String
    ): String
    
    suspend fun deleteRecipeIngredient(ingredientSlug: String)
    
    suspend fun getProductQuantity(
        productSlug: String,
        warehouseSlug: String
    ): Double?
    
    suspend fun getQuantitiesByWarehouse(warehouseSlug: String): Map<String, Double>
    
    suspend fun getQuantitiesWithMinimumByWarehouse(warehouseSlug: String): Map<String, Pair<Double, Double>> // productSlug -> (currentQuantity, minimumQuantity)
    
    suspend fun saveProductQuantity(
        productSlug: String,
        warehouseSlug: String,
        openingQuantity: Double,
        minimumQuantity: Double,
        businessSlug: String
    ): Result<Unit>
    
    suspend fun getProductQuantityForWarehouse(
        productSlug: String,
        warehouseSlug: String
    ): Pair<Double, Double>? // Returns (openingQuantity, minimumQuantity) if exists
}

class ProductsRepositoryImpl(
    private val productDataSource: ProductLocalDataSource,
    private val recipeIngredientsDao: RecipeIngredientsDao,
    private val quantityUnitDao: QuantityUnitDao,
    private val slugGenerator: SlugGenerator,
    private val productQuantitiesDataSource: ProductQuantitiesLocalDataSource,
    private val deletedRecordsDao: DeletedRecordsDao,
    private val appSessionManager: AppSessionManager
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
        // Update sync status to UnSynced and updated_at timestamp, preserve created_at
        val now = getCurrentTimestamp()
        val entity = product.toEntity().copy(
            sync_status = SyncStatus.UPDATED.value, // UnSynced
            updated_at = now
        )
        productDataSource.updateProduct(entity)
        return product.slug
    }
    
    override suspend fun softDeleteProduct(product: Product): Result<Unit> {
        return try {
            // Get session context for business slug and user slug
            val sessionContext = appSessionManager.getSessionContext()
            if (!sessionContext.isValid) {
                return Result.failure(IllegalStateException("Invalid session context: userSlug or businessSlug is null"))
            }
            
            val businessSlug = sessionContext.businessSlug!!
            val userSlug = sessionContext.userSlug!!
            
            // Soft delete: Update product status to DELETED
            val now = getCurrentTimestamp()
            val updatedProduct = product.copy(
                statusId = Status.DELETED.value,
                syncStatus = SyncStatus.NONE.value, // UnSynced
                updatedAt = now
            )
            val updatedEntity = updatedProduct.toEntity()
            productDataSource.updateProduct(updatedEntity)
            
            // Add entry to DeletedRecords table
            val deletedRecordSlug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_DELETED_RECORDS)
                ?: return Result.failure(IllegalStateException("Failed to generate slug for deleted record: Invalid session context"))
            
            val deletedRecord = DeletedRecordsEntity(
                id = 0,
                record_slug = product.slug,
                record_type = "product",
                deletion_type = "soft",
                slug = deletedRecordSlug,
                business_slug = businessSlug,
                created_by = userSlug,
                sync_status = SyncStatus.NONE.value, // UnSynced
                created_at = now,
                updated_at = now
            )
            
            deletedRecordsDao.insertDeletedRecord(deletedRecord)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    override suspend fun getProductQuantity(
        productSlug: String,
        warehouseSlug: String
    ): Double? {
        return productQuantitiesDataSource.getQuantityByProductAndWarehouse(productSlug, warehouseSlug)
            ?.current_quantity
    }
    
    override suspend fun getQuantitiesByWarehouse(warehouseSlug: String): Map<String, Double> {
        val quantities = productQuantitiesDataSource.getQuantitiesByWarehouseList(warehouseSlug)
        return quantities.associate { 
            (it.product_slug ?: "") to it.current_quantity 
        }
    }
    
    override suspend fun getQuantitiesWithMinimumByWarehouse(warehouseSlug: String): Map<String, Pair<Double, Double>> {
        val quantities = productQuantitiesDataSource.getQuantitiesByWarehouseList(warehouseSlug)
        return quantities.associate { 
            (it.product_slug ?: "") to (it.current_quantity to it.minimum_quantity)
        }
    }
    
    override suspend fun saveProductQuantity(
        productSlug: String,
        warehouseSlug: String,
        openingQuantity: Double,
        minimumQuantity: Double,
        businessSlug: String
    ): Result<Unit> {
        return try {
            val existingQuantity = productQuantitiesDataSource.getQuantityByProductAndWarehouse(
                productSlug,
                warehouseSlug
            )
            
            val quantityEntity = if (existingQuantity != null) {
                // Update existing
                existingQuantity.copy(
                    opening_quantity = openingQuantity,
                    minimum_quantity = minimumQuantity,
                    current_quantity = if (existingQuantity.current_quantity == 0.0) openingQuantity else existingQuantity.current_quantity
                )
            } else {
                // Create new
                com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity(
                    product_slug = productSlug,
                    warehouse_slug = warehouseSlug,
                    opening_quantity = openingQuantity,
                    current_quantity = openingQuantity,
                    minimum_quantity = minimumQuantity,
                    maximum_quantity = 0.0,
                    business_slug = businessSlug,
                    sync_status = 1 // Needs sync
                )
            }
            
            productQuantitiesDataSource.saveProductQuantity(quantityEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductQuantityForWarehouse(
        productSlug: String,
        warehouseSlug: String
    ): Pair<Double, Double>? {
        val quantity = productQuantitiesDataSource.getQuantityByProductAndWarehouse(
            productSlug,
            warehouseSlug
        )
        return quantity?.let { it.opening_quantity to it.minimum_quantity }
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

