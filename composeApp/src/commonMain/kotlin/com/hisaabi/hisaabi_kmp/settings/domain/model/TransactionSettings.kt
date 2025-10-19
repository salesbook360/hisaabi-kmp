package com.hisaabi.hisaabi_kmp.settings.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionSettings(
    // Main Features
    val isCashInOutEnabled: Boolean = true,
    val isCustomersEnabled: Boolean = true,
    val isProductsEnabled: Boolean = true,
    val isServicesEnabled: Boolean = true,
    val isRecipeProducts: Boolean = true,
    val isStockEnabled: Boolean = true,
    val isMultipleVendorEnabled: Boolean = true,
    val isMultipleWarehouseEnabled: Boolean = false,
    
    // Cash In/Out Settings
    val isAutoFillPaidNow: Boolean = false,
    val isHaveTax: Boolean = true,
    val isHaveAdditionalCharges: Boolean = true,
    val isTakeDescriptionWithAdditionalCharges: Boolean = true,
    
    // Product Settings
    val enableTaxPercentOnProduct: Boolean = true,
    val enableDiscountPercentOnProduct: Boolean = true,
    val enableDescriptionWithProduct: Boolean = false,
    val showPurchasePrice: Boolean = true,
    val showRetailPrice: Boolean = true,
    val showWholeSalePrice: Boolean = true,
    val showAvgPurchasePrice: Boolean = true,
    val autoUpdateNewSalePurchasePrice: Boolean = false,
    val allowPurchaseFromCustomer: Boolean = true,
    
    // Decimal Places
    val decimalPlacesInAmount: Int = 2, // 0-4
    val decimalPlacesInQuantity: Int = 1, // 0-4
    
    // Dropdown Options
    val taxCalculationFormulaType: Int = 0, // 0=Tax After Discount, 1=Tax Before Discount
    val numberFormatterType: Int = 0, // 0=1,234,567 1=12,34,567 2=1234567
    val personGrouping: Int = 0, // 0=By Category, 1=By Area
    
    // Performance
    val speedUpTransactionLoading: Boolean = true,
    
    // Default Values (not shown in UI but stored)
    val defaultPriceType: Int = 1, // 0=Purchase, 1=Retail, 2=Wholesale
    val defaultDiscountType: Int = 0, // 0=Flat, 1=Percent
    val defaultTaxType: Int = 1, // 0=Flat, 1=Percent
    val defaultTaxValue: Double = 0.0
) {
    companion object {
        val DEFAULT = TransactionSettings()
    }
}

enum class TaxCalculationFormula(val id: Int, val displayName: String) {
    TAX_AFTER_DISCOUNT(0, "Tax After Discount"),
    TAX_BEFORE_DISCOUNT(1, "Tax Before Discount");
    
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: TAX_AFTER_DISCOUNT
    }
}

enum class NumberFormatterType(val id: Int, val displayName: String, val example: String) {
    TYPE_ONE(0, "1,234,567", "1,234,567.89"),
    TYPE_TWO(1, "12,34,567", "12,34,567.89"),
    TYPE_THREE(2, "1234567", "1234567.89");
    
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: TYPE_ONE
    }
}

enum class PersonGrouping(val id: Int, val displayName: String) {
    BY_CATEGORY(0, "By Category"),
    BY_AREA(1, "By Area");
    
    companion object {
        fun fromId(id: Int) = entries.find { it.id == id } ?: BY_CATEGORY
    }
}

