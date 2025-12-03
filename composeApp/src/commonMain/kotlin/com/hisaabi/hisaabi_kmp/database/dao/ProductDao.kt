package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.ProductEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    companion object {
        private const val SYNCED_STATUS = SyncStatus.SYNCED_VALUE
    }
    @Query("SELECT * FROM Product")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE id = :id AND status_id != 2")
    suspend fun getProductById(id: Int): ProductEntity?
    
    @Query("SELECT * FROM Product WHERE slug = :slug AND status_id != 2")
    suspend fun getProductBySlug(slug: String): ProductEntity?
    
    @Query("SELECT * FROM Product WHERE slug = :slug")
    suspend fun getProductBySlugAnyStatus(slug: String): ProductEntity?
    
    @Query("SELECT * FROM Product WHERE category_slug = :categorySlug")
    fun getProductsByCategory(categorySlug: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE business_slug = :businessSlug AND status_id != 2")
    fun getProductsByBusiness(businessSlug: String): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM Product WHERE sync_status != $SYNCED_STATUS AND business_slug = :businessSlug")
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

