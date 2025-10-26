package com.hisaabi.hisaabi_kmp.sync.data.mapper

import com.hisaabi.hisaabi_kmp.database.entity.*
import com.hisaabi.hisaabi_kmp.sync.data.model.*
import com.hisaabi.hisaabi_kmp.sync.domain.model.SyncStatus

/**
 * Mapper functions to convert between database entities and sync DTOs
 */

// Category Mappers
fun CategoryEntity.toDto() = CategoryDto(
    id = id,
    title = title,
    description = description,
    thumbnail = thumbnail,
    typeId = type_id,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun CategoryDto.toEntity() = CategoryEntity(
    id = id,
    title = title,
    description = description,
    thumbnail = thumbnail,
    type_id = typeId,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt,

)

// Product Mappers
fun ProductEntity.toDto() = ProductDto(
    id = id,
    title = title,
    description = description,
    typeId = type_id,
    tax_percentage = tax_percentage,
    discount_percentage = discount_percentage,
    retail_price = retail_price,
    wholesale_price = wholesale_price,
    thumbnail = thumbnail,
    purchase_price = purchase_price,
    status_id = status_id,
    digital_id = digital_id,
    base_unit_slug = base_unit_slug,
    default_unit_slug = default_unit_slug,
    minimum_quantity_unit_slug = minimum_quantity_unit_slug,
    opening_quantity_unit_slug = opening_quantity_unit_slug,
    category_slug = category_slug,
    avg_purchase_price = avg_purchase_price,
    opening_quantity_purchase_price = opening_quantity_purchase_price,
    expiry_date = expiry_date,
    expiry_alert = expiry_alert,
    manufacturer = manufacturer,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun ProductDto.toEntity() = ProductEntity(
    id = id,
    title = title,
    description = description,
    type_id = typeId,
    tax_percentage = tax_percentage,
    discount_percentage = discount_percentage,
    retail_price = retail_price,
    wholesale_price = wholesale_price,
    thumbnail = thumbnail,
    purchase_price = purchase_price,
    status_id = status_id,
    digital_id = digital_id,
    base_unit_slug = base_unit_slug,
    default_unit_slug = default_unit_slug,
    minimum_quantity_unit_slug = minimum_quantity_unit_slug,
    opening_quantity_unit_slug = opening_quantity_unit_slug,
    category_slug = category_slug,
    avg_purchase_price = avg_purchase_price,
    opening_quantity_purchase_price = opening_quantity_purchase_price,
    expiry_date = expiry_date,
    expiry_alert = expiry_alert,
    manufacturer = manufacturer,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Party Mappers
fun PartyEntity.toDto() = PartyDto(
    id = id,
    name = name,
    phone = phone,
    address = address,
    balance = balance,
    openingBalance = opening_balance,
    thumbnail = thumbnail,
    roleId = role_id,
    personStatus = person_status,
    digitalId = digital_id,
    latLong = lat_long,
    areaSlug = area_slug,
    categorySlug = category_slug,
    email = email,
    description = description,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun PartyDto.toEntity() = PartyEntity(
    id = id,
    name = name,
    phone = phone,
    address = address,
    balance = balance,
    opening_balance = openingBalance,
    thumbnail = thumbnail,
    role_id = roleId,
    person_status = personStatus,
    digital_id = digitalId,
    lat_long = latLong,
    area_slug = areaSlug,
    category_slug = categorySlug,
    email = email,
    description = description,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Payment Method Mappers
fun PaymentMethodEntity.toDto() = PaymentMethodDto(
    id = id,
    title = title,
    description = description,
    thumbnail = null, // Not in entity
    amount = amount,
    openingAmount = opening_amount,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun PaymentMethodDto.toEntity() = PaymentMethodEntity(
    id = id,
    title = title ?: "",
    description = description ?: "",
    amount = amount,
    opening_amount = openingAmount,
    status_id = 0,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Quantity Unit Mappers
fun QuantityUnitEntity.toDto() = QuantityUnitDto(
    id = id,
    title = title,
    description = null, // Not in entity
    parent_slug = parent_slug,
    conversion_rate = conversion_factor,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun QuantityUnitDto.toEntity() = QuantityUnitEntity(
    id = id,
    title = title ?: "",
    sort_order = 0,
    parent_slug = parent_slug,
    conversion_factor = conversion_rate,
    base_conversion_unit_slug = null,
    status_id = 0,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Warehouse Mappers
fun WareHouseEntity.toDto() = WareHouseDto(
    id = id,
    title = title,
    description = description,
    location = address, // Entity uses 'address' field
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun WareHouseDto.toEntity() = WareHouseEntity(
    id = id,
    title = title,
    address = location,
    description = description,
    lat_long = null,
    thumbnail = null,
    type_id = 0,
    status_id = 0,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Transaction Mappers
fun InventoryTransactionEntity.toDto() = TransactionDto(
    id = id,
    title = null, // Not in entity
    description = description,
    amount = total_bill,
    discount_percentage = 0.0, // Not directly in entity
    tax_percentage = 0.0, // Not directly in entity
    transaction_type_id = transaction_type,
    party_slug = customer_slug,
    payment_method_slug = payment_method_to_slug,
    warehouse_slug = ware_house_slug_to,
    category_slug = null, // Not in entity
    transaction_date = timestamp,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun TransactionDto.toEntity() = InventoryTransactionEntity(
    id = id,
    customer_slug = party_slug,
    parent_slug = null,
    total_bill = amount,
    total_paid = 0.0,
    timestamp = transaction_date,
    discount = 0.0,
    payment_method_to_slug = payment_method_slug,
    payment_method_from_slug = null,
    transaction_type = transaction_type_id,
    price_type_id = 0,
    additional_charges_slug = null,
    additional_charges_desc = null,
    additional_charges = 0.0,
    discount_type_id = 0,
    tax_type_id = 0,
    tax = 0.0,
    description = description,
    shipping_address = null,
    status_id = 0, // Active
    state_id = 0,
    remind_at_milliseconds = 0,
    ware_house_slug_from = null,
    ware_house_slug_to = warehouse_slug,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Transaction Detail Mappers
fun TransactionDetailEntity.toDto() = TransactionDetailDto(
    id = id,
    transaction_slug = transaction_slug,
    product_slug = product_slug,
    description = description,
    recipe_slug = recipe_slug,
    quantity = quantity,
    quantity_unit_slug = quantity_unit_slug,
    price = price,
    profit = profit,
    flat_discount = flat_discount,
    discount_type = discount_type,
    flat_tax = flat_tax,
    tax_type = tax_type,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun TransactionDetailDto.toEntity() = TransactionDetailEntity(
    id = id,
    transaction_slug = transaction_slug,
    product_slug = product_slug,
    description = description,
    recipe_slug = recipe_slug ?: "0",
    quantity = quantity,
    quantity_unit_slug = quantity_unit_slug,
    price = price,
    profit = profit,
    flat_discount = flat_discount,
    discount_type = discount_type,
    flat_tax = flat_tax,
    tax_type = tax_type,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Product Quantities Mappers
fun ProductQuantitiesEntity.toDto() = ProductQuantitiesDto(
    id = id,
    product_slug = product_slug,
    quantity_unit_slug = null, // Not in entity
    quantity = current_quantity,
    warehouse_slug = warehouse_slug,
    slug = null, // Not in entity
    businessSlug = business_slug,
    createdBy = null, // Not in entity
    createdAt = null, // Not in entity
    updatedAt = null // Not in entity
)

fun ProductQuantitiesDto.toEntity() = ProductQuantitiesEntity(
    id = id,
    product_slug = product_slug,
    warehouse_slug = warehouse_slug,
    opening_quantity = 0.0,
    current_quantity = quantity,
    minimum_quantity = 0.0,
    maximum_quantity = 0.0,
    business_slug = businessSlug,
    sync_status = SyncStatus.SYNCED.value
)

// Entity Media Mappers
fun EntityMediaEntity.toDto() = EntityMediaDto(
    id = id,
    entity_slug = entity_slug,
    entity_type = entity_name, // Entity uses 'entity_name'
    media_url = url,
    media_type = media_type,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun EntityMediaDto.toEntity() = EntityMediaEntity(
    id = id,
    entity_name = entity_type,
    entity_slug = entity_slug,
    local_url = null,
    url = media_url,
    media_type = media_type,
    action_required = null,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Recipe Ingredients Mappers
fun RecipeIngredientsEntity.toDto() = RecipeIngredientsDto(
    id = id,
    product_slug = recipe_slug, // Entity uses 'recipe_slug'
    ingredient_product_slug = ingredient_slug, // Entity uses 'ingredient_slug'
    quantity = quantity ?: 0.0,
    unit_slug = quantity_unit_slug,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun RecipeIngredientsDto.toEntity() = RecipeIngredientsEntity(
    id = id,
    recipe_slug = product_slug,
    ingredient_slug = ingredient_product_slug,
    quantity = quantity,
    quantity_unit_slug = unit_slug,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

// Deleted Records Mappers
fun DeletedRecordsEntity.toDto() = DeletedRecordsDto(
    id = id,
    entity_slug = record_slug, // Entity uses 'record_slug'
    entity_type = record_type, // Entity uses 'record_type'
    slug = slug,
    businessSlug = business_slug,
    createdBy  = created_by, // Entity uses 'created_by'
    createdAt = created_at, // Entity uses 'created_at' for deletion time
    updatedAt = updated_at
)

fun DeletedRecordsDto.toEntity() = DeletedRecordsEntity(
    id = id,
    record_slug = entity_slug,
    record_type = entity_type,
    deletion_type = null,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

