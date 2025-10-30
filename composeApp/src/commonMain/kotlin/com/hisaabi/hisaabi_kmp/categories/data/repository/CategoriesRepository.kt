package com.hisaabi.hisaabi_kmp.categories.data.repository

import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.database.dao.CategoryDao
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp

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
    private val categoryDao: CategoryDao,
    private val slugGenerator: SlugGenerator
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
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_CATEGORY)
            ?: throw IllegalStateException("Failed to generate slug: Invalid session context")
        
        // Get current timestamp for both created_at and updated_at
        val now = getCurrentTimestamp()
        
        // Create entity with generated slug, business info, and timestamps
        val entity = category.toEntity().copy(
            slug = slug,
            business_slug = businessSlug,
            created_by = userSlug,
            created_at = now,
            updated_at = now
        )
        
        categoryDao.insertCategory(entity)
        return slug
    }
    
    override suspend fun updateCategory(category: Category): String {
        // Update only the updated_at timestamp, preserve created_at
        val entity = category.toEntity().copy(
            updated_at = getCurrentTimestamp()
        )
        categoryDao.updateCategory(entity)
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



