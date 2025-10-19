package com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.transactions.domain.model.*
import com.hisaabi.hisaabi_kmp.transactions.domain.usecase.TransactionUseCases
import com.hisaabi.hisaabi_kmp.transactions.domain.util.TransactionCalculator
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddTransactionState(
    // Step 1: Products and Party
    val transactionType: TransactionType = TransactionType.SALE,
    val selectedParty: Party? = null,
    val selectedWarehouse: Warehouse? = null,
    val transactionDetails: List<TransactionDetailItem> = emptyList(),
    val priceType: PriceType = PriceType.RETAIL,
    val transactionDateTime: Long = System.currentTimeMillis(),
    
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
    
    // UI State
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
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
        return TransactionCalculator.roundTo2Decimal(
            calculateSubtotal() + flatTax - flatDiscount
        )
    }
}

class AddTransactionViewModel(
    private val useCases: TransactionUseCases
) : ViewModel() {
    
    private val _state = MutableStateFlow(AddTransactionState())
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()
    
    // Step 1 Functions
    fun setTransactionType(type: TransactionType) {
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
    }
    
    fun setPriceType(priceType: PriceType) {
        _state.update { it.copy(priceType = priceType) }
        // Update all product prices based on new price type
        updateProductPrices(priceType)
    }
    
    fun addProduct(product: Product, unit: QuantityUnit?) {
        val price = getProductPrice(product, _state.value.priceType)
        val newDetail = TransactionDetailItem(
            product = product,
            quantity = 1.0,
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
    
    // Step 2 Functions
    fun updateAdditionalCharges(amount: Double, description: String = "") {
        _state.update { 
            it.copy(
                additionalCharges = amount,
                additionalChargesDesc = description
            )
        }
    }
    
    fun updateDiscount(amount: Double, type: FlatOrPercent) {
        _state.update { 
            it.copy(
                flatDiscount = amount,
                discountType = type
            )
        }
    }
    
    fun updateTax(amount: Double, type: FlatOrPercent) {
        _state.update { 
            it.copy(
                flatTax = amount,
                taxType = type
            )
        }
    }
    
    fun updatePaidNow(amount: Double) {
        _state.update { it.copy(paidNow = amount) }
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
        return _state.value.transactionDetails.sumOf { it.flatDiscount }
    }
    
    fun calculateProductsTax(): Double {
        return _state.value.transactionDetails.sumOf { it.flatTax }
    }
    
    fun calculateGrandTotal(): Double {
        val subtotal = calculateSubtotal()
        val totalTax = _state.value.flatTax + calculateProductsTax()
        val totalDiscount = _state.value.flatDiscount + calculateProductsDiscount()
        
        return TransactionCalculator.calculateGrandTotal(
            subtotal,
            _state.value.additionalCharges,
            totalTax,
            totalDiscount
        )
    }
    
    fun calculatePayable(): Double {
        return TransactionCalculator.calculatePayable(
            calculateGrandTotal(),
            _state.value.paidNow
        )
    }
    
    // Save Transaction
    fun saveTransaction() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val transaction = buildTransaction()
                val result = useCases.addTransaction(transaction)
                
                result.onSuccess { slug ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Transaction saved successfully! (ID: $slug)"
                        )
                    }
                }.onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to save transaction"
                        )
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
    
    // Private Helper Functions
    private fun updateDefaultPriceType(type: TransactionType) {
        val priceType = when {
            TransactionType.isDealingWithVendor(type.value) -> PriceType.PURCHASE
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
        
        if (state.transactionDetails.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one product") }
            return false
        }
        
        return true
    }
    
    private fun buildTransaction(): Transaction {
        val state = _state.value
        
        val transactionDetails = state.transactionDetails.map { item ->
            TransactionDetail(
                productSlug = item.product.slug,
                product = item.product,
                quantity = item.quantity,
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
            transactionDetails = transactionDetails
        )
    }
}

