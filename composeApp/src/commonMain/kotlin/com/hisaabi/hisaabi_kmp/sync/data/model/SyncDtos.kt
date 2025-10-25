package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.Serializable

/**
 * DTOs for syncing entities with cloud
 * These match the API structure from the backend
 */

@Serializable
data class CategoryDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val typeId: Int = 0,
    val slug: String?,
    val businessSlug: String?,
    val createdBy: String?,
    val createdAt: String?,
    val updatedAt: String?
)

@Serializable
data class ProductDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val typeId: Int = 0,
    val tax_percentage: Double = 0.0,
    val discount_percentage: Double = 0.0,
    val retail_price: Double = 0.0,
    val wholesale_price: Double = 0.0,
    val thumbnail: String?,
    val purchase_price: Double = 0.0,
    val status_id: Int = 0,
    val digital_id: String?,
    val base_unit_slug: String?,
    val default_unit_slug: String?,
    val minimum_quantity_unit_slug: String?,
    val opening_quantity_unit_slug: String?,
    val category_slug: String?,
    val avg_purchase_price: Double = 0.0,
    val opening_quantity_purchase_price: Double = 0.0,
    val expiry_date: String?,
    val expiry_alert: String?,
    val manufacturer: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class PartyDto(
    val id: Int = 0,
    val name: String?,
    val phone: String?,
    val address: String?,
    val balance: Double = 0.0,
    val opening_balance: Double = 0.0,
    val thumbnail: String?,
    val role_id: Int = 0,
    val person_status: Int = 1,
    val digital_id: String?,
    val lat_long: String?,
    val area_slug: String?,
    val category_slug: String?,
    val email: String?,
    val description: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class PaymentMethodDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val thumbnail: String?,
    val balance: Double = 0.0,
    val opening_balance: Double = 0.0,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class QuantityUnitDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val parent_slug: String?,
    val conversion_rate: Double = 0.0,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class WareHouseDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val location: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class TransactionDto(
    val id: Int = 0,
    val title: String?,
    val description: String?,
    val amount: Double = 0.0,
    val discount_percentage: Double = 0.0,
    val tax_percentage: Double = 0.0,
    val transaction_type_id: Int = 0,
    val party_slug: String?,
    val payment_method_slug: String?,
    val warehouse_slug: String?,
    val category_slug: String?,
    val transaction_date: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class TransactionDetailDto(
    val id: Int = 0,
    val transaction_slug: String?,
    val product_slug: String?,
    val description: String?,
    val recipe_slug: String?,
    val quantity: Double = 0.0,
    val quantity_unit_slug: String?,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val flat_discount: Double = 0.0,
    val discount_type: Int = 0,
    val flat_tax: Double = 0.0,
    val tax_type: Int = 0,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class ProductQuantitiesDto(
    val id: Int = 0,
    val product_slug: String?,
    val quantity_unit_slug: String?,
    val quantity: Double = 0.0,
    val warehouse_slug: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class EntityMediaDto(
    val id: Int = 0,
    val entity_slug: String?,
    val entity_type: String?,
    val media_url: String?,
    val media_type: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class RecipeIngredientsDto(
    val id: Int = 0,
    val product_slug: String?,
    val ingredient_product_slug: String?,
    val quantity: Double = 0.0,
    val unit_slug: String?,
    val slug: String?,
    val business_slug: String?,
    val created_by: String?,
    val sync_status: Int = 0,
    val created_at: String?,
    val updated_at: String?
)

@Serializable
data class DeletedRecordsDto(
    val id: Int = 0,
    val entity_slug: String?,
    val entity_type: String?,
    val slug: String?,
    val business_slug: String?,
    val deleted_by: String?,
    val sync_status: Int = 0,
    val deleted_at: String?,
    val updated_at: String?
)

