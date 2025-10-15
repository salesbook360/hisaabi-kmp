package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult

class ForgotPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> {
        // Validate input
        if (email.isBlank()) {
            return AuthResult.Error("Email is required")
        }
        
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        
        return authRepository.forgotPassword(email.trim())
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
}


