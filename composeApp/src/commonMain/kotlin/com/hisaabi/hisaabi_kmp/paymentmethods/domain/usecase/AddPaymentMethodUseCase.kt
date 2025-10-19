package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import kotlinx.datetime.Clock

class AddPaymentMethodUseCase(
    private val repository: PaymentMethodsRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        openingAmount: Double,
        businessSlug: String?,
        createdBy: String?
    ): Result<Long> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Title cannot be empty"))
        }
        
        // Generate slug from title
        val slug = generateSlug(title)
        
        // Check if payment method with same slug exists
        val existing = repository.getPaymentMethodBySlug(slug)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Payment method with this title already exists"))
        }
        
        val now = Clock.System.now().toString()
        
        val paymentMethod = PaymentMethod(
            title = title,
            description = description,
            amount = openingAmount,
            openingAmount = openingAmount,
            statusId = 1, // Active
            slug = slug,
            businessSlug = businessSlug,
            createdBy = createdBy,
            syncStatus = 1, // Needs sync
            createdAt = now,
            updatedAt = now
        )
        
        return repository.insertPaymentMethod(paymentMethod)
    }
    
    private fun generateSlug(title: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "${title.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-$timestamp"
    }
}

