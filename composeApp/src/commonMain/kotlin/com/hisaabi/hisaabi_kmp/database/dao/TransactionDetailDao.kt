package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.*
import com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDetailDao {
    @Query("SELECT * FROM TransactionDetail")
    fun getAllTransactionDetails(): Flow<List<TransactionDetailEntity>>
    
    @Query("SELECT * FROM TransactionDetail WHERE id = :id")
    suspend fun getTransactionDetailById(id: Int): TransactionDetailEntity?
    
    @Query("SELECT * FROM TransactionDetail WHERE transaction_slug = :transactionSlug")
    fun getDetailsByTransaction(transactionSlug: String): Flow<List<TransactionDetailEntity>>
    
    @Query("SELECT COUNT(*) FROM TransactionDetail WHERE transaction_slug = :transactionSlug")
    suspend fun getDetailsCountByTransaction(transactionSlug: String): Int
    
    @Query("SELECT * FROM TransactionDetail WHERE product_slug = :productSlug")
    fun getDetailsByProduct(productSlug: String): Flow<List<TransactionDetailEntity>>
    
    @Query("SELECT * FROM TransactionDetail WHERE sync_status != 0")
    suspend fun getUnsyncedDetails(): List<TransactionDetailEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionDetail(detail: TransactionDetailEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>)
    
    @Update
    suspend fun updateTransactionDetail(detail: TransactionDetailEntity)
    
    @Delete
    suspend fun deleteTransactionDetail(detail: TransactionDetailEntity)
    
    @Query("DELETE FROM TransactionDetail WHERE id = :id")
    suspend fun deleteTransactionDetailById(id: Int)
    
    @Query("DELETE FROM TransactionDetail WHERE transaction_slug = :transactionSlug")
    suspend fun deleteDetailsByTransaction(transactionSlug: String)
    
    @Query("DELETE FROM TransactionDetail")
    suspend fun deleteAllTransactionDetails()
    
    // Dashboard Queries
    @Query("""
        SELECT SUM(profit + flat_tax - flat_discount) 
        FROM TransactionDetail 
        WHERE transaction_slug IN (:transactionSlugs)
    """)
    suspend fun calculateTotalProfit(transactionSlugs: List<String>): Double?
    
    @Query("""
        SELECT SUM(price * quantity - profit) 
        FROM TransactionDetail 
        WHERE transaction_slug IN (:transactionSlugs)
    """)
    suspend fun calculateTotalCost(transactionSlugs: List<String>): Double?
    
    @Query("""
        SELECT SUM(flat_tax) 
        FROM TransactionDetail 
        WHERE transaction_slug IN (:transactionSlugs)
    """)
    suspend fun calculateDetailTax(transactionSlugs: List<String>): Double?
    
    @Query("""
        SELECT SUM(quantity) 
        FROM TransactionDetail 
        WHERE transaction_slug IN (:transactionSlugs)
    """)
    suspend fun calculateTotalQuantity(transactionSlugs: List<String>): Double?
}

