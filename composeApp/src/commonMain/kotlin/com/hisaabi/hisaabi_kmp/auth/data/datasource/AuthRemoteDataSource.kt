package com.hisaabi.hisaabi_kmp.auth.data.datasource

import com.hisaabi.hisaabi_kmp.auth.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse
    suspend fun forgotPassword(request: ForgotPasswordRequest): Unit
    suspend fun resetPassword(request: ResetPasswordRequest): Unit
    suspend fun logout(): Unit
}

class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : AuthRemoteDataSource {
    
    companion object {
        private const val BASE_URL = "https://api.hisaabi.com/v1"
        private const val LOGIN_ENDPOINT = "$BASE_URL/auth/login"
        private const val REGISTER_ENDPOINT = "$BASE_URL/auth/register"
        private const val REFRESH_ENDPOINT = "$BASE_URL/auth/refresh"
        private const val FORGOT_PASSWORD_ENDPOINT = "$BASE_URL/auth/forgot-password"
        private const val RESET_PASSWORD_ENDPOINT = "$BASE_URL/auth/reset-password"
        private const val LOGOUT_ENDPOINT = "$BASE_URL/auth/logout"
    }
    
    override suspend fun login(request: LoginRequest): AuthResponse {
        return httpClient.post(LOGIN_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ApiResponse<AuthResponse>>().data ?: throw Exception("Login failed")
    }
    
    override suspend fun register(request: RegisterRequest): AuthResponse {
        return httpClient.post(REGISTER_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ApiResponse<AuthResponse>>().data ?: throw Exception("Registration failed")
    }
    
    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        return httpClient.post(REFRESH_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ApiResponse<AuthResponse>>().data ?: throw Exception("Token refresh failed")
    }
    
    override suspend fun forgotPassword(request: ForgotPasswordRequest): Unit {
        httpClient.post(FORGOT_PASSWORD_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
    
    override suspend fun resetPassword(request: ResetPasswordRequest): Unit {
        httpClient.post(RESET_PASSWORD_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
    
    override suspend fun logout(): Unit {
        httpClient.post(LOGOUT_ENDPOINT)
    }
}
