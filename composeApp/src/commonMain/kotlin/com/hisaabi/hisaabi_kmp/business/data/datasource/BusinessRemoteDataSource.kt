package com.hisaabi.hisaabi_kmp.business.data.datasource

import com.hisaabi.hisaabi_kmp.business.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

interface BusinessRemoteDataSource {
    suspend fun getAllBusinesses(): BusinessResponse
    suspend fun createBusiness(request: BusinessRequest): BusinessResponse
    suspend fun updateBusiness(request: BusinessRequest): BusinessResponse
    suspend fun deleteBusiness(slug: String): DeleteBusinessResponse
}

class BusinessRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : BusinessRemoteDataSource {
    
    companion object {
        private const val BASE_URL = "http://52.20.167.4:5000"
        private const val BUSINESS_ENDPOINT = "$BASE_URL/business"
        private const val DELETE_BUSINESS_ENDPOINT = "$BASE_URL/delete_business"
    }
    
    override suspend fun getAllBusinesses(): BusinessResponse {
        println("=== GET ALL BUSINESSES API CALL ===")
        println("Endpoint: $BUSINESS_ENDPOINT")
        
        return try {
            val response = httpClient.get(BUSINESS_ENDPOINT) {
                contentType(ContentType.Application.Json)
            }
            println("Get All Businesses Response Status: ${response.status}")
            val businessResponse = response.body<BusinessResponse>()
            println("Response Body: $businessResponse")
            businessResponse
        } catch (e: Exception) {
            println("Get All Businesses API Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun createBusiness(request: BusinessRequest): BusinessResponse {
        println("=== CREATE BUSINESS API CALL ===")
        println("Endpoint: $BUSINESS_ENDPOINT")
        println("Request Body: $request")
        
        return try {
            val response = httpClient.post(BUSINESS_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            println("Create Business Response Status: ${response.status}")
            val businessResponse = response.body<BusinessResponse>()
            println("Response Body: $businessResponse")
            businessResponse
        } catch (e: Exception) {
            println("Create Business API Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun updateBusiness(request: BusinessRequest): BusinessResponse {
        println("=== UPDATE BUSINESS API CALL ===")
        println("Endpoint: $BUSINESS_ENDPOINT")
        println("Request Body: $request")
        
        return try {
            val response = httpClient.put(BUSINESS_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            println("Update Business Response Status: ${response.status}")
            val businessResponse = response.body<BusinessResponse>()
            println("Response Body: $businessResponse")
            businessResponse
        } catch (e: Exception) {
            println("Update Business API Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun deleteBusiness(slug: String): DeleteBusinessResponse {
        println("=== DELETE BUSINESS API CALL ===")
        println("Endpoint: $DELETE_BUSINESS_ENDPOINT")
        println("Business Slug: $slug")
        
        return try {
            val response = httpClient.post(DELETE_BUSINESS_ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("business_key", slug)
                setBody("\"$slug\"")
            }
            println("Delete Business Response Status: ${response.status}")
            val deleteResponse = response.body<DeleteBusinessResponse>()
            println("Response Body: $deleteResponse")
            deleteResponse
        } catch (e: Exception) {
            println("Delete Business API Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

