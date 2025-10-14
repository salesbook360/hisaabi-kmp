package com.hisaabi.hisaabi_kmp.database.datasource

import com.hisaabi.hisaabi_kmp.database.dao.TransactionDetailDao
import com.hisaabi.hisaabi_kmp.database.entity.TransactionDetailEntity
import kotlinx.coroutines.flow.Flow

interface TransactionDetailLocalDataSource {
    fun getAllTransactionDetails(): Flow<List<TransactionDetailEntity>>
    suspend fun getTransactionDetailById(id: Int): TransactionDetailEntity?
    fun getDetailsByTransaction(transactionSlug: String): Flow<List<TransactionDetailEntity>>
    fun getDetailsByProduct(productSlug: String): Flow<List<TransactionDetailEntity>>
    suspend fun getUnsyncedDetails(): List<TransactionDetailEntity>
    suspend fun insertTransactionDetail(detail: TransactionDetailEntity): Long
    suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>)
    suspend fun updateTransactionDetail(detail: TransactionDetailEntity)
    suspend fun deleteTransactionDetail(detail: TransactionDetailEntity)
    suspend fun deleteTransactionDetailById(id: Int)
    suspend fun deleteDetailsByTransaction(transactionSlug: String)
    suspend fun deleteAllTransactionDetails()
}

class TransactionDetailLocalDataSourceImpl(
    private val detailDao: TransactionDetailDao
) : TransactionDetailLocalDataSource {
    override fun getAllTransactionDetails(): Flow<List<TransactionDetailEntity>> = 
        detailDao.getAllTransactionDetails()
    
    override suspend fun getTransactionDetailById(id: Int): TransactionDetailEntity? = 
        detailDao.getTransactionDetailById(id)
    
    override fun getDetailsByTransaction(transactionSlug: String): Flow<List<TransactionDetailEntity>> = 
        detailDao.getDetailsByTransaction(transactionSlug)
    
    override fun getDetailsByProduct(productSlug: String): Flow<List<TransactionDetailEntity>> = 
        detailDao.getDetailsByProduct(productSlug)
    
    override suspend fun getUnsyncedDetails(): List<TransactionDetailEntity> = 
        detailDao.getUnsyncedDetails()
    
    override suspend fun insertTransactionDetail(detail: TransactionDetailEntity): Long = 
        detailDao.insertTransactionDetail(detail)
    
    override suspend fun insertTransactionDetails(details: List<TransactionDetailEntity>) = 
        detailDao.insertTransactionDetails(details)
    
    override suspend fun updateTransactionDetail(detail: TransactionDetailEntity) = 
        detailDao.updateTransactionDetail(detail)
    
    override suspend fun deleteTransactionDetail(detail: TransactionDetailEntity) = 
        detailDao.deleteTransactionDetail(detail)
    
    override suspend fun deleteTransactionDetailById(id: Int) = 
        detailDao.deleteTransactionDetailById(id)
    
    override suspend fun deleteDetailsByTransaction(transactionSlug: String) = 
        detailDao.deleteDetailsByTransaction(transactionSlug)
    
    override suspend fun deleteAllTransactionDetails() = 
        detailDao.deleteAllTransactionDetails()
}

