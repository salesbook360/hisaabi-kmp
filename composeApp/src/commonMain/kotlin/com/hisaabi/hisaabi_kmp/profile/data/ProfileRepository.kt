package com.hisaabi.hisaabi_kmp.profile.data

import com.hisaabi.hisaabi_kmp.auth.data.datasource.AuthLocalDataSource
import com.hisaabi.hisaabi_kmp.profile.domain.model.UpdateProfileRequest
import com.hisaabi.hisaabi_kmp.profile.domain.model.UpdateProfileResponse
import com.hisaabi.hisaabi_kmp.profile.domain.model.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ProfileRepository(
    private val httpClient: HttpClient,
    private val authLocalDataSource: AuthLocalDataSource
) {
    private val baseUrl = "http://52.20.167.4:5000"
    
    suspend fun updateProfile(
        profile: UserProfile,
        authToken: String
    ): Result<UpdateProfileResponse> {
        return try {
            val request = UpdateProfileRequest(
                email = profile.email,
                firebaseId = profile.firebaseId,
                name = profile.name,
                phone = profile.phone,
                pic = profile.pic,
                slug = profile.slug
            )
            
            val response = httpClient.put("$baseUrl/register") {
                contentType(ContentType.Application.Json)
                header("Authorization", authToken)
                setBody(request)
            }
            
            val result: UpdateProfileResponse = response.body()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get current user profile from local persistence storage
    suspend fun getCurrentProfile(): Result<UserProfile> {
        return try {
            val userDto = authLocalDataSource.getUser()
            if (userDto != null) {
                val profile = UserProfile(
                    email = userDto.email,
                    firebaseId = userDto.firebaseId,
                    name = userDto.name,
                    phone = userDto.phone ?: "",
                    pic = userDto.pic ?: "",
                    slug = userDto.slug
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("No user data found in local storage"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

