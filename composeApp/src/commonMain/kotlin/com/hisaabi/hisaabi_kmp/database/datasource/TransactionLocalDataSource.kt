package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity
import kotlinx.coroutines.flow.Flow

class TransactionLocalDataSource(
    private val transactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao
) {
    // Transaction operations
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
    
    suspend fun getTransactionById(id: Int): InventoryTransactionEntity? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun getTransactionBySlug(slug: String): InventoryTransactionEntity? {
        return transactionDao.getTransactionBySlug(slug)
    }
    
    fun getTransactionsByCustomer(customerSlug: String): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getTransactionsByCustomer(customerSlug)
    }
    
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getTransactionsByType(transactionType)
    }
    
    suspend fun insertTransaction(transaction: InventoryTransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }
    
    suspend fun updateTransaction(transaction: InventoryTransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }
    
    suspend fun deleteTransaction(transaction: InventoryTransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
    
    suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }
    
    // Transaction Detail operations
    fun getDetailsByTransaction(transactionSlug: String): Flow<List<TransactionDetailEntity>> {
        return transactionDetailDao.getDetailsByTransaction(transactionSlug)
    }
    
    suspend fun insertTransactionDetail(detail: TransactionDetailEntity): Long {
        return transactionDetailDao.insertTransactionDetail(detail)
    }
    
    suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>) {
        transactionDetailDao.insertTransactionDetails(details)
    }
    
    suspend fun updateTransactionDetail(detail: TransactionDetailEntity) {
        transactionDetailDao.updateTransactionDetail(detail)
    }
    
    suspend fun deleteDetailsByTransaction(transactionSlug: String) {
        transactionDetailDao.deleteDetailsByTransaction(transactionSlug)
    }
    
    // Dashboard queries
    suspend fun getTotalTransactionsCount(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Int? {
        return transactionDao.getTotalTransactionsCount(businessSlug, fromMilli, toMilli, transactionTypes)
    }
    
    suspend fun getTotalRevenue(
        businessSlug: String,
        fromMilli: Long,
        toMilli: Long,
        transactionTypes: List<Int>
    ): Double? {
        return transactionDao.getTotalRevenue(businessSlug, fromMilli, toMilli, transactionTypes)
    }
}

