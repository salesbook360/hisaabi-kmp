package com.hisaabi.hisaabi_kmp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hisaabi.hisaabi_kmp.database.entity.UserAuthEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for accessing user authentication data from the database.
 * Provides methods to manage authenticated user profile and tokens.
 */
@Dao
interface UserAuthDao {
    
    /**
     * Insert or replace user authentication data.
     * Since only one user can be logged in at a time, this will replace existing data.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAuth(userAuth: UserAuthEntity)
    
    /**
     * Update user authentication data.
     */
    @Update
    suspend fun updateUserAuth(userAuth: UserAuthEntity)
    
    /**
     * Get the currently logged-in user's authentication data.
     * Returns null if no user is logged in.
     */
    @Query("SELECT * FROM user_auth WHERE id = 1 LIMIT 1")
    suspend fun getUserAuth(): UserAuthEntity?
    
    /**
     * Observe changes to the currently logged-in user's authentication data.
     * Emits null if no user is logged in.
     */
    @Query("SELECT * FROM user_auth WHERE id = 1 LIMIT 1")
    fun observeUserAuth(): Flow<UserAuthEntity?>
    
    /**
     * Get the access token for the currently logged-in user.
     */
    @Query("SELECT access_token FROM user_auth WHERE id = 1 LIMIT 1")
    suspend fun getAccessToken(): String?
    
    /**
     * Get the refresh token for the currently logged-in user.
     */
    @Query("SELECT refresh_token FROM user_auth WHERE id = 1 LIMIT 1")
    suspend fun getRefreshToken(): String?
    
    /**
     * Update only the access token.
     * This is useful for token refresh operations.
     */
    @Query("UPDATE user_auth SET access_token = :token, last_updated = :timestamp WHERE id = 1")
    suspend fun updateAccessToken(token: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Update both access and refresh tokens.
     * This is useful for token refresh operations.
     */
    @Query("UPDATE user_auth SET access_token = :accessToken, refresh_token = :refreshToken, last_updated = :timestamp WHERE id = 1")
    suspend fun updateTokens(accessToken: String, refreshToken: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Check if a user is logged in.
     * Returns true if user authentication data exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM user_auth WHERE id = 1")
    suspend fun isLoggedIn(): Boolean
    
    /**
     * Clear all user authentication data (logout).
     * This will delete the user's profile and tokens from the database.
     */
    @Query("DELETE FROM user_auth")
    suspend fun clearUserAuth()
    
    /**
     * Get the last update timestamp.
     */
    @Query("SELECT last_updated FROM user_auth WHERE id = 1 LIMIT 1")
    suspend fun getLastUpdated(): Long?
    
    /**
     * Get the selected business ID.
     */
    @Query("SELECT selected_business_id FROM user_auth WHERE id = 1 LIMIT 1")
    suspend fun getSelectedBusinessId(): Int?
    
    /**
     * Update the selected business ID.
     */
    @Query("UPDATE user_auth SET selected_business_id = :businessId WHERE id = 1")
    suspend fun updateSelectedBusinessId(businessId: Int?)
    
    /**
     * Observe the selected business ID.
     */
    @Query("SELECT selected_business_id FROM user_auth WHERE id = 1 LIMIT 1")
    fun observeSelectedBusinessId(): kotlinx.coroutines.flow.Flow<Int?>
}

