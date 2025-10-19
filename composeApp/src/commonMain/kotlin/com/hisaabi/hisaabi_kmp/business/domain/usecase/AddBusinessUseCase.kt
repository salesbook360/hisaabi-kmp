package com.hisaabi.hisaabi_kmp.business.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import kotlinx.datetime.Clock

class AddBusinessUseCase(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(
        title: String,
        email: String?,
        address: String?,
        phone: String?,
        logo: String?
    ): Result<Long> {
        // Validate input
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Business name cannot be empty"))
        }
        
        // Validate email format if provided
        if (!email.isNullOrBlank() && !isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        
        // Generate slug from title
        val slug = generateSlug(title)
        
        // Check if business with same slug exists
        val existing = repository.getBusinessBySlug(slug)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Business with this name already exists"))
        }
        
        val business = Business(
            title = title,
            email = email,
            address = address,
            phone = phone,
            logo = logo,
            slug = slug
        )
        
        return repository.insertBusiness(business)
    }
    
    private fun generateSlug(title: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "${title.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-$timestamp"
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}

