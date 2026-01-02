package com.hisaabi.hisaabi_kmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hisaabi.hisaabi_kmp.auth.AuthNavigation
import com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel.AuthViewModel
import com.hisaabi.hisaabi_kmp.business.data.datasource.BusinessPreferencesDataSource
import com.hisaabi.hisaabi_kmp.business.data.repository.BusinessRepository
import com.hisaabi.hisaabi_kmp.business.domain.model.Business
import com.hisaabi.hisaabi_kmp.business.presentation.ui.AddBusinessScreen
import com.hisaabi.hisaabi_kmp.business.presentation.ui.BusinessSelectionGateScreen
import com.hisaabi.hisaabi_kmp.business.presentation.ui.MyBusinessScreen
import com.hisaabi.hisaabi_kmp.business.presentation.viewmodel.MyBusinessViewModel
import com.hisaabi.hisaabi_kmp.categories.domain.model.Category
import com.hisaabi.hisaabi_kmp.categories.domain.model.CategoryType
import com.hisaabi.hisaabi_kmp.categories.presentation.ui.AddCategoryScreen
import com.hisaabi.hisaabi_kmp.categories.presentation.ui.CategoriesScreen
import com.hisaabi.hisaabi_kmp.categories.presentation.viewmodel.CategoriesViewModel
import com.hisaabi.hisaabi_kmp.core.ui.BackHandler
import com.hisaabi.hisaabi_kmp.database.entity.CategoryEntity
import com.hisaabi.hisaabi_kmp.home.HomeScreen
import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartySegment
import com.hisaabi.hisaabi_kmp.parties.domain.model.PartyType
import com.hisaabi.hisaabi_kmp.parties.presentation.ui.AddPartyScreen
import com.hisaabi.hisaabi_kmp.parties.presentation.ui.BalanceHistoryScreen
import com.hisaabi.hisaabi_kmp.parties.presentation.ui.PartiesScreen
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod
import com.hisaabi.hisaabi_kmp.paymentmethods.presentation.ui.AddPaymentMethodScreen
import com.hisaabi.hisaabi_kmp.paymentmethods.presentation.ui.PaymentMethodsScreen
import com.hisaabi.hisaabi_kmp.products.data.repository.ProductsRepository
import com.hisaabi.hisaabi_kmp.products.domain.model.Product
import com.hisaabi.hisaabi_kmp.products.domain.model.ProductType
import com.hisaabi.hisaabi_kmp.products.presentation.ui.AddProductScreen
import com.hisaabi.hisaabi_kmp.products.presentation.ui.ManageRecipeIngredientsScreen
import com.hisaabi.hisaabi_kmp.products.presentation.ui.ProductsScreen
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.AddProductViewModel
import com.hisaabi.hisaabi_kmp.products.presentation.viewmodel.ProductsViewModel
import com.hisaabi.hisaabi_kmp.profile.presentation.ui.UpdateProfileScreen
import com.hisaabi.hisaabi_kmp.quantityunits.data.repository.QuantityUnitsRepository
import com.hisaabi.hisaabi_kmp.quantityunits.domain.model.QuantityUnit
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui.AddQuantityUnitScreen
import com.hisaabi.hisaabi_kmp.quantityunits.presentation.ui.QuantityUnitsScreen
import com.hisaabi.hisaabi_kmp.receipt.ReceiptPreviewDialog
import com.hisaabi.hisaabi_kmp.receipt.ReceiptViewModel
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportAdditionalFilter
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportFilters
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportFiltersFactory
import com.hisaabi.hisaabi_kmp.reports.domain.model.ReportType
import com.hisaabi.hisaabi_kmp.reports.domain.model.RequiredEntityType
import com.hisaabi.hisaabi_kmp.reports.presentation.ReportFiltersScreen
import com.hisaabi.hisaabi_kmp.reports.presentation.ReportResultScreen
import com.hisaabi.hisaabi_kmp.reports.presentation.ReportsScreen
import com.hisaabi.hisaabi_kmp.reports.presentation.viewmodel.ReportViewModel
import com.hisaabi.hisaabi_kmp.settings.data.PreferencesManager
import com.hisaabi.hisaabi_kmp.settings.domain.model.ReceiptConfig
import com.hisaabi.hisaabi_kmp.settings.presentation.ui.DashboardSettingsScreen
import com.hisaabi.hisaabi_kmp.settings.presentation.ui.ReceiptSettingsScreen
import com.hisaabi.hisaabi_kmp.settings.presentation.ui.TransactionTypeSelectionScreen
import com.hisaabi.hisaabi_kmp.sync.domain.manager.SyncManager
import com.hisaabi.hisaabi_kmp.templates.presentation.ui.AddTemplateScreen
import com.hisaabi.hisaabi_kmp.templates.presentation.ui.TemplatesScreen
import com.hisaabi.hisaabi_kmp.transactions.domain.model.AllTransactionTypes
import com.hisaabi.hisaabi_kmp.transactions.domain.model.Transaction
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddExpenseIncomeScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddJournalVoucherScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddManufactureScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddRecordScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddTransactionStep1Screen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.AddTransactionStep2Screen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.JournalAccountTypeDialog
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.PayGetCashScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.PaymentTransferScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.StockAdjustmentScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.TransactionDetailScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.ui.TransactionsListScreen
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddExpenseIncomeViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddJournalVoucherViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddManufactureViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddRecordViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.AddTransactionViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashType
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PayGetCashViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.PaymentTransferViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.StockAdjustmentViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionDetailViewModel
import com.hisaabi.hisaabi_kmp.transactions.presentation.viewmodel.TransactionsListViewModel
import com.hisaabi.hisaabi_kmp.warehouses.domain.model.Warehouse
import com.hisaabi.hisaabi_kmp.warehouses.presentation.ui.AddWarehouseScreen
import com.hisaabi.hisaabi_kmp.warehouses.presentation.ui.WarehousesScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.hisaabi.hisaabi_kmp.core.ui.ProvideWindowSizeClass
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import kotlin.system.exitProcess

@Composable
@Preview
fun App() {
    MaterialTheme {
        ProvideWindowSizeClass {
            KoinContext {
            // Check authentication state on app launch
            val authViewModel: AuthViewModel = koinInject()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val isInitialized by authViewModel.isInitialized.collectAsState()

            // Business preferences for checking selected business
            val businessPreferences: BusinessPreferencesDataSource = koinInject()
            var selectedBusinessSlug by remember { mutableStateOf<String?>(null) }

            // Business repository for fetching and caching businesses
            val businessRepository: BusinessRepository =
                koinInject()

            // Products repository for fetching products by slugs
            val productsRepository: ProductsRepository =
                koinInject()

            // Quantity units repository for fetching unit details
            val quantityUnitsRepository: QuantityUnitsRepository =
                koinInject()

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

            // Global toast/snackbar state for success messages
            val snackbarHostState = remember { SnackbarHostState() }
            var toastMessage by remember { mutableStateOf<String?>(null) }
            var snackbarMessage by remember { mutableStateOf<String?>(null) }

            // Show toast message
            LaunchedEffect(toastMessage) {
                toastMessage?.let { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                    toastMessage = null  // Clear after showing
                }
            }
            
            // Show snackbar message from transaction actions
            LaunchedEffect(snackbarMessage) {
                snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                    snackbarMessage = null  // Clear after showing
                }
            }

            // Receipt generation
            val receiptViewModel: ReceiptViewModel = koinInject()
            val receiptState by receiptViewModel.state.collectAsState()
            val preferencesManager: PreferencesManager = koinInject()
            val receiptConfig by preferencesManager.receiptConfig.collectAsState(initial = ReceiptConfig.DEFAULT)

            // Handle receipt errors (only show errors, not success messages)
            LaunchedEffect(receiptState.error) {
                receiptState.error?.let { error ->
                    toastMessage = error
                    receiptViewModel.clearError()
                }
            }

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
            var selectedPartySegment by remember {
                mutableStateOf<PartySegment?>(
                    null
                )
            }
            var addPartyType by remember {
                mutableStateOf<PartyType?>(
                    null
                )
            }
            var partiesRefreshTrigger by remember { mutableStateOf(0) }
            var addPartyScreenKey by remember { mutableStateOf(0) }  // Key to force reset AddPartyScreen
            val addPartySaveableStateHolder = rememberSaveableStateHolder()
            var selectedPartyForBalanceHistory by remember {
                mutableStateOf<Party?>(
                    null
                )
            }
            var selectedPartyForTransactionFilter by remember {
                mutableStateOf<Party?>(
                    null
                )
            }
            var selectingPartyForFilter by remember { mutableStateOf(false) }
            var selectingAreaForFilter by remember { mutableStateOf(false) }
            var selectingCategoryForFilter by remember { mutableStateOf(false) }
            var selectedAreaForFilter by remember { mutableStateOf<CategoryEntity?>(null) }
            var selectedCategoryForFilter by remember { mutableStateOf<CategoryEntity?>(null) }
            var selectedPartyForEdit by remember {
                mutableStateOf<Party?>(
                    null
                )
            }

            // Reports navigation state
            var selectedReportType by remember {
                mutableStateOf<ReportType?>(
                    null
                )
            }
            var selectingEntityForReport by remember { mutableStateOf(false) }
            var selectedEntityForReport by remember {
                mutableStateOf<Any?>(null) // Can be Party, Product, Warehouse
            }
            var selectedReportFilters by remember {
                mutableStateOf<ReportFilters?>(null)
            }
            val reportViewModel: ReportViewModel =
                koinInject()

            // Category navigation state
            var categoryType by remember {
                mutableStateOf<CategoryType?>(
                    null
                )
            }
            var editingCategorySlug by remember {
                mutableStateOf<String?>(null)
            }
            var categoriesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedCategoryForParty by remember {
                mutableStateOf<CategoryEntity?>(
                    null
                )
            }
            var selectedAreaForParty by remember {
                mutableStateOf<CategoryEntity?>(
                    null
                )
            }
            var returnToAddParty by remember { mutableStateOf(false) }
            var selectedCategoryForProduct by remember {
                mutableStateOf<Category?>(
                    null
                )
            }
            var returnToAddProduct by remember { mutableStateOf(false) }

            // Products navigation state
            var addProductType by remember {
                mutableStateOf<ProductType?>(
                    null
                )
            }
            var addProductScreenKey by remember { mutableStateOf(0) }
            var addProductViewModelHolder by remember { mutableStateOf<AddProductViewModel?>(null) }
            var productsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedRecipeProduct by remember {
                mutableStateOf<Product?>(
                    null
                )
            }
            var selectedProductForEdit by remember {
                mutableStateOf<Product?>(
                    null
                )
            }

            // Reset Add Product ViewModel when session changes or ends
            LaunchedEffect(addProductScreenKey) {
                addProductViewModelHolder = null
            }
            LaunchedEffect(addProductType) {
                if (addProductType == null) {
                    addProductViewModelHolder = null
                }
            }

            // Payment Methods navigation state
            var paymentMethodsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedPaymentMethodForEdit by remember {
                mutableStateOf<PaymentMethod?>(
                    null
                )
            }

            // Warehouses navigation state
            var warehousesRefreshTrigger by remember { mutableStateOf(0) }
            var selectedWarehouseForEdit by remember {
                mutableStateOf<Warehouse?>(
                    null
                )
            }

            // Business navigation state
            var businessRefreshTrigger by remember { mutableStateOf(0) }
            var selectedBusinessForEdit by remember {
                mutableStateOf<Business?>(
                    null
                )
            }

            // Quantity Units navigation state
            var quantityUnitsRefreshTrigger by remember { mutableStateOf(0) }
            var selectedUnitForEdit by remember {
                mutableStateOf<QuantityUnit?>(
                    null
                )
            }
            var isAddingParentUnitType by remember { mutableStateOf(false) }
            var selectedParentUnitForChildUnit by remember {
                mutableStateOf<QuantityUnit?>(
                    null
                )
            }
            var selectedParentUnitSlugToRestore by remember { mutableStateOf<String?>(null) }

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
            var transactionType by remember {
                mutableStateOf<AllTransactionTypes?>(
                    null
                )
            }
            var initialExpenseIncomeType by remember {
                mutableStateOf<AllTransactionTypes?>(
                    null
                )
            }
            var selectingPartyForTransaction by remember { mutableStateOf(false) }
            var selectedPartyForTransaction by remember {
                mutableStateOf<Party?>(
                    null
                )
            }
            var returnToScreenAfterPartySelection by remember { mutableStateOf<AppScreen?>(null) }
            var isExpenseIncomePartySelection by remember { mutableStateOf(false) }  // Flag for expense/income context
            var isSelectingPaymentMethodFrom by remember { mutableStateOf(false) }  // Flag for payment transfer From/To
            var selectingWarehouseForTransaction by remember { mutableStateOf(false) }
            var selectedWarehouseForTransaction by remember {
                mutableStateOf<Warehouse?>(
                    null
                )
            }
            var selectingProductsForTransaction by remember { mutableStateOf(false) }
            var selectedProductQuantitiesForTransaction by remember {
                mutableStateOf<Map<String, Int>>(
                    emptyMap()
                )
            }
            var returnToScreenAfterProductSelection by remember { mutableStateOf<AppScreen?>(null) }
            var selectingPaymentMethodForTransaction by remember { mutableStateOf(false) }
            var selectedPaymentMethodForTransaction by remember {
                mutableStateOf<PaymentMethod?>(
                    null
                )
            }
            var selectedTransactionSlug by remember { mutableStateOf<String?>(null) }
            var selectedTransactionSlugForEdit by remember { mutableStateOf<String?>(null) }
            var clonedTransactionForEdit by remember { mutableStateOf<Transaction?>(null) }
            
            // Transaction action dialogs state
            var showDeleteConfirmationDialog by remember { mutableStateOf<Transaction?>(null) }
            var showCloneDialog by remember { mutableStateOf<Transaction?>(null) }

            // Track if we're in a transaction creation flow (including selection screens)
            var isInTransactionFlow by remember { mutableStateOf(false) }
            var isInPayGetCashFlow by remember { mutableStateOf(false) }
            var isInAddRecordFlow by remember { mutableStateOf(false) }
            var isInExpenseIncomeFlow by remember { mutableStateOf(false) }
            var isInPaymentTransferFlow by remember { mutableStateOf(false) }
            var isInJournalVoucherFlow by remember { mutableStateOf(false) }
            var isInStockAdjustmentFlow by remember { mutableStateOf(false) }
            var isInManufactureFlow by remember { mutableStateOf(false) }
            var isInTransactionsListFlow by remember { mutableStateOf(false) }

            // Update flow state based on current screen
            LaunchedEffect(
                currentScreen,
                returnToScreenAfterPartySelection,
                returnToScreenAfterProductSelection
            ) {
                // Reset all flows first
                val previousTransactionFlow = isInTransactionFlow
                val previousPayGetCashFlow = isInPayGetCashFlow
                val previousAddRecordFlow = isInAddRecordFlow
                val previousExpenseIncomeFlow = isInExpenseIncomeFlow
                val previousPaymentTransferFlow = isInPaymentTransferFlow
                val previousJournalVoucherFlow = isInJournalVoucherFlow
                val previousStockAdjustmentFlow = isInStockAdjustmentFlow
                val previousManufactureFlow = isInManufactureFlow

                when (currentScreen) {
                    AppScreen.ADD_TRANSACTION_STEP1, AppScreen.ADD_TRANSACTION_STEP2 -> {
                        isInTransactionFlow = true
                    }

                    AppScreen.PAY_GET_CASH -> {
                        isInPayGetCashFlow = true
                    }

                    AppScreen.ADD_RECORD -> {
                        isInAddRecordFlow = true
                    }

                    AppScreen.ADD_EXPENSE_INCOME -> {
                        isInExpenseIncomeFlow = true
                    }

                    AppScreen.PAYMENT_TRANSFER -> {
                        isInPaymentTransferFlow = true
                    }

                    AppScreen.JOURNAL_VOUCHER -> {
                        isInJournalVoucherFlow = true
                    }

                    AppScreen.STOCK_ADJUSTMENT -> {
                        isInStockAdjustmentFlow = true
                    }

                    AppScreen.ADD_MANUFACTURE -> {
                        isInManufactureFlow = true
                    }

                    AppScreen.TRANSACTIONS_LIST, AppScreen.TRANSACTION_DETAIL -> {
                        isInTransactionsListFlow = true
                    }

                    AppScreen.HOME -> {
                        // Explicitly exiting all flows
                        isInTransactionFlow = false
                        isInPayGetCashFlow = false
                        isInAddRecordFlow = false
                        isInExpenseIncomeFlow = false
                        isInPaymentTransferFlow = false
                        isInJournalVoucherFlow = false
                        isInStockAdjustmentFlow = false
                        isInManufactureFlow = false
                        isInTransactionsListFlow = false
                    }
                    // Keep flows active when selecting resources
                    AppScreen.PARTIES -> {
                        when (returnToScreenAfterPartySelection) {
                            AppScreen.ADD_TRANSACTION_STEP1 -> isInTransactionFlow =
                                previousTransactionFlow

                            AppScreen.PAY_GET_CASH -> isInPayGetCashFlow = previousPayGetCashFlow
                            AppScreen.ADD_RECORD -> isInAddRecordFlow = previousAddRecordFlow
                            AppScreen.ADD_EXPENSE_INCOME -> isInExpenseIncomeFlow =
                                previousExpenseIncomeFlow

                            AppScreen.JOURNAL_VOUCHER -> isInJournalVoucherFlow =
                                previousJournalVoucherFlow

                            else -> {
                                // Exit all flows if not returning to any known screen
                                isInTransactionFlow = false
                                isInPayGetCashFlow = false
                                isInAddRecordFlow = false
                                isInExpenseIncomeFlow = false
                                isInPaymentTransferFlow = false
                                isInJournalVoucherFlow = false
                                isInStockAdjustmentFlow = false
                                isInManufactureFlow = false
                                isInTransactionsListFlow = false
                            }
                        }
                    }

                    AppScreen.WAREHOUSES -> {
                        when (returnToScreenAfterPartySelection) {
                            AppScreen.ADD_TRANSACTION_STEP1 -> isInTransactionFlow =
                                previousTransactionFlow

                            AppScreen.STOCK_ADJUSTMENT -> isInStockAdjustmentFlow =
                                previousStockAdjustmentFlow

                            AppScreen.ADD_MANUFACTURE -> isInManufactureFlow =
                                previousManufactureFlow

                            else -> {
                                isInTransactionFlow = false
                                isInPayGetCashFlow = false
                                isInAddRecordFlow = false
                                isInExpenseIncomeFlow = false
                                isInPaymentTransferFlow = false
                                isInJournalVoucherFlow = false
                                isInStockAdjustmentFlow = false
                                isInManufactureFlow = false
                                isInTransactionsListFlow = false
                            }
                        }
                    }

                    AppScreen.PRODUCTS -> {
                        when (returnToScreenAfterProductSelection) {
                            AppScreen.ADD_TRANSACTION_STEP1 -> isInTransactionFlow =
                                previousTransactionFlow

                            AppScreen.STOCK_ADJUSTMENT -> isInStockAdjustmentFlow =
                                previousStockAdjustmentFlow

                            else -> {
                                isInTransactionFlow = false
                                isInPayGetCashFlow = false
                                isInAddRecordFlow = false
                                isInExpenseIncomeFlow = false
                                isInPaymentTransferFlow = false
                                isInJournalVoucherFlow = false
                                isInStockAdjustmentFlow = false
                                isInManufactureFlow = false
                                isInTransactionsListFlow = false
                            }
                        }
                    }

                    AppScreen.PAYMENT_METHODS -> {
                        when (returnToScreenAfterPartySelection) {
                            AppScreen.ADD_TRANSACTION_STEP1, // Desktop combined screen
                            AppScreen.ADD_TRANSACTION_STEP2 -> isInTransactionFlow =
                                previousTransactionFlow

                            AppScreen.PAY_GET_CASH -> isInPayGetCashFlow = previousPayGetCashFlow
                            AppScreen.ADD_EXPENSE_INCOME -> isInExpenseIncomeFlow =
                                previousExpenseIncomeFlow

                            AppScreen.PAYMENT_TRANSFER -> isInPaymentTransferFlow =
                                previousPaymentTransferFlow

                            AppScreen.JOURNAL_VOUCHER -> isInJournalVoucherFlow =
                                previousJournalVoucherFlow

                            else -> {
                                isInTransactionFlow = false
                                isInPayGetCashFlow = false
                                isInAddRecordFlow = false
                                isInExpenseIncomeFlow = false
                                isInPaymentTransferFlow = false
                                isInJournalVoucherFlow = false
                                isInStockAdjustmentFlow = false
                                isInManufactureFlow = false
                                isInTransactionsListFlow = false
                            }
                        }
                    }

                    else -> {
                        // Any other screen exits all flows except transactions list flow
                        isInTransactionFlow = false
                        isInPayGetCashFlow = false
                        isInAddRecordFlow = false
                        isInExpenseIncomeFlow = false
                        isInPaymentTransferFlow = false
                        isInJournalVoucherFlow = false
                        isInStockAdjustmentFlow = false
                        isInManufactureFlow = false
                        // Keep isInTransactionsListFlow as is - don't reset it here
                    }
                }
            }

            // Create ViewModels for each flow - will be disposed when navigating away from their respective flows
            // Use rememberSaveable pattern to preserve ViewModel across navigation within the same flow
            var rememberedTransactionViewModel by remember { mutableStateOf<AddTransactionViewModel?>(null) }
            
            // Only create a new ViewModel when entering the flow (not on every recomposition)
            if (isInTransactionFlow && rememberedTransactionViewModel == null) {
                rememberedTransactionViewModel = koinInject()
            } else if (!isInTransactionFlow && rememberedTransactionViewModel != null) {
                rememberedTransactionViewModel = null
            }
            
            val transactionViewModel = rememberedTransactionViewModel

            val payGetCashViewModel: PayGetCashViewModel? =
                if (isInPayGetCashFlow) {
                    koinInject()
                } else {
                    null
                }

            val recordViewModel: AddRecordViewModel? =
                if (isInAddRecordFlow) {
                    koinInject()
                } else {
                    null
                }

            val expenseIncomeViewModel: AddExpenseIncomeViewModel? =
                if (isInExpenseIncomeFlow) {
                    koinInject()
                } else {
                    null
                }

            val paymentTransferViewModel: PaymentTransferViewModel? =
                if (isInPaymentTransferFlow) {
                    koinInject()
                } else {
                    null
                }

            val journalVoucherViewModel: AddJournalVoucherViewModel? =
                if (isInJournalVoucherFlow) {
                    koinInject()
                } else {
                    null
                }

            val stockAdjustmentViewModel: StockAdjustmentViewModel? =
                if (isInStockAdjustmentFlow) {
                    koinInject()
                } else {
                    null
                }

            val manufactureViewModel: AddManufactureViewModel? =
                if (isInManufactureFlow) {
                    koinInject()
                } else {
                    null
                }

            // Compute whether we're in transactions list flow based on current screen
            // This ensures ViewModel is created immediately when navigating to transactions screens
            val shouldShowTransactionsListFlow = currentScreen == AppScreen.TRANSACTIONS_LIST ||
                    currentScreen == AppScreen.TRANSACTION_DETAIL ||
                    isInTransactionsListFlow

            val transactionsListViewModel: TransactionsListViewModel? =
                if (shouldShowTransactionsListFlow) {
                    koinInject()
                } else {
                    null
                }

            // Journal Voucher state
            var showJournalAccountTypeDialog by remember { mutableStateOf(false) }

            // Stock Adjustment state
            var isSelectingWarehouseFrom by remember { mutableStateOf(false) }

            // Show splash screen while checking auth state
            if (currentScreen == null) {
                SplashScreen()
            } else {
                // Wrap content with Box to show snackbar overlay
                Box(modifier = Modifier.fillMaxSize()) {
                    // Show content only when currentScreen is initialized
                    when (currentScreen!!) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                selectedTab = selectedBottomNavTab,
                                onTabSelected = { selectedBottomNavTab = it },
                                onNavigateToAuth = { navigateTo(AppScreen.AUTH) },
                                onNavigateToQuantityUnits = { navigateTo(AppScreen.QUANTITY_UNITS) },
                                onNavigateToCategories = { navigateTo(AppScreen.CATEGORIES) },
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
                                    addProductScreenKey++
                                    navigateTo(AppScreen.ADD_PRODUCT)
                                },
                                onNavigateToPaymentMethods = { navigateTo(AppScreen.PAYMENT_METHODS) },
                                onNavigateToWarehouses = { navigateTo(AppScreen.WAREHOUSES) },
                                onNavigateToMyBusiness = { navigateTo(AppScreen.MY_BUSINESS) },
                                onNavigateToTransactions = { navigateTo(AppScreen.TRANSACTIONS_LIST) },
                                onNavigateToAddRecord = { navigateTo(AppScreen.ADD_RECORD) },
                                onNavigateToPayGetCash = { navigateTo(AppScreen.PAY_GET_CASH) },
                                onNavigateToExpense = {
                                    initialExpenseIncomeType =
                                        AllTransactionTypes.EXPENSE
                                    navigateTo(AppScreen.ADD_EXPENSE_INCOME)
                                },
                                onNavigateToExtraIncome = {
                                    initialExpenseIncomeType =
                                        AllTransactionTypes.EXTRA_INCOME
                                    navigateTo(AppScreen.ADD_EXPENSE_INCOME)
                                },
                                onNavigateToPaymentTransfer = { navigateTo(AppScreen.PAYMENT_TRANSFER) },
                                onNavigateToJournalVoucher = { navigateTo(AppScreen.JOURNAL_VOUCHER) },
                                onNavigateToStockAdjustment = { navigateTo(AppScreen.STOCK_ADJUSTMENT) },
                                onNavigateToManufacture = {
                                    selectedTransactionSlugForEdit = null // Clear any edit state
                                    navigateTo(AppScreen.ADD_MANUFACTURE)
                                },
                                onNavigateToAddTransaction = { type ->
                                    transactionType = type
                                    navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                },
                                onNavigateToReports = { navigateTo(AppScreen.REPORTS) },
                                onReportTypeSelected = { reportType ->
                                    selectedReportType = reportType
                                    navigateTo(AppScreen.REPORT_FILTERS)
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
                            val myBusinessViewModel: MyBusinessViewModel =
                                koinInject()
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
                            // Set initial segment for report entity selection
                            if (selectingEntityForReport) {
                                val entityType = selectedReportFilters?.getRequiredEntityType()
                                when (entityType) {
                                    RequiredEntityType.CUSTOMER -> selectedPartySegment = PartySegment.CUSTOMER
                                    RequiredEntityType.VENDOR -> selectedPartySegment = PartySegment.VENDOR
                                    RequiredEntityType.INVESTOR -> selectedPartySegment = PartySegment.INVESTOR
                                    else -> {}
                                }
                            }
                            
                            PartiesScreen(
                                viewModel = koinInject(),
                                onPartyClick = { party ->
                                    if (selectingEntityForReport) {
                                        // Store selected entity for report and navigate to filters
                                        selectedEntityForReport = party
                                        selectingEntityForReport = false
                                        // Update filters with selected entity
                                        val filters = selectedReportFilters?.copy(
                                            selectedPartyId = party.slug,
                                            selectedInvestorId = if (party.roleId == PartyType.INVESTOR.type) party.slug else null
                                        ) ?: ReportFilters(reportType = selectedReportType)
                                        selectedReportFilters = filters
                                        navigateTo(AppScreen.REPORT_FILTERS)
                                    } else if (selectingPartyForTransaction) {
                                        // Store selected party and return to transaction or record
                                        selectedPartyForTransaction = party
                                        // Don't reset flags here - let the target screen handle it
                                        // Return to the appropriate screen
                                        currentScreen = returnToScreenAfterPartySelection
                                            ?: AppScreen.ADD_TRANSACTION_STEP1
                                    } else if (selectingPartyForFilter) {
                                        // Set the selected party for filter and navigate back to transactions list
                                        selectedPartyForTransactionFilter = party
                                        selectingPartyForFilter = false
                                        currentScreen = AppScreen.TRANSACTIONS_LIST
                                    } else {
                                        // Party click handled by bottom sheet
                                    }
                                },
                                onAddPartyClick = {
                                    // Determine party type based on current segment
                                    addPartyType = when (selectedPartySegment) {
                                        PartySegment.CUSTOMER ->
                                            PartyType.CUSTOMER

                                        PartySegment.VENDOR ->
                                            PartyType.VENDOR

                                        PartySegment.INVESTOR ->
                                            PartyType.INVESTOR

                                        PartySegment.EXPENSE ->
                                            PartyType.EXPENSE

                                        PartySegment.EXTRA_INCOME ->
                                            PartyType.EXTRA_INCOME

                                        else -> PartyType.CUSTOMER
                                    }
                                    addPartyScreenKey++  // Increment key to force reset
                                    currentScreen = AppScreen.ADD_PARTY
                                },
                                onNavigateBack = {
                                    if (selectingEntityForReport) {
                                        // User cancelled entity selection - go back to reports
                                        selectingEntityForReport = false
                                        selectedEntityForReport = null
                                        selectedReportFilters = null
                                        selectedReportType = null
                                        currentScreen = AppScreen.REPORTS
                                    } else if (selectingPartyForTransaction) {
                                        // User cancelled party selection - navigate back to the screen they came from
                                        val targetScreen =
                                            returnToScreenAfterPartySelection ?: AppScreen.HOME
                                        selectingPartyForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                        isExpenseIncomePartySelection = false
                                        currentScreen = targetScreen
                                    } else if (selectingPartyForFilter) {
                                        // User cancelled party filter selection - navigate back to transactions list
                                        selectingPartyForFilter = false
                                        currentScreen = AppScreen.TRANSACTIONS_LIST
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
                                    addPartyType =
                                        PartyType.fromInt(
                                            party.roleId
                                        )
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
                                    val txType =
                                        AllTransactionTypes.fromValue(
                                            transactionTypeValue
                                        )

                                    // Navigate to the appropriate transaction screen
                                    when (txType) {
                                        AllTransactionTypes.SALE,
                                        AllTransactionTypes.PURCHASE,
                                        AllTransactionTypes.CUSTOMER_RETURN,
                                        AllTransactionTypes.VENDOR_RETURN -> {
                                            transactionViewModel?.reset()
                                            transactionViewModel?.setTransactionType(txType!!)
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
                                val addPartyProviderKey = "AddPartyScreen-$addPartyScreenKey"
                                addPartySaveableStateHolder.SaveableStateProvider(
                                    addPartyProviderKey
                                ) {
                                    // Use key to force recomposition when navigating to edit
                                    key(addPartyScreenKey) {
                                        AddPartyScreen(
                                            viewModel = koinInject(),
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
                                                categoryType =
                                                    CategoryType.CUSTOMER_CATEGORY
                                                returnToAddParty = true
                                                currentScreen = AppScreen.CATEGORIES
                                            },
                                            onNavigateToAreas = {
                                                categoryType =
                                                    CategoryType.AREA
                                                returnToAddParty = true
                                                currentScreen = AppScreen.CATEGORIES
                                            },
                                            selectedCategoryFromNav = selectedCategoryForParty,
                                            selectedAreaFromNav = selectedAreaForParty
                                        )
                                    }
                                }
                            }
                        }

                        AppScreen.CATEGORIES -> {
                            val categoriesViewModel: CategoriesViewModel =
                                koinInject()
                            CategoriesScreen(
                                viewModel = categoriesViewModel,
                                categoryType = categoryType, // Optional - will default to CUSTOMER_CATEGORY if null
                                onCategorySelected = { category ->
                                    // Handle category selection for filter
                                    if (selectingAreaForFilter && categoryType == CategoryType.AREA) {
                                        category?.let {
                                            selectedAreaForFilter = CategoryEntity(
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
                                        selectingAreaForFilter = false
                                        currentScreen = AppScreen.TRANSACTIONS_LIST
                                    } else if (selectingCategoryForFilter && categoryType == CategoryType.CUSTOMER_CATEGORY) {
                                        category?.let {
                                            selectedCategoryForFilter = CategoryEntity(
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
                                        selectingCategoryForFilter = false
                                        currentScreen = AppScreen.TRANSACTIONS_LIST
                                    }
                                    // Handle category selection for AddProduct flow
                                    else if (returnToAddProduct && categoryType == CategoryType.PRODUCTS) {
                                        selectedCategoryForProduct = category
                                        currentScreen = AppScreen.ADD_PRODUCT
                                    }
                                    // Handle category selection for AddParty flow
                                    else if (returnToAddParty && categoryType != null) {
                                        val type = categoryType
                                        // Store selected category based on type
                                        if (type == CategoryType.CUSTOMER_CATEGORY) {
                                            category?.let {
                                                selectedCategoryForParty =
                                                    CategoryEntity(
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
                                        } else if (type == CategoryType.AREA) {
                                            category?.let {
                                                selectedAreaForParty =
                                                    CategoryEntity(
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
                                    }
                                    // If not from AddParty or AddProduct flow, category selection is just for viewing
                                },
                                onAddCategoryClick = {
                                    // Get current selected category type from viewModel state
                                    val currentType =
                                        categoriesViewModel.uiState.value.selectedCategoryType
                                            ?: categoryType
                                            ?: CategoryType.CUSTOMER_CATEGORY
                                    categoryType = currentType
                                    currentScreen = AppScreen.ADD_CATEGORY
                                },
                                onNavigateBack = {
                                    if (selectingAreaForFilter || selectingCategoryForFilter) {
                                        selectingAreaForFilter = false
                                        selectingCategoryForFilter = false
                                        currentScreen = AppScreen.TRANSACTIONS_LIST
                                    } else if (returnToAddProduct) {
                                        currentScreen = AppScreen.ADD_PRODUCT
                                    } else if (returnToAddParty) {
                                        currentScreen = AppScreen.ADD_PARTY
                                    } else {
                                        navigateBack()
                                    }
                                },
                                refreshTrigger = categoriesRefreshTrigger,
                                onEditCategoryClick = { category ->
                                    // Get category type from the category
                                    val type = CategoryType.fromInt(category.typeId)
                                        ?: CategoryType.CUSTOMER_CATEGORY
                                    categoryType = type
                                    editingCategorySlug = category.slug
                                    currentScreen = AppScreen.ADD_CATEGORY
                                }
                            )
                        }

                        AppScreen.ADD_CATEGORY -> {
                            categoryType?.let { type ->
                                AddCategoryScreen(
                                    viewModel = koinInject(),
                                    categoryType = type,
                                    onNavigateBack = {
                                        categoriesRefreshTrigger++  // Trigger refresh
                                        editingCategorySlug = null  // Clear editing state
                                        currentScreen = AppScreen.CATEGORIES
                                    },
                                    editingCategorySlug = editingCategorySlug
                                )
                            }
                        }

                        AppScreen.PRODUCTS -> {
                            // Clear the product type after using it to avoid persistence
                            val productsViewModel = koinInject<ProductsViewModel>()
                            val currentProductType = addProductType
                            LaunchedEffect(Unit) {
                                addProductType = null
                            }

                            // Preselect warehouse when opening Products screen from transaction screens
                            LaunchedEffect(returnToScreenAfterProductSelection) {
                                when (returnToScreenAfterProductSelection) {
                                    AppScreen.ADD_TRANSACTION_STEP1 -> {
                                        // Get warehouse from AddTransactionViewModel
                                        transactionViewModel?.let { viewModel ->
                                            val warehouse = viewModel.state.value.selectedWarehouse
                                            if (warehouse != null) {
                                                productsViewModel.selectWarehouse(warehouse)
                                            }
                                        }
                                    }

                                    AppScreen.STOCK_ADJUSTMENT -> {
                                        // Get warehouseFrom from StockAdjustmentViewModel
                                        stockAdjustmentViewModel?.let { viewModel ->
                                            val warehouse = viewModel.state.value.warehouseFrom
                                            if (warehouse != null) {
                                                productsViewModel.selectWarehouse(warehouse)
                                            }
                                        }
                                    }

                                    AppScreen.ADD_MANUFACTURE -> {
                                        // Get selectedWarehouse from AddManufactureViewModel
                                        manufactureViewModel?.let { viewModel ->
                                            val warehouse = viewModel.state.value.selectedWarehouse
                                            if (warehouse != null) {
                                                productsViewModel.selectWarehouse(warehouse)
                                            }
                                        }
                                    }

                                    else -> {
                                        // No warehouse preselection needed
                                    }
                                }
                            }

                            ProductsScreen(
                                viewModel = productsViewModel,
                                isSingleSelectionMode = selectingEntityForReport, // Enable single selection mode for report entity selection
                                onProductClick = { product ->
                                    if (selectingEntityForReport) {
                                        // Store selected product for report and navigate to filters
                                        selectedEntityForReport = product
                                        selectingEntityForReport = false
                                        // Update filters with selected product
                                        val filters = selectedReportFilters?.copy(
                                            selectedProductId = product.slug
                                        ) ?: ReportFilters(reportType = selectedReportType)
                                        selectedReportFilters = filters
                                        navigateTo(AppScreen.REPORT_FILTERS)
                                    } else if (!selectingProductsForTransaction) {
                                        // Navigate to edit product screen (reuse ADD_PRODUCT with edit mode)
                                        selectedProductForEdit = product
                                        addProductType = product.productType
                                        addProductScreenKey++
                                        currentScreen = AppScreen.ADD_PRODUCT
                                    }
                                },
                                onEditProductClick = { product ->
                                    // Navigate to edit product screen (reuse ADD_PRODUCT with edit mode)
                                    selectedProductForEdit = product
                                    addProductType = product.productType
                                    addProductScreenKey++
                                    currentScreen = AppScreen.ADD_PRODUCT
                                },
                                onAddProductClick = { selectedType ->
                                    // Use the selected product type, or default to simple product if none selected
                                    addProductType = selectedType
                                        ?: ProductType.SIMPLE_PRODUCT
                                    selectedProductForEdit =
                                        null // Clear any selected product for editing
                                    addProductScreenKey++
                                    currentScreen = AppScreen.ADD_PRODUCT
                                },
                                onNavigateToIngredients = { product ->
                                    selectedRecipeProduct = product
                                    currentScreen = AppScreen.MANAGE_RECIPE_INGREDIENTS
                                },
                                onNavigateBack = {
                                    if (selectingEntityForReport) {
                                        // User cancelled entity selection - go back to reports
                                        selectingEntityForReport = false
                                        selectedEntityForReport = null
                                        selectedReportFilters = null
                                        selectedReportType = null
                                        currentScreen = AppScreen.REPORTS
                                    } else if (selectingProductsForTransaction) {
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
                                    // When Done is clicked in selection mode, navigate back
                                    // The LaunchedEffect in ADD_TRANSACTION_STEP1 will handle adding products and clearing flags
                                    currentScreen = returnToScreenAfterProductSelection
                                        ?: AppScreen.ADD_TRANSACTION_STEP1
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
                            val addProductViewModel = addProductViewModelHolder
                                ?: koinInject<AddProductViewModel>().also {
                                    addProductViewModelHolder = it
                                }
                            addProductType?.let { type ->
                                val productToEdit = selectedProductForEdit
                                AddProductScreen(
                                    viewModel = addProductViewModel,
                                    productType = type,
                                    productToEdit = productToEdit,
                                    formSessionKey = addProductScreenKey,
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
                                    },
                                    onNavigateToCategories = {
                                        categoryType =
                                            CategoryType.PRODUCTS
                                        returnToAddProduct = true
                                        currentScreen = AppScreen.CATEGORIES
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

                                // Handle category selection for Add Product screen
                                LaunchedEffect(selectedCategoryForProduct) {
                                    selectedCategoryForProduct?.let { category ->
                                        if (returnToAddProduct) {
                                            addProductViewModel.setSelectedCategory(category)
                                            selectedCategoryForProduct = null
                                            returnToAddProduct = false
                                            categoryType = null
                                        }
                                    }
                                }
                            }
                        }

                        AppScreen.MANAGE_RECIPE_INGREDIENTS -> {
                            selectedRecipeProduct?.let { recipe ->
                                ManageRecipeIngredientsScreen(
                                    recipeProduct = recipe,
                                    onNavigateBack = {
                                        currentScreen = AppScreen.PRODUCTS
                                    }
                                )
                            }
                        }

                        AppScreen.PAYMENT_METHODS -> {
                            PaymentMethodsScreen(
                                viewModel = koinInject(),
                                onPaymentMethodClick = { paymentMethod ->
                                    if (selectingPaymentMethodForTransaction) {
                                        // Store selected payment method and return to appropriate screen
                                        selectedPaymentMethodForTransaction = paymentMethod
                                        // Don't reset selectingPaymentMethodForTransaction here - let the target screen handle it
                                        currentScreen = returnToScreenAfterPartySelection
                                            ?: AppScreen.ADD_TRANSACTION_STEP2
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
                                        val targetScreen = returnToScreenAfterPartySelection
                                            ?: AppScreen.ADD_TRANSACTION_STEP2
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
                            AddPaymentMethodScreen(
                                viewModel = koinInject(),
                                paymentMethodToEdit = selectedPaymentMethodForEdit,
                                onNavigateBack = {
                                    // Trigger refresh and navigate
                                    paymentMethodsRefreshTrigger++
                                    currentScreen = AppScreen.PAYMENT_METHODS
                                    // Clear edit state after navigation
                                    selectedPaymentMethodForEdit = null
                                }
                            )
                        }

                        AppScreen.WAREHOUSES -> {
                            WarehousesScreen(
                                viewModel = koinInject(),
                                onWarehouseClick = { warehouse ->
                                    if (selectingEntityForReport) {
                                        // Store selected warehouse for report and navigate to filters
                                        selectedEntityForReport = warehouse
                                        selectingEntityForReport = false
                                        // Update filters with selected warehouse
                                        val filters = selectedReportFilters?.copy(
                                            selectedWarehouseId = warehouse.slug
                                        ) ?: ReportFilters(reportType = selectedReportType)
                                        selectedReportFilters = filters
                                        navigateTo(AppScreen.REPORT_FILTERS)
                                    } else if (selectingWarehouseForTransaction) {
                                        // Store selected warehouse and return to the appropriate screen
                                        selectedWarehouseForTransaction = warehouse
                                        // Don't reset flags here - let the target screen handle it
                                        currentScreen = returnToScreenAfterPartySelection
                                            ?: AppScreen.ADD_TRANSACTION_STEP1
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
                                    if (selectingEntityForReport) {
                                        // User cancelled entity selection - go back to reports
                                        selectingEntityForReport = false
                                        selectedEntityForReport = null
                                        selectedReportFilters = null
                                        selectedReportType = null
                                        currentScreen = AppScreen.REPORTS
                                    } else if (selectingWarehouseForTransaction) {
                                        // Navigate back without selecting - clear the selection state
                                        val targetScreen = returnToScreenAfterPartySelection
                                            ?: AppScreen.ADD_TRANSACTION_STEP1
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
                            AddWarehouseScreen(
                                viewModel = koinInject(),
                                warehouseToEdit = selectedWarehouseForEdit,
                                onNavigateBack = {
                                    warehousesRefreshTrigger++  // Trigger refresh
                                    currentScreen = AppScreen.WAREHOUSES
                                }
                            )
                        }

                        AppScreen.MY_BUSINESS -> {
                            MyBusinessScreen(
                                viewModel = koinInject(),
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
                            AddBusinessScreen(
                                viewModel = koinInject(),
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
                            QuantityUnitsScreen(
                                viewModel = koinInject(),
                                onUnitClick = { unit ->
                                    // Save the current selected parent slug for restoration when returning
                                    selectedParentUnitSlugToRestore =
                                        if (unit.isParentUnitType) unit.slug else unit.parentSlug
                                    selectedUnitForEdit = unit
                                    isAddingParentUnitType = unit.isParentUnitType
                                    selectedParentUnitForChildUnit = null
                                    currentScreen = AppScreen.ADD_QUANTITY_UNIT
                                },
                                onAddUnitClick = {
                                    // Legacy - keep for backward compatibility
                                    selectedUnitForEdit = null
                                    isAddingParentUnitType = false
                                    selectedParentUnitForChildUnit = null
                                    currentScreen = AppScreen.ADD_QUANTITY_UNIT
                                },
                                onAddUnitTypeClick = { currentSelectedParentSlug ->
                                    // Add new parent unit type (e.g., Weight, Quantity, Liquid)
                                    // Save the current selection for when we return
                                    selectedParentUnitSlugToRestore = currentSelectedParentSlug
                                    selectedUnitForEdit = null
                                    isAddingParentUnitType = true
                                    selectedParentUnitForChildUnit = null
                                    currentScreen = AppScreen.ADD_QUANTITY_UNIT
                                },
                                onAddChildUnitClick = { parentUnit ->
                                    // Add new child unit under selected parent type
                                    // Save the parent slug for restoration when returning
                                    selectedParentUnitSlugToRestore = parentUnit.slug
                                    selectedUnitForEdit = null
                                    isAddingParentUnitType = false
                                    selectedParentUnitForChildUnit = parentUnit
                                    currentScreen = AppScreen.ADD_QUANTITY_UNIT
                                },
                                onNavigateBack = { navigateBack() },
                                refreshTrigger = quantityUnitsRefreshTrigger,
                                initialSelectedParentSlug = selectedParentUnitSlugToRestore
                            )
                        }

                        AppScreen.ADD_QUANTITY_UNIT -> {
                            AddQuantityUnitScreen(
                                viewModel = koinInject(),
                                unitToEdit = selectedUnitForEdit,
                                isAddingParentUnitType = isAddingParentUnitType,
                                parentUnit = selectedParentUnitForChildUnit,
                                onNavigateBack = {
                                    quantityUnitsRefreshTrigger++
                                    currentScreen = AppScreen.QUANTITY_UNITS
                                }
                            )
                        }

                        AppScreen.TRANSACTION_SETTINGS -> {
                            TransactionTypeSelectionScreen(
                                viewModel = koinInject(),
                                onNavigateBack = { navigateBack() }
                            )
                        }

                        AppScreen.RECEIPT_SETTINGS -> {
                            ReceiptSettingsScreen(
                                viewModel = koinInject(),
                                onNavigateBack = { navigateBack() }
                            )
                        }

                        AppScreen.DASHBOARD_SETTINGS -> {
                            DashboardSettingsScreen(
                                viewModel = koinInject(),
                                onNavigateBack = { navigateBack() }
                            )
                        }

                        AppScreen.TEMPLATES -> {
                            TemplatesScreen(
                                viewModel = koinInject(),
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
                            AddTemplateScreen(
                                viewModel = koinInject(),
                                templateId = selectedTemplateIdForEdit,
                                onNavigateBack = {
                                    templatesRefreshTrigger++
                                    currentScreen = AppScreen.TEMPLATES
                                }
                            )
                        }

                        AppScreen.UPDATE_PROFILE -> {
                            UpdateProfileScreen(
                                viewModel = koinInject(),
                                onNavigateBack = { navigateBack() }
                            )
                        }

                        AppScreen.TRANSACTIONS_LIST -> {
                            // Use the ViewModel from app level to preserve state across navigation
                            transactionsListViewModel?.let { viewModel ->
                                // Set party filter if coming from party actions
                                LaunchedEffect(selectedPartyForTransactionFilter) {
                                    selectedPartyForTransactionFilter?.let { party ->
                                        viewModel.setPartyFilter(party)
                                    }
                                }
                                
                                // Set area filter if coming from area selection
                                LaunchedEffect(selectedAreaForFilter) {
                                    selectedAreaForFilter?.let { area ->
                                        viewModel.setAreaFilter(area)
                                        selectedAreaForFilter = null
                                    }
                                }
                                
                                // Set category filter if coming from category selection
                                LaunchedEffect(selectedCategoryForFilter) {
                                    selectedCategoryForFilter?.let { category ->
                                        viewModel.setCategoryFilter(category)
                                        selectedCategoryForFilter = null
                                    }
                                }

                                TransactionsListScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        selectedPartyForTransactionFilter = null
                                        navigateBack()
                                    },
                                    onSelectParty = {
                                        selectingPartyForFilter = true
                                        navigateTo(AppScreen.PARTIES)
                                    },
                                    onSelectArea = {
                                        selectingAreaForFilter = true
                                        categoryType = CategoryType.AREA
                                        navigateTo(AppScreen.CATEGORIES)
                                    },
                                    onSelectCategory = {
                                        selectingCategoryForFilter = true
                                        categoryType = CategoryType.CUSTOMER_CATEGORY
                                        navigateTo(AppScreen.CATEGORIES)
                                    },
                                    onTransactionClick = { transaction ->
                                        selectedTransactionSlug = transaction.slug
                                        navigateTo(AppScreen.TRANSACTION_DETAIL)
                                    },
                                    onAddTransactionClick = {
                                        transactionType = null
                                        navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                    },
                                    onTransactionDeleted = {
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                    },
                                    onEditTransaction = { transaction ->
                                        // Store the slug for loading full transaction details
                                        selectedTransactionSlugForEdit = transaction.slug

                                        // Route to appropriate screen based on transaction type
                                        when {
                                            AllTransactionTypes.isRecord(
                                                transaction.transactionType
                                            ) -> {
                                                navigateTo(AppScreen.ADD_RECORD)
                                            }

                                            AllTransactionTypes.isPayGetCash(
                                                transaction.transactionType
                                            ) -> {
                                                navigateTo(AppScreen.PAY_GET_CASH)
                                            }

                                            AllTransactionTypes.isExpenseIncome(
                                                transaction.transactionType
                                            ) -> {
                                                navigateTo(AppScreen.ADD_EXPENSE_INCOME)
                                            }

                                            AllTransactionTypes.isPaymentTransfer(
                                                transaction.transactionType
                                            ) -> {
                                                navigateTo(AppScreen.PAYMENT_TRANSFER)
                                            }

                                            AllTransactionTypes.isJournalVoucher(
                                                transaction.transactionType
                                            ) -> {
                                                selectedTransactionSlugForEdit = transaction.slug
                                                navigateTo(AppScreen.JOURNAL_VOUCHER)
                                            }

                                            AllTransactionTypes.isStockAdjustment(
                                                transaction.transactionType
                                            ) -> {
                                                selectedTransactionSlugForEdit = transaction.slug
                                                navigateTo(AppScreen.STOCK_ADJUSTMENT)
                                            }

                                            transaction.transactionType == AllTransactionTypes.MANUFACTURE.value -> {
                                                selectedTransactionSlugForEdit = transaction.slug
                                                navigateTo(AppScreen.ADD_MANUFACTURE)
                                            }

                                            else -> {
                                                // Regular transactions (Sale, Purchase, Returns, Orders)
                                                transactionType =
                                                    AllTransactionTypes.fromValue(
                                                        transaction.transactionType
                                                    )
                                                navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                            }
                                        }
                                    },
                                    onConvertToSale = { transaction ->
                                        viewModel.convertToSale(
                                            transaction = transaction,
                                            onSuccess = {
                                                partiesRefreshTrigger++ // Refresh parties to show updated balances
                                                snackbarMessage = "Transaction converted to sale successfully"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onEditAndConvertToSale = { transaction ->
                                        viewModel.editAndConvertToSale(transaction) { editedTransaction ->
                                            selectedTransactionSlugForEdit = editedTransaction.slug
                                            transactionType = AllTransactionTypes.SALE
                                            navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                        }
                                    },
                                    onConvertToPurchase = { transaction ->
                                        viewModel.convertToPurchase(
                                            transaction = transaction,
                                            onSuccess = {
                                                partiesRefreshTrigger++ // Refresh parties to show updated balances
                                                snackbarMessage = "Transaction converted to purchase successfully"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onEditAndConvertToPurchase = { transaction ->
                                        viewModel.editAndConvertToPurchase(transaction) { editedTransaction ->
                                            selectedTransactionSlugForEdit = editedTransaction.slug
                                            transactionType = AllTransactionTypes.PURCHASE
                                            navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                        }
                                    },
                                    onCancelAndRemove = { transaction ->
                                        showDeleteConfirmationDialog = transaction
                                    },
                                    onRestore = { transaction ->
                                        viewModel.restoreTransaction(
                                            transaction = transaction,
                                            onSuccess = {
                                                partiesRefreshTrigger++ // Refresh parties to show updated balances
                                                snackbarMessage = "Transaction restored successfully"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onClone = { transaction ->
                                        showCloneDialog = transaction
                                    },
                                    onChangeStateToPending = { transaction ->
                                        viewModel.updateTransactionState(
                                            transaction = transaction,
                                            newState = com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState.PENDING,
                                            onSuccess = {
                                                snackbarMessage = "Transaction state updated to Pending"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onChangeStateToInProgress = { transaction ->
                                        viewModel.updateTransactionState(
                                            transaction = transaction,
                                            newState = com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState.IN_PROGRESS,
                                            onSuccess = {
                                                snackbarMessage = "Transaction state updated to In Progress"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onChangeStateToCompleted = { transaction ->
                                        viewModel.updateTransactionState(
                                            transaction = transaction,
                                            newState = com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState.COMPLETED,
                                            onSuccess = {
                                                snackbarMessage = "Transaction state updated to Completed"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onChangeStateToCanceled = { transaction ->
                                        viewModel.updateTransactionState(
                                            transaction = transaction,
                                            newState = com.hisaabi.hisaabi_kmp.transactions.domain.model.TransactionState.CANCELLED,
                                            onSuccess = {
                                                snackbarMessage = "Transaction state updated to Cancelled"
                                            },
                                            onError = { error ->
                                                snackbarMessage = error
                                            }
                                        )
                                    },
                                    onOutstandingBalanceReminder = { transaction ->
                                        // Navigate to templates screen for reminder
                                        navigateTo(AppScreen.TEMPLATES)
                                    }
                                )
                            } ?: run {
                                // If ViewModel is not available, navigate back to home
                                currentScreen = AppScreen.HOME
                            }
                            
                            // Delete confirmation dialog
                            showDeleteConfirmationDialog?.let { transaction ->
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = null },
                                    title = { Text("Cancel and Remove") },
                                    text = { Text("Are you sure you want to delete this transaction?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                transactionsListViewModel?.cancelAndRemove(
                                                    transaction = transaction,
                                                    onSuccess = {
                                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                                        snackbarMessage = "Transaction deleted successfully"
                                                        showDeleteConfirmationDialog = null
                                                    },
                                                    onError = { error ->
                                                        snackbarMessage = error
                                                        showDeleteConfirmationDialog = null
                                                    }
                                                )
                                            }
                                        ) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirmationDialog = null }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                            
                            // Clone transaction dialog
                            var selectedCloneType by remember { mutableStateOf<AllTransactionTypes?>(null) }
                            
                            showCloneDialog?.let { transaction ->
                                AlertDialog(
                                    onDismissRequest = { 
                                        selectedCloneType = null
                                        showCloneDialog = null 
                                    },
                                    title = { Text("Clone as") },
                                    text = {
                                        Column {
                                            Text("Select transaction type to clone as:")
                                            Spacer(Modifier.height(16.dp))
                                            AllTransactionTypes.SALE.let { type ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedCloneType = type }
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCloneType == type,
                                                        onClick = { selectedCloneType = type }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Sale")
                                                }
                                            }
                                            AllTransactionTypes.PURCHASE.let { type ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedCloneType = type }
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCloneType == type,
                                                        onClick = { selectedCloneType = type }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Purchase")
                                                }
                                            }
                                            AllTransactionTypes.CUSTOMER_RETURN.let { type ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedCloneType = type }
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCloneType == type,
                                                        onClick = { selectedCloneType = type }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Return from Customer")
                                                }
                                            }
                                            AllTransactionTypes.VENDOR_RETURN.let { type ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedCloneType = type }
                                                        .padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    RadioButton(
                                                        selected = selectedCloneType == type,
                                                        onClick = { selectedCloneType = type }
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Return to Vendor")
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                selectedCloneType?.let { cloneType ->
                                                    transactionsListViewModel?.cloneTransaction(
                                                        transaction = transaction,
                                                        cloneAsType = cloneType
                                                    ) { clonedTransaction ->
                                                        // Store cloned transaction to initialize ViewModel when screen loads
                                                        clonedTransactionForEdit = clonedTransaction
                                                        transactionType = cloneType
                                                        showCloneDialog = null
                                                        navigateTo(AppScreen.ADD_TRANSACTION_STEP1)
                                                    }
                                                }
                                            },
                                            enabled = selectedCloneType != null
                                        ) {
                                            Text("Clone")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showCloneDialog = null }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        }

                        AppScreen.TRANSACTION_DETAIL -> {
                            val transactionDetailViewModel: TransactionDetailViewModel =
                                koinInject()
                            selectedTransactionSlug?.let { slug ->
                                TransactionDetailScreen(
                                    viewModel = transactionDetailViewModel,
                                    transactionSlug = slug,
                                    onNavigateBack = {
                                        selectedTransactionSlug = null
                                        navigateBack()
                                    }
                                )
                            } ?: run {
                                // If no transaction slug, go back to list
                                navigateBack()
                            }
                        }

                        AppScreen.BALANCE_HISTORY -> {
                            selectedPartyForBalanceHistory?.let { party ->
                                // Fetch transactions for this party
                                val transactionsListViewModel: TransactionsListViewModel =
                                    koinInject()
                                val transactionsState by transactionsListViewModel.state.collectAsState()

                                // Set party filter to get transactions for this party
                                LaunchedEffect(party) {
                                    transactionsListViewModel.setPartyFilter(party)
                                }

                                BalanceHistoryScreen(
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

                        AppScreen.REPORTS -> {
                            ReportsScreen(
                                onBackClick = {
                                    // Clear selected report type when going back
                                    selectedReportType = null
                                    navigateBack()
                                },
                                onReportSelected = { reportType ->
                                    selectedReportType = reportType
                                    // Create default filters with OVERALL as default if available
                                    val reportTypes = ReportFiltersFactory.getReportTypes(reportType)
                                    val defaultAdditionalFilter = if (reportTypes.isNotEmpty()) {
                                        // Set OVERALL as default if it's available in report types
                                        reportTypes.firstOrNull { it == ReportAdditionalFilter.OVERALL } ?: reportTypes.firstOrNull()
                                    } else {
                                        null
                                    }
                                    val defaultFilters = ReportFilters(
                                        reportType = reportType,
                                        additionalFilter = defaultAdditionalFilter
                                    )
                                    
                                    // Skip filters screen for balance sheet - generate directly
                                    if (reportType == ReportType.BALANCE_SHEET) {
                                        reportViewModel.generateReport(defaultFilters)
                                        navigateTo(AppScreen.REPORT_RESULT)
                                    } else if (defaultFilters.requiresEntitySelection()) {
                                        // Need to select entity first
                                        selectingEntityForReport = true
                                        selectedEntityForReport = null
                                        selectedReportFilters = defaultFilters
                                        // Navigate to appropriate entity selection screen
                                        when (defaultFilters.getRequiredEntityType()) {
                                            RequiredEntityType.WAREHOUSE -> navigateTo(AppScreen.WAREHOUSES)
                                            RequiredEntityType.PRODUCT -> navigateTo(AppScreen.PRODUCTS)
                                            RequiredEntityType.CUSTOMER, RequiredEntityType.VENDOR, RequiredEntityType.INVESTOR -> navigateTo(AppScreen.PARTIES)
                                            null -> navigateTo(AppScreen.REPORT_FILTERS)
                                        }
                                    } else {
                                        // No entity selection needed, go directly to filters
                                        selectedReportFilters = defaultFilters
                                        navigateTo(AppScreen.REPORT_FILTERS)
                                    }
                                }
                            )
                        }

                        AppScreen.REPORT_FILTERS -> {
                            selectedReportType?.let { reportType ->
                                val initialFilters = selectedReportFilters ?: ReportFilters(reportType = reportType)
                                ReportFiltersScreen(
                                    reportType = reportType,
                                    filters = initialFilters,
                                    selectedEntity = selectedEntityForReport,
                                    onBackClick = { navigateBack() },
                                    onFiltersChanged = { filters ->
                                        selectedReportFilters = filters
                                    },
                                    onGenerateReport = { filters ->
                                        // Generate report and navigate to result screen
                                        reportViewModel.generateReport(filters)
                                        navigateTo(AppScreen.REPORT_RESULT)
                                    }
                                )
                            } ?: run {
                                // If no report type selected, go back to reports
                                currentScreen = AppScreen.REPORTS
                            }
                        }

                        AppScreen.REPORT_RESULT -> {
                            val reportState by reportViewModel.uiState.collectAsState()

                            // Show error toast if any
                            LaunchedEffect(reportState.error) {
                                reportState.error?.let { error ->
                                    toastMessage = error
                                    reportViewModel.clearError()
                                }
                            }

                            when {
                                reportState.isLoading -> {
                                    // Loading screen
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                reportState.reportResult != null -> {
                                    ReportResultScreen(
                                        reportResult = reportState.reportResult!!,
                                        onBackClick = {
                                            reportViewModel.clearReport()
                                            navigateBack()
                                        },
                                        onShareClick = {
                                            reportViewModel.shareReportAsPdf()
                                        }
                                    )

                                    // Show loading indicator during PDF generation
                                    if (reportState.isGeneratingPdf) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card {
                                                Column(
                                                    modifier = Modifier.padding(24.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CircularProgressIndicator()
                                                    Spacer(
                                                        modifier = Modifier.height(16.dp)
                                                    )
                                                    Text("Generating PDF...")
                                                }
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    // No report result, go back
                                    LaunchedEffect(Unit) {
                                        navigateBack()
                                    }
                                }
                            }
                        }

                        AppScreen.ADD_RECORD -> {
                            // Use ViewModel from app level flow tracking
                            recordViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                // Handle party selection for record
                                LaunchedEffect(selectedPartyForTransaction) {
                                    selectedPartyForTransaction?.let { party ->
                                        if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_RECORD) {
                                            viewModel.selectParty(party)
                                            selectedPartyForTransaction = null
                                            selectingPartyForTransaction = false
                                            returnToScreenAfterPartySelection = null
                                        }
                                    }
                                }

                                AddRecordScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { successMessage, transactionSlug ->
                                        isInAddRecordFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                        // Record types should not generate receipts, just show toast
                                        successMessage?.let {
                                            toastMessage = it
                                        }
                                        navigateBack()
                                    },
                                    onSelectParty = {
                                        selectingPartyForTransaction = true
                                        returnToScreenAfterPartySelection = AppScreen.ADD_RECORD
                                        selectedPartySegment =
                                            PartySegment.CUSTOMER
                                        currentScreen = AppScreen.PARTIES
                                    }
                                )
                            }
                        }

                        AppScreen.PAY_GET_CASH -> {
                            // Use ViewModel from app level flow tracking
                            // Handle party selection for pay/get cash
                            LaunchedEffect(selectedPartyForTransaction) {
                                selectedPartyForTransaction?.let { party ->
                                    if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.PAY_GET_CASH && payGetCashViewModel != null) {
                                        payGetCashViewModel.selectParty(party)
                                        selectedPartyForTransaction = null
                                        selectingPartyForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                    }
                                }
                            }

                            // Handle payment method selection
                            LaunchedEffect(selectedPaymentMethodForTransaction) {
                                selectedPaymentMethodForTransaction?.let { paymentMethod ->
                                    if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.PAY_GET_CASH && payGetCashViewModel != null) {
                                        payGetCashViewModel.selectPaymentMethod(paymentMethod)
                                        selectedPaymentMethodForTransaction = null
                                        selectingPaymentMethodForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                    }
                                }
                            }

                            payGetCashViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                PayGetCashScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { successMessage, transactionSlug ->
                                        isInPayGetCashFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                        // Show receipt only for Pay/Get Cash transactions
                                        successMessage?.let {
                                            val currentState = viewModel.state.value
                                            val transactionType =
                                                when (currentState.partyType) {
                                                    PartyType.CUSTOMER -> {
                                                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH)
                                                            AllTransactionTypes.PAY_TO_CUSTOMER.value
                                                        else
                                                            AllTransactionTypes.GET_FROM_CUSTOMER.value
                                                    }

                                                    PartyType.VENDOR -> {
                                                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH)
                                                            AllTransactionTypes.PAY_TO_VENDOR.value
                                                        else
                                                            AllTransactionTypes.GET_FROM_VENDOR.value
                                                    }

                                                    PartyType.INVESTOR -> {
                                                        if (currentState.payGetCashType == PayGetCashType.PAY_CASH)
                                                            AllTransactionTypes.INVESTMENT_WITHDRAW.value
                                                        else
                                                            AllTransactionTypes.INVESTMENT_DEPOSIT.value
                                                    }

                                                    else -> {
                                                        AllTransactionTypes.PAY_TO_CUSTOMER.value
                                                    }
                                                }
                                            
                                            // Only generate receipt for Pay/Get Cash transactions (not Investment)
                                            if (receiptConfig.isReceiptEnabled && 
                                                transactionSlug != null && 
                                                AllTransactionTypes.shouldGenerateReceipt(transactionType)) {
                                                // Show receipt instead of toast
                                                val transaction =
                                                    Transaction(
                                                        slug = transactionSlug,
                                                        transactionType = transactionType
                                                    )
                                                receiptViewModel.showPreview(transaction)
                                            } else {
                                                // Show toast only if not showing receipt
                                                toastMessage = it
                                            }
                                        }
                                        navigateBack()
                                    },
                                    onSelectParty = { partyType ->
                                        selectingPartyForTransaction = true
                                        returnToScreenAfterPartySelection = AppScreen.PAY_GET_CASH
                                        selectedPartySegment = when (partyType) {
                                            PartyType.CUSTOMER ->
                                                PartySegment.CUSTOMER

                                            PartyType.VENDOR ->
                                                PartySegment.VENDOR

                                            PartyType.INVESTOR ->
                                                PartySegment.INVESTOR

                                            else -> {
                                                PartySegment.CUSTOMER
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
                        }

                        AppScreen.ADD_EXPENSE_INCOME -> {
                            // Use ViewModel from app level flow tracking
                            // Handle party selection for expense/income (these are expense/income types stored as parties with roleId 14 or 15)
                            LaunchedEffect(selectedPartyForTransaction) {
                                selectedPartyForTransaction?.let { party ->
                                    if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_EXPENSE_INCOME && expenseIncomeViewModel != null) {
                                        expenseIncomeViewModel.selectParty(party)
                                        selectedPartyForTransaction = null
                                        selectingPartyForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                        isExpenseIncomePartySelection = false
                                    }
                                }
                            }

                            // Handle payment method selection
                            LaunchedEffect(selectedPaymentMethodForTransaction) {
                                selectedPaymentMethodForTransaction?.let { paymentMethod ->
                                    if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_EXPENSE_INCOME && expenseIncomeViewModel != null) {
                                        expenseIncomeViewModel.selectPaymentMethod(paymentMethod)
                                        selectedPaymentMethodForTransaction = null
                                        selectingPaymentMethodForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                    }
                                }
                            }

                            // Set initial transaction type when screen is opened
                            LaunchedEffect(initialExpenseIncomeType, expenseIncomeViewModel) {
                                initialExpenseIncomeType?.let { type ->
                                    expenseIncomeViewModel?.let { viewModel ->
                                        // Set the transaction type based on navigation source
                                        viewModel.setTransactionType(type)
                                        // Clear the initial type after setting it
                                        initialExpenseIncomeType = null
                                    }
                                }
                            }

                            expenseIncomeViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                AddExpenseIncomeScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { success, transactionType, successMessage ->
                                        isInExpenseIncomeFlow = false
                                        initialExpenseIncomeType = null // Reset initial type
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                        // Show toast if transaction was saved/updated successfully
                                        if (success && successMessage != null) {
                                            toastMessage = successMessage
                                        }
                                        navigateBack()
                                    },
                                    onSelectParty = {
                                        selectingPartyForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_EXPENSE_INCOME
                                        isExpenseIncomePartySelection =
                                            true  // Set expense/income context
                                        // Set initial segment based on transaction type
                                        val state = viewModel.state.value
                                        selectedPartySegment =
                                            if (state.transactionType == AllTransactionTypes.EXPENSE) {
                                                PartySegment.EXPENSE
                                            } else {
                                                PartySegment.EXTRA_INCOME
                                            }
                                        currentScreen = AppScreen.PARTIES
                                    },
                                    onSelectPaymentMethod = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_EXPENSE_INCOME
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    }
                                )
                            }
                        }

                        AppScreen.PAYMENT_TRANSFER -> {
                            // Use ViewModel from app level flow tracking
                            // Handle payment method selection (From or To)
                            LaunchedEffect(selectedPaymentMethodForTransaction) {
                                selectedPaymentMethodForTransaction?.let { paymentMethod ->
                                    if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.PAYMENT_TRANSFER && paymentTransferViewModel != null) {
                                        // Use the flag to determine which payment method to set
                                        if (isSelectingPaymentMethodFrom) {
                                            paymentTransferViewModel.selectPaymentMethodFrom(
                                                paymentMethod
                                            )
                                        } else {
                                            paymentTransferViewModel.selectPaymentMethodTo(
                                                paymentMethod
                                            )
                                        }
                                        // Clear all selection states
                                        selectedPaymentMethodForTransaction = null
                                        selectingPaymentMethodForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                        isSelectingPaymentMethodFrom = false
                                    }
                                }
                            }

                            paymentTransferViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                PaymentTransferScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { success ->
                                        isInPaymentTransferFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                        // Show toast if transaction was saved successfully
                                        if (success) {
                                            toastMessage = "Payment transfer saved successfully"
                                        }
                                        navigateBack()
                                    },
                                    onSelectPaymentMethodFrom = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.PAYMENT_TRANSFER
                                        isSelectingPaymentMethodFrom = true  // Set flag for "From"
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    },
                                    onSelectPaymentMethodTo = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.PAYMENT_TRANSFER
                                        isSelectingPaymentMethodFrom = false  // Set flag for "To"
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    }
                                )
                            }
                        }

                        AppScreen.JOURNAL_VOUCHER -> {
                            // Use ViewModel from app level flow tracking
                            // Handle party selection for journal voucher
                            LaunchedEffect(selectedPartyForTransaction) {
                                selectedPartyForTransaction?.let { party ->
                                    if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.JOURNAL_VOUCHER && journalVoucherViewModel != null) {
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
                                    if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.JOURNAL_VOUCHER && journalVoucherViewModel != null) {
                                        // Check if it's for the voucher payment method or for adding as account
                                        if (isSelectingPaymentMethodFrom) {
                                            // Adding payment method as an account
                                            journalVoucherViewModel.addPaymentMethod(paymentMethod)
                                        } else {
                                            // Selecting payment method for the voucher
                                            journalVoucherViewModel.selectPaymentMethod(
                                                paymentMethod
                                            )
                                        }
                                        selectedPaymentMethodForTransaction = null
                                        selectingPaymentMethodForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                        isSelectingPaymentMethodFrom = false
                                    }
                                }
                            }

                            journalVoucherViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                AddJournalVoucherScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { success ->
                                        isInJournalVoucherFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances
                                        // Show toast if transaction was saved successfully
                                        if (success) {
                                            toastMessage = "Journal voucher saved successfully"
                                        }
                                        navigateBack()
                                    },
                                    onSelectAccountType = {
                                        showJournalAccountTypeDialog = true
                                    },
                                    onSelectPaymentMethod = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.JOURNAL_VOUCHER
                                        isSelectingPaymentMethodFrom =
                                            false // For voucher payment method
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    }
                                )

                                // Account Type Selection Dialog
                                if (showJournalAccountTypeDialog) {
                                    JournalAccountTypeDialog(
                                        onDismiss = { showJournalAccountTypeDialog = false },
                                        onSelectExpense = {
                                            showJournalAccountTypeDialog = false
                                            selectingPartyForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isExpenseIncomePartySelection = true
                                            selectedPartySegment =
                                                PartySegment.EXPENSE
                                            currentScreen = AppScreen.PARTIES
                                        },
                                        onSelectExtraIncome = {
                                            showJournalAccountTypeDialog = false
                                            selectingPartyForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isExpenseIncomePartySelection = true
                                            selectedPartySegment =
                                                PartySegment.EXTRA_INCOME
                                            currentScreen = AppScreen.PARTIES
                                        },
                                        onSelectCustomer = {
                                            showJournalAccountTypeDialog = false
                                            selectingPartyForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isExpenseIncomePartySelection = false
                                            selectedPartySegment =
                                                PartySegment.CUSTOMER
                                            currentScreen = AppScreen.PARTIES
                                        },
                                        onSelectVendor = {
                                            showJournalAccountTypeDialog = false
                                            selectingPartyForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isExpenseIncomePartySelection = false
                                            selectedPartySegment =
                                                PartySegment.VENDOR
                                            currentScreen = AppScreen.PARTIES
                                        },
                                        onSelectInvestor = {
                                            showJournalAccountTypeDialog = false
                                            selectingPartyForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isExpenseIncomePartySelection = false
                                            selectedPartySegment =
                                                PartySegment.INVESTOR
                                            currentScreen = AppScreen.PARTIES
                                        },
                                        onSelectPaymentMethod = {
                                            showJournalAccountTypeDialog = false
                                            selectingPaymentMethodForTransaction = true
                                            returnToScreenAfterPartySelection =
                                                AppScreen.JOURNAL_VOUCHER
                                            isSelectingPaymentMethodFrom =
                                                true // For adding as account
                                            currentScreen = AppScreen.PAYMENT_METHODS
                                        }
                                    )
                                }
                            }
                        }

                        AppScreen.STOCK_ADJUSTMENT -> {
                            // Use ViewModel from app level flow tracking
                            // Handle warehouse selection for stock adjustment
                            LaunchedEffect(selectedWarehouseForTransaction) {
                                selectedWarehouseForTransaction?.let { warehouse ->
                                    if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.STOCK_ADJUSTMENT && stockAdjustmentViewModel != null) {
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
                            LaunchedEffect(
                                selectedProductQuantitiesForTransaction.size,
                                selectedBusinessSlug
                            ) {
                                val businessSlug = selectedBusinessSlug
                                if (selectedProductQuantitiesForTransaction.isNotEmpty() && businessSlug != null &&
                                    selectingProductsForTransaction && returnToScreenAfterProductSelection == AppScreen.STOCK_ADJUSTMENT && stockAdjustmentViewModel != null
                                ) {
                                    // Fetch products by their slugs
                                    val allProducts = productsRepository.getProducts(businessSlug)
                                    val selectedSlugs =
                                        selectedProductQuantitiesForTransaction.keys.toSet()
                                    val products = allProducts.filter { it.slug in selectedSlugs }

                                    products.forEach { product ->
                                        // Get the quantity for this product
                                        val quantity =
                                            selectedProductQuantitiesForTransaction[product.slug]
                                                ?: 1
                                        // Add product with the specified quantity
                                        stockAdjustmentViewModel.addProduct(
                                            product,
                                            quantity.toDouble()
                                        )
                                    }
                                    selectedProductQuantitiesForTransaction =
                                        emptyMap() // Clear after adding
                                    selectingProductsForTransaction = false
                                    returnToScreenAfterProductSelection = null
                                }
                            }

                            stockAdjustmentViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }
                                StockAdjustmentScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { success ->
                                        isInStockAdjustmentFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances (if any)
                                        // Show toast if transaction was saved successfully
                                        if (success) {
                                            toastMessage = "Stock adjustment saved successfully"
                                        }
                                        navigateBack()
                                    },
                                    onSelectWarehouseFrom = {
                                        selectingWarehouseForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.STOCK_ADJUSTMENT
                                        isSelectingWarehouseFrom = true
                                        currentScreen = AppScreen.WAREHOUSES
                                    },
                                    onSelectWarehouseTo = {
                                        selectingWarehouseForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.STOCK_ADJUSTMENT
                                        isSelectingWarehouseFrom = false
                                        currentScreen = AppScreen.WAREHOUSES
                                    },
                                    onSelectProducts = {
                                        selectingProductsForTransaction = true
                                        returnToScreenAfterProductSelection =
                                            AppScreen.STOCK_ADJUSTMENT
                                        currentScreen = AppScreen.PRODUCTS
                                    }
                                )
                            }
                        }

                        AppScreen.ADD_MANUFACTURE -> {
                            // Use ViewModel from app level flow tracking
                            // Handle warehouse selection for manufacture
                            LaunchedEffect(selectedWarehouseForTransaction) {
                                selectedWarehouseForTransaction?.let { warehouse ->
                                    if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_MANUFACTURE && manufactureViewModel != null) {
                                        manufactureViewModel.selectWarehouse(warehouse)
                                        selectedWarehouseForTransaction = null
                                        selectingWarehouseForTransaction = false
                                        returnToScreenAfterPartySelection = null
                                    }
                                }
                            }

                            manufactureViewModel?.let { viewModel ->
                                // Load transaction for editing if provided
                                LaunchedEffect(selectedTransactionSlugForEdit) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadManufactureTransaction(slug)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }

                                AddManufactureScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        viewModel.resetState()
                                        isInManufactureFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances (if any)
                                        currentScreen = AppScreen.HOME
                                    },
                                    onSelectWarehouse = {
                                        selectingWarehouseForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_MANUFACTURE
                                        currentScreen = AppScreen.WAREHOUSES
                                    },
                                    onSaveSuccess = {
                                        toastMessage = "Manufacture transaction saved successfully"
                                    }
                                )
                            }
                        }

                        AppScreen.ADD_TRANSACTION_STEP1 -> {
                            // Use shared ViewModel instance created at app level
                            transactionViewModel?.let { viewModel ->
                                // Set transaction type if provided
                                LaunchedEffect(transactionType) {
                                    transactionType?.let { type ->
                                        viewModel.setTransactionType(type)
                                    }
                                }

                                // Load transaction for editing if provided
                                // Pass transactionType override if set (for edit and convert scenarios)
                                LaunchedEffect(selectedTransactionSlugForEdit, transactionType) {
                                    selectedTransactionSlugForEdit?.let { slug ->
                                        viewModel.loadTransactionForEdit(slug, transactionType)
                                        selectedTransactionSlugForEdit = null // Clear after loading
                                    }
                                }
                                
                                // Initialize ViewModel with cloned transaction if provided
                                LaunchedEffect(clonedTransactionForEdit) {
                                    clonedTransactionForEdit?.let { clonedTransaction ->
                                        viewModel.initializeFromTransaction(clonedTransaction)
                                        clonedTransactionForEdit = null // Clear after loading
                                    }
                                }

                                // Set selected party if returned from party selection
                                LaunchedEffect(selectedPartyForTransaction) {
                                    selectedPartyForTransaction?.let { party ->
                                        if (selectingPartyForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_TRANSACTION_STEP1) {
                                            viewModel.selectParty(party)
                                            selectedPartyForTransaction =
                                                null // Clear after setting
                                            selectingPartyForTransaction = false
                                            returnToScreenAfterPartySelection = null
                                        }
                                    }
                                }

                                // Set selected warehouse if returned from warehouse selection
                                LaunchedEffect(selectedWarehouseForTransaction) {
                                    selectedWarehouseForTransaction?.let { warehouse ->
                                        if (selectingWarehouseForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_TRANSACTION_STEP1) {
                                            viewModel.selectWarehouse(warehouse)
                                            selectedWarehouseForTransaction =
                                                null // Clear after setting
                                            selectingWarehouseForTransaction = false
                                            returnToScreenAfterPartySelection = null
                                        }
                                    }
                                }

                                // Add selected products if returned from product selection
                                LaunchedEffect(
                                    selectedProductQuantitiesForTransaction.size,
                                    selectedBusinessSlug
                                ) {
                                    val businessSlug = selectedBusinessSlug
                                    if (selectedProductQuantitiesForTransaction.isNotEmpty() && businessSlug != null &&
                                        selectingProductsForTransaction && returnToScreenAfterProductSelection == AppScreen.ADD_TRANSACTION_STEP1
                                    ) {
                                        // Fetch products by their slugs
                                        val allProducts =
                                            productsRepository.getProducts(businessSlug)
                                        val selectedSlugs =
                                            selectedProductQuantitiesForTransaction.keys.toSet()
                                        val products =
                                            allProducts.filter { it.slug in selectedSlugs }

                                        products.forEach { product ->
                                            // Get the quantity for this product
                                            val quantity =
                                                selectedProductQuantitiesForTransaction[product.slug]
                                                    ?: 1
                                            // Get default unit for the product from its defaultUnitSlug
                                            val defaultUnit = product.defaultUnitSlug?.let { slug ->
                                                quantityUnitsRepository.getUnitBySlug(slug)
                                            }
                                            // Add product with the specified quantity
                                            viewModel.addProduct(
                                                product,
                                                defaultUnit,
                                                quantity.toDouble()
                                            )
                                        }
                                        selectedProductQuantitiesForTransaction =
                                            emptyMap() // Clear after adding
                                        selectingProductsForTransaction = false
                                        returnToScreenAfterProductSelection = null
                                    }
                                }
                                
                                // Handle payment method selection for desktop combined screen
                                LaunchedEffect(selectedPaymentMethodForTransaction) {
                                    selectedPaymentMethodForTransaction?.let { paymentMethod ->
                                        if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_TRANSACTION_STEP1) {
                                            viewModel.selectPaymentMethod(paymentMethod)
                                            selectedPaymentMethodForTransaction = null
                                            selectingPaymentMethodForTransaction = false
                                            returnToScreenAfterPartySelection = null
                                        }
                                    }
                                }

                                AddTransactionStep1Screen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navigateBack() },
                                    onSelectParty = {
                                        // Determine party segment based on transaction type
                                        val state = viewModel.state.value
                                        selectedPartySegment =
                                            if (AllTransactionTypes.isDealingWithVendor(
                                                    state.transactionType.value
                                                )
                                            ) {
                                                PartySegment.VENDOR
                                            } else {
                                                PartySegment.CUSTOMER
                                            }
                                        selectingPartyForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_TRANSACTION_STEP1
                                        currentScreen = AppScreen.PARTIES
                                    },
                                    onSelectProducts = {
                                        selectingProductsForTransaction = true
                                        returnToScreenAfterProductSelection =
                                            AppScreen.ADD_TRANSACTION_STEP1
                                        currentScreen = AppScreen.PRODUCTS
                                    },
                                    onSelectWarehouse = {
                                        selectingWarehouseForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_TRANSACTION_STEP1
                                        currentScreen = AppScreen.WAREHOUSES
                                    },
                                    onProceedToStep2 = { navigateTo(AppScreen.ADD_TRANSACTION_STEP2) },
                                    // Desktop-only callbacks for combined single-screen experience
                                    onSelectPaymentMethod = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_TRANSACTION_STEP1
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    },
                                    onTransactionSaved = { transactionSlug, transactionType ->
                                        // Exit transaction flow and navigate to home
                                        isInTransactionFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances

                                        // Show receipt only for specific transaction types
                                        if (receiptConfig.isReceiptEnabled && 
                                            transactionSlug != null && 
                                            AllTransactionTypes.shouldGenerateReceipt(transactionType)) {
                                            // Show receipt instead of toast
                                            val transaction = Transaction(
                                                slug = transactionSlug,
                                                transactionType = transactionType
                                            )
                                            receiptViewModel.showPreview(transaction)
                                        } else {
                                            // Show toast only if not showing receipt
                                            toastMessage = "Transaction saved successfully!"
                                        }

                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }
                        }

                        AppScreen.ADD_TRANSACTION_STEP2 -> {
                            // Use shared ViewModel instance created at app level
                            transactionViewModel?.let { viewModel ->
                                // Set selected payment method if returned from payment method selection
                                LaunchedEffect(selectedPaymentMethodForTransaction) {
                                    selectedPaymentMethodForTransaction?.let { paymentMethod ->
                                        if (selectingPaymentMethodForTransaction && returnToScreenAfterPartySelection == AppScreen.ADD_TRANSACTION_STEP2) {
                                            viewModel.selectPaymentMethod(paymentMethod)
                                            selectedPaymentMethodForTransaction =
                                                null // Clear after setting
                                            selectingPaymentMethodForTransaction = false
                                            returnToScreenAfterPartySelection = null
                                        }
                                    }
                                }

                                AddTransactionStep2Screen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navigateBack() },
                                    onSelectPaymentMethod = {
                                        selectingPaymentMethodForTransaction = true
                                        returnToScreenAfterPartySelection =
                                            AppScreen.ADD_TRANSACTION_STEP2
                                        currentScreen = AppScreen.PAYMENT_METHODS
                                    },
                                    onTransactionSaved = { transactionSlug, transactionType ->
                                        // Exit transaction flow and navigate to home
                                        isInTransactionFlow = false
                                        partiesRefreshTrigger++ // Refresh parties to show updated balances

                                        // Show receipt only for specific transaction types
                                        if (receiptConfig.isReceiptEnabled && 
                                            transactionSlug != null && 
                                            AllTransactionTypes.shouldGenerateReceipt(transactionType)) {
                                            // Show receipt instead of toast
                                            val transaction =
                                                Transaction(
                                                    slug = transactionSlug,
                                                    transactionType = transactionType
                                                )
                                            receiptViewModel.showPreview(transaction)
                                        } else {
                                            // Show toast only if not showing receipt
                                            toastMessage = "Transaction saved successfully!"
                                        }

                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }
                        }

                    }

                    // Snackbar Host at bottom for toast messages
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                }

                // Receipt Preview Dialog
                if (receiptState.showPreview && receiptState.currentTransaction != null) {
                    ReceiptPreviewDialog(
                        transaction = receiptState.currentTransaction!!,
                        config = receiptConfig,
                        isGenerating = receiptState.isGenerating,
                        onDismiss = { receiptViewModel.hidePreview() },
                        onShare = {
                            receiptState.currentTransaction?.let { transaction ->
                                receiptViewModel.generateAndShareReceipt(transaction)
                            }
                        }
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
    BALANCE_HISTORY,
    REPORTS,
    REPORT_FILTERS,
    REPORT_RESULT
}