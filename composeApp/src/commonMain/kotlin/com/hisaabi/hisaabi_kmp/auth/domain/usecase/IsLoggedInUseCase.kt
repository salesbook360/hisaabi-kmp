package com.hisaabi.hisaabi_kmp.auth.domain.usecase

import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class IsLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    fun observe(): Flow<Boolean> {
        return authRepository.observeAuthState()
    }
}
