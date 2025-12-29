package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.InventoryTransactionDao
import com.hisaabi.hisaabi_kmp.database.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

interface InventoryTransactionLocalDataSource {
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>
    suspend fun getTransactionById(id: Int): InventoryTransactionEntity?
    suspend fun getTransactionBySlug(slug: String): InventoryTransactionEntity?
    fun getTransactionsByCustomer(partySlug: String): Flow<List<InventoryTransactionEntity>>
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>>
    fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>>
    suspend fun getUnsyncedTransactions(businessSlug: String): List<InventoryTransactionEntity>
    suspend fun insertTransaction(transaction: InventoryTransactionEntity): Long
    suspend fun insertTransactions(transactions: List<InventoryTransactionEntity>)
    suspend fun updateTransaction(transaction: InventoryTransactionEntity)
    suspend fun deleteTransaction(transaction: InventoryTransactionEntity)
    suspend fun deleteTransactionById(id: Int)
    suspend fun deleteAllTransactions()
}

class InventoryTransactionLocalDataSourceImpl(
    private val transactionDao: InventoryTransactionDao
) : InventoryTransactionLocalDataSource {
    override fun getAllTransactions(): Flow<List<InventoryTransactionEntity>> = 
        transactionDao.getAllTransactions()
    
    override suspend fun getTransactionById(id: Int): InventoryTransactionEntity? = 
        transactionDao.getTransactionById(id)
    
    override suspend fun getTransactionBySlug(slug: String): InventoryTransactionEntity? = 
        transactionDao.getTransactionBySlug(slug)
    
    override fun getTransactionsByCustomer(partySlug: String): Flow<List<InventoryTransactionEntity>> = 
        transactionDao.getTransactionsByCustomer(partySlug)
    
    override fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>> = 
        transactionDao.getTransactionsByType(transactionType)
    
    override fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>> = 
        transactionDao.getTransactionsByBusiness(businessSlug)
    
    override suspend fun getUnsyncedTransactions(businessSlug: String): List<InventoryTransactionEntity> = 
        transactionDao.getUnsyncedTransactions(businessSlug)
    
    override suspend fun insertTransaction(transaction: InventoryTransactionEntity): Long = 
        transactionDao.insertTransaction(transaction)
    
    override suspend fun insertTransactions(transactions: List<InventoryTransactionEntity>) = 
        transactionDao.insertTransactions(transactions)
    
    override suspend fun updateTransaction(transaction: InventoryTransactionEntity) = 
        transactionDao.updateTransaction(transaction)
    
    override suspend fun deleteTransaction(transaction: InventoryTransactionEntity) = 
        transactionDao.deleteTransaction(transaction)
    
    override suspend fun deleteTransactionById(id: Int) = 
        transactionDao.deleteTransactionById(id)
    
    override suspend fun deleteAllTransactions() = 
        transactionDao.deleteAllTransactions()
}

