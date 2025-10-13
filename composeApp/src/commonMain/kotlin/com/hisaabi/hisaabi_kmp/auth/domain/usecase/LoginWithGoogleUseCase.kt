package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import com.hisaabi.hisaabi_kmp.auth.domain.model.User

class LoginWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(authToken: String): AuthResult<User> {
        // Validate input
        if (authToken.isBlank()) {
            return AuthResult.Error("Google auth token is required")
        }
        
        return authRepository.loginWithGoogle(authToken)
    }
}

