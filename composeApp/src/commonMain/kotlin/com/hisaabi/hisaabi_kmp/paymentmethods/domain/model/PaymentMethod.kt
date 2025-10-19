package com.hisaabi.hisaabi_kmp.paymentmethods.domain.model

data class PaymentMethod(
    val id: Int = 0,
    val title: String,
    val description: String?,
    val amount: Double = 0.0,
    val openingAmount: Double = 0.0,
    val statusId: Int = 1, // 1=Active, 2=Inactive, 3=Deleted
    val slug: String?,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
) {
    val displayName: String
        get() = title.ifEmpty { "Unknown" }
    
    val isActive: Boolean
        get() = statusId == 1
}

enum class PaymentMethodStatus(val statusId: Int, val displayName: String) {
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive"),
    DELETED(3, "Deleted");
    
    companion object {
        fun fromInt(value: Int): PaymentMethodStatus? = entries.find { it.statusId == value }
    }
}

