package com.hisaabi.hisaabi_kmp.categories.data.repository

import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity

interface CategoriesRepository {
    suspend fun getCategoriesByType(
        categoryType: CategoryType,
        businessSlug: String
    ): List<Category>
    
    suspend fun addCategory(
        category: Category,
        businessSlug: String,
        userSlug: String
    ): String
    
    suspend fun updateCategory(category: Category): String
    
    suspend fun deleteCategory(categorySlug: String)
}

class CategoriesRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoriesRepository {
    
    override suspend fun getCategoriesByType(
        categoryType: CategoryType,
        businessSlug: String
    ): List<Category> {
        val entities = categoryDao.getCategoriesByTypeAndBusiness(categoryType.type, businessSlug)
        return entities.map { it.toDomainModel() }
    }
    
    override suspend fun addCategory(
        category: Category,
        businessSlug: String,
        userSlug: String
    ): String {
        val entity = category.toEntity()
        val newId = categoryDao.insertCategory(entity)
        
        // Generate slug based on ID
        val slug = "CAT_${newId}"
        
        // Update the category with generated slug and business info
        val updatedEntity = entity.copy(
            id = newId.toInt(),
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug
        )
        
        categoryDao.updateCategory(updatedEntity)
        return slug
    }
    
    override suspend fun updateCategory(category: Category): String {
        categoryDao.updateCategory(category.toEntity())
        return category.slug
    }
    
    override suspend fun deleteCategory(categorySlug: String) {
        val category = categoryDao.getCategoryBySlug(categorySlug)
        if (category != null) {
            categoryDao.deleteCategory(category)
        }
    }
}

// Extension function to convert entity to domain model
fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        title = title ?: "",
        description = description,
        thumbnail = thumbnail,
        typeId = type_id,
        slug = slug ?: "",
        businessSlug = business_slug,
        createdBy = created_by,
        syncStatus = sync_status,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

// Extension function to convert domain model to entity
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        title = title,
        description = description,
        thumbnail = thumbnail,
        type_id = typeId,
        slug = slug,
        business_slug = businessSlug,
        created_by = createdBy,
        sync_status = syncStatus,
        created_at = createdAt,
        updated_at = updatedAt
    )
}



