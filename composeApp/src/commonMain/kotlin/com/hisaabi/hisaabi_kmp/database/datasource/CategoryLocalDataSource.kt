package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryLocalDataSource {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: Int): CategoryEntity?
    suspend fun getCategoryBySlug(slug: String): CategoryEntity?
    fun getCategoriesByType(typeId: Int): Flow<List<CategoryEntity>>
    fun getCategoriesByBusiness(businessSlug: String): Flow<List<CategoryEntity>>
    suspend fun getUnsyncedCategories(businessSlug: String): List<CategoryEntity>
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun insertCategories(categories: List<CategoryEntity>)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
    suspend fun deleteCategoryById(id: Int)
    suspend fun deleteAllCategories()
}

class CategoryLocalDataSourceImpl(
    private val categoryDao: CategoryDao
) : CategoryLocalDataSource {
    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    
    override suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)
    
    override suspend fun getCategoryBySlug(slug: String): CategoryEntity? = categoryDao.getCategoryBySlug(slug)
    
    override fun getCategoriesByType(typeId: Int): Flow<List<CategoryEntity>> = 
        categoryDao.getCategoriesByType(typeId)
    
    override fun getCategoriesByBusiness(businessSlug: String): Flow<List<CategoryEntity>> = 
        categoryDao.getCategoriesByBusiness(businessSlug)
    
    override suspend fun getUnsyncedCategories(businessSlug: String): List<CategoryEntity> = 
        categoryDao.getUnsyncedCategories(businessSlug)
    
    override suspend fun insertCategory(category: CategoryEntity): Long = 
        categoryDao.insertCategory(category)
    
    override suspend fun insertCategories(categories: List<CategoryEntity>) = 
        categoryDao.insertCategories(categories)
    
    override suspend fun updateCategory(category: CategoryEntity) = 
        categoryDao.updateCategory(category)
    
    override suspend fun deleteCategory(category: CategoryEntity) = 
        categoryDao.deleteCategory(category)
    
    override suspend fun deleteCategoryById(id: Int) = 
        categoryDao.deleteCategoryById(id)
    
    override suspend fun deleteAllCategories() = 
        categoryDao.deleteAllCategories()
}

