package com.hisaabi.hisaabi_kmp.core.session

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.domain.usecase.GetSelectedBusinessUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Centralized session manager that provides current user and business context.
 * All repositories should use this to get userSlug and businessSlug instead of hardcoding values.
 */
interface AppSessionManager {
    /**
     * Get current user slug
     */
    suspend fun getUserSlug(): String?
    
    /**
     * Get current business slug
     */
    suspend fun getBusinessSlug(): String?
    
    /**
     * Observe user slug changes
     */
    fun observeUserSlug(): Flow<String?>
    
    /**
     * Observe business slug changes
     */
    fun observeBusinessSlug(): Flow<String?>
    
    /**
     * Observe both user and business slugs as a pair
     */
    fun observeSessionContext(): Flow<SessionContext>
    
    /**
     * Get current session context (user + business)
     */
    suspend fun getSessionContext(): SessionContext
}

/**
 * Container for current session context
 */
data class SessionContext(
    val userSlug: String?,
    val businessSlug: String?
) {
    val isValid: Boolean
        get() = userSlug != null && businessSlug != null
    
    fun requireValid(): SessionContext {
        require(isValid) { 
            "Invalid session context: userSlug=$userSlug, businessSlug=$businessSlug" 
        }
        return this
    }
}

class AppSessionManagerImpl(
    private val authLocalDataSource: AuthLocalDataSource,
    private val businessPreferences: BusinessPreferencesDataSource,
    private val getSelectedBusinessUseCase: GetSelectedBusinessUseCase
) : AppSessionManager {
    
    override suspend fun getUserSlug(): String? {
        return authLocalDataSource.getUser()?.slug
    }
    
    override suspend fun getBusinessSlug(): String? {
        return businessPreferences.getSelectedBusinessSlug()
    }
    
    override fun observeUserSlug(): Flow<String?> {
        return authLocalDataSource.observeUser().map { user -> user?.slug }
    }
    
    override fun observeBusinessSlug(): Flow<String?> {
        return businessPreferences.observeSelectedBusinessSlug()
    }
    
    override fun observeSessionContext(): Flow<SessionContext> {
        return combine(
            observeUserSlug(),
            observeBusinessSlug()
        ) { userSlug, businessSlug ->
            SessionContext(userSlug, businessSlug)
        }
    }
    
    override suspend fun getSessionContext(): SessionContext {
        return SessionContext(
            userSlug = getUserSlug(),
            businessSlug = getBusinessSlug()
        )
    }
}

