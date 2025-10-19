package com.hisaabi.hisaabi_kmp.profile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val email: String = "",
    val firebaseId: String = "",
    val name: String = "",
    val phone: String = "",
    val pic: String = "",
    val slug: String = ""
) {
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               email.isNotBlank() && 
               phone.isNotBlank()
    }
    
    companion object {
        val EMPTY = UserProfile()
    }
}

@Serializable
data class UpdateProfileRequest(
    val email: String,
    val firebaseId: String,
    val name: String,
    val phone: String,
    val pic: String,
    val slug: String
)

@Serializable
data class UpdateProfileResponse(
    val success: Boolean = false,
    val message: String = "",
    val user: UserProfile? = null
)

