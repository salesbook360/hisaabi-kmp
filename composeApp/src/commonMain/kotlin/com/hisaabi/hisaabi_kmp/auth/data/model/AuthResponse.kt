package com.hisaabi.hisaabi_kmp.auth.data.model

import kotlinx.serialization.Serializable

// Success Response Structure
@Serializable
data class RegisterResponse(
    val data: RegisterData? = null,
    val message: String? = null,
    val status: Int? = null,
    val timestamp: String? = null,
    // Error response fields
    val statusCode: String? = null
)

@Serializable
data class RegisterData(
    val list: List<UserDto> = emptyList(),
    val totalRecords: Int = 0
)

// User DTO matching the API response
@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val address: String?,
    val email: String,
    val phone: String?,
    val slug: String,
    val firebaseId: String,
    val pic: String? = null,  // Made nullable as API can return null
    val authInfo: AuthInfo
)

@Serializable
data class AuthInfo(
    val accessToken: String,
    val refreshToken: String
)

// Error response models
@Serializable
data class ApiError(
    val message: String,
    val code: String? = null,
    val details: Map<String, String>? = null
)

@Serializable
data class ApiErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: String
)
