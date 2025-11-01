package com.hisaabi.hisaabi_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.key
import com.hisaabi.hisaabi_kmp.auth.AuthNavigation
import com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel.AuthViewModel
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.presentation.ui.BusinessSelectionGateScreen
import com.hisaabi.hisaabi_kmp.home.HomeScreen
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.sync.domain.manager.SyncManager
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import kotlin.system.exitProcess

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            // Check authentication state on app launch
            val authViewModel: AuthViewModel = koinInject()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isInitialized by authViewModel.isInitialized.collectAsState()
            
            // Business preferences for checking selected business
            val businessPreferences: BusinessPreferencesDataSource = koinInject()
            var selectedBusinessSlug by remember { mutableStateOf<String?>(null) }
            
            // Business repository for fetching and caching businesses
            val businessRepository: com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository = koinInject()
            
            // Products repository for fetching products by slugs
            val productsRepository: com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository = koinInject()
            
            // Sync manager for background sync
            val syncManager: SyncManager = koinInject()
            
            // Observe selected business slug
            LaunchedEffect(Unit) {
                businessPreferences.observeSelectedBusinessSlug().collect { businessSlug ->
                    selectedBusinessSlug = businessSlug
                }
            }
            
            // Fetch and cache businesses on app launch when user is logged in
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    try {
                        println("=== FETCHING BUSINESSES ON APP LAUNCH ===")
                        val result = businessRepository.fetchAndCacheBusinesses()
                        if (result.isSuccess) {
                            println("Successfully cached ${result.getOrNull()?.size ?: 0} businesses")
                        } else {
                            println("Failed to cache businesses: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        println("Error fetching businesses on app launch: ${e.message}")
                    }
                }
            }
            
            // Start background sync when user is logged in and business is selected
            LaunchedEffect(isLoggedIn, selectedBusinessSlug) {
                if (isLoggedIn && selectedBusinessSlug != null) {
                    syncManager.startBackgroundSync()
                } else {
                    syncManager.stopBackgroundSync()
                }
            }
            
            // Set initial screen based on authentication state
            var currentScreen by remember { mutableStateOf<AppScreen?>(null) }
            
            // Navigation stack for back button handling
            var navigationStack by remember { mutableStateOf<List<AppScreen>>(emptyList()) }
            
            // Helper functions for navigation
            fun navigateTo(screen: AppScreen) {
                currentScreen?.let { 
                    navigationStack = navigationStack + it 
                }
                currentScreen = screen
            }
            
            fun navigateBack() {
                if (navigationStack.isNotEmpty()) {
                    currentScreen = navigationStack.last()
                    navigationStack = navigationStack.dropLast(1)
                } else {
                    // No more screens in stack, exit app
                    exitProcess(0)
                }
            }
            
            // Handle Android back button
            BackHandler {
                navigateBack()
            }
            
            // Bottom navigation state - managed at app level to persist across nested navigation
            var selectedBottomNavTab by remember { mutableStateOf(0) }
            
            // Track if we're navigating from HOME screen to retain bottom nav selection
            LaunchedEffect(currentScreen) {
                // When returning to HOME screen, don't reset the bottom nav selection
                // The selection is already persisted in selectedBottomNavTab
            }
            
            // Set initial screen after auth check is complete
            LaunchedEffect(isInitialized, isLoggedIn, selectedBusinessSlug) {
                if (isInitialized && currentScreen == null) {
                    currentScreen = when {
                        !isLoggedIn -> AppScreen.AUTH
                        selectedBusinessSlug == null -> AppScreen.BUSINESS_SELECTION_GATE
                        else -> AppScreen.HOME
                    }
                }
            }
            
            // Handle auth and business selection state changes after initialization
            LaunchedEffect(isLoggedIn, selectedBusinessSlug) {
                if (currentScreen != null) {
                    when {
                        // User logged out - go to auth
                        !isLoggedIn && currentScreen != AppScreen.AUTH -> {
                            currentScreen = AppScreen.AUTH
                        }
                        // User logged in but no business selected - go to gate
                        isLoggedIn && selectedBusinessSlug == null && 
                        currentScreen != AppScreen.BUSINESS_SELECTION_GATE &&
                        currentScreen != AppScreen.ADD_BUSINESS -> {
                            currentScreen = AppScreen.BUSINESS_SELECTION_GATE
                        }
                        // User logged in and has selected business - allow navigation
                        isLoggedIn && selectedBusinessSlug != null &&
                        (currentScreen == AppScreen.AUTH || currentScreen == AppScreen.BUSINESS_SELECTION_GATE) -> {
                            currentScreen = AppScreen.HOME
                        }
                    }
                }
            }
            var selectedPartySegment by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment?>(null) }
            var addPartyType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType?>(null) }
            var partiesRefreshTrigger by remember { mutableStateOf(0) }
            var addPartyScreenKey by remember { mutableStateOf(0) }  // Key to force reset AddPartyScreen
            var selectedPartyForBalanceHistory by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.Party?>(null) }
            var selectedPartyForTransactionFilter by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.Party?>(null) }
            var selectedPartyForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.Party?>(null) }
            
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
            var selectedProductForEdit by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.products.domain.model.Product?>(null) }
            
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
            var transactionType by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes?>(null) }
            var selectingPartyForTransaction by remember { mutableStateOf(false) }
            var selectedPartyForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.parties.domain.model.Party?>(null) }
            var returnToScreenAfterPartySelection by remember { mutableStateOf<AppScreen?>(null) }
            var isExpenseIncomePartySelection by remember { mutableStateOf(false) }  // Flag for expense/income context
            var isSelectingPaymentMethodFrom by remember { mutableStateOf(false) }  // Flag for payment transfer From/To
            var selectingWarehouseForTransaction by remember { mutableStateOf(false) }
            var selectedWarehouseForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse?>(null) }
            var selectingProductsForTransaction by remember { mutableStateOf(false) }
            var selectedProductsForTransaction by remember { mutableStateOf<List<com.hisaabi.hisaabi_kmp.products.domain.model.Product>>(emptyList()) }
            var selectedProductQuantitiesForTransaction by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
            var returnToScreenAfterProductSelection by remember { mutableStateOf<AppScreen?>(null) }
            var selectingPaymentMethodForTransaction by remember { mutableStateOf(false) }
            var selectedPaymentMethodForTransaction by remember { mutableStateOf<com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod?>(null) }
            var selectedTransactionSlug by remember { mutableStateOf<String?>(null) }
            
            // Create transaction ViewModel once and reuse it across both steps
            val koin = org.koin.compose.getKoin()
            val transactionViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel>()
            }
            val recordViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel>()
            }
            val payGetCashViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel>()
            }
            val expenseIncomeViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddExpenseIncomeViewModel>()
            }
            val paymentTransferViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PaymentTransferViewModel>()
            }
            val journalVoucherViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddJournalVoucherViewModel>()
            }
            val stockAdjustmentViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.StockAdjustmentViewModel>()
            }
            val manufactureViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddManufactureViewModel>()
            }
            val transactionDetailViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailViewModel>()
            }
            val productsViewModel = remember(koin) {
                koin.get<com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ProductsViewModel>()
            }

            // Journal Voucher state
            var showJournalAccountTypeDialog by remember { mutableStateOf(false) }
            
            // Stock Adjustment state
            var isSelectingWarehouseFrom by remember { mutableStateOf(false) }

            // Show splash screen while checking auth state
            if (currentScreen == null) {
                SplashScreen()
            } else {
                // Show content only when currentScreen is initialized
                when (currentScreen!!) {
                AppScreen.HOME -> {
                    HomeScreen(
                        selectedTab = selectedBottomNavTab,
                        onTabSelected = { selectedBottomNavTab = it },
                        onNavigateToAuth = { navigateTo(AppScreen.AUTH) },
                        onNavigateToQuantityUnits = { navigateTo(AppScreen.QUANTITY_UNITS) },
                        onNavigateToTransactionSettings = { navigateTo(AppScreen.TRANSACTION_SETTINGS) },
                        onNavigateToReceiptSettings = { navigateTo(AppScreen.RECEIPT_SETTINGS) },
                        onNavigateToDashboardSettings = { navigateTo(AppScreen.DASHBOARD_SETTINGS) },
                        onNavigateToTemplates = { navigateTo(AppScreen.TEMPLATES) },
                        onNavigateToUpdateProfile = { navigateTo(AppScreen.UPDATE_PROFILE) },
                        onNavigateToBusinessSelection = { navigateTo(AppScreen.MY_BUSINESS) },
                        onNavigateToParties = { segment ->
                            selectedPartySegment = segment
                            navigateTo(AppScreen.PARTIES)
                        },
                        onNavigateToProducts = { productType ->
                            // Store the selected product type for ProductsScreen
                            addProductType = productType
                            navigateTo(AppScreen.PRODUCTS)
                        },
                        onNavigateToAddProduct = { type ->
                            addProductType = type
                            navigateTo(AppScreen.ADD_PRODUCT)
                        },
                        onNavigateToPaymentMethods = { navigateTo(AppScreen.PAYMENT_METHODS) },
                        onNavigateToWarehouses = { navigateTo(AppScreen.WAREHOUSES) },
                        onNavigateToMyBusiness = { navigateTo(AppScreen.MY_BUSINESS) },
                        onNavigateToTransactions = { navigateTo(AppScreen.TRANSACTIONS_LIST) },
                        onNavigateToAddRecord = { navigateTo(AppScreen.ADD_RECORD) },
                        onNavigateToPayGetCash = { navigateTo(AppScreen.PAY_GET_CASH) },
                        onNavigateToExpense = {
                            expenseIncomeViewModel.setTransactionType(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.EXPENSE)
                            navigateTo(AppScreen.ADD_EXPENSE_INCOME)
                        },
                        onNavigateToExtraIncome = {
                            expenseIncomeViewModel.setTransactionType(com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.EXTRA_INCOME)
                            navigateTo(AppScreen.ADD_EXPENSE_INCOME)
                        },
                        onNavigateToPaymentTransfer = { navigateTo(AppScreen.PAYMENT_TRANSFER) },
                        onNavigateToJournalVoucher = { navigateTo(AppScreen.JOURNAL_VOUCHER) },
                        onNavigateToStockAdjustment = { navigateTo(AppScreen.STOCK_ADJUSTMENT) },
                        onNavigateToManufacture = { navigateTo(AppScreen.ADD_MANUFACTURE) },
                        onNavigateToAddTransaction = { type ->
                            transactionType = type
                            navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                        }
                    )
                }
                AppScreen.AUTH -> {
                    AuthNavigation(
                        onNavigateToMain = { 
                            // After login, check if business is selected
                            currentScreen = if (selectedBusinessSlug != null) {
                                AppScreen.HOME
                            } else {
                                AppScreen.BUSINESS_SELECTION_GATE
                            }
                        }
                    )
                }
                AppScreen.BUSINESS_SELECTION_GATE -> {
                    val myBusinessViewModel: com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.MyBusinessViewModel = koinInject()
                    BusinessSelectionGateScreen(
                        viewModel = myBusinessViewModel,
                        onBusinessSelected = {
                            // Business selected, navigate to home
                            currentScreen = AppScreen.HOME
                        },
                        onAddBusinessClick = {
                            // Navigate to add business screen
                            selectedBusinessForEdit = null
                            navigateTo(AppScreen.ADD_BUSINESS)
                        },
                        onExitApp = {
                            // Exit the app when back is pressed without business selection
                            exitProcess(0)
                        }
                    )
                }
                AppScreen.PARTIES -> {
                    com.hisaabi.hisaabi_kmp.parties.presentation.ui.PartiesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onPartyClick = { party ->
                            if (selectingPartyForTransaction) {
                                // Store selected party and return to transaction or record
                                selectedPartyForTransaction = party
                                // Don't reset flags here - let the target screen handle it
                                // Return to the appropriate screen
                                currentScreen = returnToScreenAfterPartySelection ?: AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                // Party click handled by bottom sheet
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
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXPENSE -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.EXPENSE
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXTRA_INCOME -> 
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.EXTRA_INCOME
                                else -> com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.CUSTOMER
                            }
                            addPartyScreenKey++  // Increment key to force reset
                            currentScreen = AppScreen.ADD_PARTY
                        },
                        onNavigateBack = { 
                            if (selectingPartyForTransaction) {
                                // User cancelled party selection - navigate back to the screen they came from
                                val targetScreen = returnToScreenAfterPartySelection ?: AppScreen.HOME
                                selectingPartyForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isExpenseIncomePartySelection = false
                                currentScreen = targetScreen
                            } else {
                                currentScreen = AppScreen.HOME
                            }
                        },
                        onSegmentChanged = { segment ->
                            // Update the selected segment when user switches tabs
                            selectedPartySegment = segment
                        },
                        initialSegment = selectedPartySegment,
                        refreshTrigger = partiesRefreshTrigger,
                        isExpenseIncomeContext = isExpenseIncomePartySelection,  // Pass the flag
                        // Bottom sheet action callbacks
                        onPayGetPayment = { party ->
                            selectedPartyForTransaction = party
                            navigateTo(AppScreen.PAY_GET_CASH)
                        },
                        onEditParty = { party ->
                            selectedPartyForEdit = party
                            addPartyType = com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType.fromInt(party.roleId)
                            addPartyScreenKey++  // Increment key to force reset
                            navigateTo(AppScreen.ADD_PARTY)
                        },
                        onViewTransactions = { party ->
                            selectedPartyForTransactionFilter = party
                            navigateTo(AppScreen.TRANSACTIONS_LIST)
                        },
                        onViewBalanceHistory = { party ->
                            selectedPartyForBalanceHistory = party
                            navigateTo(AppScreen.BALANCE_HISTORY)
                        },
                        onPaymentReminder = { party ->
                            // Navigate to templates screen for reminder
                            navigateTo(AppScreen.TEMPLATES)
                        },
                        onNewTransaction = { party, transactionTypeValue ->
                            // Set up for new transaction with selected party and type
                            selectedPartyForTransaction = party
                            val txType = com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.fromValue(transactionTypeValue)
                            
                            // Navigate to the appropriate transaction screen
                            when (txType) {
                                com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.SALE,
                                com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.PURCHASE,
                                com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.CUSTOMER_RETURN,
                                com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.VENDOR_RETURN -> {
                                    transactionViewModel.reset()
                                    transactionViewModel.setTransactionType(txType!!)
                                    navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                }
                                else -> {
                                    // Handle other transaction types if needed
                                    navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                }
                            }
                        }
                    )
                }
                AppScreen.ADD_PARTY -> {
                    addPartyType?.let { type ->
                        // Use key to force recomposition when navigating to edit
                        key(addPartyScreenKey) {
                            com.hisaabi.hisaabi_kmp.parties.presentation.ui.AddPartyScreen(
                                viewModel = org.koin.compose.koinInject(),
                                partyType = type,
                                partyToEdit = selectedPartyForEdit,
                                onNavigateBack = { 
                                // Trigger refresh and navigate first
                                partiesRefreshTrigger++
                                currentScreen = AppScreen.PARTIES
                                // Then clear state after navigation
                                selectedPartyForEdit = null
                                addPartyType = null
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
                    // Clear the product type after using it to avoid persistence
                    val currentProductType = addProductType
                    LaunchedEffect(Unit) {
                        addProductType = null
                    }
                    
                    com.hisaabi.hisaabi_kmp.products.presentation.ui.ProductsScreen(
                        viewModel = productsViewModel,
                        onProductClick = { product ->
                            // This is no longer used in selection mode - handled via onSelectionChanged
                            if (!selectingProductsForTransaction) {
                                // Navigate to edit product screen (reuse ADD_PRODUCT with edit mode)
                                selectedProductForEdit = product
                                addProductType = product.productType
                                currentScreen = AppScreen.ADD_PRODUCT
                            }
                        },
                        onEditProductClick = { product ->
                            // Navigate to edit product screen (reuse ADD_PRODUCT with edit mode)
                            selectedProductForEdit = product
                            addProductType = product.productType
                            currentScreen = AppScreen.ADD_PRODUCT
                        },
                        onAddProductClick = { selectedType ->
                            // Use the selected product type, or default to simple product if none selected
                            addProductType = selectedType ?: com.hisaabi.hisaabi_kmp.products.domain.model.ProductType.SIMPLE_PRODUCT
                            selectedProductForEdit = null // Clear any selected product for editing
                            currentScreen = AppScreen.ADD_PRODUCT
                        },
                        onNavigateToIngredients = { product ->
                            selectedRecipeProduct = product
                            currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                        },
                        onNavigateBack = { 
                            if (selectingProductsForTransaction) {
                                // Navigate back without selecting - clear the selection state
                                selectingProductsForTransaction = false
                                selectedProductQuantitiesForTransaction = emptyMap()
                                returnToScreenAfterProductSelection = null
                                currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                            } else {
                                currentScreen = AppScreen.HOME
                            }
                        },
                        refreshTrigger = productsRefreshTrigger,
                        initialProductType = currentProductType,
                        isSelectionMode = selectingProductsForTransaction,
                        selectedProducts = selectedProductQuantitiesForTransaction,
                        onSelectionChanged = { quantities ->
                            selectedProductQuantitiesForTransaction = quantities
                        },
                        onSelectionDone = {
                            // When Done is clicked in selection mode, fetch the actual products by their slugs
                            // For now, we need to trigger the LaunchedEffect to load products
                            selectingProductsForTransaction = false
                            currentScreen = returnToScreenAfterProductSelection ?: AppScreen.ADD_TRANSACTION_STEP1
                        },
                        onNavigateToWarehouses = {
                            selectingWarehouseForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.PRODUCTS
                            currentScreen = AppScreen.WAREHOUSES
                        }
                    )
                    
                    // Handle warehouse selection for Products screen
                    LaunchedEffect(selectedWarehouseForTransaction) {
                        selectedWarehouseForTransaction?.let { warehouse ->
                            if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.PRODUCTS) {
                                productsViewModel.selectWarehouse(warehouse)
                                selectedWarehouseForTransaction = null
                                selectingWarehouseForTransaction = false
                                returnToScreenAfterPartySelection = null
                                currentScreen = AppScreen.PRODUCTS
                            }
                        }
                    }
                }
                
                AppScreen.ADD_PRODUCT -> {
                    addProductType?.let { type ->
                        val productToEdit = selectedProductForEdit
                        val addProductViewModel = remember(koin) {
                            koin.get<com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.AddProductViewModel>()
                        }
                        
                        com.hisaabi.hisaabi_kmp.products.presentation.ui.AddProductScreen(
                            viewModel = addProductViewModel,
                            productType = type,
                            productToEdit = productToEdit,
                            onNavigateBack = {
                                productsRefreshTrigger++  // Trigger refresh
                                currentScreen = AppScreen.PRODUCTS
                            },
                            onNavigateToIngredients = { recipeSlug ->
                                // TODO: Load the recipe product by slug
                                // For now, create a placeholder
                                currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                            },
                            onNavigateToWarehouses = {
                                selectingWarehouseForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.ADD_PRODUCT
                                currentScreen = AppScreen.WAREHOUSES
                            }
                        )
                        
                        // Handle warehouse selection for Add Product screen
                        LaunchedEffect(selectedWarehouseForTransaction) {
                            selectedWarehouseForTransaction?.let { warehouse ->
                                if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_PRODUCT) {
                                    addProductViewModel.setSelectedWarehouse(warehouse)
                                    selectedWarehouseForTransaction = null
                                    selectingWarehouseForTransaction = false
                                    returnToScreenAfterPartySelection = null
                                    currentScreen = AppScreen.ADD_PRODUCT
                                }
                            }
                        }
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
                                // Store selected payment method and return to appropriate screen
                                selectedPaymentMethodForTransaction = paymentMethod
                                // Don't reset selectingPaymentMethodForTransaction here - let the target screen handle it
                                currentScreen = returnToScreenAfterPartySelection ?: AppScreen.ADD_TRANSACTION_STEP2
                                // Don't reset returnToScreenAfterPartySelection here either
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
                                // Navigate back without selecting - clear the selection state
                                val targetScreen = returnToScreenAfterPartySelection ?: AppScreen.ADD_TRANSACTION_STEP2
                                selectingPaymentMethodForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isSelectingPaymentMethodFrom = false
                                currentScreen = targetScreen
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
                                // Store selected warehouse and return to the appropriate screen
                                selectedWarehouseForTransaction = warehouse
                                // Don't reset flags here - let the target screen handle it
                                currentScreen = returnToScreenAfterPartySelection ?: AppScreen.ADD_TRANSACTION_STEP1
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
                                // Navigate back without selecting - clear the selection state
                                val targetScreen = returnToScreenAfterPartySelection ?: AppScreen.ADD_TRANSACTION_STEP1
                                selectingWarehouseForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isSelectingWarehouseFrom = false
                                currentScreen = targetScreen
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
                        onNavigateBack = { navigateBack() },
                        refreshTrigger = businessRefreshTrigger
                    )
                }
                
                AppScreen.ADD_BUSINESS -> {
                    com.hisaabi.hisaabi_kmp.business.presentation.ui.AddBusinessScreen(
                        viewModel = org.koin.compose.koinInject(),
                        businessToEdit = selectedBusinessForEdit,
                        onNavigateBack = {
                            businessRefreshTrigger++
                            // Go back to gate if no business is selected, otherwise go to MY_BUSINESS
                            currentScreen = if (selectedBusinessSlug == null) {
                                AppScreen.BUSINESS_SELECTION_GATE
                            } else {
                                AppScreen.MY_BUSINESS
                            }
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
                        onNavigateBack = { navigateBack() },
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
                        onNavigateBack = { navigateBack() }
                    )
                }
                
                AppScreen.RECEIPT_SETTINGS -> {
                    com.hisaabi.hisaabi_kmp.settings.presentation.ui.ReceiptSettingsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { navigateBack() }
                    )
                }
                AppScreen.DASHBOARD_SETTINGS -> {
                    com.hisaabi.hisaabi_kmp.settings.presentation.ui.DashboardSettingsScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { navigateBack() }
                    )
                }
                AppScreen.TEMPLATES -> {
                    com.hisaabi.hisaabi_kmp.templates.presentation.ui.TemplatesScreen(
                        viewModel = org.koin.compose.koinInject(),
                        onNavigateBack = { navigateBack() },
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
                        onNavigateBack = { navigateBack() }
                    )
                }
                AppScreen.TRANSACTIONS_LIST -> {
                    val transactionsListViewModel: com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel = org.koin.compose.koinInject()
                    
                    // Set party filter if coming from party actions
                    LaunchedEffect(selectedPartyForTransactionFilter) {
                        selectedPartyForTransactionFilter?.let { party ->
                            transactionsListViewModel.setPartyFilter(party)
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.TransactionsListScreen(
                        viewModel = transactionsListViewModel,
                        onNavigateBack = { 
                            selectedPartyForTransactionFilter = null
                            navigateBack() 
                        },
                        onTransactionClick = { transaction ->
                            selectedTransactionSlug = transaction.slug
                            currentScreen = AppScreen.TRANSACTION_DETAIL
                        },
                        onAddTransactionClick = {
                            transactionType = null
                            currentScreen = AppScreen.ADD_TRANSACTION_STEP1
                        }
                    )
                }
                
                AppScreen.TRANSACTION_DETAIL -> {
                    selectedTransactionSlug?.let { slug ->
                        com.hisaabi.hisaabi_kmp.transactions.presentation.ui.TransactionDetailScreen(
                            viewModel = transactionDetailViewModel,
                            transactionSlug = slug,
                            onNavigateBack = { 
                                selectedTransactionSlug = null
                                currentScreen = AppScreen.TRANSACTIONS_LIST
                            }
                        )
                    } ?: run {
                        // If no transaction slug, go back to list
                        currentScreen = AppScreen.TRANSACTIONS_LIST
                    }
                }
                
                AppScreen.BALANCE_HISTORY -> {
                    selectedPartyForBalanceHistory?.let { party ->
                        // Fetch transactions for this party
                        val transactionsListViewModel: com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel = org.koin.compose.koinInject()
                        val transactionsState by transactionsListViewModel.state.collectAsState()
                        
                        // Set party filter to get transactions for this party
                        LaunchedEffect(party) {
                            transactionsListViewModel.setPartyFilter(party)
                        }
                        
                        com.hisaabi.hisaabi_kmp.parties.presentation.ui.BalanceHistoryScreen(
                            party = party,
                            transactions = transactionsState.transactions,
                            onNavigateBack = { 
                                selectedPartyForBalanceHistory = null
                                navigateBack() 
                            }
                        )
                    } ?: run {
                        // If no party selected, go back to parties
                        currentScreen = AppScreen.PARTIES
                    }
                }
                
                AppScreen.ADD_RECORD -> {
                    // Handle party selection for record
                    LaunchedEffect(selectedPartyForTransaction) {
                        selectedPartyForTransaction?.let { party ->
                            recordViewModel.selectParty(party)
                            selectedPartyForTransaction = null
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddRecordScreen(
                        viewModel = recordViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectParty = {
                            selectingPartyForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.ADD_RECORD
                            selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                            currentScreen = AppScreen.PARTIES
                        }
                    )
                }
                
                AppScreen.PAY_GET_CASH -> {
                    // Handle party selection for pay/get cash
                    LaunchedEffect(selectedPartyForTransaction) {
                        selectedPartyForTransaction?.let { party ->
                            payGetCashViewModel.selectParty(party)
                            selectedPartyForTransaction = null
                        }
                    }
                    
                    // Handle payment method selection
                    LaunchedEffect(selectedPaymentMethodForTransaction) {
                        selectedPaymentMethodForTransaction?.let { paymentMethod ->
                            payGetCashViewModel.selectPaymentMethod(paymentMethod)
                            selectedPaymentMethodForTransaction = null
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.PayGetCashScreen(
                        viewModel = payGetCashViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectParty = { partyType ->
                            selectingPartyForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.PAY_GET_CASH
                            selectedPartySegment = when (partyType) {
                                PartyType.CUSTOMER ->
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                                PartyType.VENDOR ->
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR
                                PartyType.INVESTOR ->
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.INVESTOR

                                else -> {
                                    com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                                }
                            }
                            currentScreen = AppScreen.PARTIES
                        },
                        onSelectPaymentMethod = {
                            selectingPaymentMethodForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.PAY_GET_CASH
                            currentScreen = AppScreen.PAYMENT_METHODS
                        }
                    )
                }
                
                AppScreen.ADD_EXPENSE_INCOME -> {
                    // Handle party selection for expense/income (these are expense/income types stored as parties with roleId 14 or 15)
                    LaunchedEffect(selectedPartyForTransaction) {
                        selectedPartyForTransaction?.let { party ->
                            expenseIncomeViewModel.selectParty(party)
                            selectedPartyForTransaction = null
                        }
                    }
                    
                    // Handle payment method selection
                    LaunchedEffect(selectedPaymentMethodForTransaction) {
                        selectedPaymentMethodForTransaction?.let { paymentMethod ->
                            expenseIncomeViewModel.selectPaymentMethod(paymentMethod)
                            selectedPaymentMethodForTransaction = null
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddExpenseIncomeScreen(
                        viewModel = expenseIncomeViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectParty = {
                            selectingPartyForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.ADD_EXPENSE_INCOME
                            isExpenseIncomePartySelection = true  // Set expense/income context
                            // Set initial segment based on transaction type
                            val state = expenseIncomeViewModel.state.value
                            selectedPartySegment = if (state.transactionType == com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.EXPENSE) {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXPENSE
                            } else {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXTRA_INCOME
                            }
                            currentScreen = AppScreen.PARTIES
                        },
                        onSelectPaymentMethod = {
                            selectingPaymentMethodForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.ADD_EXPENSE_INCOME
                            currentScreen = AppScreen.PAYMENT_METHODS
                        }
                    )
                }
                
                AppScreen.PAYMENT_TRANSFER -> {
                    // Handle payment method selection (From or To)
                    LaunchedEffect(selectedPaymentMethodForTransaction) {
                        selectedPaymentMethodForTransaction?.let { paymentMethod ->
                            if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.PAYMENT_TRANSFER) {
                                // Use the flag to determine which payment method to set
                                if (isSelectingPaymentMethodFrom) {
                                    paymentTransferViewModel.selectPaymentMethodFrom(paymentMethod)
                                } else {
                                    paymentTransferViewModel.selectPaymentMethodTo(paymentMethod)
                                }
                                // Clear all selection states
                                selectedPaymentMethodForTransaction = null
                                selectingPaymentMethodForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isSelectingPaymentMethodFrom = false
                            }
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.PaymentTransferScreen(
                        viewModel = paymentTransferViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectPaymentMethodFrom = {
                            selectingPaymentMethodForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.PAYMENT_TRANSFER
                            isSelectingPaymentMethodFrom = true  // Set flag for "From"
                            currentScreen = AppScreen.PAYMENT_METHODS
                        },
                        onSelectPaymentMethodTo = {
                            selectingPaymentMethodForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.PAYMENT_TRANSFER
                            isSelectingPaymentMethodFrom = false  // Set flag for "To"
                            currentScreen = AppScreen.PAYMENT_METHODS
                        }
                    )
                }
                
                AppScreen.JOURNAL_VOUCHER -> {
                    // Handle party selection for journal voucher
                    LaunchedEffect(selectedPartyForTransaction) {
                        selectedPartyForTransaction?.let { party ->
                            if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.JOURNAL_VOUCHER) {
                                journalVoucherViewModel.addParty(party)
                                selectedPartyForTransaction = null
                                selectingPartyForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isExpenseIncomePartySelection = false
                            }
                        }
                    }

                    // Handle payment method selection for journal voucher
                    LaunchedEffect(selectedPaymentMethodForTransaction) {
                        selectedPaymentMethodForTransaction?.let { paymentMethod ->
                            if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.JOURNAL_VOUCHER) {
                                // Check if it's for the voucher payment method or for adding as account
                                if (isSelectingPaymentMethodFrom) {
                                    // Adding payment method as an account
                                    journalVoucherViewModel.addPaymentMethod(paymentMethod)
                                } else {
                                    // Selecting payment method for the voucher
                                    journalVoucherViewModel.selectPaymentMethod(paymentMethod)
                                }
                                selectedPaymentMethodForTransaction = null
                                selectingPaymentMethodForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isSelectingPaymentMethodFrom = false
                            }
                        }
                    }

                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddJournalVoucherScreen(
                        viewModel = journalVoucherViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectAccountType = {
                            showJournalAccountTypeDialog = true
                        },
                        onSelectPaymentMethod = {
                            selectingPaymentMethodForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                            isSelectingPaymentMethodFrom = false // For voucher payment method
                            currentScreen = AppScreen.PAYMENT_METHODS
                        }
                    )

                    // Account Type Selection Dialog
                    if (showJournalAccountTypeDialog) {
                        com.hisaabi.hisaabi_kmp.transactions.presentation.ui.JournalAccountTypeDialog(
                            onDismiss = { showJournalAccountTypeDialog = false },
                            onSelectExpense = {
                                showJournalAccountTypeDialog = false
                                selectingPartyForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isExpenseIncomePartySelection = true
                                selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXPENSE
                                currentScreen = AppScreen.PARTIES
                            },
                            onSelectExtraIncome = {
                                showJournalAccountTypeDialog = false
                                selectingPartyForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isExpenseIncomePartySelection = true
                                selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.EXTRA_INCOME
                                currentScreen = AppScreen.PARTIES
                            },
                            onSelectCustomer = {
                                showJournalAccountTypeDialog = false
                                selectingPartyForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isExpenseIncomePartySelection = false
                                selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                                currentScreen = AppScreen.PARTIES
                            },
                            onSelectVendor = {
                                showJournalAccountTypeDialog = false
                                selectingPartyForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isExpenseIncomePartySelection = false
                                selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR
                                currentScreen = AppScreen.PARTIES
                            },
                            onSelectInvestor = {
                                showJournalAccountTypeDialog = false
                                selectingPartyForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isExpenseIncomePartySelection = false
                                selectedPartySegment = com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.INVESTOR
                                currentScreen = AppScreen.PARTIES
                            },
                            onSelectPaymentMethod = {
                                showJournalAccountTypeDialog = false
                                selectingPaymentMethodForTransaction = true
                                returnToScreenAfterPartySelection = AppScreen.JOURNAL_VOUCHER
                                isSelectingPaymentMethodFrom = true // For adding as account
                                currentScreen = AppScreen.PAYMENT_METHODS
                            }
                        )
                    }
                }
                
                AppScreen.STOCK_ADJUSTMENT -> {
                    // Handle warehouse selection for stock adjustment
                    LaunchedEffect(selectedWarehouseForTransaction) {
                        selectedWarehouseForTransaction?.let { warehouse ->
                            if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.STOCK_ADJUSTMENT) {
                                if (isSelectingWarehouseFrom) {
                                    stockAdjustmentViewModel.setWarehouseFrom(warehouse)
                                } else {
                                    stockAdjustmentViewModel.setWarehouseTo(warehouse)
                                }
                                selectedWarehouseForTransaction = null
                                selectingWarehouseForTransaction = false
                                returnToScreenAfterPartySelection = null
                                isSelectingWarehouseFrom = false
                            }
                        }
                    }

                    // Handle product selection for stock adjustment
                    LaunchedEffect(selectedProductsForTransaction) {
                        if (selectingProductsForTransaction && returnToScreenAfterProductSelection == AppScreen.STOCK_ADJUSTMENT) {
                            selectedProductsForTransaction.forEach { product ->
                                stockAdjustmentViewModel.addProduct(product)
                            }
                            selectedProductsForTransaction = emptyList()
                            selectingProductsForTransaction = false
                            returnToScreenAfterProductSelection = null
                        }
                    }

                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.StockAdjustmentScreen(
                        viewModel = stockAdjustmentViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectWarehouseFrom = {
                            selectingWarehouseForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.STOCK_ADJUSTMENT
                            isSelectingWarehouseFrom = true
                            currentScreen = AppScreen.WAREHOUSES
                        },
                        onSelectWarehouseTo = {
                            selectingWarehouseForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.STOCK_ADJUSTMENT
                            isSelectingWarehouseFrom = false
                            currentScreen = AppScreen.WAREHOUSES
                        },
                        onSelectProducts = {
                            selectingProductsForTransaction = true
                            returnToScreenAfterProductSelection = AppScreen.STOCK_ADJUSTMENT
                            currentScreen = AppScreen.PRODUCTS
                        }
                    )
                }
                
                AppScreen.ADD_MANUFACTURE -> {
                    // Handle warehouse selection for manufacture
                    LaunchedEffect(selectedWarehouseForTransaction) {
                        selectedWarehouseForTransaction?.let { warehouse ->
                            if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_MANUFACTURE) {
                                manufactureViewModel.selectWarehouse(warehouse)
                                selectedWarehouseForTransaction = null
                                selectingWarehouseForTransaction = false
                                returnToScreenAfterPartySelection = null
                            }
                        }
                    }

                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddManufactureScreen(
                        viewModel = manufactureViewModel,
                        onNavigateBack = { 
                            manufactureViewModel.resetState()
                            currentScreen = AppScreen.HOME 
                        },
                        onSelectWarehouse = {
                            selectingWarehouseForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.ADD_MANUFACTURE
                            currentScreen = AppScreen.WAREHOUSES
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
                    LaunchedEffect(selectedProductQuantitiesForTransaction.size, selectedBusinessSlug) {
                        val businessSlug = selectedBusinessSlug
                        if (selectedProductQuantitiesForTransaction.isNotEmpty() && businessSlug != null) {
                            // Fetch products by their slugs
                            val allProducts = productsRepository.getProducts(businessSlug)
                            val selectedSlugs = selectedProductQuantitiesForTransaction.keys.toSet()
                            val products = allProducts.filter { it.slug in selectedSlugs }
                            
                            products.forEach { product ->
                                // Get the quantity for this product
                                val quantity = selectedProductQuantitiesForTransaction[product.slug] ?: 1
                                // Get default unit for the product
                                val defaultUnit = null // TODO: Fetch from quantity units
                                // Add product with the specified quantity
                                transactionViewModel.addProduct(product, defaultUnit, quantity.toDouble())
                            }
                            selectedProductQuantitiesForTransaction = emptyMap() // Clear after adding
                        }
                    }
                    
                    com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddTransactionStep1Screen(
                        viewModel = transactionViewModel,
                        onNavigateBack = { navigateBack() },
                        onSelectParty = { 
                            // Determine party segment based on transaction type
                            val state = transactionViewModel.state.value
                            selectedPartySegment = if (com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes.isDealingWithVendor(state.transactionType.value)) {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.VENDOR
                            } else {
                                com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment.CUSTOMER
                            }
                            selectingPartyForTransaction = true
                            returnToScreenAfterPartySelection = AppScreen.ADD_TRANSACTION_STEP1
                            currentScreen = AppScreen.PARTIES
                        },
                        onSelectProducts = { 
                            selectingProductsForTransaction = true
                            returnToScreenAfterProductSelection = AppScreen.ADD_TRANSACTION_STEP1
                            currentScreen = AppScreen.PRODUCTS
                        },
                        onSelectWarehouse = { 
                            selectingWarehouseForTransaction = true
                            currentScreen = AppScreen.WAREHOUSES
                        },
                        onProceedToStep2 = { navigateTo(AppScreen.ADD_TRANSACTION_STEP2) }
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
                        onNavigateBack = { navigateBack() },
                        onSelectPaymentMethod = { 
                            selectingPaymentMethodForTransaction = true
                            currentScreen = AppScreen.PAYMENT_METHODS
                        },
                        onTransactionSaved = { navigateTo(AppScreen.HOME) }
                    )
                }
                }
            }
        }
    }
}

enum class AppScreen {
    HOME,
    AUTH,
    BUSINESS_SELECTION_GATE, // Gate screen to ensure business is selected
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
    ADD_RECORD,
    PAY_GET_CASH,
    ADD_EXPENSE_INCOME,
    PAYMENT_TRANSFER,
    JOURNAL_VOUCHER,
    STOCK_ADJUSTMENT,
    ADD_MANUFACTURE,
    ADD_TRANSACTION_STEP1,
    ADD_TRANSACTION_STEP2,
    TRANSACTION_DETAIL,
    BALANCE_HISTORY
}