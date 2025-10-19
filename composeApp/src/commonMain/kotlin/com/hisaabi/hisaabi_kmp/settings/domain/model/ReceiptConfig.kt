package com.hisaabi.hisaabi_kmp.settings.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptConfig(
    // General Settings
    val isReceiptEnabled: Boolean = true,
    val generateReceiptType: ReceiptGenerateOption = ReceiptGenerateOption.ASK_EVERY_TIME,
    val thermalPrinterType: ThermalPrinterType = ThermalPrinterType.THERMAL_80MM,
    
    // Invoice Details
    val showOrderNo: Boolean = true,
    val showTransactionDate: Boolean = true,
    val showTransactionType: Boolean = true,
    val showPaymentMethod: Boolean = true,
    val showTax: Boolean = true,
    val showDiscount: Boolean = true,
    val showAdditionalCharges: Boolean = true,
    val showTotalItems: Boolean = true,
    val showPreviousBalance: Boolean = true,
    val showCurrentBalance: Boolean = true,
    val showPayableAmount: Boolean = true,
    val showInvoiceTerms: Boolean = true,
    
    // Customer Details
    val showCustomerName: Boolean = true,
    val showCustomerPhone: Boolean = true,
    val showCustomerAddress: Boolean = true,
    
    // Business Details
    val showLogoOnReceipt: Boolean = false,
    val showBusinessName: Boolean = true,
    val showBusinessEmail: Boolean = true,
    val showBusinessPhone: Boolean = false,
    val showBusinessAddress: Boolean = false,
    val showRegardsMessage: Boolean = true,
    
    // Editable Fields
    val logoUrl: String? = null,
    val logoLocalPath: String? = null,
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val invoiceTerms: String = "Due on Receipt",
    val regardsMessage: String? = null
) {
    companion object {
        val DEFAULT = ReceiptConfig()
    }
}

@Serializable
enum class ReceiptGenerateOption(val displayName: String) {
    PRINT("Print Receipt"),
    SMS("Auto SMS Receipt"),
    PDF("Generate PDF"),
    ASK_EVERY_TIME("Ask After Each Transaction"),
    NONE("Receipt Not Required");
    
    companion object {
        fun fromOrdinal(ordinal: Int) = entries.getOrNull(ordinal) ?: ASK_EVERY_TIME
    }
}

@Serializable
enum class ThermalPrinterType(val displayName: String, val widthMM: Int) {
    THERMAL_45MM("Thermal 45mm", 45),
    THERMAL_55MM("Thermal 55mm", 55),
    THERMAL_80MM("Thermal 80mm", 80),
    THERMAL_112MM("Thermal 112mm", 112);
    
    companion object {
        fun fromOrdinal(ordinal: Int) = entries.getOrNull(ordinal) ?: THERMAL_80MM
    }
}

