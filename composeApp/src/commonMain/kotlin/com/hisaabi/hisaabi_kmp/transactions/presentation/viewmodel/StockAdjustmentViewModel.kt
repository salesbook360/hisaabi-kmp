package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class StockAdjustmentState(
    val adjustmentType: AllTransactionTypes = AllTransactionTypes.STOCK_INCREASE,
    val warehouseFrom: Warehouse? = null,
    val warehouseTo: Warehouse? = null,
    val products: List<TransactionDetail> = emptyList(),
    val dateTime: Long = Clock.System.now().toEpochMilliseconds(),
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val editingTransactionSlug: String? = null
)

class StockAdjustmentViewModel(
    private val transactionUseCases: TransactionUseCases,
    private val getTransactionWithDetailsUseCase: GetTransactionWithDetailsUseCase,
    private val appSessionManager: AppSessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(StockAdjustmentState())
    val state: StateFlow<StockAdjustmentState> = _state.asStateFlow()

    fun setAdjustmentType(type: AllTransactionTypes) {
        _state.update { it.copy(adjustmentType = type) }
    }

    fun setWarehouseFrom(warehouse: Warehouse?) {
        _state.update { it.copy(warehouseFrom = warehouse) }
    }

    fun setWarehouseTo(warehouse: Warehouse?) {
        _state.update { it.copy(warehouseTo = warehouse) }
    }

    fun addProduct(product: Product, quantity: Double = 1.0) {
        val existingProduct = _state.value.products.find { it.productSlug == product.slug }
        if (existingProduct != null) {
            // Update quantity if product already exists
            updateProductQuantity(product.slug, existingProduct.quantity + quantity)
        } else {
            // Add new product
            val transactionDetail = TransactionDetail(
                productSlug = product.slug,
                product = product,
                quantity = quantity,
                price = product.retailPrice,
                flatDiscount = 0.0,
                flatTax = 0.0,
                quantityUnitSlug = product.defaultUnitSlug,
                description = ""
            )
            _state.update { 
                it.copy(products = it.products + transactionDetail)
            }
        }
    }

    fun removeProduct(productSlug: String) {
        _state.update { 
            it.copy(products = it.products.filter { detail -> detail.productSlug != productSlug })
        }
    }

    fun updateProductQuantity(productSlug: String, quantity: Double) {
        _state.update { 
            it.copy(
                products = it.products.map { detail ->
                    if (detail.productSlug == productSlug) {
                        detail.copy(quantity = quantity)
                    } else {
                        detail
                    }
                }
            )
        }
    }

    fun setDateTime(timestamp: Long) {
        _state.update { it.copy(dateTime = timestamp) }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun loadTransactionForEdit(transactionSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load full transaction with all details
                val transaction = getTransactionWithDetailsUseCase(transactionSlug)
                    ?: throw IllegalStateException("Transaction not found")
                
                // Determine transaction type
                val transactionType = AllTransactionTypes.fromValue(transaction.transactionType)
                    ?: throw IllegalStateException("Invalid transaction type for Stock Adjustment")
                
                if (!AllTransactionTypes.isStockAdjustment(transaction.transactionType)) {
                    throw IllegalStateException("Transaction is not a Stock Adjustment transaction")
                }
                
                // Parse timestamp
                val timestamp = transaction.timestamp?.toLongOrNull() ?: Clock.System.now().toEpochMilliseconds()
                
                // Set state with transaction data
                _state.update { 
                    it.copy(
                        adjustmentType = transactionType,
                        warehouseFrom = transaction.warehouseFrom,
                        warehouseTo = transaction.warehouseTo,
                        products = transaction.transactionDetails,
                        dateTime = timestamp,
                        description = transaction.description ?: "",
                        editingTransactionSlug = transaction.slug,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transaction for editing"
                    )
                }
            }
        }
    }

    fun saveStockAdjustment() {
        val currentState = _state.value

        // Validation
        if (currentState.products.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one product") }
            return
        }

        // Validate warehouse selection based on adjustment type
        when (currentState.adjustmentType) {
            AllTransactionTypes.STOCK_TRANSFER -> {
                if (currentState.warehouseFrom == null) {
                    _state.update { it.copy(error = "Please select 'From' warehouse") }
                    return
                }
                if (currentState.warehouseTo == null) {
                    _state.update { it.copy(error = "Please select 'To' warehouse") }
                    return
                }
                if (currentState.warehouseFrom.slug == currentState.warehouseTo.slug) {
                    _state.update { it.copy(error = "Source and destination warehouses cannot be the same") }
                    return
                }
            }
            AllTransactionTypes.STOCK_INCREASE, AllTransactionTypes.STOCK_REDUCE -> {
                if (currentState.warehouseFrom == null) {
                    _state.update { it.copy(error = "Please select a warehouse") }
                    return
                }
            }
            else -> {
                // No validation for other types
            }
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    slug = currentState.editingTransactionSlug, // Include slug if editing
                    transactionType = currentState.adjustmentType.value,
                    wareHouseSlugFrom = currentState.warehouseFrom?.slug,
                    warehouseFrom = currentState.warehouseFrom,
                    wareHouseSlugTo = if (currentState.adjustmentType == AllTransactionTypes.STOCK_TRANSFER) {
                        currentState.warehouseTo?.slug
                    } else null,
                    warehouseTo = if (currentState.adjustmentType == AllTransactionTypes.STOCK_TRANSFER) {
                        currentState.warehouseTo
                    } else null,
                    transactionDetails = currentState.products,
                    description = currentState.description.ifBlank { null },
                    timestamp = currentState.dateTime.toString(),
                    totalBill = 0.0,
                    totalPaid = 0.0,
                    flatDiscount = 0.0,
                    flatTax = 0.0,
                    additionalCharges = 0.0,
                    businessSlug = appSessionManager.getBusinessSlug(),
                    createdBy = appSessionManager.getUserSlug()
                )

                // Check if we're editing or creating
                if (currentState.editingTransactionSlug != null) {
                    // Update existing transaction
                    transactionUseCases.updateTransaction(transaction)
                        .onSuccess {
                            _state.update { 
                                StockAdjustmentState(
                                    success = true,
                                    adjustmentType = currentState.adjustmentType,
                                    dateTime = Clock.System.now().toEpochMilliseconds()
                                )
                            }
                        }
                        .onFailure { exception ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to update stock adjustment"
                                )
                            }
                        }
                } else {
                    // Create new transaction
                    transactionUseCases.addTransaction(transaction)
                        .onSuccess {
                            _state.update { 
                                StockAdjustmentState(
                                    success = true,
                                    adjustmentType = currentState.adjustmentType,
                                    dateTime = Clock.System.now().toEpochMilliseconds()
                                )
                            }
                        }
                        .onFailure { exception ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to save stock adjustment"
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(success = false) }
    }

    fun resetForm() {
        _state.update { 
            StockAdjustmentState(
                adjustmentType = it.adjustmentType,
                dateTime = Clock.System.now().toEpochMilliseconds(),
                editingTransactionSlug = null
            )
        }
    }
}


