package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?
    
    @Query("SELECT * FROM Product WHERE slug = :slug")
    suspend fun getProductBySlug(slug: String): ProductEntity?
    
    @Query("SELECT * FROM Product WHERE category_slug = :categorySlug")
    fun getProductsByCategory(categorySlug: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE business_slug = :businessSlug")
    fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE sync_status != 2 AND business_slug = :businessSlug")
    suspend fun getUnsyncedProducts(businessSlug: String): List<ProductEntity>
    
    @Query("SELECT * FROM Product WHERE status_id = :statusId")
    fun getProductsByStatus(statusId: Int): Flow<List<ProductEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM Product WHERE id = :id")
    suspend fun deleteProductById(id: Int)
    
    @Query("DELETE FROM Product")
    suspend fun deleteAllProducts()
    
    // Dashboard Queries
    @Query("""
        SELECT COUNT(*) FROM Product 
        WHERE business_slug = :businessSlug
    """)
    suspend fun getTotalProductsCount(businessSlug: String): Int?
    
    @Query("SELECT MAX(id) FROM Product")
    suspend fun getMaxId(): Int?
}

