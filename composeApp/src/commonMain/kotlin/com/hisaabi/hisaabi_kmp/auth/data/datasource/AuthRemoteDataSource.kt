package com.hisaabi.hisaabi_kmp.auth.data.datasource

import com.hisaabi.hisaabi_kmp.auth.data.model.*
import com.hisaabi.hisaabi_kmp.config.AppConfig
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
    suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse
    suspend fun forgotPassword(request: ForgotPasswordRequest): Unit
    suspend fun resetPassword(request: ResetPasswordRequest): Unit
    suspend fun logout(): Unit
}

class AuthRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig
) : AuthRemoteDataSource {
    
    private val baseUrl: String
        get() = appConfig.baseUrl
    
    private val LOGIN_ENDPOINT: String
        get() = "$baseUrl/login"
    private val REGISTER_ENDPOINT: String
        get() = "$baseUrl/register"
    private val LOGIN_WITH_GOOGLE_ENDPOINT: String
        get() = "$baseUrl/login-with-google"
    private val REFRESH_ENDPOINT: String
        get() = "$baseUrl/refresh-auth-token"
    private val FORGOT_PASSWORD_ENDPOINT: String
        get() = "$baseUrl/forgot-password"
    private val RESET_PASSWORD_ENDPOINT: String
        get() = "$baseUrl/reset-password"
    private val LOGOUT_ENDPOINT: String
        get() = "$baseUrl/logout"
    
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
    
    override suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
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
        val refreshResponse = json.decodeFromString<RefreshTokenResponse>(rawBody)
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
