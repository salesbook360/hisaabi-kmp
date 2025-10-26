package com.hisaabi.hisaabi_kmp.paymentmethods.domain.model

data class PaymentMethod(
    val id: Int = 0,
    val title: String,
    val description: String?,
    val amount: Double = 0.0,
    val openingAmount: Double = 0.0,
    val statusId: Int = 0, // 0=Active, 2=Deleted
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
        get() = statusId == 0
}

