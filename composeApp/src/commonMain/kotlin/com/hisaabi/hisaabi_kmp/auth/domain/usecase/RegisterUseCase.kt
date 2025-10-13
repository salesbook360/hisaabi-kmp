package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import com.hisaabi.hisaabi_kmp.auth.domain.model.User

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        address: String,
        password: String,
        phone: String,
        pic: String = ""
    ): AuthResult<User> {
        // Validate input
        if (name.isBlank()) {
            return AuthResult.Error("Name is required")
        }
        
        if (email.isBlank()) {
            return AuthResult.Error("Email is required")
        }
        
        if (address.isBlank()) {
            return AuthResult.Error("Address is required")
        }
        
        if (password.isBlank()) {
            return AuthResult.Error("Password is required")
        }
        
        if (phone.isBlank()) {
            return AuthResult.Error("Phone number is required")
        }
        
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        
        if (password.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }
        
        if (!isValidPhone(phone)) {
            return AuthResult.Error("Please enter a valid phone number (e.g., +923464889821)")
        }
        
        if (name.length < 2) {
            return AuthResult.Error("Name must be at least 2 characters long")
        }
        
        return authRepository.register(
            name = name.trim(),
            email = email.trim(),
            address = address.trim(),
            password = password,
            phone = phone.trim(),
            pic = pic
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
    
    private fun isValidPhone(phone: String): Boolean {
        // Basic phone validation - starts with + and contains digits
        val phoneRegex = "^\\+?[0-9]{10,15}$".toRegex()
        return phoneRegex.matches(phone.replace(" ", "").replace("-", ""))
    }
}
