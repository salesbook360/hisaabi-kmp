# Transactions List State Persistence Fix

## Problem
The `TransactionsListScreen` was losing its state (filters, search query, scroll position) when navigating to transaction details and back. Additionally, when opening the screen from the home bottom navigation, it would immediately close.

## Root Causes

### Issue 1: ViewModel Recreation
The `TransactionsListViewModel` was being created inside the `AppScreen.TRANSACTIONS_LIST` case using `koinInject()`, which meant a new ViewModel instance was created every time the screen was shown, losing all previous state.

### Issue 2: Flow State Management
After moving the ViewModel to app-level scope, the flow state (`isInTransactionsListFlow`) wasn't being properly managed across different navigation scenarios, causing the screen to close immediately.

### Issue 3: Timing Problem (Flicker Issue)
The flow state flag `isInTransactionsListFlow` was updated asynchronously in a `LaunchedEffect`, which ran AFTER the initial composition. This created a timing issue where:
1. User navigates to TRANSACTIONS_LIST
2. Screen tries to render but `isInTransactionsListFlow` is still `false`
3. ViewModel is `null`, triggering the fallback that navigates to HOME
4. This caused a visible flicker

## Solution

### 1. Added Transactions List Flow State
Added a new flow state flag to track when the user is viewing transactions:
```kotlin
var isInTransactionsListFlow by remember { mutableStateOf(false) }
```

### 2. Created ViewModel at App Level with Immediate Screen Check
Created the `TransactionsListViewModel` at the app level (similar to other ViewModels) so it persists across navigation. To fix the timing issue, we check the `currentScreen` directly in addition to the flow state flag:

```kotlin
// Compute whether we're in transactions list flow based on current screen
// This ensures ViewModel is created immediately when navigating to transactions screens
val shouldShowTransactionsListFlow = currentScreen == AppScreen.TRANSACTIONS_LIST || 
                                       currentScreen == AppScreen.TRANSACTION_DETAIL || 
                                       isInTransactionsListFlow

val transactionsListViewModel: TransactionsListViewModel? = if (shouldShowTransactionsListFlow) {
    koinInject()
} else {
    null
}
```

This approach ensures the ViewModel is created **immediately** when navigating to the transactions screens, without waiting for the `LaunchedEffect` to run.

### 3. Updated Flow State Management
Updated the `LaunchedEffect` that manages flow states to properly handle the transactions list flow:

- **Activate flow** when navigating to `TRANSACTIONS_LIST` or `TRANSACTION_DETAIL`
- **Deactivate flow** when explicitly navigating to `HOME`
- **Preserve flow** in the general `else` block to allow navigation between related screens
- **Reset flow** in resource selection screens (PARTIES, WAREHOUSES, etc.) when not returning to transactions

### 4. Updated All Flow Reset Blocks
Updated all flow reset blocks in the resource selection screens (PARTIES, WAREHOUSES, PRODUCTS, PAYMENT_METHODS) and the general `else` block to properly include `isInTransactionsListFlow = false` when exiting flows.

### 5. Updated TRANSACTIONS_LIST Screen
Changed the screen to use the app-level ViewModel instead of creating a new one:
```kotlin
AppScreen.TRANSACTIONS_LIST -> {
    transactionsListViewModel?.let { viewModel ->
        // Use the persisted ViewModel
        TransactionsListScreen(viewModel = viewModel, ...)
    } ?: run {
        // Fallback to HOME if ViewModel not available
        currentScreen = AppScreen.HOME
    }
}
```

## Benefits

1. **State Persistence**: Filters, search queries, and scroll position are now preserved when navigating away and back
2. **No Flicker**: Screen renders immediately without any visible flicker or navigation glitches
3. **Better UX**: Users don't lose their place or have to re-apply filters after viewing transaction details
4. **Performance**: Avoids re-loading and re-filtering transactions on every navigation
5. **Consistency**: Follows the same pattern as other ViewModels in the app (e.g., `transactionViewModel`, `payGetCashViewModel`)
6. **Synchronous Creation**: ViewModel is created synchronously when the screen changes, eliminating timing issues

## Testing Recommendations

1. **Flicker Test**: Navigate to Transactions List from home bottom nav - verify no flicker or immediate close
2. **State Persistence**: 
   - Apply filters (transaction type, sort order)
   - Use search to filter transactions
   - Scroll down the list
   - Click on a transaction to view details
   - Navigate back - verify filters, search, and scroll position are preserved
3. **Navigation Flow**: Navigate between transaction list and transaction detail multiple times - verify no crashes
4. **Flow Exit**: Navigate to home and back to transactions - verify state is properly reset when exiting the flow
5. **Rapid Navigation**: Quickly tap the Transactions button multiple times - verify no crashes or duplicate screens

## Files Modified

- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/App.kt`
  - Added `isInTransactionsListFlow` state flag (line ~272)
  - Added flow state management for transactions list in `LaunchedEffect` (lines ~312-313)
  - Created `shouldShowTransactionsListFlow` computed value for immediate screen checking (lines ~471-473)
  - Created `transactionsListViewModel` at app level with synchronous creation (lines ~475-479)
  - Updated `TRANSACTIONS_LIST` screen to use app-level ViewModel (lines ~1135-1203)
  - Updated all flow reset blocks to properly handle transactions list flow:
    - PARTIES screen else block (line ~345)
    - WAREHOUSES screen else block (line ~363)
    - PRODUCTS screen else block (line ~380)
    - PAYMENT_METHODS screen else block (line ~400)
    - General else block - preserved flow state (line ~414)

## Key Changes Summary

1. **Synchronous ViewModel Creation**: By checking `currentScreen` directly, the ViewModel is created immediately when navigating to transactions screens
2. **Flow State Management**: Added `isInTransactionsListFlow` to track the flow lifecycle
3. **Proper Cleanup**: All flow reset blocks now include transactions list flow state
4. **No Timing Issues**: The computed `shouldShowTransactionsListFlow` eliminates the asynchronous timing problem

