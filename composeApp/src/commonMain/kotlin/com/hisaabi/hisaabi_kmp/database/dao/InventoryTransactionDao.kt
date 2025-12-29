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
    @Query("SELECT * FROM InventoryTransaction WHERE parent_slug IS NULL OR parent_slug = '' ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE id = :id")
    suspend fun getTransactionById(id: Int): InventoryTransactionEntity?
    
    @Query("SELECT * FROM InventoryTransaction WHERE slug = :slug")
    suspend fun getTransactionBySlug(slug: String): InventoryTransactionEntity?
    
    @Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug ORDER BY timestamp ASC")
    fun getChildTransactions(parentSlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug ORDER BY timestamp ASC")
    suspend fun getChildTransactionsList(parentSlug: String): List<InventoryTransactionEntity>
    
    @Query("SELECT * FROM InventoryTransaction WHERE party_slug = :partySlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByCustomer(partySlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE transaction_type = :transactionType AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>>
    
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

