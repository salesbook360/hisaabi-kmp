package com.hisaabi.hisaabi_kmp.business.domain.usecase

import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business

class UpdateBusinessUseCase(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(business: Business): Result<Unit> {
        // Validate input
        if (business.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Business name cannot be empty"))
        }
        
        // Validate email format if provided
        if (!business.email.isNullOrBlank() && !isValidEmail(business.email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        
        return repository.updateBusiness(business)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}

