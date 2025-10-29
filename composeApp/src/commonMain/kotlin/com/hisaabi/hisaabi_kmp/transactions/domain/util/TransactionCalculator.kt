package com.hisaabi.hisaabi_kmp.transactions.domain.util

import com.hisaabi.hisaabi_kmp.transactions.domain.model.FlatOrPercent
import com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionDetail
import kotlin.math.round

object TransactionCalculator {
    
    /**
     * Calculate flat value from percentage
     */
    fun calculateFlatFromPercent(percent: Double, amount: Double): Double {
        return roundTo2Decimal((percent / 100.0) * amount)
    }
    
    /**
     * Calculate percentage from flat value
     */
    fun calculatePercentFromFlat(flat: Double, amount: Double): Double {
        if (amount == 0.0) return 0.0
        return roundTo2Decimal((flat / amount) * 100.0)
    }
    
    /**
     * Round to 2 decimal places
     */
    fun roundTo2Decimal(value: Double): Double {
        return round(value * 100) / 100
    }
    
    /**
     * Calculate subtotal for a list of transaction details
     */
    fun calculateSubtotal(details: List<TransactionDetail>): Double {
        return roundTo2Decimal(details.sumOf { it.price * it.quantity })
    }
    
    /**
     * Calculate total tax for transaction details
     */
    fun calculateDetailsTax(details: List<TransactionDetail>): Double {
        return roundTo2Decimal(details.sumOf { it.flatTax })
    }
    
    /**
     * Calculate total discount for transaction details
     */
    fun calculateDetailsDiscount(details: List<TransactionDetail>): Double {
        return roundTo2Decimal(details.sumOf { it.flatDiscount })
    }
    
    /**
     * Calculate tax on transaction level (after discount or before discount based on formula)
     */
    fun calculateTransactionTax(
        subtotal: Double,
        additionalCharges: Double,
        discount: Double,
        taxValue: Double,
        taxType: Int,
        taxBeforeDiscount: Boolean
    ): Double {
        val baseAmount = if (taxBeforeDiscount) {
            subtotal + additionalCharges
        } else {
            subtotal + additionalCharges - discount
        }
        
        return if (taxType == FlatOrPercent.PERCENT.value) {
            calculateFlatFromPercent(taxValue, baseAmount)
        } else {
            taxValue
        }
    }
    
    /**
     * Calculate discount on transaction level
     */
    fun calculateTransactionDiscount(
        subtotal: Double,
        additionalCharges: Double,
        discountValue: Double,
        discountType: Int
    ): Double {
        val baseAmount = subtotal + additionalCharges
        
        return if (discountType == FlatOrPercent.PERCENT.value) {
            calculateFlatFromPercent(discountValue, baseAmount)
        } else {
            discountValue
        }
    }
    
    /**
     * Calculate grand total for a transaction
     */
    fun calculateGrandTotal(
        subtotal: Double,
        additionalCharges: Double,
        totalTax: Double,
        totalDiscount: Double
    ): Double {
        return roundTo2Decimal(subtotal + additionalCharges + totalTax - totalDiscount)
    }
    
    /**
     * Calculate payable amount (grand total - paid amount)
     */
    fun calculatePayable(grandTotal: Double, paidAmount: Double, previousBalance : Double): Double {
        return roundTo2Decimal(grandTotal - paidAmount-previousBalance)
    }
    
    /**
     * Calculate profit for a sale transaction detail
     */
    fun calculateProfit(
        salePrice: Double,
        purchasePrice: Double,
        quantity: Double,
        isReturn: Boolean = false
    ): Double {
        val profitPerUnit = salePrice - purchasePrice
        val totalProfit = profitPerUnit * quantity
        return roundTo2Decimal(if (isReturn) -totalProfit else totalProfit)
    }
    
    /**
     * Calculate tax/discount for a product detail
     */
    fun calculateProductTaxDiscount(
        price: Double,
        quantity: Double,
        taxPercent: Double,
        discountPercent: Double,
        taxBeforeDiscount: Boolean
    ): Pair<Double, Double> {
        val subtotal = price * quantity
        
        return if (taxBeforeDiscount) {
            val tax = calculateFlatFromPercent(taxPercent, subtotal)
            val discount = calculateFlatFromPercent(discountPercent, subtotal + tax)
            Pair(tax, discount)
        } else {
            val discount = calculateFlatFromPercent(discountPercent, subtotal)
            val tax = calculateFlatFromPercent(taxPercent, subtotal - discount)
            Pair(tax, discount)
        }
    }
    
    /**
     * Convert quantity from one unit to another using conversion factor
     */
    fun convertQuantity(
        quantity: Double,
        fromUnitConversionFactor: Double,
        toUnitConversionFactor: Double
    ): Double {
        // First convert to base unit, then to target unit
        val baseQuantity = quantity * fromUnitConversionFactor
        return roundTo2Decimal(baseQuantity / toUnitConversionFactor)
    }
    
    /**
     * Format currency value
     */
    fun formatCurrency(value: Double, currencySymbol: String = "₨"): String {
        val absValue = kotlin.math.abs(value)
        val formatted = String.format("%.2f", absValue)
        return "$currencySymbol $formatted"
    }
    
    /**
     * Format quantity with unit
     */
    fun formatQuantity(quantity: Double, unitName: String): String {
        return "${String.format("%.2f", quantity)} $unitName"
    }
}

