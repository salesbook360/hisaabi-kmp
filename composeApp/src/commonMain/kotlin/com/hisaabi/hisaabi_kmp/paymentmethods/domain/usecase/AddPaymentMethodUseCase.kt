package com.hisaabi.hisaabi_kmp.paymentmethods.domain.usecase

import com.hisaabi.hisaabi_kmp.core.domain.model.EntityTypeEnum
import com.hisaabi.hisaabi_kmp.core.util.SlugGenerator
import com.hisaabi.hisaabi_kmp.paymentmethods.data.repository.PaymentMethodsRepository
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.utils.getCurrentTimestamp

class AddPaymentMethodUseCase(
    private val repository: PaymentMethodsRepository,
    private val slugGenerator: SlugGenerator
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
        
        // Generate slug using centralized slug generator
        val slug = slugGenerator.generateSlug(EntityTypeEnum.ENTITY_TYPE_PAYMENT_METHOD)
            ?: return Result.failure(IllegalStateException("Failed to generate slug: Invalid session context"))
        
        // Get current timestamp in ISO 8601 format
        val now = getCurrentTimestamp()
        
        val paymentMethod = PaymentMethod(
            title = title,
            description = description,
            amount = openingAmount,
            openingAmount = openingAmount,
            statusId = 0, // Active
            slug = slug,
            businessSlug = businessSlug,
            createdBy = createdBy,
            syncStatus = 1, // Needs sync
            createdAt = now,
            updatedAt = now
        )
        
        return repository.insertPaymentMethod(paymentMethod)
    }
}

