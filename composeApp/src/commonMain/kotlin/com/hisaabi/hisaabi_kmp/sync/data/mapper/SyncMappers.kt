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
    taxPercentage = tax_percentage,
    discountPercentage = discount_percentage,
    retailPrice = retail_price,
    thumbnail = thumbnail,
    purchasePrice = purchase_price,
    statusId = status_id,
    digitalId = digital_id,
    baseUnitSlug = base_unit_slug,
    defaultUnitSlug = default_unit_slug,
    minimumQuantityUnitSlug = minimum_quantity_unit_slug,
    openingQuantityUnitSlug = opening_quantity_unit_slug,
    categorySlug = category_slug,
    wholeSalePrice = wholesale_price,
    averagePurchasePrice = avg_purchase_price,
    openingQuantityPurchasePrice = opening_quantity_purchase_price,
    expiryAlert = expiry_alert,
    manufacturer = manufacturer,
    expiryDate = expiry_date?.toLongOrNull(),
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
    tax_percentage = taxPercentage,
    discount_percentage = discountPercentage,
    retail_price = retailPrice,
    wholesale_price = wholeSalePrice,
    thumbnail = thumbnail,
    purchase_price = purchasePrice,
    status_id = statusId,
    digital_id = digitalId,
    base_unit_slug = baseUnitSlug,
    default_unit_slug = defaultUnitSlug,
    minimum_quantity_unit_slug = minimumQuantityUnitSlug,
    opening_quantity_unit_slug = openingQuantityUnitSlug,
    category_slug = categorySlug,
    avg_purchase_price = averagePurchasePrice,
    opening_quantity_purchase_price = openingQuantityPurchasePrice,
    expiry_date = expiryDate?.toString(),
    expiry_alert = expiryAlert,
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
    updatedAt = updated_at,
    statusId = status_id
)

fun PaymentMethodDto.toEntity() = PaymentMethodEntity(
    id = id,
    title = title ?: "",
    description = description ?: "",
    amount = amount,
    opening_amount = openingAmount,
    status_id = statusId ?: 0,
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
    parentSlug = parent_slug,
    conversionRate = conversion_factor,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at,
    sortOrder = sort_order,
    conversionFactor = conversion_factor,
    baseConversionUnitSlug = base_conversion_unit_slug,
    statusId = status_id
)

fun QuantityUnitDto.toEntity() = QuantityUnitEntity(
    id = id,
    title = title ?: "",
    sort_order = sortOrder,
    parent_slug = parentSlug,
    conversion_factor = conversionRate,
    base_conversion_unit_slug = baseConversionUnitSlug,
    status_id = statusId,
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
    updatedAt = updated_at,
    address = address,
    latLong = lat_long,
    thumbnail = thumbnail,
    typeId = type_id
)

fun WareHouseDto.toEntity() = WareHouseEntity(
    id = id,
    title = title,
    address = address ?: location,
    description = description,
    lat_long = latLong,
    thumbnail = thumbnail,
    type_id = typeId,
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
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at,
    customerSlug = customer_slug,
    totalBill = total_bill,
    totalPaid = total_paid,
    timestamp = timestamp?.toLongOrNull(),
    remindAtMilliseconds = remind_at_milliseconds,
    flatDiscount = discount,
    paymentMethodTo = payment_method_to_slug,
    paymentMethodFrom = payment_method_from_slug,
    transactionType = transaction_type,
    priceTypeId = price_type_id,
    additionalChargesType = additional_charges_slug,
    additionalChargesDesc = additional_charges_desc,
    additionalCharges = additional_charges,
    discountTypeId = discount_type_id.toString(),
    taxTypeId = tax_type_id,
    flatTax = tax,
    description = description,
    statusId = status_id,
    stateId = state_id,
    wareHouseSlugFrom = ware_house_slug_from,
    wareHouseSlugTo = ware_house_slug_to,
    shippingAddress = shipping_address,
    parentSlug = parent_slug
)

fun TransactionDto.toEntity() = InventoryTransactionEntity(
    id = id,
    customer_slug = customerSlug,
    parent_slug = parentSlug,
    total_bill = totalBill,
    total_paid = totalPaid,
    timestamp = timestamp?.toString(),
    discount = flatDiscount,
    payment_method_to_slug = paymentMethodTo,
    payment_method_from_slug = paymentMethodFrom,
    transaction_type = transactionType,
    price_type_id = priceTypeId,
    additional_charges_slug = additionalChargesType,
    additional_charges_desc = additionalChargesDesc,
    additional_charges = additionalCharges,
    discount_type_id = discountTypeId?.toIntOrNull() ?: 0,
    tax_type_id = taxTypeId,
    tax = flatTax,
    description = description,
    shipping_address = shippingAddress,
    status_id = statusId,
    state_id = stateId ?: 0,
    remind_at_milliseconds = remindAtMilliseconds ?: 0,
    ware_house_slug_from = wareHouseSlugFrom,
    ware_house_slug_to = wareHouseSlugTo,
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
    transactionSlug = transaction_slug,
    flatTax = flat_tax,
    taxType = tax_type,
    flatDiscount = flat_discount,
    discountType = discount_type,
    productSlug = product_slug,
    quantity = quantity,
    price = price,
    profit = profit,
    quantityUnitSlug = quantity_unit_slug,
    businessSlug = business_slug,
    updatedAt = updated_at,
    description = description,
    recipeSlug = recipe_slug,
    slug = slug,
    createdBy = created_by,
    createdAt = created_at
)

fun TransactionDetailDto.toEntity() = TransactionDetailEntity(
    id = id,
    transaction_slug = transactionSlug,
    product_slug = productSlug,
    description = description,
    recipe_slug = recipeSlug ?: "0",
    quantity = quantity,
    quantity_unit_slug = quantityUnitSlug,
    price = price,
    profit = profit,
    flat_discount = flatDiscount,
    discount_type = discountType,
    flat_tax = flatTax,
    tax_type = taxType,
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
    productSlug = product_slug,
    wareHouseSlug = warehouse_slug,
    openingQuantity = opening_quantity,
    currentQuantity = current_quantity,
    minimumQuantity = minimum_quantity,
    maxQuantity = maximum_quantity,
    businessSlug = business_slug,
    syncStatus = sync_status,
    updatedAt = null, // Not in entity
    slug = null, // Not in entity
    createdBy = null, // Not in entity
    createdAt = null // Not in entity
)

fun ProductQuantitiesDto.toEntity() = ProductQuantitiesEntity(
    id = id,
    product_slug = productSlug,
    warehouse_slug = wareHouseSlug,
    opening_quantity = openingQuantity,
    current_quantity = currentQuantity,
    minimum_quantity = minimumQuantity,
    maximum_quantity = maxQuantity,
    business_slug = businessSlug,
    sync_status = SyncStatus.SYNCED.value
)

// Entity Media Mappers
fun EntityMediaEntity.toDto() = EntityMediaDto(
    id = id,
    entitySlug = entity_slug,
    entityType = entity_name, // Entity uses 'entity_name'
    url = url,
    mediaType = media_type,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun EntityMediaDto.toEntity() = EntityMediaEntity(
    id = id,
    entity_name = entityType,
    entity_slug = entitySlug,
    local_url = null,
    url = url,
    media_type = mediaType,
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
    recipeSlug = recipe_slug, // Entity uses 'recipe_slug'
    ingredientSlug = ingredient_slug, // Entity uses 'ingredient_slug'
    quantity = quantity?.toString(),
    quantityUnitSlug = quantity_unit_slug,
    slug = slug,
    businessSlug = business_slug,
    createdBy = created_by,
    createdAt = created_at,
    updatedAt = updated_at
)

fun RecipeIngredientsDto.toEntity() = RecipeIngredientsEntity(
    id = id,
    recipe_slug = recipeSlug,
    ingredient_slug = ingredientSlug,
    quantity = quantity?.toDoubleOrNull(),
    quantity_unit_slug = quantityUnitSlug,
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
    recordSlug = record_slug, // Entity uses 'record_slug'
    recordType = record_type, // Entity uses 'record_type'
    deletionType = deletion_type,
    businessSlug = business_slug,
    createdAt = created_at, // Entity uses 'created_at' for deletion time
    updatedAt = updated_at,
    slug = slug,
    createdBy = created_by
)

fun DeletedRecordsDto.toEntity() = DeletedRecordsEntity(
    id = id,
    record_slug = recordSlug,
    record_type = recordType,
    deletion_type = deletionType,
    slug = slug,
    business_slug = businessSlug,
    created_by = createdBy,
    sync_status = SyncStatus.SYNCED.value,
    created_at = createdAt,
    updated_at = updatedAt
)

