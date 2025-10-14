package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM Category WHERE id = :id")
    suspend fun getCategoryById(id: Int): CategoryEntity?
    
    @Query("SELECT * FROM Category WHERE slug = :slug")
    suspend fun getCategoryBySlug(slug: String): CategoryEntity?
    
    @Query("SELECT * FROM Category WHERE type_id = :typeId")
    fun getCategoriesByType(typeId: Int): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM Category WHERE business_slug = :businessSlug")
    fun getCategoriesByBusiness(businessSlug: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM Category WHERE sync_status != 0")
    suspend fun getUnsyncedCategories(): List<CategoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM Category WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)
    
    @Query("DELETE FROM Category")
    suspend fun deleteAllCategories()
    
    // Dashboard Queries
    @Query("""
        SELECT COUNT(*) FROM Category 
        WHERE type_id = 1 
        AND sync_status != 3 
        AND business_slug = :businessSlug
    """)
    suspend fun getTotalProductCategories(businessSlug: String): Int?
}

