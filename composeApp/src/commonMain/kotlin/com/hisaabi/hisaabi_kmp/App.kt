package com.hisaabi.hisaabi_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hisaabi.hisaabi_kmp.auth.AuthNavigation
import com.hisaabi.hisaabi_kmp.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
            var selectedPartySegment by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment?>(null) }
            var addPartyType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType?>(null) }
            var partiesRefreshTrigger by remember { mutableStateOf(0) }
            
            // Category navigation state
            var categoryType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType?>(null) }
            var categoriesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedCategoryForParty by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity?>(null) }
            var selectedAreaForParty by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity?>(null) }
            var returnToAddParty by remember { mutableStateOf(false) }
            
            // Products navigation state
            var addProductType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.products.domain.model.ProductType?>(null) }
            var productsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedRecipeProduct by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.products.domain.model.Product?>(null) }
            
            // Payment Methods navigation state
            var paymentMethodsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedPaymentMethodForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod?>(null) }
            
            // Warehouses navigation state
            var warehousesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedWarehouseForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse?>(null) }
            
            // Business navigation state
            var businessRefreshTrigger by remember { mutableStateOf(0) }
            var selectedBusinessForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.business.domain.model.Business?>(null) }
            
            // Quantity Units navigation state
            var quantityUnitsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedUnitForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit?>(null) }
            
            // Transaction Settings navigation state
            // No specific state needed as it's a preference screen
            
            // Receipt Settings navigation state
            // No specific state needed as it's a preference screen
            
            // Dashboard Settings navigation state
            // No specific state needed as it's a preference screen
            
            // Templates navigation state
            var templatesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedTemplateIdForEdit by remember { mutableStateOf<String?>(null) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToAuth = { currentScreen = AppScreen.AUTH },
                        onNavigateToQuantityUnits = { currentScreen = AppScreen.QUANTITY_UNITS },
                        onNavigateToTransactionSettings = { currentScreen = AppScreen.TRANSACTION_SETTINGS },
                        onNavigateToReceiptSettings = { currentScreen = AppScreen.RECEIPT_SETTINGS },
                        onNavigateToDashboardSettings = { currentScreen = AppScreen.DASHBOARD_SETTINGS },
                        onNavigateToTemplates = { currentScreen = AppScreen.TEMPLATES },
                        onNavigateToParties = { segment ->
                            selectedPartySegment = segment
                            currentScreen = AppScreen.PARTIES
                        },
                        onNavigateToProducts = {
                            currentScreen = AppScreen.PRODUCTS
                        },
                        onNavigateToAddProduct = { type ->
                            addProductType = type
                            currentScreen = AppScreen.ADD_PRODUCT
                        },
                        onNavigateToPaymentMethods = {
                            currentScreen = AppScreen.PAYMENT_METHODS
                        },
                        onNavigateToWarehouses = {
                            currentScreen = AppScreen.WAREHOUSES
                        },
                        onNavigateToMyBusiness = {
                            currentScreen = AppScreen.MY_BUSINESS
                        }
                    )
                }
                AppScreen.AUTH -> {
                    AuthNavigation(
                        onNavigateToMain = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.PARTIES -> {
                    com.hisaabi.hisaabi_kmp.parties.presentation.ui.PartiesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onPartyClick = { /* TODO: Navigate to party details */ },
                        onAddPartyClick = {
                            // Determine party type based on current segment
                            addPartyType = when (selectedPartySegment) {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.CUSTOMER
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.VENDOR
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.INVESTOR -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.INVESTOR
                                else -> com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.CUSTOMER
                            }
                            currentScreen = AppScreen.ADD_PARTY
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onSegmentChanged = { segment ->
                            // Update the selected segment when user switches tabs
                            selectedPartySegment = segment
                        },
                        initialSegment = selectedPartySegment,
                        refreshTrigger = partiesRefreshTrigger
                    )
                }
                AppScreen.ADD_PARTY -> {
                    addPartyType?.let { type ->
                        com.hisaabi.hisaabi_kmp.parties.presentation.ui.AddPartyScreen(
                            viewModel = org.koin.compose.koinInject(),
                            partyType = type,
                            onNavigateBack = { 
                                if (!returnToAddParty) {
                                    partiesRefreshTrigger++  // Trigger refresh
                                    currentScreen = AppScreen.PARTIES
                                }
                                returnToAddParty = false
                            },
                            onNavigateToCategories = {
                                categoryType = com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.CUSTOMER_CATEGORY
                                returnToAddParty = true
                                currentScreen = AppScreen.CATEGORIES
                            },
                            onNavigateToAreas = {
                                categoryType = com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.AREA
                                returnToAddParty = true
                                currentScreen = AppScreen.CATEGORIES
                            },
                            selectedCategoryFromNav = selectedCategoryForParty,
                            selectedAreaFromNav = selectedAreaForParty
                        )
                    }
                }
                
                AppScreen.CATEGORIES -> {
                    categoryType?.let { type ->
                        com.hisaabi.hisaabi_kmp.categories.presentation.ui.CategoriesScreen(
                            viewModel = org.koin.compose.koinInject(),
                            categoryType = type,
                            onCategorySelected = { category ->
                                // Store selected category based on type
                                if (type == com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.CUSTOMER_CATEGORY) {
                                    category?.let {
                                        selectedCategoryForParty = com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity(
                                            id = it.id,
                                            title = it.title,
                                            description = it.description,
                                            thumbnail = it.thumbnail,
                                            type_id = it.typeId,
                                            slug = it.slug,
                                            business_slug = it.businessSlug,
                                            created_by = it.createdBy,
                                            sync_status = it.syncStatus,
                                            created_at = it.createdAt,
                                            updated_at = it.updatedAt
                                        )
                                    }
                                } else if (type == com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType.AREA) {
                                    category?.let {
                                        selectedAreaForParty = com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity(
                                            id = it.id,
                                            title = it.title,
                                            description = it.description,
                                            thumbnail = it.thumbnail,
                                            type_id = it.typeId,
                                            slug = it.slug,
                                            business_slug = it.businessSlug,
                                            created_by = it.createdBy,
                                            sync_status = it.syncStatus,
                                            created_at = it.createdAt,
                                            updated_at = it.updatedAt
                                        )
                                    }
                                }
                                currentScreen = AppScreen.ADD_PARTY
                            },
                            onAddCategoryClick = {
                                currentScreen = AppScreen.ADD_CATEGORY
                            },
                            onNavigateBack = {
                                if (returnToAddParty) {
                                    currentScreen = AppScreen.ADD_PARTY
                                } else {
                                    currentScreen = AppScreen.PARTIES
                                }
                            },
                            refreshTrigger = categoriesRefreshTrigger
                        )
                    }
                }
                
                AppScreen.ADD_CATEGORY -> {
                    categoryType?.let { type ->
                        com.hisaabi.hisaabi_kmp.categories.presentation.ui.AddCategoryScreen(
                            viewModel = org.koin.compose.koinInject(),
                            categoryType = type,
                            onNavigateBack = {
                                categoriesRefreshTrigger++  // Trigger refresh
                                currentScreen = AppScreen.CATEGORIES
                            }
                        )
                    }
                }
                
                AppScreen.PRODUCTS -> {
                    com.hisaabi.hisaabi_kmp.products.presentation.ui.ProductsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onProductClick = { product ->
                            // If it's a recipe, navigate to ingredients screen
                            if (product.isRecipe) {
                                selectedRecipeProduct = product
                                currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                            } else {
                                // TODO: Navigate to product details
                            }
                        },
                        onAddProductClick = { selectedType ->
                            // Use the selected product type, or default to simple product if none selected
                            addProductType = selectedType ?: com.hisaabi.hisaabi_kmp.products.domain.model.ProductType.SIMPLE_PRODUCT
                            currentScreen = AppScreen.ADD_PRODUCT
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        refreshTrigger = productsRefreshTrigger
                    )
                }
                
                AppScreen.ADD_PRODUCT -> {
                    addProductType?.let { type ->
                        com.hisaabi.hisaabi_kmp.products.presentation.ui.AddProductScreen(
                            viewModel = org.koin.compose.koinInject(),
                            productType = type,
                            onNavigateBack = {
                                productsRefreshTrigger++  // Trigger refresh
                                currentScreen = AppScreen.PRODUCTS
                            },
                            onNavigateToIngredients = { recipeSlug ->
                                // TODO: Load the recipe product by slug
                                // For now, create a placeholder
                                currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                            }
                        )
                    }
                }
                
                AppScreen.MANAGE_RECIPE_INGREDIENTS -> {
                    selectedRecipeProduct?.let { recipe ->
                        com.hisaabi.hisaabi_kmp.products.presentation.ui.ManageRecipeIngredientsScreen(
                            recipeProduct = recipe,
                            onNavigateBack = {
                                currentScreen = AppScreen.PRODUCTS
                            }
                        )
                    }
                }
                
                AppScreen.PAYMENT_METHODS -> {
                    com.hisaabi.hisaabi_kmp.paymentmethods.presentation.ui.PaymentMethodsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onPaymentMethodClick = { paymentMethod ->
                            selectedPaymentMethodForEdit = paymentMethod
                            currentScreen = AppScreen.ADD_PAYMENT_METHOD
                        },
                        onAddPaymentMethodClick = {
                            selectedPaymentMethodForEdit = null
                            currentScreen = AppScreen.ADD_PAYMENT_METHOD
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        refreshTrigger = paymentMethodsRefreshTrigger
                    )
                }
                
                AppScreen.ADD_PAYMENT_METHOD -> {
                    com.hisaabi.hisaabi_kmp.paymentmethods.presentation.ui.AddPaymentMethodScreen(
                        viewModel = org.koin.compose.koinInject(),
                        paymentMethodToEdit = selectedPaymentMethodForEdit,
                        onNavigateBack = {
                            paymentMethodsRefreshTrigger++  // Trigger refresh
                            currentScreen = AppScreen.PAYMENT_METHODS
                        }
                    )
                }
                
                AppScreen.WAREHOUSES -> {
                    com.hisaabi.hisaabi_kmp.warehouses.presentation.ui.WarehousesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onWarehouseClick = { warehouse ->
                            selectedWarehouseForEdit = warehouse
                            currentScreen = AppScreen.ADD_WAREHOUSE
                        },
                        onAddWarehouseClick = {
                            selectedWarehouseForEdit = null
                            currentScreen = AppScreen.ADD_WAREHOUSE
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        refreshTrigger = warehousesRefreshTrigger
                    )
                }
                
                AppScreen.ADD_WAREHOUSE -> {
                    com.hisaabi.hisaabi_kmp.warehouses.presentation.ui.AddWarehouseScreen(
                        viewModel = org.koin.compose.koinInject(),
                        warehouseToEdit = selectedWarehouseForEdit,
                        onNavigateBack = {
                            warehousesRefreshTrigger++  // Trigger refresh
                            currentScreen = AppScreen.WAREHOUSES
                        }
                    )
                }
                
                AppScreen.MY_BUSINESS -> {
                    com.hisaabi.hisaabi_kmp.business.presentation.ui.MyBusinessScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onBusinessClick = { business ->
                            selectedBusinessForEdit = business
                            currentScreen = AppScreen.ADD_BUSINESS
                        },
                        onAddBusinessClick = {
                            selectedBusinessForEdit = null
                            currentScreen = AppScreen.ADD_BUSINESS
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        refreshTrigger = businessRefreshTrigger
                    )
                }
                
                AppScreen.ADD_BUSINESS -> {
                    com.hisaabi.hisaabi_kmp.business.presentation.ui.AddBusinessScreen(
                        viewModel = org.koin.compose.koinInject(),
                        businessToEdit = selectedBusinessForEdit,
                        onNavigateBack = {
                            businessRefreshTrigger++
                            currentScreen = AppScreen.MY_BUSINESS
                        }
                    )
                }
                
                AppScreen.QUANTITY_UNITS -> {
                    com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui.QuantityUnitsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onUnitClick = { unit ->
                            selectedUnitForEdit = unit
                            currentScreen = AppScreen.ADD_QUANTITY_UNIT
                        },
                        onAddUnitClick = {
                            selectedUnitForEdit = null
                            currentScreen = AppScreen.ADD_QUANTITY_UNIT
                        },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        refreshTrigger = quantityUnitsRefreshTrigger
                    )
                }
                
                AppScreen.ADD_QUANTITY_UNIT -> {
                    com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui.AddQuantityUnitScreen(
                        viewModel = org.koin.compose.koinInject(),
                        unitToEdit = selectedUnitForEdit,
                        onNavigateBack = {
                            quantityUnitsRefreshTrigger++
                            currentScreen = AppScreen.QUANTITY_UNITS
                        }
                    )
                }
                
                AppScreen.TRANSACTION_SETTINGS -> {
                    com.hisaabi.hisaabi_kmp.settings.presentation.ui.TransactionTypeSelectionScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                
                AppScreen.RECEIPT_SETTINGS -> {
                    com.hisaabi.hisaabi_kmp.settings.presentation.ui.ReceiptSettingsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.DASHBOARD_SETTINGS -> {
                    com.hisaabi.hisaabi_kmp.settings.presentation.ui.DashboardSettingsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.TEMPLATES -> {
                    com.hisaabi.hisaabi_kmp.templates.presentation.ui.TemplatesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onAddTemplateClick = {
                            selectedTemplateIdForEdit = null
                            currentScreen = AppScreen.ADD_TEMPLATE
                        },
                        onEditTemplateClick = { templateId ->
                            selectedTemplateIdForEdit = templateId
                            currentScreen = AppScreen.ADD_TEMPLATE
                        }
                    )
                }
                AppScreen.ADD_TEMPLATE -> {
                    com.hisaabi.hisaabi_kmp.templates.presentation.ui.AddTemplateScreen(
                        viewModel = org.koin.compose.koinInject(),
                        templateId = selectedTemplateIdForEdit,
                        onNavigateBack = {
                            templatesRefreshTrigger++
                            currentScreen = AppScreen.TEMPLATES
                        }
                    )
                }
            }
        }
    }
}

enum class AppScreen {
    HOME,
    AUTH,
    PARTIES,
    ADD_PARTY,
    CATEGORIES,
    ADD_CATEGORY,
    PRODUCTS,
    ADD_PRODUCT,
    MANAGE_RECIPE_INGREDIENTS,
    PAYMENT_METHODS,
    ADD_PAYMENT_METHOD,
    WAREHOUSES,
    ADD_WAREHOUSE,
    MY_BUSINESS,
    ADD_BUSINESS,
    QUANTITY_UNITS,
    ADD_QUANTITY_UNIT,
    TRANSACTION_SETTINGS,
    RECEIPT_SETTINGS,
    DASHBOARD_SETTINGS,
    TEMPLATES,
    ADD_TEMPLATE
}