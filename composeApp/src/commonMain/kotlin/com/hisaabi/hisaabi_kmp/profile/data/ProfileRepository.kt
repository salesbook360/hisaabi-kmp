package com.hisaabi.hisaabi_kmp.profile.data

import com.hisaabi.hisaabi_kmp.profile.domain.model.UpdateProfileRequest
import com.hisaabi.hisaabi_kmp.profile.domain.model.UpdateProfileResponse
import com.hisaabi.hisaabi_kmp.profile.domain.model.UserProfile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ProfileRepository(
    private val httpClient: HttpClient
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
    
    // Mock method to get current user profile
    // TODO: Replace with actual API call to fetch user profile
    suspend fun getCurrentProfile(): Result<UserProfile> {
        return try {
            // For now, return a mock profile
            // In production, this should fetch from API or local storage
            val mockProfile = UserProfile(
                email = "user@example.com",
                firebaseId = "mock_firebase_id",
                name = "John Doe",
                phone = "923001234567",
                pic = "",
                slug = "JD"
            )
            Result.success(mockProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

