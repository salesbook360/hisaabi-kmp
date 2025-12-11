package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.core.session.AppSessionManager
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.domain.model.*
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.transactions.domain.util.TransactionCalculator
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.data.repository.WarehousesRepository
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.hisaabi.hisaabi_kmp.utils.currentTimeMillis

data class AddTransactionState(
    // Step 1: Products and Party
    val transactionType: AllTransactionTypes = AllTransactionTypes.SALE,
    val selectedParty: Party? = null,
    val selectedWarehouse: Warehouse? = null,
    val transactionDetails: List<TransactionDetailItem> = emptyList(),
    val priceType: PriceType = PriceType.RETAIL,
    val transactionDateTime: Long = currentTimeMillis(),
    
    // Step 2: Payment Details
    val previousBalance: Double = 0.0,
    val additionalCharges: Double = 0.0,
    val additionalChargesDesc: String = "",
    val flatDiscount: Double = 0.0,
    val discountType: FlatOrPercent = FlatOrPercent.FLAT,
    val flatTax: Double = 0.0,
    val taxType: FlatOrPercent = FlatOrPercent.FLAT,
    val paidNow: Double = 0.0,
    val remarks: String = "",
    val shippingAddress: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val attachments: List<String> = emptyList(),
    val totalPayable: Double = 0.0,
    
    // UI State
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val savedTransactionSlug: String? = null,
    val editingTransactionSlug: String? = null
)

data class TransactionDetailItem(
    val product: Product,
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val flatDiscount: Double = 0.0,
    val discountType: FlatOrPercent = FlatOrPercent.FLAT,
    val flatTax: Double = 0.0,
    val taxType: FlatOrPercent = FlatOrPercent.FLAT,
    val description: String = "",
    val selectedUnit: QuantityUnit? = null
) {
    fun calculateSubtotal(): Double {
        return TransactionCalculator.roundTo2Decimal(price * quantity)
    }
    
    fun calculateTotal(): Double {
        val subtotal = calculateSubtotal()
        
        // Calculate actual discount and tax based on type
        val actualDiscount = if (discountType == FlatOrPercent.PERCENT) {
            TransactionCalculator.calculateFlatFromPercent(flatDiscount, subtotal)
        } else {
            flatDiscount
        }
        
        val actualTax = if (taxType == FlatOrPercent.PERCENT) {
            TransactionCalculator.calculateFlatFromPercent(flatTax, subtotal)
        } else {
            flatTax
        }
        
        return TransactionCalculator.roundTo2Decimal(
            subtotal + actualTax - actualDiscount
        )
    }
}

class AddTransactionViewModel(
    private val useCases: TransactionUseCases,
    private val sessionManager: AppSessionManager,
    private val getTransactionWithDetailsUseCase: com.hisaabi.hisaabi_kmp.transactions.domain.usecase.GetTransactionWithDetailsUseCase,
    private val quantityUnitsRepository: com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository,
    private val warehousesRepository: WarehousesRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()
    
    init {
        // Load last selected warehouse from preferences on initialization
        loadLastSelectedWarehouse()
    }
    
    /**
     * Load the last selected warehouse from preferences and set it if available.
     * This enables speedy transaction creation by pre-populating the warehouse.
     */
    private fun loadLastSelectedWarehouse() {
        viewModelScope.launch {
            val lastWarehouseSlug = preferencesManager.getLastSelectedWarehouseSlug()
            if (lastWarehouseSlug != null) {
                val warehouse = warehousesRepository.getWarehouseBySlug(lastWarehouseSlug)
                if (warehouse != null) {
                    _state.update { it.copy(selectedWarehouse = warehouse) }
                }
            }
        }
    }
    
    // Step 1 Functions
    fun setTransactionType(type: AllTransactionTypes) {
        _state.update { it.copy(transactionType = type) }
        updateDefaultPriceType(type)
    }
    
    fun selectParty(party: Party) {
        _state.update { 
            it.copy(
                selectedParty = party,
                previousBalance = party.balance
            )
        }
    }
    
    fun selectWarehouse(warehouse: Warehouse) {
        _state.update { it.copy(selectedWarehouse = warehouse) }
        // Save selected warehouse to preferences for speedy future transactions
        warehouse.slug?.let { slug ->
            preferencesManager.setLastSelectedWarehouseSlug(slug)
        }
    }
    
    fun setPriceType(priceType: PriceType) {
        _state.update { it.copy(priceType = priceType) }
        // Update all product prices based on new price type
        updateProductPrices(priceType)
    }
    
    fun addProduct(product: Product, unit: QuantityUnit?, quantity: Double = 1.0) {
        val price = getProductPrice(product, _state.value.priceType)
        val newDetail = TransactionDetailItem(
            product = product,
            quantity = quantity,
            price = price,
            flatDiscount = 0.0,
            flatTax = 0.0,
            selectedUnit = unit
        )
        
        _state.update { 
            it.copy(transactionDetails = it.transactionDetails + newDetail)
        }
    }
    
    fun removeProduct(index: Int) {
        _state.update { 
            it.copy(transactionDetails = it.transactionDetails.filterIndexed { i, _ -> i != index })
        }
    }
    
    fun updateProductQuantity(index: Int, quantity: Double) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(quantity = quantity)
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    fun updateProductPrice(index: Int, price: Double) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(price = price)
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    fun updateProductDiscount(index: Int, discount: Double, type: FlatOrPercent) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(
                    flatDiscount = discount,
                    discountType = type
                )
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    fun updateProductTax(index: Int, tax: Double, type: FlatOrPercent) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(
                    flatTax = tax,
                    taxType = type
                )
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    fun updateProductDescription(index: Int, description: String) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(description = description)
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    fun updateProductUnit(index: Int, unit: QuantityUnit) {
        _state.update { 
            val updatedDetails = it.transactionDetails.toMutableList()
            if (index in updatedDetails.indices) {
                updatedDetails[index] = updatedDetails[index].copy(selectedUnit = unit)
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    /**
     * Get sibling units (units with the same parent) for a given unit.
     * This is used to populate the unit selection bottom sheet.
     */
    suspend fun getSiblingUnits(unit: QuantityUnit?): List<QuantityUnit> {
        val parentSlug = unit?.parentSlug ?: return emptyList()
        return quantityUnitsRepository.getUnitsByParentSuspend(parentSlug)
    }
    
    /**
     * Get a unit by its slug.
     */
    suspend fun getUnitBySlug(slug: String): QuantityUnit? {
        return quantityUnitsRepository.getUnitBySlug(slug)
    }
    
    // Step 2 Functions
    fun updateAdditionalCharges(amount: Double, description: String = "") {
        _state.update { 
            it.copy(
                additionalCharges = amount,
                additionalChargesDesc = description
            )
        }
        updateTotalBillState()
    }

    private fun updateTotalBillState() {
        _state.update { it.copy(totalPayable = calculatePayable()) }
    }

    fun updateDiscount(amount: Double, type: FlatOrPercent) {
        _state.update { 
            it.copy(
                flatDiscount = amount,
                discountType = type
            )
        }
        updateTotalBillState()
    }
    
    fun updateTax(amount: Double, type: FlatOrPercent) {
        _state.update { 
            it.copy(
                flatTax = amount,
                taxType = type
            )
        }
        updateTotalBillState()
    }
    
    fun updatePaidNow(amount: Double) {
        _state.update { it.copy(paidNow = amount) }
        updateTotalBillState()
    }
    
    fun updateRemarks(remarks: String) {
        _state.update { it.copy(remarks = remarks) }
    }
    
    fun updateShippingAddress(address: String) {
        _state.update { it.copy(shippingAddress = address) }
    }
    
    fun selectPaymentMethod(method: PaymentMethod) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }
    
    fun addAttachment(attachmentPath: String) {
        _state.update { 
            it.copy(attachments = it.attachments + attachmentPath)
        }
    }
    
    fun removeAttachment(index: Int) {
        _state.update { 
            it.copy(attachments = it.attachments.filterIndexed { i, _ -> i != index })
        }
    }
    
    // Navigation
    fun goToStep2() {
        if (validateStep1()) {
            _state.update { it.copy(currentStep = 2) }
            updateTotalBillState()
        }
    }
    
    fun goToStep1() {
        _state.update { it.copy(currentStep = 1) }
    }
    
    // Calculations
    fun calculateSubtotal(): Double {
        return _state.value.transactionDetails.sumOf { it.calculateSubtotal() }
    }
    
    fun calculateProductsDiscount(): Double {
        return _state.value.transactionDetails.sumOf { detail ->
            if (detail.discountType == FlatOrPercent.PERCENT) {
                TransactionCalculator.calculateFlatFromPercent(detail.flatDiscount, detail.calculateSubtotal())
            } else {
                detail.flatDiscount
            }
        }
    }
    
    fun calculateProductsTax(): Double {
        return _state.value.transactionDetails.sumOf { detail ->
            if (detail.taxType == FlatOrPercent.PERCENT) {
                TransactionCalculator.calculateFlatFromPercent(detail.flatTax, detail.calculateSubtotal())
            } else {
                detail.flatTax
            }
        }
    }
    
    fun calculateTransactionDiscount(): Double {
        val subtotal = calculateSubtotal()
        val additionalCharges = _state.value.additionalCharges
        
        return TransactionCalculator.calculateTransactionDiscount(
            subtotal,
            additionalCharges,
            _state.value.flatDiscount,
            _state.value.discountType.value
        )
    }
    
    fun calculateTransactionTax(): Double {
        val subtotal = calculateSubtotal()
        val additionalCharges = _state.value.additionalCharges
        val transactionDiscount = calculateTransactionDiscount()
        
        return TransactionCalculator.calculateTransactionTax(
            subtotal,
            additionalCharges,
            transactionDiscount,
            _state.value.flatTax,
            _state.value.taxType.value,
            true // taxBeforeDiscount - you may want to make this configurable
        )
    }
    
    fun calculateGrandTotal(): Double {
        val subtotal = calculateSubtotal()
        val additionalCharges = _state.value.additionalCharges
        
        // Calculate transaction-level discount and tax using proper calculation methods
        val transactionDiscount = calculateTransactionDiscount()
        val transactionTax = calculateTransactionTax()
        
        val totalTax = transactionTax + calculateProductsTax()
        val totalDiscount = transactionDiscount + calculateProductsDiscount()
        
        return TransactionCalculator.calculateGrandTotal(
            subtotal,
            additionalCharges,
            totalTax,
            totalDiscount
        )
    }
    
    fun calculatePayable(): Double {
        return TransactionCalculator.calculatePayable(
            calculateGrandTotal(),
            _state.value.paidNow,
            _state.value.previousBalance
        )
    }
    
    // Load Transaction for Editing by Slug
    fun loadTransactionForEdit(transactionSlug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load full transaction with all details
                val transaction = getTransactionWithDetailsUseCase(transactionSlug)
                    ?: throw IllegalStateException("Transaction not found")
                
                // Convert transaction type
                val transactionType = AllTransactionTypes.fromValue(transaction.transactionType) 
                    ?: AllTransactionTypes.SALE
                
                // Convert transaction details to items
                val detailItems = transaction.transactionDetails.map { detail ->
                    // Convert quantity from base unit back to selected unit by dividing with conversion factor
                    val conversionFactor = detail.quantityUnit?.conversionFactor ?: 1.0
                    val quantityInSelectedUnit = if (conversionFactor != 0.0) {
                        detail.quantity / conversionFactor
                    } else {
                        detail.quantity
                    }
                    
                    TransactionDetailItem(
                        product = detail.product ?: throw IllegalStateException("Product not loaded"),
                        quantity = quantityInSelectedUnit,
                        price = detail.price,
                        flatDiscount = detail.flatDiscount,
                        discountType = FlatOrPercent.fromValue(detail.discountType) ?: FlatOrPercent.FLAT,
                        flatTax = detail.flatTax,
                        taxType = FlatOrPercent.fromValue(detail.taxType) ?: FlatOrPercent.FLAT,
                        description = detail.description ?: "",
                        selectedUnit = detail.quantityUnit
                    )
                }
                
                // Parse timestamp
                val timestamp = transaction.timestamp?.toLongOrNull() ?: currentTimeMillis()
                
                // Set state with transaction data
                _state.update { 
                    it.copy(
                        transactionType = transactionType,
                        selectedParty = transaction.party,
                        selectedWarehouse = transaction.warehouseFrom,
                        transactionDetails = detailItems,
                        priceType = PriceType.fromValue(transaction.priceTypeId) ?: PriceType.RETAIL,
                        transactionDateTime = timestamp,
                        previousBalance = transaction.party?.balance ?: 0.0,
                        additionalCharges = transaction.additionalCharges,
                        additionalChargesDesc = transaction.additionalChargesDesc ?: "",
                        flatDiscount = transaction.flatDiscount,
                        discountType = FlatOrPercent.fromValue(transaction.discountTypeId) ?: FlatOrPercent.FLAT,
                        flatTax = transaction.flatTax,
                        taxType = FlatOrPercent.fromValue(transaction.taxTypeId) ?: FlatOrPercent.FLAT,
                        paidNow = transaction.totalPaid,
                        remarks = transaction.description ?: "",
                        shippingAddress = transaction.shippingAddress ?: "",
                        selectedPaymentMethod = transaction.paymentMethodTo,
                        editingTransactionSlug = transaction.slug,
                        isLoading = false
                    )
                }
                
                // Recalculate totals
                updateTotalBillState()
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
    
    // Save Transaction
    fun saveTransaction() {
        val currentState = _state.value
        
        // Validation: Check if payment method is selected
        if (currentState.selectedPaymentMethod == null) {
            _state.update { it.copy(error = "Please select a payment method") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val businessSlug = sessionManager.getBusinessSlug()
                if (businessSlug == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "No business selected. Please select a business first."
                        )
                    }
                    return@launch
                }
                
                val transaction = buildTransaction(businessSlug)
                
                // Check if we're editing or creating
                if (currentState.editingTransactionSlug != null) {
                    // Update existing transaction
                    useCases.updateTransaction(transaction)
                        .onSuccess {
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Transaction updated successfully!",
                                    savedTransactionSlug = currentState.editingTransactionSlug
                                )
                            }
                        }
                        .onFailure { error ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to update transaction"
                                )
                            }
                        }
                } else {
                    // Create new transaction
                    useCases.addTransaction(transaction)
                        .onSuccess { slug ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Transaction saved successfully! (ID: $slug)",
                                    savedTransactionSlug = slug
                                )
                            }
                        }
                        .onFailure { error ->
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Failed to save transaction"
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
        _state.update { it.copy(successMessage = null) }
    }
    
    fun reset() {
        _state.value = AddTransactionState()
    }
    
    // Private Helper Functions
    private fun updateDefaultPriceType(type: AllTransactionTypes) {
        val priceType = when {
            AllTransactionTypes.isDealingWithVendor(type.value) -> PriceType.PURCHASE
            else -> PriceType.RETAIL
        }
        _state.update { it.copy(priceType = priceType) }
    }
    
    private fun updateProductPrices(priceType: PriceType) {
        _state.update { 
            val updatedDetails = it.transactionDetails.map { detail ->
                detail.copy(price = getProductPrice(detail.product, priceType))
            }
            it.copy(transactionDetails = updatedDetails)
        }
    }
    
    private fun getProductPrice(product: Product, priceType: PriceType): Double {
        return when (priceType) {
            PriceType.RETAIL -> product.retailPrice
            PriceType.WHOLESALE -> product.wholesalePrice
            PriceType.PURCHASE -> product.purchasePrice
        }
    }
    
    private fun validateStep1(): Boolean {
        val state = _state.value
        
        if (state.selectedParty == null) {
            _state.update { it.copy(error = "Please select a party (customer/vendor)") }
            return false
        }
        
        // Validate warehouse for transaction types that require it
        if (AllTransactionTypes.requiresWarehouse(state.transactionType.value) && state.selectedWarehouse == null) {
            _state.update { it.copy(error = "Please select a warehouse") }
            return false
        }
        
        if (state.transactionDetails.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one product") }
            return false
        }
        
        return true
    }
    
    /**
     * Check if warehouse is selected (for enabling/disabling product selection).
     * Returns true if warehouse is not required for the current transaction type,
     * or if it's required and a warehouse has been selected.
     */
    fun isWarehouseSelectedOrNotRequired(): Boolean {
        val state = _state.value
        return !AllTransactionTypes.requiresWarehouse(state.transactionType.value) || state.selectedWarehouse != null
    }
    
    private fun buildTransaction(businessSlug: String): Transaction {
        val state = _state.value
        
        val transactionDetails = state.transactionDetails.map { item ->
            // Convert quantity to base unit by multiplying with conversion factor
            val conversionFactor = item.selectedUnit?.conversionFactor ?: 1.0
            val quantityInBaseUnit = item.quantity * conversionFactor
            
            TransactionDetail(
                productSlug = item.product.slug,
                product = item.product,
                quantity = quantityInBaseUnit,
                price = item.price,
                flatTax = item.flatTax,
                taxType = item.taxType.value,
                flatDiscount = item.flatDiscount,
                discountType = item.discountType.value,
                description = item.description,
                quantityUnitSlug = item.selectedUnit?.slug,
                quantityUnit = item.selectedUnit
            )
        }
        
        return Transaction(
            slug = state.editingTransactionSlug, // Include slug if editing
            customerSlug = state.selectedParty?.slug,
            party = state.selectedParty,
            totalBill = calculateSubtotal(),
            totalPaid = state.paidNow,
            timestamp = state.transactionDateTime.toString(),
            flatDiscount = state.flatDiscount,
            discountTypeId = state.discountType.value,
            flatTax = state.flatTax,
            taxTypeId = state.taxType.value,
            additionalCharges = state.additionalCharges,
            additionalChargesDesc = state.additionalChargesDesc,
            paymentMethodToSlug = state.selectedPaymentMethod?.slug,
            transactionType = state.transactionType.value,
            priceTypeId = state.priceType.value,
            description = state.remarks,
            shippingAddress = state.shippingAddress,
            wareHouseSlugFrom = state.selectedWarehouse?.slug,
            transactionDetails = transactionDetails,
            businessSlug = businessSlug
        )
    }
}

