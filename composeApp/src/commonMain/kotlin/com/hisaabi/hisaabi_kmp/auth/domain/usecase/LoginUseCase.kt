package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import com.hisaabi.hisaabi_kmp.auth.domain.model.User

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        // Validate input
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email and password are required")
        }
        
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }
        
        return authRepository.login(email.trim(), password)
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
}
