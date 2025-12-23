package com.hisaabi.hisaabi_kmp.sync.data.datasource

import com.hisaabi.hisaabi_kmp.sync.data.model.*
import com.hisaabi.hisaabi_kmp.sync.util.JsonSanitizer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Remote data source for sync operations
 * Handles all API calls for syncing data with cloud
 */
interface SyncRemoteDataSource {
    // Categories
    suspend fun syncCategoriesUp(categories: List<CategoryDto>): SyncResponse<CategoryDto>
    suspend fun syncCategoriesDown(lastSyncTime: String): SyncResponse<CategoryDto>
    
    // Products
    suspend fun syncProductsUp(products: List<ProductDto>): SyncResponse<ProductDto>
    suspend fun syncProductsDown(lastSyncTime: String): SyncResponse<ProductDto>
    suspend fun addProducts(products: List<ProductDto>): SyncResponse<ProductDto>
    
    // Parties (Persons)
    suspend fun syncPartiesUp(parties: List<PartyDto>): SyncResponse<PartyDto>
    suspend fun syncPartiesDown(lastSyncTime: String): SyncResponse<PartyDto>
    suspend fun addParties(parties: List<PartyDto>): SyncResponse<PartyDto>
    
    // Payment Methods
    suspend fun syncPaymentMethodsUp(paymentMethods: List<PaymentMethodDto>): SyncResponse<PaymentMethodDto>
    suspend fun syncPaymentMethodsDown(lastSyncTime: String): SyncResponse<PaymentMethodDto>
    suspend fun addPaymentMethods(paymentMethods: List<PaymentMethodDto>): SyncResponse<PaymentMethodDto>
    
    // Quantity Units
    suspend fun syncQuantityUnitsUp(quantityUnits: List<QuantityUnitDto>): SyncResponse<QuantityUnitDto>
    suspend fun syncQuantityUnitsDown(lastSyncTime: String): SyncResponse<QuantityUnitDto>
    
    // Warehouses
    suspend fun syncWarehousesUp(warehouses: List<WareHouseDto>): SyncResponse<WareHouseDto>
    suspend fun syncWarehousesDown(lastSyncTime: String): SyncResponse<WareHouseDto>
    
    // Transactions
    suspend fun syncTransactionsUp(transactions: List<TransactionDto>): SyncResponse<TransactionDto>
    suspend fun syncTransactionsDown(lastSyncTime: String): SyncResponse<TransactionDto>
    suspend fun addTransactions(transactions: List<TransactionDto>): SyncResponse<TransactionDto>
    
    // Transaction Details (Sync Down Only - Details included in transaction for Sync Up)
    suspend fun syncTransactionDetailsDown(lastSyncTime: String): SyncResponse<TransactionDetailDto>
    
    // Product Quantities (Sync Down Only - Backend calculates for Sync Up)
    suspend fun syncProductQuantitiesDown(lastSyncTime: String): SyncResponse<ProductQuantitiesDto>
    
    // Media
    suspend fun syncMediaUp(media: List<EntityMediaDto>): SyncResponse<EntityMediaDto>
    suspend fun syncMediaDown(lastSyncTime: String): SyncResponse<EntityMediaDto>
    
    // Recipe Ingredients
    suspend fun syncRecipeIngredientsUp(ingredients: List<RecipeIngredientsDto>): SyncResponse<RecipeIngredientsDto>
    suspend fun syncRecipeIngredientsDown(lastSyncTime: String): SyncResponse<RecipeIngredientsDto>
    suspend fun addRecipeIngredients(ingredients: List<RecipeIngredientsDto>): SyncResponse<RecipeIngredientsDto>
    
    // Deleted Records
    suspend fun syncDeletedRecordsDown(lastSyncTime: String): SyncResponse<DeletedRecordsDto>
    suspend fun deleteRecords(records: List<DeletedRecordsDto>): SyncResponse<Any>
}

class SyncRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : SyncRemoteDataSource {
    
    companion object {
      //  private const val BASE_URL = "http://52.20.167.4:5000" // Production.
        private const val BASE_URL = "http://10.68.53.161:3000" // Dev environment.
    }
    
    // Categories
    override suspend fun syncCategoriesUp(categories: List<CategoryDto>): SyncResponse<CategoryDto> {
        // Sanitize categories before sending to prevent JSON parsing errors
        val sanitizedCategories = JsonSanitizer.sanitizeCategories(categories)
        return httpClient.post("$BASE_URL/sync-categories") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedCategories))
        }.body()
    }
    
    override suspend fun syncCategoriesDown(lastSyncTime: String): SyncResponse<CategoryDto> {
        return httpClient.get("$BASE_URL/sync-categories") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Products
    override suspend fun syncProductsUp(products: List<ProductDto>): SyncResponse<ProductDto> {
        // Sanitize products before sending to prevent JSON parsing errors
        val sanitizedProducts = JsonSanitizer.sanitizeProducts(products)
        return httpClient.post("$BASE_URL/products") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedProducts))
        }.body()
    }
    
    override suspend fun syncProductsDown(lastSyncTime: String): SyncResponse<ProductDto> {
        return httpClient.get("$BASE_URL/sync-product") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun addProducts(products: List<ProductDto>): SyncResponse<ProductDto> {
        // Sanitize products before sending to prevent JSON parsing errors
        val sanitizedProducts = JsonSanitizer.sanitizeProducts(products)
        return httpClient.post("$BASE_URL/products") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedProducts))
        }.body()
    }
    
    // Parties
    override suspend fun syncPartiesUp(parties: List<PartyDto>): SyncResponse<PartyDto> {
        // Sanitize parties before sending to prevent JSON parsing errors
        val sanitizedParties = JsonSanitizer.sanitizeParties(parties)
        return httpClient.post("$BASE_URL/person") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedParties))
        }.body()
    }
    
    override suspend fun syncPartiesDown(lastSyncTime: String): SyncResponse<PartyDto> {
        return httpClient.get("$BASE_URL/sync-person") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun addParties(parties: List<PartyDto>): SyncResponse<PartyDto> {
        // Sanitize parties before sending to prevent JSON parsing errors
        val sanitizedParties = JsonSanitizer.sanitizeParties(parties)
        return httpClient.post("$BASE_URL/person") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedParties))
        }.body()
    }
    
    // Payment Methods
    override suspend fun syncPaymentMethodsUp(paymentMethods: List<PaymentMethodDto>): SyncResponse<PaymentMethodDto> {
        // Sanitize payment methods before sending to prevent JSON parsing errors
        val sanitizedPaymentMethods = JsonSanitizer.sanitizePaymentMethods(paymentMethods)
        return httpClient.post("$BASE_URL/payment-method") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedPaymentMethods))
        }.body()
    }
    
    override suspend fun syncPaymentMethodsDown(lastSyncTime: String): SyncResponse<PaymentMethodDto> {
        return httpClient.get("$BASE_URL/sync-payment-method") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun addPaymentMethods(paymentMethods: List<PaymentMethodDto>): SyncResponse<PaymentMethodDto> {
        // Sanitize payment methods before sending to prevent JSON parsing errors
        val sanitizedPaymentMethods = JsonSanitizer.sanitizePaymentMethods(paymentMethods)
        return httpClient.post("$BASE_URL/payment-method") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedPaymentMethods))
        }.body()
    }
    
    // Quantity Units
    override suspend fun syncQuantityUnitsUp(quantityUnits: List<QuantityUnitDto>): SyncResponse<QuantityUnitDto> {
        // Sanitize quantity units before sending to prevent JSON parsing errors
        val sanitizedQuantityUnits = JsonSanitizer.sanitizeQuantityUnits(quantityUnits)
        return httpClient.post("$BASE_URL/sync-quantity-unit") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedQuantityUnits))
        }.body()
    }
    
    override suspend fun syncQuantityUnitsDown(lastSyncTime: String): SyncResponse<QuantityUnitDto> {
        return httpClient.get("$BASE_URL/sync-quantity-unit") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Warehouses
    override suspend fun syncWarehousesUp(warehouses: List<WareHouseDto>): SyncResponse<WareHouseDto> {
        // Sanitize warehouses before sending to prevent JSON parsing errors
        val sanitizedWarehouses = JsonSanitizer.sanitizeWarehouses(warehouses)
        return httpClient.post("$BASE_URL/sync-warehouse") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedWarehouses))
        }.body()
    }
    
    override suspend fun syncWarehousesDown(lastSyncTime: String): SyncResponse<WareHouseDto> {
        return httpClient.get("$BASE_URL/sync-warehouse") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Transactions
    override suspend fun syncTransactionsUp(transactions: List<TransactionDto>): SyncResponse<TransactionDto> {
        // Sanitize transactions before sending to prevent JSON parsing errors
        val sanitizedTransactions = JsonSanitizer.sanitizeTransactions(transactions)
        return httpClient.post("$BASE_URL/transaction") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedTransactions))
        }.body()
    }
    
    override suspend fun syncTransactionsDown(lastSyncTime: String): SyncResponse<TransactionDto> {
        return httpClient.get("$BASE_URL/sync-transaction") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun addTransactions(transactions: List<TransactionDto>): SyncResponse<TransactionDto> {
        // Sanitize transactions before sending to prevent JSON parsing errors
        val sanitizedTransactions = JsonSanitizer.sanitizeTransactions(transactions)
        return httpClient.post("$BASE_URL/transaction") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedTransactions))
        }.body()
    }
    
    // Transaction Details (Sync Down Only)
    override suspend fun syncTransactionDetailsDown(lastSyncTime: String): SyncResponse<TransactionDetailDto> {
        return httpClient.get("$BASE_URL/sync-transaction-detail") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Product Quantities (Sync Down Only)
    override suspend fun syncProductQuantitiesDown(lastSyncTime: String): SyncResponse<ProductQuantitiesDto> {
        return httpClient.get("$BASE_URL/sync-product-quantities") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Media
    override suspend fun syncMediaUp(media: List<EntityMediaDto>): SyncResponse<EntityMediaDto> {
        // Sanitize media before sending to prevent JSON parsing errors
        val sanitizedMedia = JsonSanitizer.sanitizeEntityMediaList(media)
        return httpClient.post("$BASE_URL/sync-media") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedMedia))
        }.body()
    }
    
    override suspend fun syncMediaDown(lastSyncTime: String): SyncResponse<EntityMediaDto> {
        return httpClient.get("$BASE_URL/sync-media") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    // Recipe Ingredients
    override suspend fun syncRecipeIngredientsUp(ingredients: List<RecipeIngredientsDto>): SyncResponse<RecipeIngredientsDto> {
        // Sanitize recipe ingredients before sending to prevent JSON parsing errors
        val sanitizedIngredients = JsonSanitizer.sanitizeRecipeIngredients(ingredients)
        return httpClient.post("$BASE_URL/recipe-ingredients") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedIngredients))
        }.body()
    }
    
    override suspend fun syncRecipeIngredientsDown(lastSyncTime: String): SyncResponse<RecipeIngredientsDto> {
        return httpClient.get("$BASE_URL/sync-recipe-ingredients") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun addRecipeIngredients(ingredients: List<RecipeIngredientsDto>): SyncResponse<RecipeIngredientsDto> {
        // Sanitize recipe ingredients before sending to prevent JSON parsing errors
        val sanitizedIngredients = JsonSanitizer.sanitizeRecipeIngredients(ingredients)
        return httpClient.post("$BASE_URL/recipe-ingredients") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedIngredients))
        }.body()
    }
    
    // Deleted Records
    override suspend fun syncDeletedRecordsDown(lastSyncTime: String): SyncResponse<DeletedRecordsDto> {
        return httpClient.get("$BASE_URL/delete-records") {
            parameter("last-sync-time", lastSyncTime)
        }.body()
    }
    
    override suspend fun deleteRecords(records: List<DeletedRecordsDto>): SyncResponse<Any> {
        // Sanitize deleted records before sending to prevent JSON parsing errors
        val sanitizedRecords = JsonSanitizer.sanitizeDeletedRecords(records)
        return httpClient.post("$BASE_URL/delete-records") {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(sanitizedRecords))
        }.body()
    }
}

