package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import com.hisaabi.hisaabi_kmp.auth.domain.model.User

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): AuthResult<User> {
        // Validate input
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            return AuthResult.Error("All fields are required")
        }
        
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        
        if (password.length < 8) {
            return AuthResult.Error("Password must be at least 8 characters long")
        }
        
        if (!isValidPassword(password)) {
            return AuthResult.Error("Password must contain at least one uppercase letter, one lowercase letter, and one number")
        }
        
        if (firstName.length < 2 || lastName.length < 2) {
            return AuthResult.Error("First and last names must be at least 2 characters long")
        }
        
        return authRepository.register(
            email = email.trim(),
            password = password,
            firstName = firstName.trim(),
            lastName = lastName.trim()
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPassword(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit
    }
}
