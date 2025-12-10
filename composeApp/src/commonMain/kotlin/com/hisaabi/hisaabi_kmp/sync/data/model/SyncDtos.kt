package com.hisaabi.hisaabi_kmp.sync.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
    @SerialName("typeId")
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
    @SerialName("typeId")
    val typeId: Int = 0,
    @SerialName("taxPercentage")
    val taxPercentage: Double = 0.0,
    @SerialName("discountPercentage")
    val discountPercentage: Double = 0.0,
    @SerialName("retailPrice")
    val retailPrice: Double = 0.0,
    val thumbnail: String? = null,
    @SerialName("purchasePrice")
    val purchasePrice: Double = 0.0,
    @SerialName("statusId")
    val statusId: Int = 0,
    @SerialName("digitalId")
    val digitalId: String? = null,
    @SerialName("baseUnitSlug")
    val baseUnitSlug: String? = null,
    @SerialName("defaultUnitSlug")
    val defaultUnitSlug: String? = null,
    @SerialName("minimumQuantityUnitSlug")
    val minimumQuantityUnitSlug: String? = null,
    @SerialName("openingQuantityUnitSlug")
    val openingQuantityUnitSlug: String? = null,
    @SerialName("categorySlug")
    val categorySlug: String? = null,
    @SerialName("wholeSalePrice")
    val wholeSalePrice: Double = 0.0,
    @SerialName("averagePurchasePrice")
    val averagePurchasePrice: Double = 0.0,
    @SerialName("openingQuantityPurchasePrice")
    val openingQuantityPurchasePrice: Double = 0.0,
    @SerialName("expiryAlert")
    val expiryAlert: String? = null,
    val manufacturer: String? = null,
    @SerialName("expiryDate")
    val expiryDate: Long? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("allQuantities")
    val allQuantities: List<ProductQuantitiesDto>? = null
)

@Serializable
data class PartyDto(
    val id: Int = 0,
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val balance: Double = 0.0,
    @SerialName("openingBalance")
    val openingBalance: Double = 0.0,
    val thumbnail: String? = null,
    @SerialName("roleId")
    val roleId: Int = 0,
    @SerialName("personStatus")
    val personStatus: Int = 1,
    @SerialName("digitalId")
    val digitalId: String? = null,
    @SerialName("latLong")
    val latLong: String? = null,
    @SerialName("areaSlug")
    val areaSlug: String? = null,
    @SerialName("categorySlug")
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
    @SerialName("openingAmount")
    val openingAmount: Double = 0.0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("statusId")
    val statusId: Int? = null
)

@Serializable
data class QuantityUnitDto(
    val id: Int = 0,
    val title: String? = null,
    val description: String? = null,
    val parentSlug: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("sortOrder")
    val sortOrder: Int = 0,
    @SerialName("conversionFactor")
    val conversionFactor: Double = 0.0,
    @SerialName("baseConversionUnitSlug")
    val baseConversionUnitSlug: String? = null,
    @SerialName("statusId")
    val statusId: Int = 0
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
    val updatedAt: String? = null,
    val address: String? = null,
    val latLong: String? = null,
    val thumbnail: String? = null,
    @SerialName("typeId")
    val typeId: Int = 0
)

@Serializable
data class TransactionDto(
    val id: Int = 0,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val customerSlug: String? = null,
    val totalBill: Double = 0.0,
    val totalPaid: Double = 0.0,
    val timestamp: Long? = null,
    val remindAtMilliseconds: Long? = null,
    val flatDiscount: Double = 0.0,
    val paymentMethodTo: String? = null,
    val paymentMethodFrom: String? = null,
    val transactionType: Int = 0,
    val priceTypeId: Int = 0,
    val additionalChargesType: String? = null,
    val additionalChargesDesc: String? = null,
    val additionalCharges: Double = 0.0,
    val discountTypeId: String? = null,
    val taxTypeId: Int = 0,
    val flatTax: Double = 0.0,
    val description: String? = null,
    val statusId: Int = 0,
    val stateId: Int? = null,
    val wareHouseSlugFrom: String? = null,
    val wareHouseSlugTo: String? = null,
    val shippingAddress: String? = null,
    val parentSlug: String? = null,
    val transactionDetails: List<TransactionDetailDto>? = null
)

@Serializable
data class TransactionDetailDto(
    val id: Int = 0,
    val transactionSlug: String? = null,
    val flatTax: Double = 0.0,
    val taxType: Int = 0,
    val flatDiscount: Double = 0.0,
    val discountType: Int = 0,
    val productSlug: String? = null,
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val profit: Double = 0.0,
    val quantityUnitSlug: String? = null,
    val businessSlug: String? = null,
    val updatedAt: String? = null,
    val description: String? = null,
    val recipeSlug: String? = null,
    val slug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ProductQuantitiesDto(
    val id: Int = 0,
    @SerialName("productSlug")
    val productSlug: String? = null,
    @SerialName("wareHouseSlug")
    val wareHouseSlug: String? = null,
    @SerialName("openingQuantity")
    val openingQuantity: Double = 0.0,
    @SerialName("currentQuantity")
    val currentQuantity: Double = 0.0,
    @SerialName("minimumQuantity")
    val minimumQuantity: Double = 0.0,
    @SerialName("maxQuantity")
    val maxQuantity: Double = 0.0,
    val businessSlug: String? = null,
    @SerialName("syncStatus")
    val syncStatus: Int = 0,
    val updatedAt: String? = null,
    val slug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null
)

@Serializable
data class EntityMediaDto(
    val id: Int = 0,
    @SerialName("entitySlug")
    val entitySlug: String? = null,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("mediaType")
    val mediaType: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class RecipeIngredientsDto(
    val id: Int = 0,
    @SerialName("recipeSlug")
    val recipeSlug: String? = null,
    @SerialName("ingredientSlug")
    val ingredientSlug: String? = null,
    val quantity: Double? = null,
    @SerialName("quantityUnitSlug")
    val quantityUnitSlug: String? = null,
    val slug: String? = null,
    val businessSlug: String? = null,
    val createdBy: String? = null,
    @SerialName("syncStatus")
    val syncStatus: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class DeletedRecordsDto(
    val id: Int = 0,
    @SerialName("record_slug")
    val recordSlug: String? = null,
    @SerialName("record_type")
    val recordType: String? = null,
    @SerialName("deletion_type")
    val deletionType: String? = null,
    @SerialName("business_slug")
    val businessSlug: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("slug")
    val slug: String? = null,
    val createdBy: String? = null
)

