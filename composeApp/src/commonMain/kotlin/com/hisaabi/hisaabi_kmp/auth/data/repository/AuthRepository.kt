package com.hisaabi.hisaabi_kmp.auth.data.repository

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSource
import com.hisaabi.hisaabi_kmp.auth.data.model.*
import com.hisaabi.hisaabi_kmp.auth.domain.model.User
import com.hisaabi.hisaabi_kmp.auth.domain.model.AuthResult
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun register(name: String, email: String, address: String, password: String, phone: String, pic: String = ""): AuthResult<User>
    suspend fun loginWithGoogle(authToken: String): AuthResult<User>
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
            println("Login Request: $request")
            val response = remoteDataSource.login(request)
            println("Login Response: $response")
            
            // Check if this is an error response
            if (response.statusCode != null) {
                // This is an error response
                val errorMessage = response.message ?: "Login failed"
                println("Login Error from API: $errorMessage (Status: ${response.statusCode})")
                return AuthResult.Error(errorMessage)
            }
            
            // Check if data exists (success response)
            if (response.data == null) {
                return AuthResult.Error("Invalid response from server")
            }
            
            // Extract user from the list (should be first item)
            val userDto = response.data.list.firstOrNull()
                ?: return AuthResult.Error("No user data received from server")
            
            // Save tokens and user data locally
            localDataSource.saveAccessToken(userDto.authInfo.accessToken)
            localDataSource.saveRefreshToken(userDto.authInfo.refreshToken)
            localDataSource.saveUser(userDto)
            
            AuthResult.Success(userDto.toDomainModel())
        } catch (e: ResponseException) {
            println("Login ResponseException: ${e.message}")
            val errorBody = try {
                e.response.body<RegisterResponse>()
            } catch (ex: Exception) {
                println("Failed to parse error response: ${ex.message}")
                null
            }
            val errorMessage = errorBody?.message ?: e.message ?: "Login failed"
            println("Login Error Message: $errorMessage")
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            println("Login Exception: ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Login failed")
        }
    }
    
    override suspend fun register(name: String, email: String, address: String, password: String, phone: String, pic: String): AuthResult<User> {
        return try {
            val request = RegisterRequest(name, email, address, password, phone, pic)
            println("Register Request: $request")
            val response = remoteDataSource.register(request)
            println("Register Response: $response")
            
            // Check if this is an error response
            if (response.statusCode != null) {
                // This is an error response
                val errorMessage = response.message ?: "Registration failed"
                println("Register Error from API: $errorMessage (Status: ${response.statusCode})")
                return AuthResult.Error(errorMessage)
            }
            
            // Check if data exists (success response)
            if (response.data == null) {
                return AuthResult.Error("Invalid response from server")
            }
            
            // Extract user from the list (should be first item)
            val userDto = response.data.list.firstOrNull()
                ?: return AuthResult.Error("No user data received from server")
            
            // Save tokens and user data locally
            localDataSource.saveAccessToken(userDto.authInfo.accessToken)
            localDataSource.saveRefreshToken(userDto.authInfo.refreshToken)
            localDataSource.saveUser(userDto)
            
            AuthResult.Success(userDto.toDomainModel())
        } catch (e: ResponseException) {
            println("Register ResponseException: ${e.message}")
            val errorBody = try {
                e.response.body<RegisterResponse>()
            } catch (ex: Exception) {
                println("Failed to parse error response: ${ex.message}")
                null
            }
            val errorMessage = errorBody?.message ?: e.message ?: "Registration failed"
            println("Register Error Message: $errorMessage")
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            println("Register Exception: ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }
    
    override suspend fun loginWithGoogle(authToken: String): AuthResult<User> {
        return try {
            val request = GoogleSignInRequest(authToken)
            println("Google Sign-In Request: $request")
            val response = remoteDataSource.loginWithGoogle(request)
            println("Google Sign-In Response: $response")
            
            // Check if this is an error response
            if (response.statusCode != null) {
                val errorMessage = response.message ?: "Google Sign-In failed"
                println("Google Sign-In Error from API: $errorMessage (Status: ${response.statusCode})")
                return AuthResult.Error(errorMessage)
            }
            
            // Check if data exists (success response)
            if (response.data == null) {
                return AuthResult.Error("Invalid response from server")
            }
            
            // Extract user from the list (should be first item)
            val userDto = response.data?.list?.firstOrNull()
                ?: return AuthResult.Error("No user data received from server")
            
            // Save tokens and user data locally
            localDataSource.saveAccessToken(userDto.authInfo.accessToken)
            localDataSource.saveRefreshToken(userDto.authInfo.refreshToken)
            localDataSource.saveUser(userDto)
            
            AuthResult.Success(userDto.toDomainModel())
        } catch (e: ResponseException) {
            println("Google Sign-In ResponseException: ${e.message}")
            val errorBody = try {
                e.response.body<RegisterResponse>()
            } catch (ex: Exception) {
                println("Failed to parse error response: ${ex.message}")
                null
            }
            val errorMessage = errorBody?.message ?: e.message ?: "Google Sign-In failed"
            println("Google Sign-In Error Message: $errorMessage")
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            println("Google Sign-In Exception: ${e.message}")
            e.printStackTrace()
            AuthResult.Error(e.message ?: "Google Sign-In failed")
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
            
            println("Token refresh successful - updating local tokens")
            
            // Update tokens in local storage
            localDataSource.saveAccessToken(response.accessToken)
            localDataSource.saveRefreshToken(response.refreshToken)
            
            // Get current user from local storage (refresh response doesn't include user data)
            val currentUser = localDataSource.getUser()
                ?: return AuthResult.Error("No user data in local storage")
            
            AuthResult.Success(currentUser.toDomainModel())
        } catch (e: Exception) {
            println("Token refresh exception: ${e.message}")
            e.printStackTrace()
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
    
    override fun observeAuthState(): Flow<Boolean> {
        // Observe database changes for auth state using Room's Flow
        // This will automatically emit when auth data is cleared (logout) or saved (login)
        return localDataSource.observeAuthState()
    }
}

// Extension function to convert DTO to domain model
private fun UserDto.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        address = address.orEmpty(),
        email = email,
        phone = phone.orEmpty(),
        slug = slug,
        firebaseId = firebaseId,
        pic = pic ?: ""  // Convert null to empty string
    )
}
