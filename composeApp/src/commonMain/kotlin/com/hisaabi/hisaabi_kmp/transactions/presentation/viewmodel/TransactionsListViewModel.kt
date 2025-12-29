package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionSortOption
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.utils.currentTimeMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    // Scroll position preservation
    val scrollPosition: Int = 0,
    val scrollOffset: Int = 0
)

class TransactionsListViewModel(
    private val transactionUseCases: TransactionUseCases,
    private val transactionsRepository: com.hisaabi.hisaabi_kmp.transactions.data.repository.TransactionsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TransactionsListState())
    val state: StateFlow<TransactionsListState> = _state.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                transactionUseCases.getTransactions()
                    .catch { e ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load transactions"
                            )
                        }
                    }
                    .collect { transactions ->
                        // Load detail counts for all transaction types that have products/items
                        // Includes: Sale, Purchase, Returns, Orders, Quotations, Manufacture, Stock Adjustments
                        val transactionTypesWithItems = listOf(
                            AllTransactionTypes.SALE.value,                    // 0
                            AllTransactionTypes.CUSTOMER_RETURN.value,         // 1
                            AllTransactionTypes.PURCHASE.value,                // 2
                            AllTransactionTypes.VENDOR_RETURN.value,           // 3
                            AllTransactionTypes.STOCK_TRANSFER.value,          // 13
                            AllTransactionTypes.STOCK_INCREASE.value,          // 14
                            AllTransactionTypes.STOCK_REDUCE.value,            // 15
                            AllTransactionTypes.PURCHASE_ORDER.value,          // 16
                            AllTransactionTypes.SALE_ORDER.value,              // 17
                            AllTransactionTypes.QUOTATION.value,               // 18
                            AllTransactionTypes.MANUFACTURE.value              // 20
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
                                    // Get child transactions
                                    val childTransactions = transactionUseCases.getChildTransactions(slug)
                                    
                                    // Find the purchase transaction (contains the manufactured product/recipe)
                                    val purchaseTransaction = childTransactions.find { 
                                        it.transactionType == AllTransactionTypes.PURCHASE.value 
                                    }
                                    
                                    // Get recipe info from purchase transaction's details
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
                        
                        _state.update { 
                            it.copy(
                                transactions = applyFilters(transactions),
                                transactionDetailsCounts = counts,
                                manufactureInfo = manufactureInfoMap,
                                isLoading = false,
                                error = null
                            )
                        }
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
    
    private fun applyFilters(transactions: List<Transaction>): List<Transaction> {
        val state = _state.value
        var filtered = transactions
        
        // Filter by transaction types (multiple selection)
        if (state.selectedTransactionTypes.isNotEmpty()) {
            val selectedTypeValues = state.selectedTransactionTypes.map { it.value }.toSet()
            filtered = filtered.filter { it.transactionType in selectedTypeValues }
        }
        
        // Filter by party
        state.selectedParty?.let { party ->
            filtered = filtered.filter { it.partySlug == party.slug }
        }
        
        // Filter by party area
        state.selectedArea?.slug?.let { areaSlug ->
            filtered = filtered.filter { transaction ->
                transaction.party?.areaSlug == areaSlug
            }
        }
        
        // Filter by party category
        state.selectedCategory?.slug?.let { categorySlug ->
            filtered = filtered.filter { transaction ->
                transaction.party?.categorySlug == categorySlug
            }
        }
        
        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.party?.name?.contains(state.searchQuery, ignoreCase = true) == true ||
                transaction.description?.contains(state.searchQuery, ignoreCase = true) == true ||
                transaction.slug?.contains(state.searchQuery, ignoreCase = true) == true
            }
        }
        
        // Filter by id or slug
        if (state.idOrSlugFilter.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.id.toString() == state.idOrSlugFilter ||
                transaction.slug?.equals(state.idOrSlugFilter, ignoreCase = true) == true ||
                transaction.slug?.contains(state.idOrSlugFilter, ignoreCase = true) == true
            }
        }
        
        // Filter by date range
        if (state.startDate != null || state.endDate != null) {
            filtered = filtered.filter { transaction ->
                val transactionDateMillis = when (state.dateFilterType) {
                    TransactionSortOption.ENTRY_DATE -> {
                        // Use createdAt (ISO format) for entry date
                        transaction.createdAt?.let { isoDate ->
                            try {
                                // Parse ISO format: yyyy-MM-DDTHH:mm:ss.000Z
                                val parts = isoDate.replace(".000Z", "Z").split("T")
                                if (parts.size == 2) {
                                    val dateParts = parts[0].split("-")
                                    if (dateParts.size == 3) {
                                        val year = dateParts[0].toIntOrNull()
                                        val month = dateParts[1].toIntOrNull()
                                        val day = dateParts[2].toIntOrNull()
                                        if (year != null && month != null && day != null) {
                                            val localDateTime = kotlinx.datetime.LocalDateTime(
                                                year = year,
                                                monthNumber = month,
                                                dayOfMonth = day,
                                                hour = 0,
                                                minute = 0
                                            )
                                            localDateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
                                        } else null
                                    } else null
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    TransactionSortOption.TRANSACTION_DATE -> {
                        // Use timestamp (milliseconds) for transaction date
                        transaction.timestamp?.toLongOrNull()
                    }
                }
                
                if (transactionDateMillis == null) return@filter false
                
                // Get date-only timestamps (start of day) for comparison
                fun getStartOfDay(millis: Long): Long {
                    val date = Instant.fromEpochMilliseconds(millis)
                    val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault())
                    val startOfDay = kotlinx.datetime.LocalDateTime(
                        year = localDate.year,
                        monthNumber = localDate.monthNumber,
                        dayOfMonth = localDate.dayOfMonth,
                        hour = 0,
                        minute = 0
                    )
                    return startOfDay.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                }
                
                fun getEndOfDay(millis: Long): Long {
                    val date = Instant.fromEpochMilliseconds(millis)
                    val localDate = date.toLocalDateTime(TimeZone.currentSystemDefault())
                    val endOfDay = kotlinx.datetime.LocalDateTime(
                        year = localDate.year,
                        monthNumber = localDate.monthNumber,
                        dayOfMonth = localDate.dayOfMonth,
                        hour = 23,
                        minute = 59,
                        second = 59
                    )
                    return endOfDay.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                }
                
                val transactionDateOnly = getStartOfDay(transactionDateMillis)
                val startOfStartDate = state.startDate?.let { getStartOfDay(it) }
                val endOfEndDate = state.endDate?.let { getEndOfDay(it) }
                
                val afterStart = startOfStartDate == null || transactionDateOnly >= startOfStartDate
                val beforeEnd = endOfEndDate == null || transactionDateOnly <= endOfEndDate
                
                afterStart && beforeEnd
            }
        }
        
        // Sort by selected option
        return when (state.sortBy) {
            TransactionSortOption.ENTRY_DATE -> {
                filtered.sortedByDescending { it.createdAt }
            }
            TransactionSortOption.TRANSACTION_DATE -> {
                filtered.sortedByDescending { it.timestamp }
            }
        }
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
        refreshFilters()
    }
    
    fun clearTransactionTypeFilters() {
        _state.update { it.copy(selectedTransactionTypes = emptySet()) }
        refreshFilters()
    }
    
    fun setPartyFilter(party: Party?) {
        _state.update { it.copy(selectedParty = party) }
        refreshFilters()
    }
    
    fun setAreaFilter(area: CategoryEntity?) {
        _state.update { it.copy(selectedArea = area) }
        refreshFilters()
    }
    
    fun setCategoryFilter(category: CategoryEntity?) {
        _state.update { it.copy(selectedCategory = category) }
        refreshFilters()
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        refreshFilters()
    }
    
    fun setIdOrSlugFilter(filter: String) {
        _state.update { it.copy(idOrSlugFilter = filter) }
        refreshFilters()
    }
    
    fun setSortBy(sortBy: TransactionSortOption) {
        _state.update { it.copy(sortBy = sortBy) }
        refreshFilters()
    }
    
    fun setDateRange(startDate: Long?, endDate: Long?) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        refreshFilters()
    }
    
    fun setDateFilterType(dateFilterType: TransactionSortOption) {
        _state.update { it.copy(dateFilterType = dateFilterType) }
        refreshFilters()
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
        refreshFilters()
    }
    
    private fun refreshFilters() {
        val currentTransactions = _state.value.transactions
        _state.update { it.copy(transactions = applyFilters(currentTransactions)) }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionUseCases.deleteTransaction(transaction)
                    .onSuccess {
                        // Transaction deleted successfully
                        // The list will automatically update via Flow
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
                
                // Always load full transaction with details to ensure we have all product information
                val fullTransaction = transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                    ?: run {
                        onError("Transaction not found")
                        return@launch
                    }
                
                if (fullTransaction.transactionDetails.isEmpty()) {
                    onError("Transaction must have at least one product")
                    return@launch
                }
                
                // Update existing transaction by changing its type
                // Preserve all details, warehouse, and other fields
                val updatedTransaction = fullTransaction.copy(
                    transactionType = AllTransactionTypes.SALE.value
                )
                
                transactionUseCases.updateTransaction(updatedTransaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions() // Refresh the list
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to convert transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to convert transaction")
            }
        }
    }
    
    // Edit and Convert Sale Order/Quotation to Sale
    fun editAndConvertToSale(transaction: Transaction, onNavigateToEdit: (Transaction) -> Unit) {
        val copyOfTransaction = transaction.copy(
            transactionType = AllTransactionTypes.SALE.value
        )
        onNavigateToEdit(copyOfTransaction)
    }
    
    // Convert Purchase Order to Purchase
    fun convertToPurchase(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                if (transaction.slug == null) {
                    onError("Transaction slug is required for conversion")
                    return@launch
                }
                
                // Always load full transaction with details to ensure we have all product information
                val fullTransaction = transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                    ?: run {
                        onError("Transaction not found")
                        return@launch
                    }
                
                if (fullTransaction.transactionDetails.isEmpty()) {
                    onError("Transaction must have at least one product")
                    return@launch
                }
                
                // Ensure warehouse is set (required for stock updates)
                if (fullTransaction.wareHouseSlugFrom == null) {
                    onError("Warehouse is required for purchase transactions")
                    return@launch
                }
                
                // Update existing transaction by changing its type
                // Preserve all details, warehouse, and other fields
                val updatedTransaction = fullTransaction.copy(
                    transactionType = AllTransactionTypes.PURCHASE.value
                )
                
                transactionUseCases.updateTransaction(updatedTransaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions() // Refresh the list
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to convert transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to convert transaction")
            }
        }
    }
    
    // Edit and Convert Purchase Order to Purchase
    fun editAndConvertToPurchase(transaction: Transaction, onNavigateToEdit: (Transaction) -> Unit) {
        val copyOfTransaction = transaction.copy(
            transactionType = AllTransactionTypes.PURCHASE.value
        )
        onNavigateToEdit(copyOfTransaction)
    }
    
    // Cancel and Remove (Delete) transaction
    fun cancelAndRemove(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                transactionUseCases.deleteTransaction(transaction)
                    .onSuccess {
                        onSuccess()
                        loadTransactions() // Refresh the list
                    }
                    .onFailure { e ->
                        onError(e.message ?: "Failed to delete transaction")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete transaction")
            }
        }
    }
    
    // Restore deleted transaction
    fun restoreTransaction(transaction: Transaction, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val restoredTransaction = transaction.copy(
                    statusId = 0 // Active
                )
                
                // If transaction has slug, update it; otherwise add as new
                if (restoredTransaction.slug != null) {
                    transactionUseCases.updateTransaction(restoredTransaction)
                        .onSuccess {
                            onSuccess()
                            loadTransactions() // Refresh the list
                        }
                        .onFailure { e ->
                            onError(e.message ?: "Failed to restore transaction")
                        }
                } else {
                    // If no slug, add as new transaction
                    transactionUseCases.addTransaction(restoredTransaction)
                        .onSuccess {
                            onSuccess()
                            loadTransactions() // Refresh the list
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
    
    // Clone transaction
    fun cloneTransaction(transaction: Transaction, cloneAsType: AllTransactionTypes?, onNavigateToEdit: (Transaction) -> Unit) {
        viewModelScope.launch {
            try {
                // Always load full transaction with details to ensure we have all data
                val fullTransaction = if (transaction.transactionDetails.isEmpty() && transaction.slug != null) {
                    transactionsRepository.getTransactionWithDetails(transaction.slug!!)
                        ?: transaction
                } else {
                    transaction
                }
                
                // Create cloned transaction with all data preserved
                val clonedTransaction = fullTransaction.copy(
                    slug = null, // New transaction will get a new slug
                    id = 0,
                    timestamp = currentTimeMillis().toString(),
                    statusId = 0, // Active
                    totalPaid = 0.0, // Reset payment for cloned transaction
                    transactionDetails = fullTransaction.transactionDetails.map { detail ->
                        detail.copy(
                            id = 0,
                            slug = null,
                            transactionSlug = null
                        )
                    },
                    transactionType = cloneAsType?.value ?: fullTransaction.transactionType,
                    parentSlug = null, // Clear parent slug for cloned transaction
                    remindAtMilliseconds = 0, // Clear reminder
                    stateId = TransactionState.PENDING.value // Reset to pending state
                )
                
                onNavigateToEdit(clonedTransaction)
            } catch (e: Exception) {
                // If loading fails, still try to clone with available data
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
    
    // Update transaction state
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
                        loadTransactions() // Refresh the list
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

