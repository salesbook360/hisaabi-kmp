package com.hisaabi.hisaabi_kmp.sync.domain.manager

import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.sync.data.datasource.SyncPreferencesDataSource
import com.hisaabi.hisaabi_kmp.sync.data.repository.SyncRepository
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncConfig
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncState
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Central manager for all sync operations
 * Coordinates sync-up and sync-down operations with proper error handling and progress tracking
 */
interface SyncManager {
    /**
     * Observable sync state
     */
    val syncState: StateFlow<SyncState>
    
    /**
     * Observable last sync time
     */
    val lastSyncTime: StateFlow<Instant?>
    
    /**
     * Trigger manual sync
     * @param syncUp If true, sync local changes to cloud
     * @param syncDown If true, sync cloud changes to local
     */
    suspend fun syncData(syncUp: Boolean = true, syncDown: Boolean = true)
    
    /**
     * Check if sync should be performed based on last sync time
     */
    suspend fun shouldSync(): Boolean
    
    /**
     * Get last sync time for current business
     */
    suspend fun getLastSyncTime(): Instant?
    
    /**
     * Start periodic background sync
     */
    fun startBackgroundSync()
    
    /**
     * Stop periodic background sync
     */
    fun stopBackgroundSync()
}

class SyncManagerImpl(
    private val syncRepository: SyncRepository,
    private val preferencesDataSource: SyncPreferencesDataSource,
    private val sessionManager: AppSessionManager
) : SyncManager {
    
    private val scope = CoroutineScope(Dispatchers.Default)
    private var backgroundSyncJob: Job? = null
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Instant?>(null)
    override val lastSyncTime: StateFlow<Instant?> = _lastSyncTime.asStateFlow()
    
    init {
        // Load last sync time on initialization
        scope.launch {
            val session = sessionManager.getSessionContext()
            if (session.isValid) {
                _lastSyncTime.value = preferencesDataSource.getLastSyncTime(
                    session.businessSlug!!,
                    session.userSlug!!
                )
            }
        }
        
        // Observe sync progress
        scope.launch {
            syncRepository.getSyncProgress().collect { progress ->
                progress?.let {
                    _syncState.value = SyncState.Progress(it)
                }
            }
        }
    }
    
    override suspend fun syncData(syncUp: Boolean, syncDown: Boolean) {
        val session = sessionManager.getSessionContext()
        
        if (!session.isValid) {
            _syncState.value = SyncState.Error("Invalid session: User or business not selected")
            return
        }
        
        val userSlug = session.userSlug!!
        val businessSlug = session.businessSlug!!
        
        _syncState.value = SyncState.InProgress
        
        try {
            // Sync Up - Local to Cloud
//            if (syncUp) {
//                syncUpData()
//            }
            
            // Sync Down - Cloud to Local
            if (syncDown) {
                syncDownData()
            }
            
            // Update last sync time
            val now = Clock.System.now()
            preferencesDataSource.setLastSyncTime(businessSlug, userSlug, now)
            _lastSyncTime.value = now
            
            _syncState.value = SyncState.Success(now)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Unknown error occurred during sync")
        }
    }
    
    private suspend fun syncUpData() {
        try {
            // Order matters - dependencies should be synced first
            
            // 1. Media
            syncRepository.syncMediaUp()
                .onFailure { throw it }
            
            // 2. Categories
            syncRepository.syncCategoriesUp()
                .onFailure { throw it }
            
            // 3. Payment Methods
            syncRepository.syncPaymentMethodsUp()
                .onFailure { throw it }
            
            // 4. Quantity Units
            syncRepository.syncQuantityUnitsUp()
                .onFailure { throw it }
            
            // 5. Warehouses
            syncRepository.syncWarehousesUp()
                .onFailure { throw it }
            
            // 6. Products (depends on categories, quantity units)
            syncRepository.syncProductsUp()
                .onFailure { throw it }
            
            // 7. Parties (Customers/Suppliers)
            syncRepository.syncPartiesUp()
                .onFailure { throw it }
            
            // 8. Transactions (depends on parties, payment methods, warehouses)
            syncRepository.syncTransactionsUp()
                .onFailure { throw it }
            
            // 9. Transaction Details (depends on transactions, products)
            syncRepository.syncTransactionDetailsUp()
                .onFailure { throw it }
            
            // 10. Product Quantities (depends on products, warehouses)
            syncRepository.syncProductQuantitiesUp()
                .onFailure { throw it }
            
            // 11. Deleted Records
            syncRepository.syncDeletedRecordsUp()
                .onFailure { throw it }
                
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(
                message = e.message ?: "Sync up failed",
                syncType = SyncType.SYNC_UP
            )
            throw e
        }
    }
    
    private suspend fun syncDownData() {
        try {
            val session = sessionManager.getSessionContext().requireValid()
            val lastSyncTime = "1970-01-01T00%3A00%3A00Z" // TODO: getLastSyncTimeFormatted(session.businessSlug!!, session.userSlug!!)
            
            // Order matters - dependencies should be synced first
            
            // 1. Media
//            syncRepository.syncMediaDown(lastSyncTime)
//                .onFailure { throw it }
//
            // 2. Categories
            syncRepository.syncCategoriesDown(lastSyncTime)
                .onFailure { throw it }
            
            // 3. Payment Methods
//            syncRepository.syncPaymentMethodsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 4. Quantity Units
//            syncRepository.syncQuantityUnitsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 5. Warehouses
//            syncRepository.syncWarehousesDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 6. Products (depends on categories, quantity units)
//            syncRepository.syncProductsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 7. Recipe Ingredients (depends on products)
//            syncRepository.syncRecipeIngredientsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 8. Parties (Customers/Suppliers)
//            syncRepository.syncPartiesDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 9. Transactions (depends on parties, payment methods, warehouses)
//            syncRepository.syncTransactionsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 10. Transaction Details (depends on transactions, products)
//            syncRepository.syncTransactionDetailsDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 11. Product Quantities (depends on products, warehouses)
//            syncRepository.syncProductQuantitiesDown(lastSyncTime)
//                .onFailure { throw it }
//
//            // 12. Deleted Records (returns timestamp)
            val timestamp = syncRepository.syncDeletedRecordsDown(lastSyncTime)
                .getOrThrow()
                
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(
                message = e.message ?: "Sync down failed",
                syncType = SyncType.SYNC_DOWN
            )
            throw e
        }
    }
    
    override suspend fun shouldSync(): Boolean {
        val session = sessionManager.getSessionContext()
        if (!session.isValid) return false
        
        return preferencesDataSource.shouldSync(
            businessSlug = session.businessSlug!!,
            userSlug = session.userSlug!!,
            intervalMillis = SyncConfig.SYNC_INTERVAL.inWholeMilliseconds
        )
    }
    
    override suspend fun getLastSyncTime(): Instant? {
        val session = sessionManager.getSessionContext()
        if (!session.isValid) return null
        
        return preferencesDataSource.getLastSyncTime(
            businessSlug = session.businessSlug!!,
            userSlug = session.userSlug!!
        )
    }
    
    private suspend fun getLastSyncTimeFormatted(businessSlug: String, userSlug: String): String {
        val lastSync = preferencesDataSource.getLastSyncTime(businessSlug, userSlug)
        return lastSync?.toString() ?: "1970-01-01T00:00:00Z" // Unix epoch if never synced
    }
    
    override fun startBackgroundSync() {
        if (backgroundSyncJob?.isActive == true) return
        
        backgroundSyncJob = scope.launch {
            while (true) {
                try {
                    if (shouldSync()) {
                        syncData(syncUp = true, syncDown = true)
                    }
                } catch (e: Exception) {
                    // Log error but don't stop background sync
                    println("Background sync error: ${e.message}")
                }
                
                // Wait for sync interval before next sync
                kotlinx.coroutines.delay(SyncConfig.SYNC_INTERVAL)
            }
        }
    }
    
    override fun stopBackgroundSync() {
        backgroundSyncJob?.cancel()
        backgroundSyncJob = null
    }
}

