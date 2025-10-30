package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryTransactionDao {
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
    
    @Query("SELECT * FROM InventoryTransaction WHERE customer_slug = :customerSlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByCustomer(customerSlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE transaction_type = :transactionType AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
    fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>>
    
    @Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug and sync_status != 0")
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
        SELECT SUM(total_bill + additional_charges + tax - discount) 
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
        SELECT SUM(tax) 
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
}

