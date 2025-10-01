package com.hisaabi.hisaabi_kmp.auth.domain.model

import kotlinx.datetime.Instant

data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isEmailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val displayName: String
        get() = if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            fullName
        } else {
            email
        }
}
