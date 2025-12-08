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
        
        val business = Business(
            title = title,
            email = email,
            address = address,
            phone = phone,
            logo = logo
        )
        
        return repository.insertBusiness(business)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}

