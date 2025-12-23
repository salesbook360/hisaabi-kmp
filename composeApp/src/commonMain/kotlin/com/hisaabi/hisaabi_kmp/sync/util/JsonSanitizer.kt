package com.hisaabi.hisaabi_kmp.sync.util

import com.hisaabi.hisaabi_kmp.sync.data.model.*

/**
 * Utility for sanitizing JSON data before sending to backend
 * Removes invalid characters and ensures proper JSON escaping
 */
object JsonSanitizer {
    
    /**
     * Sanitizes a string value to ensure it's valid JSON
     * Removes invalid characters and ensures proper JSON escaping
     * Returns null for empty strings so kotlinx.serialization will omit the key
     */
    fun sanitizeString(value: String?): String? {
        if (value == null) return null
        if(value.isEmpty()) return null
        
        // Handle special cases that cause JSON parsing errors
        // If the string is exactly "" (two quotes), convert to null
        // This prevents serialization to "\"\"" which causes MySQL JSON parsing errors
        if (value == "\"\"") return null
        
        // Handle empty strings - return null so serializer will ignore the key
        if (value.isEmpty() || value.isBlank()) return null
        
        // Remove control characters except newlines, tabs, and carriage returns
        // Control characters (0x00-0x1F) except \n (0x0A), \r (0x0D), \t (0x09)
        var sanitized = value.filter { char ->
            val code = char.code
            // Allow printable ASCII characters (32-126), newline (10), tab (9), carriage return (13)
            // Also allow extended ASCII and Unicode characters (128+)
            code >= 32 || code == 9 || code == 10 || code == 13
        }
        
        // Remove any remaining problematic characters that could break JSON
        // This includes unpaired surrogates and other invalid UTF-8 sequences
        sanitized = sanitized.replace(Regex("[\u0000-\u0008\u000B\u000C\u000E-\u001F]"), "")
            .replace("\uFFFD", "") // Remove replacement characters
        
        // Remove any unpaired surrogate characters that could break JSON encoding
        sanitized = sanitized.filter { char ->
            !char.isSurrogate()
        }
        
        // Trim whitespace but preserve the string if it's not empty
        var trimmed = sanitized.trim()
        
        // Final check: if the trimmed result is just quotes (single or double), return null
        // This handles cases like: "", ", \"\", etc.
        if (trimmed == "\"\"" || trimmed == "\"" || trimmed == "''" || trimmed == "'") {
            return null
        }
        
        // Check for strings that are only whitespace and quotes
        val quoteStripped = trimmed.trim { it == '"' || it == '\'' || it.isWhitespace() }
        if (quoteStripped.isEmpty() && (trimmed.contains('"') || trimmed.contains('\''))) {
            return null
        }
        
        // Return null for empty strings, otherwise return the trimmed value
        return if (trimmed.isEmpty()) null else trimmed
    }
    
    /**
     * Sanitizes a TransactionDetailDto by cleaning all string fields
     */
    fun sanitizeTransactionDetail(detail: TransactionDetailDto): TransactionDetailDto {
        return detail.copy(
            transactionSlug = sanitizeString(detail.transactionSlug),
            productSlug = sanitizeString(detail.productSlug),
            quantityUnitSlug = sanitizeString(detail.quantityUnitSlug),
            businessSlug = sanitizeString(detail.businessSlug),
            updatedAt = sanitizeString(detail.updatedAt),
            description = sanitizeString(detail.description),
            recipeSlug = sanitizeString(detail.recipeSlug),
            slug = sanitizeString(detail.slug),
            createdBy = sanitizeString(detail.createdBy),
            createdAt = sanitizeString(detail.createdAt)
        )
    }
    
    /**
     * Sanitizes a TransactionDto by cleaning all string fields and nested transaction details
     */
    fun sanitizeTransaction(transaction: TransactionDto): TransactionDto {
        return transaction.copy(
            slug = sanitizeString(transaction.slug),
            businessSlug = sanitizeString(transaction.businessSlug),
            createdBy = sanitizeString(transaction.createdBy),
            createdAt = sanitizeString(transaction.createdAt),
            updatedAt = sanitizeString(transaction.updatedAt),
            customerSlug = sanitizeString(transaction.customerSlug),
            timestamp = sanitizeString(transaction.timestamp),
            paymentMethodTo = sanitizeString(transaction.paymentMethodTo),
            paymentMethodFrom = sanitizeString(transaction.paymentMethodFrom),
            additionalChargesType = sanitizeString(transaction.additionalChargesType),
            additionalChargesDesc = sanitizeString(transaction.additionalChargesDesc),
            discountTypeId = sanitizeString(transaction.discountTypeId),
            description = sanitizeString(transaction.description),
            wareHouseSlugFrom = sanitizeString(transaction.wareHouseSlugFrom),
            wareHouseSlugTo = sanitizeString(transaction.wareHouseSlugTo),
            shippingAddress = sanitizeString(transaction.shippingAddress),
            parentSlug = sanitizeString(transaction.parentSlug),
            transactionDetails = transaction.transactionDetails?.map { sanitizeTransactionDetail(it) }
        )
    }
    
    /**
     * Sanitizes a list of TransactionDto objects
     */
    fun sanitizeTransactions(transactions: List<TransactionDto>): List<TransactionDto> {
        return transactions.map { sanitizeTransaction(it) }
    }
    
    /**
     * Sanitizes a CategoryDto by cleaning all string fields
     */
    fun sanitizeCategory(category: CategoryDto): CategoryDto {
        return category.copy(
            title = sanitizeString(category.title),
            description = sanitizeString(category.description),
            thumbnail = sanitizeString(category.thumbnail),
            slug = sanitizeString(category.slug),
            businessSlug = sanitizeString(category.businessSlug),
            createdBy = sanitizeString(category.createdBy),
            createdAt = sanitizeString(category.createdAt),
            updatedAt = sanitizeString(category.updatedAt)
        )
    }
    
    /**
     * Sanitizes a list of CategoryDto objects
     */
    fun sanitizeCategories(categories: List<CategoryDto>): List<CategoryDto> {
        return categories.map { sanitizeCategory(it) }
    }
    
    /**
     * Sanitizes a ProductQuantitiesDto by cleaning all string fields
     */
    fun sanitizeProductQuantities(quantities: ProductQuantitiesDto): ProductQuantitiesDto {
        return quantities.copy(
            productSlug = sanitizeString(quantities.productSlug),
            wareHouseSlug = sanitizeString(quantities.wareHouseSlug),
            businessSlug = sanitizeString(quantities.businessSlug),
            updatedAt = sanitizeString(quantities.updatedAt),
            slug = sanitizeString(quantities.slug),
            createdBy = sanitizeString(quantities.createdBy),
            createdAt = sanitizeString(quantities.createdAt)
        )
    }
    
    /**
     * Sanitizes a ProductDto by cleaning all string fields and nested quantities
     */
    fun sanitizeProduct(product: ProductDto): ProductDto {
        return product.copy(
            title = sanitizeString(product.title),
            description = sanitizeString(product.description),
            thumbnail = sanitizeString(product.thumbnail),
            digitalId = sanitizeString(product.digitalId),
            baseUnitSlug = sanitizeString(product.baseUnitSlug),
            defaultUnitSlug = sanitizeString(product.defaultUnitSlug),
            minimumQuantityUnitSlug = sanitizeString(product.minimumQuantityUnitSlug),
            openingQuantityUnitSlug = sanitizeString(product.openingQuantityUnitSlug),
            categorySlug = sanitizeString(product.categorySlug),
            expiryAlert = sanitizeString(product.expiryAlert),
            manufacturer = sanitizeString(product.manufacturer),
            slug = sanitizeString(product.slug),
            businessSlug = sanitizeString(product.businessSlug),
            createdBy = sanitizeString(product.createdBy),
            createdAt = sanitizeString(product.createdAt),
            updatedAt = sanitizeString(product.updatedAt),
            allQuantities = product.allQuantities?.map { sanitizeProductQuantities(it) }
        )
    }
    
    /**
     * Sanitizes a list of ProductDto objects
     */
    fun sanitizeProducts(products: List<ProductDto>): List<ProductDto> {
        return products.map { sanitizeProduct(it) }
    }
    
    /**
     * Sanitizes a PartyDto by cleaning all string fields
     */
    fun sanitizeParty(party: PartyDto): PartyDto {
        return party.copy(
            name = sanitizeString(party.name),
            phone = sanitizeString(party.phone),
            address = sanitizeString(party.address),
            thumbnail = sanitizeString(party.thumbnail),
            digitalId = sanitizeString(party.digitalId),
            latLong = sanitizeString(party.latLong),
            areaSlug = sanitizeString(party.areaSlug),
            categorySlug = sanitizeString(party.categorySlug),
            email = sanitizeString(party.email),
            description = sanitizeString(party.description),
            slug = sanitizeString(party.slug),
            businessSlug = sanitizeString(party.businessSlug),
            createdBy = sanitizeString(party.createdBy),
            createdAt = sanitizeString(party.createdAt),
            updatedAt = sanitizeString(party.updatedAt)
        )
    }
    
    /**
     * Sanitizes a list of PartyDto objects
     */
    fun sanitizeParties(parties: List<PartyDto>): List<PartyDto> {
        return parties.map { sanitizeParty(it) }
    }
    
    /**
     * Sanitizes a PaymentMethodDto by cleaning all string fields
     */
    fun sanitizePaymentMethod(paymentMethod: PaymentMethodDto): PaymentMethodDto {
        return paymentMethod.copy(
            title = sanitizeString(paymentMethod.title),
            description = sanitizeString(paymentMethod.description),
            thumbnail = sanitizeString(paymentMethod.thumbnail),
            slug = sanitizeString(paymentMethod.slug),
            businessSlug = sanitizeString(paymentMethod.businessSlug),
            createdBy = sanitizeString(paymentMethod.createdBy),
            createdAt = sanitizeString(paymentMethod.createdAt),
            updatedAt = sanitizeString(paymentMethod.updatedAt)
        )
    }
    
    /**
     * Sanitizes a list of PaymentMethodDto objects
     */
    fun sanitizePaymentMethods(paymentMethods: List<PaymentMethodDto>): List<PaymentMethodDto> {
        return paymentMethods.map { sanitizePaymentMethod(it) }
    }
    
    /**
     * Sanitizes a QuantityUnitDto by cleaning all string fields
     */
    fun sanitizeQuantityUnit(quantityUnit: QuantityUnitDto): QuantityUnitDto {
        return quantityUnit.copy(
            title = sanitizeString(quantityUnit.title),
            description = sanitizeString(quantityUnit.description),
            parentSlug = sanitizeString(quantityUnit.parentSlug),
            slug = sanitizeString(quantityUnit.slug),
            businessSlug = sanitizeString(quantityUnit.businessSlug),
            createdBy = sanitizeString(quantityUnit.createdBy),
            createdAt = sanitizeString(quantityUnit.createdAt),
            updatedAt = sanitizeString(quantityUnit.updatedAt),
            baseConversionUnitSlug = sanitizeString(quantityUnit.baseConversionUnitSlug)
        )
    }
    
    /**
     * Sanitizes a list of QuantityUnitDto objects
     */
    fun sanitizeQuantityUnits(quantityUnits: List<QuantityUnitDto>): List<QuantityUnitDto> {
        return quantityUnits.map { sanitizeQuantityUnit(it) }
    }
    
    /**
     * Sanitizes a WareHouseDto by cleaning all string fields
     */
    fun sanitizeWarehouse(warehouse: WareHouseDto): WareHouseDto {
        return warehouse.copy(
            title = sanitizeString(warehouse.title),
            description = sanitizeString(warehouse.description),
            location = sanitizeString(warehouse.location),
            slug = sanitizeString(warehouse.slug),
            businessSlug = sanitizeString(warehouse.businessSlug),
            createdBy = sanitizeString(warehouse.createdBy),
            createdAt = sanitizeString(warehouse.createdAt),
            updatedAt = sanitizeString(warehouse.updatedAt),
            address = sanitizeString(warehouse.address),
            latLong = sanitizeString(warehouse.latLong),
            thumbnail = sanitizeString(warehouse.thumbnail)
        )
    }
    
    /**
     * Sanitizes a list of WareHouseDto objects
     */
    fun sanitizeWarehouses(warehouses: List<WareHouseDto>): List<WareHouseDto> {
        return warehouses.map { sanitizeWarehouse(it) }
    }
    
    /**
     * Sanitizes an EntityMediaDto by cleaning all string fields
     */
    fun sanitizeEntityMedia(media: EntityMediaDto): EntityMediaDto {
        return media.copy(
            entitySlug = sanitizeString(media.entitySlug),
            entityType = sanitizeString(media.entityType),
            url = sanitizeString(media.url),
            mediaType = sanitizeString(media.mediaType),
            slug = sanitizeString(media.slug),
            businessSlug = sanitizeString(media.businessSlug),
            createdBy = sanitizeString(media.createdBy),
            createdAt = sanitizeString(media.createdAt),
            updatedAt = sanitizeString(media.updatedAt)
        )
    }
    
    /**
     * Sanitizes a list of EntityMediaDto objects
     */
    fun sanitizeEntityMediaList(mediaList: List<EntityMediaDto>): List<EntityMediaDto> {
        return mediaList.map { sanitizeEntityMedia(it) }
    }
    
    /**
     * Sanitizes a RecipeIngredientsDto by cleaning all string fields
     */
    fun sanitizeRecipeIngredient(ingredient: RecipeIngredientsDto): RecipeIngredientsDto {
        return ingredient.copy(
            recipeSlug = sanitizeString(ingredient.recipeSlug),
            ingredientSlug = sanitizeString(ingredient.ingredientSlug),
            quantityUnitSlug = sanitizeString(ingredient.quantityUnitSlug),
            slug = sanitizeString(ingredient.slug),
            businessSlug = sanitizeString(ingredient.businessSlug),
            createdBy = sanitizeString(ingredient.createdBy),
            createdAt = sanitizeString(ingredient.createdAt),
            updatedAt = sanitizeString(ingredient.updatedAt)
        )
    }
    
    /**
     * Sanitizes a list of RecipeIngredientsDto objects
     */
    fun sanitizeRecipeIngredients(ingredients: List<RecipeIngredientsDto>): List<RecipeIngredientsDto> {
        return ingredients.map { sanitizeRecipeIngredient(it) }
    }
    
    /**
     * Sanitizes a DeletedRecordsDto by cleaning all string fields
     */
    fun sanitizeDeletedRecord(record: DeletedRecordsDto): DeletedRecordsDto {
        return record.copy(
            recordSlug = sanitizeString(record.recordSlug),
            recordType = sanitizeString(record.recordType),
            deletionType = sanitizeString(record.deletionType),
            businessSlug = sanitizeString(record.businessSlug),
            createdAt = sanitizeString(record.createdAt),
            updatedAt = sanitizeString(record.updatedAt),
            slug = sanitizeString(record.slug),
            createdBy = sanitizeString(record.createdBy)
        )
    }
    
    /**
     * Sanitizes a list of DeletedRecordsDto objects
     */
    fun sanitizeDeletedRecords(records: List<DeletedRecordsDto>): List<DeletedRecordsDto> {
        return records.map { sanitizeDeletedRecord(it) }
    }
}

