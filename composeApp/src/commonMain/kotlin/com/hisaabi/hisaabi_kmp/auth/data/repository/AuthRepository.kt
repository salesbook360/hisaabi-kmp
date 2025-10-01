package com.hisaabi.hisaabi_kmp.auth.data.repository

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSource
import com.hisaabi.hisaabi_kmp.auth.data.model.*
import com.hisaabi.hisaabi_kmp.auth.domain.model.User
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun register(email: String, password: String, firstName: String, lastName: String): AuthResult<User>
    suspend fun logout(): AuthResult<Unit>
    suspend fun refreshToken(): AuthResult<User>
    suspend fun forgotPassword(email: String): AuthResult<Unit>
    suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
    fun observeAuthState(): Flow<Boolean>
}

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): AuthResult<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = remoteDataSource.login(request)
            
            // Save tokens and user data locally
            localDataSource.saveAccessToken(response.accessToken)
            localDataSource.saveRefreshToken(response.refreshToken)
            localDataSource.saveUser(response.user)
            
            AuthResult.Success(response.user.toDomainModel())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }
    
    override suspend fun register(email: String, password: String, firstName: String, lastName: String): AuthResult<User> {
        return try {
            val request = RegisterRequest(email, password, firstName, lastName)
            val response = remoteDataSource.register(request)
            
            // Save tokens and user data locally
            localDataSource.saveAccessToken(response.accessToken)
            localDataSource.saveRefreshToken(response.refreshToken)
            localDataSource.saveUser(response.user)
            
            AuthResult.Success(response.user.toDomainModel())
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }
    
    override suspend fun logout(): AuthResult<Unit> {
        return try {
            remoteDataSource.logout()
            localDataSource.clearAuthData()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            // Even if remote logout fails, clear local data
            localDataSource.clearAuthData()
            AuthResult.Success(Unit)
        }
    }
    
    override suspend fun refreshToken(): AuthResult<User> {
        return try {
            val refreshToken = localDataSource.getRefreshToken()
                ?: return AuthResult.Error("No refresh token available")
            
            val request = RefreshTokenRequest(refreshToken)
            val response = remoteDataSource.refreshToken(request)
            
            // Update tokens and user data
            localDataSource.saveAccessToken(response.accessToken)
            localDataSource.saveRefreshToken(response.refreshToken)
            localDataSource.saveUser(response.user)
            
            AuthResult.Success(response.user.toDomainModel())
        } catch (e: Exception) {
            // If refresh fails, clear auth data
            localDataSource.clearAuthData()
            AuthResult.Error(e.message ?: "Token refresh failed")
        }
    }
    
    override suspend fun forgotPassword(email: String): AuthResult<Unit> {
        return try {
            val request = ForgotPasswordRequest(email)
            remoteDataSource.forgotPassword(request)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }
    
    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> {
        return try {
            val request = ResetPasswordRequest(token, newPassword)
            remoteDataSource.resetPassword(request)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        return localDataSource.getUser()?.toDomainModel()
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return localDataSource.isLoggedIn()
    }
    
    override fun observeAuthState(): Flow<Boolean> = flow {
        emit(localDataSource.isLoggedIn())
    }
}

// Extension function to convert DTO to domain model
private fun UserDto.toDomainModel(): User {
    return User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        isEmailVerified = isEmailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
