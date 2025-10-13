package com.hisaabi.hisaabi_kmp.auth.data.datasource

import com.hisaabi.hisaabi_kmp.auth.data.model.UserDto

interface AuthLocalDataSource {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveUser(user: UserDto)
    suspend fun getUser(): UserDto?
    suspend fun clearAuthData()
    suspend fun isLoggedIn(): Boolean
}

class AuthLocalDataSourceImpl : AuthLocalDataSource {
    
    // NOTE: This is currently an in-memory implementation
    // For true persistence across app restarts, consider using:
    // - multiplatform-settings library
    // - DataStore (Jetpack)
    // - Platform-specific storage solutions
    
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var currentUser: UserDto? = null
    
    override suspend fun saveAccessToken(token: String) {
        accessToken = token
    }
    
    override suspend fun getAccessToken(): String? {
        return accessToken
    }
    
    override suspend fun saveRefreshToken(token: String) {
        refreshToken = token
    }
    
    override suspend fun getRefreshToken(): String? {
        return refreshToken
    }
    
    override suspend fun saveUser(user: UserDto) {
        currentUser = user
    }
    
    override suspend fun getUser(): UserDto? {
        return currentUser
    }
    
    override suspend fun clearAuthData() {
        accessToken = null
        refreshToken = null
        currentUser = null
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return accessToken != null && currentUser != null
    }
}


