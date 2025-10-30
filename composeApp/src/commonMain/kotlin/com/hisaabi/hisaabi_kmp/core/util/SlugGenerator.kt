package com.hisaabi.hisaabi_kmp.core.util

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.database.dao.*

/**
 * Centralized slug generator for all entities.
 * Template: "$businessSlug-$activeUserSlug-$ACTIVE_DEVICE_SLUG-$entitySlug-${id}"
 * 
 * Example: "BUS123-USR456-A-PA-1"
 *   - BUS123: Business slug
 *   - USR456: User slug
 *   - A: Android device (A/I/W/D for Android/iOS/Web/Desktop)
 *   - PA: Party entity
 *   - 1: Incremented max ID
 */
class SlugGenerator(
    private val sessionManager: AppSessionManager,
    private val partyDao: PartyDao,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
    private val inventoryTransactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val quantityUnitDao: QuantityUnitDao,
    private val wareHouseDao: WareHouseDao,
    private val entityMediaDao: EntityMediaDao,
    private val deletedRecordsDao: DeletedRecordsDao,
    private val recipeIngredientsDao: RecipeIngredientsDao,
    private val productQuantitiesDao: ProductQuantitiesDao
) {
    
    /**
     * Generates a slug for the given entity type.
     * Returns the generated slug that should be used when inserting the entity.
     * 
     * @param entityType The type of entity for which to generate the slug
     * @return Generated slug string, or null if session context is invalid
     */
    suspend fun generateSlug(entityType: EntityTypeEnum): String? {
        // Get session context
        val sessionContext = sessionManager.getSessionContext()
        if (!sessionContext.isValid) {
            println("Warning: Invalid session context when generating slug for ${entityType.entityName}")
            return null
        }
        
        val businessSlug = sessionContext.businessSlug!!
        val userSlug = sessionContext.userSlug!!
        val deviceSlug = PlatformUtil.getDeviceSlug()
        val entitySlug = entityType.entitySlug
        
        // Get max ID for the entity and increment it
        val maxId = getMaxId(entityType)
        val nextId = maxId + 1
        
        // Generate slug using template
        return "$businessSlug-$userSlug-$deviceSlug-$entitySlug-$nextId"
    }
    
    /**
     * Gets the maximum ID from the entity's table.
     * Returns 0 if table is empty.
     */
    private suspend fun getMaxId(entityType: EntityTypeEnum): Int {
        return when (entityType) {
            EntityTypeEnum.ENTITY_TYPE_PARTY -> partyDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_CATEGORY -> categoryDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_PRODUCT -> productDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_TRANSACTION -> inventoryTransactionDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_TRANSACTION_DETAIL -> transactionDetailDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_PAYMENT_METHOD -> paymentMethodDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_QUANTITY_UNIT -> quantityUnitDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_WAREHOUSE -> wareHouseDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_ENTITY_MEDIA -> entityMediaDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_DELETED_RECORDS -> deletedRecordsDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_RECIPE_INGREDIENTS -> recipeIngredientsDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_PRODUCT_QUANTITIES -> productQuantitiesDao.getMaxId() ?: 0
            EntityTypeEnum.ENTITY_TYPE_ALL_RECORDS -> 0 // Not a real table
        }
    }
}

