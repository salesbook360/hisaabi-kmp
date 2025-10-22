package com.hisaabi.hisaabi_kmp.auth.data.interceptor

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles authentication-related interceptor logic including:
 * - Token refresh on 401 responses
 * - Logout on refresh failure
 */
class AuthInterceptor(
    private val authLocalDataSource: AuthLocalDataSource,
    private val authRepository: AuthRepository
) {
    // Coroutine scope for async operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Callback to be invoked when user needs to be logged out
    var onLogoutRequired: (() -> Unit)? = null
    
    private var isRefreshing = false
    
    /**
     * Handles 401 Unauthorized responses by attempting to refresh the access token.
     * If refresh fails, triggers logout.
     */
    fun handle401Response() {
        // Prevent multiple simultaneous refresh attempts
        if (isRefreshing) {
            println("Token refresh already in progress, skipping...")
            return
        }
        
        isRefreshing = true
        
        scope.launch {
            try {
                println("Attempting to refresh access token...")
                
                val refreshToken = authLocalDataSource.getRefreshToken()
                if (refreshToken == null) {
                    println("No refresh token available, triggering logout")
                    handleLogout()
                    return@launch
                }
                
                // Attempt to refresh token
                val result = authRepository.refreshToken()
                
                when (result) {
                    is AuthResult.Success -> {
                        println("Token refresh successful")
                        // Token has been updated in the repository
                    }
                    is AuthResult.Error -> {
                        println("Token refresh failed: ${result.message}")
                        handleLogout()
                    }

                    else -> {}
                }
            } finally {
                isRefreshing = false
            }
        }
    }
    
    /**
     * Handles logout by clearing auth data and invoking the logout callback
     */
    private suspend fun handleLogout() {
        println("Clearing auth data and triggering logout...")
        authLocalDataSource.clearAuthData()
        
        // Invoke logout callback on main thread if set
        onLogoutRequired?.invoke()
    }
}

