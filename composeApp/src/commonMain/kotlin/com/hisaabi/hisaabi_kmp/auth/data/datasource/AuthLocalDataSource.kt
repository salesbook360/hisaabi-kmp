package com.hisaabi.hisaabi_kmp.auth.data.datasource

import com.hisaabi.hisaabi_kmp.auth.data.model.UserDto
import com.hisaabi.hisaabi_kmp.database.dao.UserAuthDao
import com.hisaabi.hisaabi_kmp.database.entity.UserAuthEntity
import kotlinx.coroutines.flow.map

interface AuthLocalDataSource {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveUser(user: UserDto)
    suspend fun getUser(): UserDto?
    suspend fun clearAuthData()
    suspend fun isLoggedIn(): Boolean
    fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean>
}

/**
 * Implementation of AuthLocalDataSource using Room database for persistent storage.
 * This ensures user authentication data persists across app restarts.
 */
class AuthLocalDataSourceImpl(
    private val userAuthDao: UserAuthDao
) : AuthLocalDataSource {
    
    override suspend fun saveAccessToken(token: String) {
        val currentAuth = userAuthDao.getUserAuth()
        if (currentAuth != null) {
            userAuthDao.updateAccessToken(token)
        } else {
            // This shouldn't happen in normal flow, but handle it gracefully
            println("Warning: Attempting to save access token without user data")
        }
    }
    
    override suspend fun getAccessToken(): String? {
        return userAuthDao.getAccessToken()
    }
    
    override suspend fun saveRefreshToken(token: String) {
        val currentAuth = userAuthDao.getUserAuth()
        if (currentAuth != null) {
            val accessToken = currentAuth.accessToken
            userAuthDao.updateTokens(accessToken, token)
        } else {
            println("Warning: Attempting to save refresh token without user data")
        }
    }
    
    override suspend fun getRefreshToken(): String? {
        return userAuthDao.getRefreshToken()
    }
    
    override suspend fun saveUser(user: UserDto) {
        val userAuthEntity = UserAuthEntity(
            id = 1, // Fixed ID since only one user can be logged in
            userId = user.id,
            name = user.name,
            email = user.email,
            address = user.address,
            phone = user.phone,
            slug = user.slug,
            firebaseId = user.firebaseId,
            pic = user.pic,
            accessToken = user.authInfo.accessToken,
            refreshToken = user.authInfo.refreshToken,
            lastUpdated = System.currentTimeMillis()
        )
        userAuthDao.insertUserAuth(userAuthEntity)
    }
    
    override suspend fun getUser(): UserDto? {
        val userAuth = userAuthDao.getUserAuth() ?: return null
        
        return UserDto(
            id = userAuth.userId,
            name = userAuth.name,
            email = userAuth.email,
            address = userAuth.address,
            phone = userAuth.phone,
            slug = userAuth.slug,
            firebaseId = userAuth.firebaseId,
            pic = userAuth.pic,
            authInfo = com.hisaabi.hisaabi_kmp.auth.data.model.AuthInfo(
                accessToken = userAuth.accessToken,
                refreshToken = userAuth.refreshToken
            )
        )
    }
    
    override suspend fun clearAuthData() {
        userAuthDao.clearUserAuth()
    }
    
    override suspend fun isLoggedIn(): Boolean {
        return userAuthDao.isLoggedIn()
    }
    
    override fun observeAuthState(): kotlinx.coroutines.flow.Flow<Boolean> {
        // Observe database changes using Room's Flow
        return userAuthDao.observeUserAuth().map { userAuth ->
            userAuth != null
        }
    }
}


