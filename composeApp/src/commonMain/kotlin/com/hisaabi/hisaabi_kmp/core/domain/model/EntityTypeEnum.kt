package com.hisaabi.hisaabi_kmp.core.domain.model

/**
 * Enum representing all entity types in the application.
 * Each entity has a human-readable name, a two-letter slug code, and the local database table name.
 */
enum class EntityTypeEnum(
    val entityName: String,
    val entitySlug: String,
    val localDbTableName: String
) {
    ENTITY_TYPE_TRANSACTION("transaction", "TR", "InventoryTransaction"),
    ENTITY_TYPE_PRODUCT("product", "PR", "Product"),
    ENTITY_TYPE_PARTY("party", "PA", "Party"),
    ENTITY_TYPE_CATEGORY("category", "CA", "Category"),
    ENTITY_TYPE_PAYMENT_METHOD("payment_method", "PM", "PaymentMethod"),
    ENTITY_TYPE_QUANTITY_UNIT("quantity_unit", "QU", "QuantityUnit"),
    ENTITY_TYPE_WAREHOUSE("warehouse", "WH", "WareHouse"),
    ENTITY_TYPE_TRANSACTION_DETAIL("transaction_detail", "TD", "TransactionDetail"),
    ENTITY_TYPE_PRODUCT_QUANTITIES("product_quantities", "PQ", "ProductQuantities"),
    ENTITY_TYPE_ENTITY_MEDIA("entity_media", "EM", "EntityMedia"),
    ENTITY_TYPE_DELETED_RECORDS("deleted_record", "DR", "DeletedRecords"),
    ENTITY_TYPE_RECIPE_INGREDIENTS("recipe_ingredients", "RI", "RecipeIngredients"),
    ENTITY_TYPE_ALL_RECORDS("all_records", "AR", "AllRecords") // Holds all deleted records
}

