package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.Serializable

/**
 * DTOs for syncing entities with cloud
 * These match the API structure from the backend
 */

@Serializable
data class CategoryDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val typeId: Int = 0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ProductDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val typeId: Int = 0,
    val tax_percentage: Double = 0.0,
    val discount_percentage: Double = 0.0,
    val retail_price: Double = 0.0,
    val wholesale_price: Double = 0.0,
    val thumbnail: String? = null,
    val purchase_price: Double = 0.0,
    val status_id: Int = 0,
    val digital_id: String? = null,
    val base_unit_slug: String? = null,
    val default_unit_slug: String? = null,
    val minimum_quantity_unit_slug: String? = null,
    val opening_quantity_unit_slug: String? = null,
    val category_slug: String? = null,
    val avg_purchase_price: Double = 0.0,
    val opening_quantity_purchase_price: Double = 0.0,
    val expiry_date: String? = null,
    val expiry_alert: String? = null,
    val manufacturer: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class PartyDto(
    val id: Int = 0,
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val balance: Double = 0.0,
    val openingBalance: Double = 0.0,
    val thumbnail: String? = null,
    val roleId: Int = 0,
    val personStatus: Int = 1,
    val digitalId: String? = null,
    val latLong: String? = null,
    val areaSlug: String? = null,
    val categorySlug: String? = null,
    val email: String? = null,
    val description: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class PaymentMethodDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val thumbnail: String? = null,
    val amount: Double = 0.0,
    val openingAmount: Double = 0.0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val statusId: Int? = null
)

@Serializable
data class QuantityUnitDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val parent_slug: String? = null,
    val conversion_rate: Double = 0.0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class WareHouseDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class TransactionDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val amount: Double = 0.0,
    val discount_percentage: Double = 0.0,
    val tax_percentage: Double = 0.0,
    val transaction_type_id: Int = 0,
    val party_slug: String? = null,
    val payment_method_slug: String? = null,
    val warehouse_slug: String? = null,
    val category_slug: String? = null,
    val transaction_date: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class TransactionDetailDto(
    val id: Int = 0,
    val transaction_slug: String? = null,
    val product_slug: String? = null,
    val description: String? = null,
    val recipe_slug: String? = null,
    val quantity: Double = 0.0,
    val quantity_unit_slug: String? = null,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val flat_discount: Double = 0.0,
    val discount_type: Int = 0,
    val flat_tax: Double = 0.0,
    val tax_type: Int = 0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ProductQuantitiesDto(
    val id: Int = 0,
    val product_slug: String? = null,
    val quantity_unit_slug: String? = null,
    val quantity: Double = 0.0,
    val warehouse_slug: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class EntityMediaDto(
    val id: Int = 0,
    val entity_slug: String? = null,
    val entity_type: String? = null,
    val media_url: String? = null,
    val media_type: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class RecipeIngredientsDto(
    val id: Int = 0,
    val product_slug: String? = null,
    val ingredient_product_slug: String? = null,
    val quantity: Double = 0.0,
    val unit_slug: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class DeletedRecordsDto(
    val id: Int = 0,
    val entity_slug: String? = null,
    val entity_type: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

