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
    
    fun getTransactionsByCustomer(partySlug: String): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getTransactionsByCustomer(partySlug)
    }
    
    fun getTransactionsByType(transactionType: Int): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getTransactionsByType(transactionType)
    }
    
    fun getTransactionsByBusiness(businessSlug: String): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getTransactionsByBusiness(businessSlug)
    }
    
    // Paginated queries with all filters applied at DB level
    suspend fun getTransactionsPaginated(
        businessSlug: String,
        partySlug: String?,
        transactionTypes: List<Int>?,
        startDate: Long?,
        endDate: Long?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?,
        limit: Int,
        offset: Int
    ): List<InventoryTransactionEntity> {
        // filterByTypes: 1 = apply filter, -1 = skip filter (using -1 to avoid conflict with SALE type = 0)
        val filterByTypes = if (transactionTypes.isNullOrEmpty()) -1 else 1
        return transactionDao.getTransactionsPaginated(
            businessSlug = businessSlug,
            partySlug = partySlug,
            filterByTypes = filterByTypes,
            transactionTypes = transactionTypes ?: emptyList(),
            startDate = startDate,
            endDate = endDate,
            searchQuery = searchQuery,
            idOrSlugFilter = idOrSlugFilter,
            areaSlug = areaSlug,
            categorySlug = categorySlug,
            limit = limit,
            offset = offset
        )
    }
    
    suspend fun getTransactionsPaginatedByEntryDate(
        businessSlug: String,
        partySlug: String?,
        transactionTypes: List<Int>?,
        startDate: Long?,
        startDateStr: String?,
        endDate: Long?,
        endDateStr: String?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?,
        limit: Int,
        offset: Int
    ): List<InventoryTransactionEntity> {
        val filterByTypes = if (transactionTypes.isNullOrEmpty()) -1 else 1
        return transactionDao.getTransactionsPaginatedByEntryDate(
            businessSlug = businessSlug,
            partySlug = partySlug,
            filterByTypes = filterByTypes,
            transactionTypes = transactionTypes ?: emptyList(),
            startDate = startDate,
            startDateStr = startDateStr,
            endDate = endDate,
            endDateStr = endDateStr,
            searchQuery = searchQuery,
            idOrSlugFilter = idOrSlugFilter,
            areaSlug = areaSlug,
            categorySlug = categorySlug,
            limit = limit,
            offset = offset
        )
    }
    
    suspend fun getTransactionsCount(
        businessSlug: String,
        partySlug: String?,
        transactionTypes: List<Int>?,
        startDate: Long?,
        endDate: Long?,
        searchQuery: String,
        idOrSlugFilter: String,
        areaSlug: String?,
        categorySlug: String?
    ): Int {
        val filterByTypes = if (transactionTypes.isNullOrEmpty()) -1 else 1
        return transactionDao.getTransactionsCount(
            businessSlug = businessSlug,
            partySlug = partySlug,
            filterByTypes = filterByTypes,
            transactionTypes = transactionTypes ?: emptyList(),
            startDate = startDate,
            endDate = endDate,
            searchQuery = searchQuery,
            idOrSlugFilter = idOrSlugFilter,
            areaSlug = areaSlug,
            categorySlug = categorySlug
        )
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
    
    suspend fun getDetailsCountByTransaction(transactionSlug: String): Int {
        return transactionDetailDao.getDetailsCountByTransaction(transactionSlug)
    }
    
    fun getChildTransactions(parentSlug: String): Flow<List<InventoryTransactionEntity>> {
        return transactionDao.getChildTransactions(parentSlug)
    }
    
    suspend fun getChildTransactionsList(parentSlug: String): List<InventoryTransactionEntity> {
        return transactionDao.getChildTransactionsList(parentSlug)
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

