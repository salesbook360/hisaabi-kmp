package com.hisaabi.hisaabi_kmp.sync.data.repository

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.database.dao.*
import com.hisaabi.hisaabi_kmp.database.entity.*
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncPreferencesDataSource
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncRemoteDataSource
import com.hisaabi.hisaabi_kmp.sync.data.mapper.*
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncProgress
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

/**
 * Repository for handling all sync operations
 * Coordinates between local database and remote API
 */
interface SyncRepository {
    suspend fun syncCategoriesUp(): Result<Unit>
    suspend fun syncCategoriesDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncProductsUp(): Result<Unit>
    suspend fun syncProductsDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncPartiesUp(): Result<Unit>
    suspend fun syncPartiesDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncPaymentMethodsUp(): Result<Unit>
    suspend fun syncPaymentMethodsDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncQuantityUnitsUp(): Result<Unit>
    suspend fun syncQuantityUnitsDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncWarehousesUp(): Result<Unit>
    suspend fun syncWarehousesDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncTransactionsUp(): Result<Unit>
    suspend fun syncTransactionsDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncTransactionDetailsDown(lastSyncTime: String): Result<Unit>
    suspend fun syncProductQuantitiesDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncMediaUp(): Result<Unit>
    suspend fun syncMediaDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncRecipeIngredientsUp(): Result<Unit>
    suspend fun syncRecipeIngredientsDown(lastSyncTime: String): Result<Unit>
    
    suspend fun syncDeletedRecordsUp(): Result<Unit>
    suspend fun syncDeletedRecordsDown(lastSyncTime: String): Result<String?>
    
    fun getSyncProgress(): Flow<SyncProgress?>
}

class SyncRepositoryImpl(
    private val remoteDataSource: SyncRemoteDataSource,
    private val preferencesDataSource: SyncPreferencesDataSource,
    private val sessionManager: AppSessionManager,
    private val categoryDao: CategoryDao,
    private val productDao: ProductDao,
    private val partyDao: PartyDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val quantityUnitDao: QuantityUnitDao,
    private val warehouseDao: WareHouseDao,
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val productQuantitiesDao: ProductQuantitiesDao,
    private val entityMediaDao: EntityMediaDao,
    private val recipeIngredientsDao: RecipeIngredientsDao,
    private val deletedRecordsDao: DeletedRecordsDao
) : SyncRepository {
    
    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    
    override fun getSyncProgress(): Flow<SyncProgress?> = _syncProgress
    
    private fun updateProgress(recordType: String, completed: Int, total: Int, syncType: SyncType) {
        _syncProgress.value = SyncProgress(recordType, completed, total, syncType)
    }
    
    // Categories
    override suspend fun syncCategoriesUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = categoryDao.getUnsyncedCategories(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Categories", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncCategoriesUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                categoryDao.updateCategory(updated)
            }
        }
        
        updateProgress("Categories", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncCategoriesDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Categories", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncCategoriesDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                categoryDao.insertCategory(entity)
            }
        }
        
        updateProgress("Categories", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Products
    override suspend fun syncProductsUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = productDao.getUnsyncedProducts(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Products", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncProductsUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                productDao.updateProduct(updated)
            }
        }
        
        updateProgress("Products", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncProductsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Products", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncProductsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                productDao.insertProduct(entity)
            }
        }
        
        updateProgress("Products", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Parties (Customers/Suppliers)
    override suspend fun syncPartiesUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = partyDao.getUnsyncedParties(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Parties", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncPartiesUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                partyDao.updateParty(updated)
            }
        }
        
        updateProgress("Parties", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncPartiesDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Parties", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncPartiesDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                partyDao.insertParty(entity)
            }
        }
        
        updateProgress("Parties", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Payment Methods
    override suspend fun syncPaymentMethodsUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = paymentMethodDao.getUnsyncedPaymentMethods(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Payment Methods", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncPaymentMethodsUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                paymentMethodDao.updatePaymentMethod(updated)
            }
        }
        
        updateProgress("Payment Methods", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncPaymentMethodsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Payment Methods", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncPaymentMethodsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                paymentMethodDao.insertPaymentMethod(entity)
            }
        }
        
        updateProgress("Payment Methods", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Quantity Units
    override suspend fun syncQuantityUnitsUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = quantityUnitDao.getUnsyncedUnits(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Quantity Units", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }

        val response = remoteDataSource.syncQuantityUnitsUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                quantityUnitDao.updateUnit(updated)
            }
        }
        
        updateProgress("Quantity Units", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncQuantityUnitsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Quantity Units", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncQuantityUnitsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                quantityUnitDao.insertUnit(entity)
            }
        }
        
        updateProgress("Quantity Units", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Warehouses
    override suspend fun syncWarehousesUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = warehouseDao.getUnsyncedWareHouses(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Warehouses", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        val response = remoteDataSource.syncWarehousesUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                warehouseDao.updateWareHouse(updated)
            }
        }
        
        updateProgress("Warehouses", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncWarehousesDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Warehouses", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncWarehousesDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                warehouseDao.insertWareHouse(entity)
            }
        }
        
        updateProgress("Warehouses", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Transactions
    override suspend fun syncTransactionsUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = transactionDao.getUnsyncedTransactions(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Transactions", 0, unsynced.size, SyncType.SYNC_UP)
        
        // Fetch transaction details for all transactions
        val transactionSlugs = unsynced.mapNotNull { it.slug }
        val allDetails = if (transactionSlugs.isNotEmpty()) {
            transactionDetailDao.getDetailsByTransactionSlugs(transactionSlugs)
        } else {
            emptyList()
        }
        
        // Group details by transaction slug
        val detailsByTransaction = allDetails.groupBy { it.transaction_slug }
        
        // Map transactions to DTOs with their details
        val dtos = unsynced.map { transaction ->
            val details = detailsByTransaction[transaction.slug]?.map { it.toDto() } ?: emptyList()
            transaction.toDto().copy(transactionDetails = details)
        }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncTransactionsUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original transactions as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                transactionDao.updateTransaction(updated)
            }
            // Also mark transaction details as synced
            allDetails.forEach { detail ->
                val updated = detail.copy(sync_status = SyncStatus.SYNCED.value)
                transactionDetailDao.updateTransactionDetail(updated)
            }
        }
        
        updateProgress("Transactions", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncTransactionsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Transactions", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncTransactionsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                // Save transaction (details synced separately)
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                transactionDao.insertTransaction(entity)
            }
        }
        
        updateProgress("Transactions", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Transaction Details (Sync Down Only)
    override suspend fun syncTransactionDetailsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Transaction Details", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncTransactionDetailsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                transactionDetailDao.insertTransactionDetail(entity)
            }
        }
        
        updateProgress("Transaction Details", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Product Quantities (Sync Down Only)
    override suspend fun syncProductQuantitiesDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Product Quantities", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncProductQuantitiesDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                productQuantitiesDao.insertProductQuantity(entity)
            }
        }
        
        updateProgress("Product Quantities", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Media
    override suspend fun syncMediaUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = entityMediaDao.getUnsyncedMedia(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Media", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.syncMediaUp(dtos)
        
        if (response.isSuccess()) {
            // Mark original entities as synced
            unsynced.forEach { entity ->
                val updated = entity.copy(sync_status = SyncStatus.SYNCED.value)
                entityMediaDao.updateEntityMedia(updated)
            }
        }
        
        updateProgress("Media", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncMediaDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Media", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncMediaDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                entityMediaDao.insertEntityMedia(entity)
            }
        }
        
        updateProgress("Media", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Recipe Ingredients
    override suspend fun syncRecipeIngredientsUp(): Result<Unit> = runCatching {
//        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
//        val unsynced = recipeIngredientsDao.getUnsyncedRecipeIngredients(businessSlug)
//
//        if (unsynced.isEmpty()) return@runCatching
//
//        updateProgress("Recipe Ingredients", 0, unsynced.size, SyncType.SYNC_UP)
//
//        val dtos = unsynced.map { it.toDto() }
//        val response = remoteDataSource.syncRecipeIngredientsUp(dtos)
//
//        if (response.isSuccess()) {
//            response.data?.list?.forEach { dto ->
//                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
//                recipeIngredientsDao.updateRecipeIngredient(entity)
//            }
//        }
        
       // updateProgress("Recipe Ingredients", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncRecipeIngredientsDown(lastSyncTime: String): Result<Unit> = runCatching {
        updateProgress("Recipe Ingredients", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncRecipeIngredientsDown(lastSyncTime)
        
        if (response.isSuccess()) {
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                recipeIngredientsDao.insertRecipeIngredient(entity)
            }
        }
        
        updateProgress("Recipe Ingredients", 1, 1, SyncType.SYNC_DOWN)
    }
    
    // Deleted Records
    override suspend fun syncDeletedRecordsUp(): Result<Unit> = runCatching {
        val businessSlug = sessionManager.getBusinessSlug() ?: return@runCatching
        val unsynced = deletedRecordsDao.getUnsyncedDeletedRecords(businessSlug)
        
        if (unsynced.isEmpty()) return@runCatching
        
        updateProgress("Deleted Records", 0, unsynced.size, SyncType.SYNC_UP)
        
        val dtos = unsynced.map { it.toDto() }
        
        // Double-check before API call
        if (dtos.isEmpty()) return@runCatching
        
        val response = remoteDataSource.deleteRecords(dtos)
        
        if (response.isSuccess()) {
            // Mark original records as synced
            unsynced.forEach { record ->
                val updated = record.copy(sync_status = SyncStatus.SYNCED.value)
                deletedRecordsDao.updateDeletedRecord(updated)
            }
        }
        
        updateProgress("Deleted Records", unsynced.size, unsynced.size, SyncType.SYNC_UP)
    }
    
    override suspend fun syncDeletedRecordsDown(lastSyncTime: String): Result<String?> = runCatching {
        updateProgress("Deleted Records", 0, 1, SyncType.SYNC_DOWN)
        
        val response = remoteDataSource.syncDeletedRecordsDown(lastSyncTime)
        var timestamp: String? = null
        
        if (response.isSuccess()) {
            timestamp = response.timestamp
            response.data?.list?.forEach { dto ->
                val entity = dto.toEntity().copy(sync_status = SyncStatus.SYNCED.value)
                deletedRecordsDao.insertDeletedRecord(entity)
            }
        }
        
        updateProgress("Deleted Records", 1, 1, SyncType.SYNC_DOWN)
        timestamp
    }
}

