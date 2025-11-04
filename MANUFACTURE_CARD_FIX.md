# Manufacture Transaction Card Fix

## Problem
The Manufacture transaction card in the transactions list screen was showing:
1. No recipe name or quantity
2. Total cost as ₨0.00

## Root Cause
Manufacture transactions store data differently than other transactions:
- **Parent transaction** (what's shown in the list) has empty `transactionDetails`
- **Child transactions** contain the actual data:
  - Child Sale transaction (type 1): Contains ingredients used
  - Child Purchase transaction (type 2): Contains the manufactured product/recipe with quantity

The card was trying to:
1. Get recipe info from `transaction.transactionDetails` (which is empty)
2. Calculate cost using `calculateManufacturingCost()` which iterates over empty details

## Solution

### 1. Created New Use Case
**File:** `GetChildTransactionsUseCase.kt`
- Added use case to fetch child transactions for a parent transaction
- Allows accessing ingredient and recipe data from child transactions

### 2. Updated State Management
**File:** `TransactionsListViewModel.kt`
- Added `ManufactureInfo` data class to store recipe name and quantity
- Added `manufactureInfo: Map<String, ManufactureInfo>` to state
- Updated `loadTransactions()` to:
  - Load child transactions for each manufacture transaction
  - Find the Purchase child transaction (contains recipe)
  - Extract recipe name and quantity from the child transaction's details
  - Store in the `manufactureInfo` map

### 3. Updated UI
**File:** `TransactionsListScreen.kt`
- Updated `TransactionCard` to accept `manufactureInfo` map
- Modified `ManufactureCard` to:
  - Accept optional `ManufactureInfo` parameter
  - Display recipe name and quantity from `manufactureInfo`
  - Use `transaction.totalPaid` directly for cost (already stored on parent)
  - Remove dependency on `calculateManufacturingCost()`

### 4. Updated Dependency Injection
**File:** `TransactionsModule.kt`
- Registered `GetChildTransactionsUseCase`
- Added to `TransactionUseCases` aggregator

## Changes Made

### New Files
- `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/domain/usecase/GetChildTransactionsUseCase.kt`

### Modified Files
1. `TransactionUseCases.kt` - Added `getChildTransactions` use case
2. `TransactionsModule.kt` - Registered new use case in DI
3. `TransactionsListViewModel.kt`:
   - Added `ManufactureInfo` data class
   - Added `manufactureInfo` to state
   - Load manufacture info in `loadTransactions()`
4. `TransactionsListScreen.kt`:
   - Pass `manufactureInfo` to `TransactionCard`
   - Update `ManufactureCard` to use manufacture info and `totalPaid`
   - Remove unused `calculateManufacturingCost` import

## Result

Now the Manufacture transaction card correctly displays:
- ✅ Recipe name (e.g., "Pizza", "Burger")
- ✅ Total manufacturing cost from `transaction.totalPaid` (includes ingredients + additional charges) - displayed on the left
- ✅ Manufactured quantity with unit (e.g., "10 pcs", "5 kg") - displayed on the right

## Technical Details

### Data Flow
1. User views transaction list
2. ViewModel loads all transactions
3. For each manufacture transaction:
   - Fetch child transactions using `getChildTransactions()`
   - Find Purchase child (type 2) which has the recipe
   - Load its transaction details to get product name and quantity
   - Store in `manufactureInfo` map
4. UI receives manufacture info and displays it on the card

### Cost Calculation
The parent manufacture transaction's `totalPaid` field stores:
```
totalPaid = Σ(ingredient.price × ingredient.quantity) + additionalCharges
```

This is set when creating the transaction and doesn't need recalculation in the list view.

## Performance Considerations
- Child transactions are loaded only for manufacture transactions (not all transactions)
- Loading happens asynchronously in the ViewModel
- Errors loading manufacture info are silently ignored (card shows without recipe info)
- The parent transaction's `totalPaid` is used directly (no calculation needed)

