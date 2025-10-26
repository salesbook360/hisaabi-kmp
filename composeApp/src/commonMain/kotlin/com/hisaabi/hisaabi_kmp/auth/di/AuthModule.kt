package com.hisaabi.hisaabi_kmp.auth.di

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSourceImpl
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSourceImpl
import com.hisaabi.hisaabi_kmp.auth.data.interceptor.AuthInterceptor
import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepositoryImpl
import com.hisaabi.hisaabi_kmp.auth.domain.usecase.*
import com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel.AuthViewModel
import com.hisaabi.hisaabi_kmp.network.interceptor.LoggingInterceptor
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authModule = module {
    
    // Data Sources - Now uses database for persistent storage
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl(get()) }
    
    // HTTP Client - created first without interceptor
    single<HttpClient> {
        val authLocalDataSource = get<AuthLocalDataSource>()
        val businessPreferences = get<com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource>()
        
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
            
            // Install custom logging interceptor for detailed API logging
            install(LoggingInterceptor().createPlugin())
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
            
            // Request interceptor - adds Authorization and business_key headers
            install(io.ktor.client.plugins.api.createClientPlugin("AuthAndBusinessInterceptor") {
                onRequest { request, _ ->
                    // Don't add auth header for login/register/refresh endpoints
                    val url = request.url.toString()
                    val shouldSkipAuth = url.contains("/login") || 
                                       url.contains("/register") || 
                                       url.contains("/refresh-auth-token") ||
                                       url.contains("/forgot-password")
                    
                    if (!shouldSkipAuth) {
                        // Use runBlocking here since we're in a callback context
                        val token = kotlinx.coroutines.runBlocking {
                            authLocalDataSource.getAccessToken()
                        }
                        if (token != null) {
                            request.headers.append("Authorization", token)
                            println("Added Authorization header to request: ${request.url}")
                        } else {
                            println("Warning: No access token available for request: ${request.url}")
                        }
                        
                        // Add business_key header with selected business slug
                        val businessSlug = kotlinx.coroutines.runBlocking {
                            businessPreferences.getSelectedBusinessSlug()
                        }
                        if (businessSlug != null) {
                            request.headers.append("business_key", businessSlug)
                            println("Added business_key header to request: ${request.url}")
                        } else {
                            println("Warning: No business selected for request: ${request.url}")
                        }
                    }
                }
                
                onResponse { response ->
                    // Handle 401 Unauthorized responses
                    if (response.status.value == 401) {
                        println("Received 401 Unauthorized - attempting token refresh")
                        // Get AuthInterceptor lazily to avoid circular dependency
                        val authInterceptor = get<AuthInterceptor>()
                        authInterceptor.handle401Response()
                    }
                }
            })
            
            install(DefaultRequest) {
                header("Content-Type", "application/json")
            }
        }
    }
    
    // Remote Data Source - now HttpClient exists
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    
    // Repository - now both data sources exist
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    
    // Auth Interceptor - handles automatic token refresh and 401 responses
    single<AuthInterceptor> { AuthInterceptor(get(), get()) }
    
    // Use Cases
    singleOf(::LoginUseCase)
    singleOf(::RegisterUseCase)
    singleOf(::LoginWithGoogleUseCase)
    singleOf(::LogoutUseCase)
    singleOf(::GetCurrentUserUseCase)
    singleOf(::IsLoggedInUseCase)
    singleOf(::ForgotPasswordUseCase)
    
    // ViewModel
    singleOf(::AuthViewModel)
}
