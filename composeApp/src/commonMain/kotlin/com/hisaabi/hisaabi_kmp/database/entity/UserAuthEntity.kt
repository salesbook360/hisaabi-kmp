package com.hisaabi.hisaabi_kmp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hisaabi.hisaabi_kmp.utils.currentTimeMillis

/**
 * Entity to store authenticated user profile and tokens persistently.
 * Only one user can be logged in at a time, so we use a fixed ID.
 */
@Entity(tableName = "user_auth")
data class UserAuthEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1, // Fixed ID since only one user can be logged in at a time
    
    // User Profile Information
    @ColumnInfo(name = "user_id")
    val userId: Int,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "phone")
    val phone: String,
    
    @ColumnInfo(name = "slug")
    val slug: String,
    
    @ColumnInfo(name = "firebase_id")
    val firebaseId: String,
    
    @ColumnInfo(name = "pic")
    val pic: String? = null,
    
    // Authentication Tokens
    @ColumnInfo(name = "access_token")
    val accessToken: String,
    
    @ColumnInfo(name = "refresh_token")
    val refreshToken: String,
    
    // Selected Business (for multi-business support)
    @ColumnInfo(name = "selected_business_slug")
    val selectedBusinessSlug: String? = null,
    
    // Metadata
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = currentTimeMillis()
)

