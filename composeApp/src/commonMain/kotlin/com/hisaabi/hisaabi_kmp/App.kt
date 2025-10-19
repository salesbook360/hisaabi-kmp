package com.hisaabi.hisaabi_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            
            // Profile navigation state
            // No specific state needed
            
            // Transactions navigation state
            var transactionType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionType?>(null) }
            var selectingPartyForTransaction by remember { mutableStateOf(false) }
            var selectedPartyForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.Party?>(null) }
            var selectingWarehouseForTransaction by remember { mutableStateOf(false) }
            var selectedWarehouseForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse?>(null) }
            var selectingProductsForTransaction by remember { mutableStateOf(false) }
            var selectedProductsForTransaction by remember { mutableStateOf<List<com.hisaabi.hisaabi_kmp.products.domain.model.Product>>(emptyList()) }
            var selectingPaymentMethodForTransaction by remember { mutableStateOf(false) }
            var selectedPaymentMethodForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod?>(null) }
            
            // Create transaction ViewModel once and reuse it across both steps
            val koin = org.koin.compose.getKoin()
            val transactionViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel>()
            }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToAuth = { currentScreen = AppScreen.AUTH },
                        onNavigateToQuantityUnits = { currentScreen = AppScreen.QUANTITY_UNITS },
                        onNavigateToTransactionSettings = { currentScreen = AppScreen.TRANSACTION_SETTINGS },
                        onNavigateToReceiptSettings = { currentScreen = AppScreen.RECEIPT_SETTINGS },
                        onNavigateToDashboardSettings = { currentScreen = AppScreen.DASHBOARD_SETTINGS },
                        onNavigateToTemplates = { currentScreen = AppScreen.TEMPLATES },
                        onNavigateToUpdateProfile = { currentScreen = AppScreen.UPDATE_PROFILE },
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
                        },
                        onNavigateToTransactions = {
                            currentScreen = AppScreen.TRANSACTIONS_LIST
                        },
                        onNavigateToAddTransaction = { type ->
                            transactionType = type
                            currentScreen = AppScreen.ADD_TRANSACTION_STEP1
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
                        onPartyClick = { party ->
                            if (selectingPartyForTransaction) {
                                // Store selected party and return to transaction
                                selectedPartyForTransaction = party
                                selectingPartyForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                // TODO: Navigate to party details
                            }
                        },
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
                            if (selectingProductsForTransaction) {
                                // Add product to selection list
                                selectedProductsForTransaction = selectedProductsForTransaction + product
                                // Return to transaction screen
                                selectingProductsForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                // If it's a recipe, navigate to ingredients screen
                                if (product.isRecipe) {
                                    selectedRecipeProduct = product
                                    currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                                } else {
                                    // TODO: Navigate to product details
                                }
                            }
                        },
                        onAddProductClick = { selectedType ->
                            // Use the selected product type, or default to simple product if none selected
                            addProductType = selectedType ?: com.hisaabi.hisaabi_kmp.products.domain.model.ProductType.SIMPLE_PRODUCT
                            currentScreen = AppScreen.ADD_PRODUCT
                        },
                        onNavigateBack = { 
                            if (selectingProductsForTransaction) {
                                selectingProductsForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                currentScreen = AppScreen.HOME
                            }
                        },
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
                            if (selectingPaymentMethodForTransaction) {
                                // Store selected payment method and return to transaction
                                selectedPaymentMethodForTransaction = paymentMethod
                                selectingPaymentMethodForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP2
                            } else {
                                selectedPaymentMethodForEdit = paymentMethod
                                currentScreen = AppScreen.ADD_PAYMENT_METHOD
                            }
                        },
                        onAddPaymentMethodClick = {
                            selectedPaymentMethodForEdit = null
                            currentScreen = AppScreen.ADD_PAYMENT_METHOD
                        },
                        onNavigateBack = { 
                            if (selectingPaymentMethodForTransaction) {
                                selectingPaymentMethodForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP2
                            } else {
                                currentScreen = AppScreen.HOME
                            }
                        },
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
                            if (selectingWarehouseForTransaction) {
                                // Store selected warehouse and return to transaction
                                selectedWarehouseForTransaction = warehouse
                                selectingWarehouseForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                selectedWarehouseForEdit = warehouse
                                currentScreen = AppScreen.ADD_WAREHOUSE
                            }
                        },
                        onAddWarehouseClick = {
                            selectedWarehouseForEdit = null
                            currentScreen = AppScreen.ADD_WAREHOUSE
                        },
                        onNavigateBack = { 
                            if (selectingWarehouseForTransaction) {
                                selectingWarehouseForTransaction = false
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                currentScreen = AppScreen.HOME
                            }
                        },
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
                AppScreen.UPDATE_PROFILE -> {
                    com.hisaabi.hisaabi_kmp.profile.presentation.ui.UpdateProfileScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME }
                    )
                }
                AppScreen.TRANSACTIONS_LIST -> {
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.TransactionsListScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onTransactionClick = { transaction ->
                            // TODO: Navigate to transaction detail
                        },
                        onAddTransactionClick = {
                            transactionType = null
                            currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                        }
                    )
                }
                AppScreen.ADD_TRANSACTION_STEP1 -> {
                    // Set transaction type if provided
                    LaunchedEffect(transactionType) {
                        transactionType?.let { type ->
                            transactionViewModel.setTransactionType(type)
                        }
                    }
                    
                    // Set selected party if returned from party selection
                    LaunchedEffect(selectedPartyForTransaction) {
                        selectedPartyForTransaction?.let { party ->
                            transactionViewModel.selectParty(party)
                            selectedPartyForTransaction = null // Clear after setting
                        }
                    }
                    
                    // Set selected warehouse if returned from warehouse selection
                    LaunchedEffect(selectedWarehouseForTransaction) {
                        selectedWarehouseForTransaction?.let { warehouse ->
                            transactionViewModel.selectWarehouse(warehouse)
                            selectedWarehouseForTransaction = null // Clear after setting
                        }
                    }
                    
                    // Add selected products if returned from product selection
                    LaunchedEffect(selectedProductsForTransaction.size) {
                        if (selectedProductsForTransaction.isNotEmpty()) {
                            selectedProductsForTransaction.forEach { product ->
                                // Get default unit for the product
                                val defaultUnit = null // TODO: Fetch from quantity units
                                transactionViewModel.addProduct(product, defaultUnit)
                            }
                            selectedProductsForTransaction = emptyList() // Clear after adding
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddTransactionStep1Screen(
                        viewModel = transactionViewModel,
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onSelectParty = { 
                            // Determine party segment based on transaction type
                            val state = transactionViewModel.state.value
                            selectedPartySegment = if (com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionType.isDealingWithVendor(state.transactionType.value)) {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR
                            } else {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                            }
                            selectingPartyForTransaction = true
                            currentScreen = AppScreen.PARTIES
                        },
                        onSelectProducts = { 
                            selectingProductsForTransaction = true
                            currentScreen = AppScreen.PRODUCTS
                        },
                        onSelectWarehouse = { 
                            selectingWarehouseForTransaction = true
                            currentScreen = AppScreen.WAREHOUSES
                        },
                        onProceedToStep2 = { currentScreen = AppScreen.ADD_TRANSACTION_STEP2 }
                    )
                }
                AppScreen.ADD_TRANSACTION_STEP2 -> {
                    // Set selected payment method if returned from payment method selection
                    LaunchedEffect(selectedPaymentMethodForTransaction) {
                        selectedPaymentMethodForTransaction?.let { paymentMethod ->
                            transactionViewModel.selectPaymentMethod(paymentMethod)
                            selectedPaymentMethodForTransaction = null // Clear after setting
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddTransactionStep2Screen(
                        viewModel = transactionViewModel,
                        onNavigateBack = { currentScreen = AppScreen.ADD_TRANSACTION_STEP1 },
                        onSelectPaymentMethod = { 
                            selectingPaymentMethodForTransaction = true
                            currentScreen = AppScreen.PAYMENT_METHODS
                        },
                        onTransactionSaved = { currentScreen = AppScreen.HOME }
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
    ADD_TEMPLATE,
    UPDATE_PROFILE,
    TRANSACTIONS_LIST,
    ADD_TRANSACTION_STEP1,
    ADD_TRANSACTION_STEP2
}