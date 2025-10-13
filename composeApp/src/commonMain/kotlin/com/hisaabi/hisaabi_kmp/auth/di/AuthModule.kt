package com.hisaabi.hisaabi_kmp.auth.di

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSourceImpl
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSource
import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthRemoteDataSourceImpl
import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepository
import com.hisaabi.hisaabi_kmp.auth.data.repository.AuthRepositoryImpl
import com.hisaabi.hisaabi_kmp.auth.domain.usecase.*
import com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel.AuthViewModel
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authModule = module {
    
    // Data Sources - Create local datasource first to avoid circular dependency
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl() }
    
    // HTTP Client
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            install(Auth) {
                bearer {
                    loadTokens {
                        // Load tokens from local storage
                        val localDataSource = get<AuthLocalDataSource>()
                        val accessToken = localDataSource.getAccessToken()
                        if (accessToken != null) {
                            BearerTokens(accessToken, "")
                        } else {
                            null
                        }
                    }
                }
            }
            
            install(DefaultRequest) {
                header("Content-Type", "application/json")
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 30000
                socketTimeoutMillis = 30000
            }
        }
    }
    
    // Remote Data Source
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get()) }
    
    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    
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
