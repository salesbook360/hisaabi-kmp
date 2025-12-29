package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionSortOption
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionFilterParams
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionsUseCase
import com.hisaabi.hisaabi_kmp.utils.currentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity

data class ManufactureInfo(
    val recipeName: String,
    val recipeQuantity: String
)

data class TransactionsListState(
    val transactions: List<Transaction> = emptyList(),
    val transactionDetailsCounts: Map<String, Int> = emptyMap(),
    val manufactureInfo: Map<String, ManufactureInfo> = emptyMap(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedTransactionTypes: Set<AllTransactionTypes> = emptySet(),
    val selectedParty: Party? = null,
    val selectedArea: CategoryEntity? = null,
    val selectedCategory: CategoryEntity? = null,
    val searchQuery: String = "",
    val idOrSlugFilter: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val dateFilterType: TransactionSortOption = TransactionSortOption.TRANSACTION_DATE,
    val showFilters: Boolean = false,
    val sortBy: TransactionSortOption = TransactionSortOption.TRANSACTION_DATE,
    // Pagination state
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    // Scroll position preservation
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0
)

class TransactionsListViewModel(
    private val transactionUseCases: TransactionUseCases,
    private val transactionsRepository: com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
) : ViewModel() {
    
    companion object {
        const val PAGE_SIZE = 20
    }
    
    private val _state = MutableStateFlow(TransactionsListState())
    val state: StateFlow<TransactionsListState> = _state.asStateFlow()
    
    private var loadJob: Job? = null
    
    init {
        loadTransactions()
    }
    
    /**
     * Load first page of transactions. Resets pagination state.
     */
    fun loadTransactions() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    currentPage = 0,
                    transactions = emptyList(),
                    transactionDetailsCounts = emptyMap(),
                    manufactureInfo = emptyMap()
                ) 
            }
            
            try {
                val params = buildFilterParams()
                val result = transactionUseCases.getTransactions.paginated(
                    params = params,
                    page = 0,
                    pageSize = PAGE_SIZE
                )
                
                // Load detail counts and manufacture info for loaded transactions
                val (counts, manufactureInfoMap) = loadTransactionMetadata(result.transactions)
                
                _state.update { currentState ->
                    currentState.copy(
                        transactions = result.transactions,
                        transactionDetailsCounts = counts,
                        manufactureInfo = manufactureInfoMap,
                        isLoading = false,
                        hasMore = result.hasMore,
                        totalCount = result.totalCount,
                        currentPage = 0,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transactions"
                    )
                }
            }
        }
    }
    
    /**
     * Load more transactions (next page). Called when user scrolls near the end.
     */
    fun loadMore() {
        val currentState = _state.value
        
        // Don't load if already loading, no more items, or initial load hasn't completed
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) {
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            
            try {
                val nextPage = currentState.currentPage + 1
                val params = buildFilterParams()
                
                val result = transactionUseCases.getTransactions.paginated(
                    params = params,
                    page = nextPage,
                    pageSize = PAGE_SIZE
                )
                
                // Load metadata for newly loaded transactions
                val (counts, manufactureInfoMap) = loadTransactionMetadata(result.transactions)
                
                _state.update { state ->
                    state.copy(
                        transactions = state.transactions + result.transactions,
                        transactionDetailsCounts = state.transactionDetailsCounts + counts,
                        manufactureInfo = state.manufactureInfo + manufactureInfoMap,
                        isLoadingMore = false,
                        hasMore = result.hasMore,
                        currentPage = nextPage,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Failed to load more transactions"
                    )
                }
            }
        }
    }
    
    /**
     * Build filter parameters from current state.
     */
    private fun buildFilterParams(): TransactionFilterParams {
        val currentState = _state.value
        
        return TransactionFilterParams(
            partySlug = currentState.selectedParty?.slug,
            transactionTypes = if (currentState.selectedTransactionTypes.isEmpty()) null 
                else currentState.selectedTransactionTypes.map { it.value },
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            searchQuery = currentState.searchQuery,
            sortByEntryDate = currentState.sortBy == TransactionSortOption.ENTRY_DATE,
            areaSlug = currentState.selectedArea?.slug,
            categorySlug = currentState.selectedCategory?.slug,
            idOrSlugFilter = currentState.idOrSlugFilter
        )
    }
    
    /**
     * Load detail counts and manufacture info for transactions.
     */
    private suspend fun loadTransactionMetadata(
        transactions: List<Transaction>
    ): Pair<Map<String, Int>, Map<String, ManufactureInfo>> {
        val transactionTypesWithItems = listOf(
            AllTransactionTypes.SALE.value,
            AllTransactionTypes.CUSTOMER_RETURN.value,
            AllTransactionTypes.PURCHASE.value,
            AllTransactionTypes.VENDOR_RETURN.value,
            AllTransactionTypes.STOCK_TRANSFER.value,
            AllTransactionTypes.STOCK_INCREASE.value,
            AllTransactionTypes.STOCK_REDUCE.value,
            AllTransactionTypes.PURCHASE_ORDER.value,
            AllTransactionTypes.SALE_ORDER.value,
            AllTransactionTypes.QUOTATION.value,
            AllTransactionTypes.MANUFACTURE.value
        )
        
        val transactionSlugs = transactions
            .filter { it.transactionType in transactionTypesWithItems }
            .mapNotNull { it.slug }
        
        val counts = mutableMapOf<String, Int>()
        transactionSlugs.forEach { slug ->
            try {
                counts[slug] = transactionUseCases.getTransactionDetailsCount(slug)
            } catch (e: Exception) {
                counts[slug] = 0
            }
        }
        
        // Load manufacture info for manufacture transactions
        val manufactureTransactions = transactions.filter { 
            it.transactionType == AllTransactionTypes.MANUFACTURE.value 
        }
        
        val manufactureInfoMap = mutableMapOf<String, ManufactureInfo>()
        manufactureTransactions.forEach { transaction ->
            transaction.slug?.let { slug ->
                try {
                    val childTransactions = transactionUseCases.getChildTransactions(slug)
                    val purchaseTransaction = childTransactions.find { 
                        it.transactionType == AllTransactionTypes.PURCHASE.value 
                    }
                    
                    purchaseTransaction?.let { purchase ->
                        val recipeDetail = transactionUseCases.getTransactions.withDetails(purchase.slug ?: "")
                            ?.transactionDetails?.firstOrNull()
                        
                        recipeDetail?.let { detail ->
                            manufactureInfoMap[slug] = ManufactureInfo(
                                recipeName = detail.product?.title ?: "Unknown Recipe",
                                recipeQuantity = detail.getDisplayQuantity()
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors loading manufacture info
                }
            }
        }
        
        return Pair(counts, manufactureInfoMap)
    }
    
    fun toggleTransactionTypeFilter(type: AllTransactionTypes) {
        _state.update { currentState ->
            val newTypes = if (currentState.selectedTransactionTypes.contains(type)) {
                currentState.selectedTransactionTypes - type
            } else {
                currentState.selectedTransactionTypes + type
            }
            currentState.copy(selectedTransactionTypes = newTypes)
        }
        loadTransactions() // Reset pagination and reload
    }
    
    fun clearTransactionTypeFilters() {
        _state.update { it.copy(selectedTransactionTypes = emptySet()) }
        loadTransactions()
    }
    
    fun setPartyFilter(party: Party?) {
        _state.update { it.copy(selectedParty = party) }
        loadTransactions()
    }
    
    fun setAreaFilter(area: CategoryEntity?) {
        _state.update { it.copy(selectedArea = area) }
        loadTransactions()
    }
    
    fun setCategoryFilter(category: CategoryEntity?) {
        _state.update { it.copy(selectedCategory = category) }
        loadTransactions()
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadTransactions()
    }
    
    fun setIdOrSlugFilter(filter: String) {
        _state.update { it.copy(idOrSlugFilter = filter) }
        loadTransactions()
    }
    
    fun setSortBy(sortBy: TransactionSortOption) {
        _state.update { it.copy(sortBy = sortBy) }
        loadTransactions()
    }
    
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        loadTransactions()
    }
    
    fun setDateFilterType(dateFilterType: TransactionSortOption) {
        _state.update { it.copy(dateFilterType = dateFilterType) }
        loadTransactions()
    }
    
    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }
    
    fun clearFilters() {
        _state.update { 
            it.copy(
                selectedTransactionTypes = emptySet(),
                selectedParty = null,
                selectedArea = null,
                selectedCategory = null,
                searchQuery = "",
                idOrSlugFilter = "",
                startDate = null,
                endDate = null,
                dateFilterType = TransactionSortOption.TRANSACTION_DATE,
                sortBy = TransactionSortOption.TRANSACTION_DATE
            )
        }
        loadTransactions()
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionUseCases.deleteTransaction(transaction)
                    .onSuccess {
                        // Reload to reflect the change
                        loadTransactions()
                    }
                    .onFailure { e ->
                        _state.update { 
                            it.copy(error = e.message ?: "Failed to delete transaction")
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to delete transaction")
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun updateScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        _state.update { 
            it.copy(
                scrollPosition = firstVisibleItemIndex,
                scrollOffset = firstVisibleItemScrollOffset
            )
        }
    }
    
    // Convert Sale Order/Quotation to Sale
    fun convertToSale(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (transaction.slug == null) {
                    onError("Transaction slug is required for conversion")
                    return@launch
                }
                
                val fullTransaction = transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                    ?: run {
                        onError("Transaction not found")
                        return@launch
                    }
                
                if (fullTransaction.transactionDetails.isEmpty()) {
                    onError("Transaction must have at least one product")
                    return@launch
                }
                
                val updatedTransaction = fullTransaction.copy(
                    transactionType = AllTransactionTypes.SALE.value
                )
                
                transactionUseCases.updateTransaction(updatedTransaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions()
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to convert transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to convert transaction")
            }
        }
    }
    
    fun editAndConvertToSale(transaction: Transaction, onNavigateToEdit: (Transaction) -> Unit) {
        val copyOfTransaction = transaction.copy(
            transactionType = AllTransactionTypes.SALE.value
        )
        onNavigateToEdit(copyOfTransaction)
    }
    
    fun convertToPurchase(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (transaction.slug == null) {
                    onError("Transaction slug is required for conversion")
                    return@launch
                }
                
                val fullTransaction = transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                    ?: run {
                        onError("Transaction not found")
                        return@launch
                    }
                
                if (fullTransaction.transactionDetails.isEmpty()) {
                    onError("Transaction must have at least one product")
                    return@launch
                }
                
                if (fullTransaction.wareHouseSlugFrom == null) {
                    onError("Warehouse is required for purchase transactions")
                    return@launch
                }
                
                val updatedTransaction = fullTransaction.copy(
                    transactionType = AllTransactionTypes.PURCHASE.value
                )
                
                transactionUseCases.updateTransaction(updatedTransaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions()
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to convert transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to convert transaction")
            }
        }
    }
    
    fun editAndConvertToPurchase(transaction: Transaction, onNavigateToEdit: (Transaction) -> Unit) {
        val copyOfTransaction = transaction.copy(
            transactionType = AllTransactionTypes.PURCHASE.value
        )
        onNavigateToEdit(copyOfTransaction)
    }
    
    fun cancelAndRemove(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                transactionUseCases.deleteTransaction(transaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions()
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to delete transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete transaction")
            }
        }
    }
    
    fun restoreTransaction(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val restoredTransaction = transaction.copy(
                    statusId = 0 // Active
                )
                
                if (restoredTransaction.slug != null) {
                    transactionUseCases.updateTransaction(restoredTransaction)
                        .onSuccess {
                            onSuccess()
                            loadTransactions()
                        }
                        .onFailure { e ->
                            onError(e.message ?: "Failed to restore transaction")
                        }
                } else {
                    transactionUseCases.addTransaction(restoredTransaction)
                        .onSuccess {
                            onSuccess()
                            loadTransactions()
                        }
                        .onFailure { e ->
                            onError(e.message ?: "Failed to restore transaction")
                        }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to restore transaction")
            }
        }
    }
    
    fun cloneTransaction(transaction: Transaction, cloneAsType: AllTransactionTypes?, onNavigateToEdit: (Transaction) -> Unit) {
        viewModelScope.launch {
            try {
                val fullTransaction = if (transaction.transactionDetails.isEmpty() && transaction.slug != null) {
                    transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                        ?: transaction
                } else {
                    transaction
                }
                
                val clonedTransaction = fullTransaction.copy(
                    slug = null,
                    id = 0,
                    timestamp = currentTimeMillis().toString(),
                    statusId = 0,
                    totalPaid = 0.0,
                    transactionDetails = fullTransaction.transactionDetails.map { detail ->
                        detail.copy(
                            id = 0,
                            slug = null,
                            transactionSlug = null
                        )
                    },
                    transactionType = cloneAsType?.value ?: fullTransaction.transactionType,
                    parentSlug = null,
                    remindAtMilliseconds = 0,
                    stateId = TransactionState.PENDING.value
                )
                
                onNavigateToEdit(clonedTransaction)
            } catch (e: Exception) {
                val clonedTransaction = transaction.copy(
                    slug = null,
                    id = 0,
                    timestamp = currentTimeMillis().toString(),
                    statusId = 0,
                    totalPaid = 0.0,
                    transactionDetails = transaction.transactionDetails.map { detail ->
                        detail.copy(
                            id = 0,
                            slug = null,
                            transactionSlug = null
                        )
                    },
                    transactionType = cloneAsType?.value ?: transaction.transactionType,
                    parentSlug = null,
                    remindAtMilliseconds = 0,
                    stateId = TransactionState.PENDING.value
                )
                onNavigateToEdit(clonedTransaction)
            }
        }
    }
    
    fun updateTransactionState(
        transaction: Transaction,
        newState: com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedTransaction = transaction.copy(
                    stateId = newState.value
                )
                
                transactionUseCases.updateTransaction(updatedTransaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions()
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to update transaction state")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update transaction state")
            }
        }
    }
}
