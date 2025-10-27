package com.hisaabi.hisaabi_kmp.business.data.model

import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.database.entity.BusinessEntity
import kotlinx.serialization.Serializable

/**
 * Business DTO matching the API response/request structure
 */
@Serializable
data class BusinessDto(
    val id: Int = 0,
    val title: String,
    val email: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val logo: String? = null,
    val slug: String? = null
)

/**
 * Request body for creating/updating a business
 */
@Serializable
data class BusinessRequest(
    val id: Int = 0,
    val title: String,
    val email: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val logo: String? = null,
    val slug: String? = null
)

/**
 * Response wrapper for business API calls
 */
@Serializable
data class BusinessResponse(
    val data: BusinessData? = null,
    val message: String? = null,
    val status: Int? = null,
    val timestamp: String? = null,
    // Error response fields
    val statusCode: String? = null
)

@Serializable
data class BusinessData(
    val list: List<BusinessDto> = emptyList(),
    val totalRecords: Int = 0
)

/**
 * Response for delete operations
 */
@Serializable
data class DeleteBusinessResponse(
    val message: String? = null,
    val status: Int? = null,
    val statusCode: String? = null
)

// Extension functions for mapping between domain and DTO
fun BusinessDto.toDomainModel(): Business {
    return Business(
        id = id,
        title = title,
        email = email,
        address = address,
        phone = phone,
        logo = logo,
        slug = slug
    )
}

fun Business.toDto(): BusinessDto {
    return BusinessDto(
        id = id,
        title = title,
        email = email,
        address = address,
        phone = phone,
        logo = logo,
        slug = slug
    )
}

fun Business.toRequest(): BusinessRequest {
    return BusinessRequest(
        id = id,
        title = title,
        email = email,
        address = address,
        phone = phone,
        logo = logo,
        slug = slug
    )
}

// Extension function for mapping DTO to Entity
fun BusinessDto.toEntity(): BusinessEntity {
    return BusinessEntity(
        id = id,
        title = title,
        email = email,
        address = address,
        phone = phone,
        logo = logo,
        slug = slug
    )
}

// Extension function for mapping Entity to Domain Model
fun BusinessEntity.toDomainModel(): Business {
    return Business(
        id = id,
        title = title ?: "",
        email = email,
        address = address,
        phone = phone,
        logo = logo,
        slug = slug
    )
}

