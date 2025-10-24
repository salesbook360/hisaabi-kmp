package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.ProductQuantitiesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductQuantitiesDao {
    @Query("SELECT * FROM ProductQuantities")
    fun getAllProductQuantities(): Flow<List<ProductQuantitiesEntity>>
    
    @Query("SELECT * FROM ProductQuantities WHERE id = :id")
    suspend fun getProductQuantityById(id: Int): ProductQuantitiesEntity?
    
    @Query("SELECT * FROM ProductQuantities WHERE product_slug = :productSlug")
    fun getQuantitiesByProduct(productSlug: String): Flow<List<ProductQuantitiesEntity>>
    
    @Query("SELECT * FROM ProductQuantities WHERE warehouse_slug = :warehouseSlug")
    fun getQuantitiesByWarehouse(warehouseSlug: String): Flow<List<ProductQuantitiesEntity>>
    
    @Query("SELECT * FROM ProductQuantities WHERE product_slug = :productSlug AND warehouse_slug = :warehouseSlug")
    suspend fun getProductQuantityByProductAndWarehouse(
        productSlug: String,
        warehouseSlug: String
    ): ProductQuantitiesEntity?
    
    @Query("SELECT * FROM ProductQuantities WHERE sync_status != 2 AND business_slug = :businessSlug")
    suspend fun getUnsyncedQuantities(businessSlug: String): List<ProductQuantitiesEntity>
    
    @Query("SELECT * FROM ProductQuantities WHERE current_quantity <= minimum_quantity")
    fun getLowStockProducts(): Flow<List<ProductQuantitiesEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductQuantity(productQuantity: ProductQuantitiesEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductQuantities(productQuantities: List<ProductQuantitiesEntity>)
    
    @Update
    suspend fun updateProductQuantity(productQuantity: ProductQuantitiesEntity)
    
    @Delete
    suspend fun deleteProductQuantity(productQuantity: ProductQuantitiesEntity)
    
    @Query("DELETE FROM ProductQuantities WHERE id = :id")
    suspend fun deleteProductQuantityById(id: Int)
    
    @Query("DELETE FROM ProductQuantities")
    suspend fun deleteAllProductQuantities()
    
    // Dashboard Queries
    @Query("""
        SELECT SUM(current_quantity) FROM ProductQuantities 
        WHERE business_slug = :businessSlug
    """)
    suspend fun getTotalQuantityInHand(businessSlug: String): Double?
    
    @Query("""
        SELECT COUNT(*) FROM ProductQuantities 
        WHERE current_quantity <= minimum_quantity 
        AND business_slug = :businessSlug
    """)
    suspend fun getLowStockCount(businessSlug: String): Int?
}

