package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryTransactionDao {
    companion object {
        private const val SYNCED_STATUS = SyncStatus.SYNCED_VALUE
    }
    @Query("SELECT * FROM InventoryTransaction WHERE (parent_slug IS NULL OR parent_slug = '') AND status_id != 2 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE id = :id")
    suspend fun getTransactionById(id: Int): InventoryTransactionEntity?
    
    @Query("SELECT * FROM InventoryTransaction WHERE slug = :slug")
    suspend fun getTransactionBySlug(slug: String): InventoryTransactionEntity?
    
    @Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug AND status_id != 2 ORDER BY timestamp ASC")
    fun getChildTransactions(parentSlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug AND status_id != 2 ORDER BY timestamp ASC")
    suspend fun getChildTransactionsList(parentSlug: String): List<InventoryTransactionEntity>
    
    @Query("SELECT * FROM InventoryTransaction WHERE party_slug = :partySlug AND (parent_slug IS NULL OR parent_slug = '') AND status_id != 2 ORDER BY timestamp DESC")
    fun getTransactionsByCustomer(partySlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE transaction_type = :transactionType AND (parent_slug IS NULL OR parent_slug = '') AND status_id != 2 ORDER BY timestamp DESC")
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug AND (parent_slug IS NULL OR parent_slug = '') AND status_id != 2 ORDER BY timestamp DESC")
    fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>>
    
    // Paginated query - sorted by transaction date (timestamp)
    // All filters are applied at DB level including party area/category via subquery
    // Note: filterByTypes = 1 means apply the transaction type filter, -1 means skip it (using -1 to avoid conflict with SALE type which is 0)
    // Status filter: showActive=true showDeleted=false -> only active (status_id=0)
    //                showActive=false showDeleted=true -> only deleted (status_id=2)
    //                showActive=true showDeleted=true -> both active and deleted
    //                showActive=false showDeleted=false -> none (empty result)
    @Query("""
        SELECT * FROM InventoryTransaction 
        WHERE business_slug = :businessSlug 
        AND (parent_slug IS NULL OR parent_slug = '')
        AND (
            (:showActive = 1 AND :showDeleted = 1) OR
            (:showActive = 1 AND :showDeleted = 0 AND status_id = 0) OR
            (:showActive = 0 AND :showDeleted = 1 AND status_id = 2)
        )
        AND (:partySlug IS NULL OR party_slug = :partySlug)
        AND (:filterByTypes = -1 OR transaction_type IN (:transactionTypes))
        AND (:startDate IS NULL OR CAST(timestamp AS INTEGER) >= :startDate)
        AND (:endDate IS NULL OR CAST(timestamp AS INTEGER) <= :endDate)
        AND (:searchQuery = '' OR slug LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' 
             OR party_slug IN (SELECT slug FROM Party WHERE name LIKE '%' || :searchQuery || '%'))
        AND (:idOrSlugFilter = '' OR slug = :idOrSlugFilter OR slug LIKE '%' || :idOrSlugFilter || '%' OR CAST(id AS TEXT) = :idOrSlugFilter)
        AND (:areaSlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE area_slug = :areaSlug))
        AND (:categorySlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE category_slug = :categorySlug))
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTransactionsPaginated(
        businessSlug: String,
        partySlug: String?,
        filterByTypes: Int,
        transactionTypes: List<Int>,
        startDate: Long?,
        endDate: Long?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?,
        showActive: Boolean,
        showDeleted: Boolean,
        limit: Int,
        offset: Int
    ): List<InventoryTransactionEntity>
    
    // Paginated query - sorted by entry date (created_at)
    @Query("""
        SELECT * FROM InventoryTransaction 
        WHERE business_slug = :businessSlug 
        AND (parent_slug IS NULL OR parent_slug = '')
        AND (
            (:showActive = 1 AND :showDeleted = 1) OR
            (:showActive = 1 AND :showDeleted = 0 AND status_id = 0) OR
            (:showActive = 0 AND :showDeleted = 1 AND status_id = 2)
        )
        AND (:partySlug IS NULL OR party_slug = :partySlug)
        AND (:filterByTypes = -1 OR transaction_type IN (:transactionTypes))
        AND (:startDate IS NULL OR created_at >= :startDateStr)
        AND (:endDate IS NULL OR created_at <= :endDateStr)
        AND (:searchQuery = '' OR slug LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'
             OR party_slug IN (SELECT slug FROM Party WHERE name LIKE '%' || :searchQuery || '%'))
        AND (:idOrSlugFilter = '' OR slug = :idOrSlugFilter OR slug LIKE '%' || :idOrSlugFilter || '%' OR CAST(id AS TEXT) = :idOrSlugFilter)
        AND (:areaSlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE area_slug = :areaSlug))
        AND (:categorySlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE category_slug = :categorySlug))
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTransactionsPaginatedByEntryDate(
        businessSlug: String,
        partySlug: String?,
        filterByTypes: Int,
        transactionTypes: List<Int>,
        startDate: Long?,
        startDateStr: String?,
        endDate: Long?,
        endDateStr: String?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?,
        showActive: Boolean,
        showDeleted: Boolean,
        limit: Int,
        offset: Int
    ): List<InventoryTransactionEntity>
    
    // Count query for pagination - matching paginated query filters
    @Query("""
        SELECT COUNT(*) FROM InventoryTransaction 
        WHERE business_slug = :businessSlug 
        AND (parent_slug IS NULL OR parent_slug = '')
        AND (
            (:showActive = 1 AND :showDeleted = 1) OR
            (:showActive = 1 AND :showDeleted = 0 AND status_id = 0) OR
            (:showActive = 0 AND :showDeleted = 1 AND status_id = 2)
        )
        AND (:partySlug IS NULL OR party_slug = :partySlug)
        AND (:filterByTypes = -1 OR transaction_type IN (:transactionTypes))
        AND (:startDate IS NULL OR CAST(timestamp AS INTEGER) >= :startDate)
        AND (:endDate IS NULL OR CAST(timestamp AS INTEGER) <= :endDate)
        AND (:searchQuery = '' OR slug LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'
             OR party_slug IN (SELECT slug FROM Party WHERE name LIKE '%' || :searchQuery || '%'))
        AND (:idOrSlugFilter = '' OR slug = :idOrSlugFilter OR slug LIKE '%' || :idOrSlugFilter || '%' OR CAST(id AS TEXT) = :idOrSlugFilter)
        AND (:areaSlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE area_slug = :areaSlug))
        AND (:categorySlug IS NULL OR party_slug IN (SELECT slug FROM Party WHERE category_slug = :categorySlug))
    """)
    suspend fun getTransactionsCount(
        businessSlug: String,
        partySlug: String?,
        filterByTypes: Int,
        transactionTypes: List<Int>,
        startDate: Long?,
        endDate: Long?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?,
        showActive: Boolean,
        showDeleted: Boolean
    ): Int
    
    @Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug AND sync_status != $SYNCED_STATUS")
    suspend fun getUnsyncedTransactions(businessSlug:String): List<InventoryTransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransactionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<InventoryTransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: InventoryTransactionEntity)
    
    @Delete
    suspend fun deleteTransaction(transaction: InventoryTransactionEntity)
    
    @Query("DELETE FROM InventoryTransaction WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
    
    @Query("UPDATE InventoryTransaction SET status_id = 2 WHERE slug = :slug")
    suspend fun softDeleteTransactionBySlug(slug: String)
    
    @Query("UPDATE InventoryTransaction SET status_id = 2 WHERE id = :id")
    suspend fun softDeleteTransactionById(id: Int)
    
    @Query("DELETE FROM InventoryTransaction")
    suspend fun deleteAllTransactions()
    
    // Dashboard Queries
    @Query("""
        SELECT COUNT(*) FROM InventoryTransaction 
        WHERE transaction_type IN (:transactionTypes) 
        AND business_slug = :businessSlug 
        AND status_id != 2 
        AND timestamp BETWEEN :fromMilli AND :toMilli
    """)
    suspend fun getTotalTransactionsCount(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Int?
    
    @Query("""
        SELECT SUM(total_bill + additional_charges + flat_tax - flat_discount) 
        FROM InventoryTransaction 
        WHERE transaction_type IN (:transactionTypes) 
        AND business_slug = :businessSlug 
        AND status_id != 2 
        AND timestamp BETWEEN :fromMilli AND :toMilli
    """)
    suspend fun getTotalRevenue(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Double?
    
    @Query("""
        SELECT SUM(total_paid) 
        FROM InventoryTransaction 
        WHERE transaction_type IN (:transactionTypes) 
        AND business_slug = :businessSlug 
        AND status_id != 2 
        AND timestamp BETWEEN :fromMilli AND :toMilli
    """)
    suspend fun getTotalPaid(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Double?
    
    @Query("""
        SELECT SUM(flat_tax) 
        FROM InventoryTransaction 
        WHERE transaction_type IN (:transactionTypes) 
        AND business_slug = :businessSlug 
        AND status_id != 2 
        AND timestamp BETWEEN :fromMilli AND :toMilli
    """)
    suspend fun getTotalTax(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Double?
    
    @Query("""
        SELECT slug FROM InventoryTransaction 
        WHERE transaction_type IN (:transactionTypes) 
        AND business_slug = :businessSlug 
        AND status_id != 2 
        AND timestamp BETWEEN :fromMilli AND :toMilli
    """)
    suspend fun getTransactionSlugs(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): List<String>
    
    @Query("SELECT MAX(id) FROM InventoryTransaction")
    suspend fun getMaxId(): Int?
    
    // Report Queries
    @Query("""
        SELECT * FROM InventoryTransaction 
        WHERE business_slug = :businessSlug 
        AND transaction_type IN (:transactionTypes)
        AND status_id != 2
        AND timestamp BETWEEN :fromDate AND :toDate
        ORDER BY timestamp DESC
    """)
    suspend fun getTransactionsForReport(
        businessSlug: String,
        transactionTypes: List<Int>,
        fromDate: String,
        toDate: String
    ): List<InventoryTransactionEntity>
}

