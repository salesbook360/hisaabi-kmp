package com.hisaabi.hisaabi_kmp.auth.data.datasource

import com.hisaabi.hisaabi_kmp.auth.data.model.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequest): RegisterResponse
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun loginWithGoogle(request: GoogleSignInRequest): RegisterResponse
    suspend fun refreshToken(request: RefreshTokenRequest): RegisterResponse
    suspend fun forgotPassword(request: ForgotPasswordRequest): Unit
    suspend fun resetPassword(request: ResetPasswordRequest): Unit
    suspend fun logout(): Unit
}

class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : AuthRemoteDataSource {
    
    companion object {
        private const val BASE_URL = "http://52.20.167.4:5000"
        private const val LOGIN_ENDPOINT = "$BASE_URL/login"
        private const val REGISTER_ENDPOINT = "$BASE_URL/register"
        private const val LOGIN_WITH_GOOGLE_ENDPOINT = "$BASE_URL/login-with-google"
        private const val REFRESH_ENDPOINT = "$BASE_URL/refresh-auth-token"
        private const val FORGOT_PASSWORD_ENDPOINT = "$BASE_URL/forgot-password"
        private const val RESET_PASSWORD_ENDPOINT = "$BASE_URL/reset-password"
        private const val LOGOUT_ENDPOINT = "$BASE_URL/logout"
    }
    
    @OptIn(InternalAPI::class)
    override suspend fun login(request: LoginRequest): RegisterResponse {
        println("=== LOGIN API CALL ===")
        println("Endpoint: $LOGIN_ENDPOINT")
        println("Request Body: $request")
        
        return try {
            // Manually serialize and use TextContent to avoid LiveEdit continuation issues
            val json = Json { ignoreUnknownKeys = true }
            val requestBody = json.encodeToString(LoginRequest.serializer(), request)
            
            val requestBuilder = HttpRequestBuilder().apply {
                url.takeFrom(LOGIN_ENDPOINT)
                method = HttpMethod.Post
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                this.body = TextContent(requestBody, ContentType.Application.Json)
            }
            
            val response = httpClient.request(requestBuilder)
            println("Login Response Status: ${response.status}")
            response.body<RegisterResponse>()
        } catch (e: Exception) {
            println("Login API Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun register(request: RegisterRequest): RegisterResponse {
        println("=== REGISTER API CALL ===")
        println("Endpoint: $REGISTER_ENDPOINT")
        println("Request Body: $request")
        println("Headers: Content-Type=application/json, auth=BiVrDgQKR2BR52UF1")
        
        val response = httpClient.post(REGISTER_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("auth", "BiVrDgQKR2BR52UF1")
            setBody(request)
        }
        
        println("Response Status: ${response.status}")
        val registerResponse = response.body<RegisterResponse>()
        println("Response Body: $registerResponse")
        
        return registerResponse
    }
    
    override suspend fun loginWithGoogle(request: GoogleSignInRequest): RegisterResponse {
        println("=== GOOGLE SIGN-IN API CALL ===")
        println("Endpoint: $LOGIN_WITH_GOOGLE_ENDPOINT")
        println("Headers: Content-Type=application/json, auth=aaa")
        
        val response = httpClient.post(LOGIN_WITH_GOOGLE_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("auth", "aaa")
            setBody(request)
        }
        
        println("Response Status: ${response.status}")
        val loginResponse = response.body<RegisterResponse>()
        println("Response Body: $loginResponse")
        
        return loginResponse
    }
    
    override suspend fun refreshToken(request: RefreshTokenRequest): RegisterResponse {
        println("=== REFRESH TOKEN API CALL ===")
        println("Endpoint: $REFRESH_ENDPOINT")
        println("Refresh Token: ${request.refreshToken}")
        
        val response = httpClient.post(REFRESH_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("refreshToken", request.refreshToken)
            setBody("{}")  // Empty JSON object
        }
        
        println("Refresh Response Status: ${response.status}")
        
        // Log raw response for debugging
        val rawBody = response.body<String>()
        println("Raw Response Body: $rawBody")
        
        // Parse the response with lenient settings
        val json = Json { 
            ignoreUnknownKeys = true 
            isLenient = true
        }
        val refreshResponse = json.decodeFromString<RegisterResponse>(rawBody)
        println("Parsed Response: $refreshResponse")
        
        return refreshResponse
    }
    
    override suspend fun forgotPassword(request: ForgotPasswordRequest): Unit {
        httpClient.post(FORGOT_PASSWORD_ENDPOINT) {
            contentType(ContentType.Application.Json)
            header("auth", "aaa")
            url {
                parameters.append("email", request.email)
            }
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
