# Transaction Edit Implementation

## Overview
This document describes the implementation of edit functionality for all transaction types in the Hisaabi KMP application. The implementation allows users to edit existing transactions, which automatically reverts old effects and applies new ones.

## Key Components Modified

### 1. TransactionsListScreen.kt
**Changes:**
- Added `onEditTransaction` callback parameter
- Modified `TransactionCard` to include `onEditClick` parameter
- Updated the Edit menu item to call `onEditClick()` instead of showing TODO

**Functionality:**
- When a user clicks "Edit" from the transaction card menu, the transaction is passed to the edit handler

### 2. App.kt Navigation
**Changes:**
- Added `selectedTransactionForEdit` state variable to track the transaction being edited
- Updated `TransactionsListScreen` instantiation to handle `onEditTransaction` callback
- Added routing logic to navigate to the appropriate screen based on transaction type:
  - Records → ADD_RECORD
  - Pay/Get Cash → PAY_GET_CASH
  - Expense/Income → ADD_EXPENSE_INCOME
  - Payment Transfer → PAYMENT_TRANSFER
  - Journal Voucher → JOURNAL_VOUCHER
  - Stock Adjustment → STOCK_ADJUSTMENT
  - Manufacture → ADD_MANUFACTURE
  - Regular transactions (Sale, Purchase, etc.) → ADD_TRANSACTION_STEP1

**Navigation Flow:**
```kotlin
onEditTransaction = { transaction ->
    selectedTransactionSlugForEdit = transaction.slug // Store slug, not full object
    // Route to appropriate screen based on transaction type
    currentScreen = determineScreenByType(transaction.transactionType)
}
```

### 3. AddTransactionViewModel.kt
**Changes:**
- Added `editingTransactionSlug` field to `AddTransactionState`
- Added `GetTransactionWithDetailsUseCase` as a constructor dependency
- Implemented `loadTransactionForEdit(transactionSlug: String)` method that:
  - Loads full transaction with all details using `GetTransactionWithDetailsUseCase`
  - Populates all state fields from the transaction data
  - Converts transaction details to `TransactionDetailItem` objects
- Updated `saveTransaction()` to check if editing and call `updateTransaction` or `addTransaction` accordingly
- Updated `buildTransaction()` to include slug when editing

**Edit Flow:**
1. Transaction slug is passed to `loadTransactionForEdit()`
2. Full transaction with all details is loaded from repository
3. All state fields are populated from the transaction data
4. Transaction details are converted to `TransactionDetailItem` objects
5. On save, the system detects `editingTransactionSlug` is not null
6. Calls `updateTransaction` which:
   - Reverses old transaction effects (stock, party balance, payment method)
   - Applies new transaction effects

### 4. AddRecordViewModel.kt
**Changes:**
- Added `editingTransactionSlug` field to `AddRecordState`
- Added `GetTransactionWithDetailsUseCase` as a constructor dependency  
- Implemented `loadTransactionForEdit(transactionSlug: String)` method that:
  - Loads full transaction using `GetTransactionWithDetailsUseCase`
  - Populates all record fields
- Updated `saveRecord()` to support both create and update operations

**Edit Handling:**
- Loads record type, state, party, description, amount, and date/time
- Handles reminder date/time for Cash Reminder type
- Preserves transaction state (Pending, Completed, Cancelled)

### 5. App.kt Integration for ViewModels
**Changes:**
- Added `LaunchedEffect` in ADD_TRANSACTION_STEP1 screen handler to load transaction for editing
- When `selectedTransactionForEdit` is set, calls `viewModel.loadTransactionForEdit(transaction)`
- Clears `selectedTransactionForEdit` after loading

## Transaction Types Supported

### Fully Implemented:
1. **Regular Transactions** (via AddTransactionViewModel)
   - Sale
   - Purchase
   - Customer Return
   - Vendor Return
   - Sale Order
   - Purchase Order
   - Quotation

2. **Records** (via AddRecordViewModel)
   - Meeting
   - Task
   - Client Note
   - Self Note
   - Cash Reminder

### Requires Additional Implementation:
3. **Pay/Get Cash** (PayGetCashViewModel)
4. **Expense/Income** (AddExpenseIncomeViewModel)
5. **Payment Transfer** (PaymentTransferViewModel)
6. **Journal Voucher** (JournalVoucherViewModel)
7. **Stock Adjustment** (StockAdjustmentViewModel)
8. **Manufacture** (AddManufactureViewModel)

## How Edit Works (Transaction Processing)

The edit functionality relies on the `TransactionProcessor` which handles:

### 1. Reverse Old Transaction
When updating a transaction, the system first reverses the old transaction's effects:
- **Party Balance**: Reverts balance changes
- **Payment Method Balance**: Reverts payment method updates
- **Stock Levels**: Reverts stock quantity changes
- **Child Transactions**: Handles related transactions (for Journal Voucher, Manufacture)

### 2. Apply New Transaction
After reverting, the system applies the new transaction:
- Updates party balances based on new amounts
- Updates payment method balances
- Updates stock levels based on new quantities
- Creates any necessary child transactions

### 3. Validation
Before any changes:
- Validates stock availability (for stock-affecting transactions)
- Validates party requirements
- Validates payment method requirements
- Validates transaction-specific business rules

## Usage Example

```kotlin
// User clicks Edit on a transaction card
onEditTransaction = { transaction ->
    // Store the transaction being edited
    selectedTransactionForEdit = transaction
    
    // Determine the correct screen
    when {
        AllTransactionTypes.isRecord(transaction.transactionType) -> {
            currentScreen = AppScreen.ADD_RECORD
        }
        // ... other transaction types
    }
}

// In the screen's LaunchedEffect
LaunchedEffect(selectedTransactionSlugForEdit) {
    selectedTransactionSlugForEdit?.let { slug ->
        viewModel.loadTransactionForEdit(slug) // Pass slug
        selectedTransactionSlugForEdit = null // Clear after loading
    }
}

// ViewModel loads the full transaction by slug
fun loadTransactionForEdit(transactionSlug: String) {
    viewModelScope.launch {
        // Load full transaction with all details
        val transaction = getTransactionWithDetailsUseCase(transactionSlug)
        
        // Populate all state fields from transaction
        _state.update { 
            it.copy(
                // ... all fields
                editingTransactionSlug = transaction.slug
            )
        }
    }
}

// On save
fun saveTransaction() {
    val result = if (currentState.editingTransactionSlug != null) {
        useCases.updateTransaction(transaction) // Edit mode
    } else {
        useCases.addTransaction(transaction) // Create mode
    }
}
```

## Benefits

1. **Automatic Effect Reversal**: Old transaction effects are automatically reversed
2. **Type Safety**: Transaction types are routed to appropriate screens
3. **Data Integrity**: All related entities (stock, balances) are correctly updated
4. **Reusable Components**: Same screens/ViewModels handle both create and edit
5. **User Experience**: Seamless editing experience across all transaction types
6. **Full Data Loading**: Transactions are loaded with complete details (products, parties, etc.) using `GetTransactionWithDetailsUseCase`
7. **Performance**: Only transaction slug is passed during navigation, full data is loaded when needed

## Testing Recommendations

1. **Regular Transactions**
   - Edit a sale transaction and verify stock and party balance updates
   - Edit quantities and prices
   - Change payment methods

2. **Records**
   - Edit meeting/task status
   - Update dates and descriptions
   - Verify party linkage preservation

3. **Edge Cases**
   - Edit a transaction with insufficient stock for new quantities
   - Edit with changed payment methods
   - Edit with changed parties

## Future Enhancements

1. Complete edit implementation for remaining transaction types:
   - PayGetCashViewModel
   - AddExpenseIncomeViewModel
   - PaymentTransferViewModel
   - JournalVoucherViewModel
   - StockAdjustmentViewModel
   - AddManufactureViewModel

2. Add edit button in TransactionDetailScreen
3. Add edit history/audit trail
4. Add confirmation dialog for significant changes
5. Add "Duplicate Transaction" feature using similar logic

## Files Modified

1. `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/presentation/ui/TransactionsListScreen.kt`
2. `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/App.kt`
3. `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/presentation/viewmodel/AddTransactionViewModel.kt`
4. `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/presentation/viewmodel/AddRecordViewModel.kt`

## Repository Methods Used

- `TransactionsRepository.updateTransaction()`: Handles the update logic
- `TransactionProcessor.validateTransaction()`: Validates before updating
- `TransactionProcessor.reverseTransaction()`: Reverts old effects
- `TransactionProcessor.processTransaction()`: Applies new effects
- `GetTransactionWithDetailsUseCase`: Loads complete transaction data for editing


