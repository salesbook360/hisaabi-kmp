# Average Purchase Price and Profit Calculation Implementation

## Overview
This document describes the implementation of two critical features for the KMP POS application:
1. **Average Purchase Price Calculation** - Tracks the weighted average cost of products
2. **Profit Calculation** - Automatically calculates profit on sales and returns

Both implementations are based on the legacy Android application and maintain full compatibility.

---

## Part 1: Average Purchase Price Implementation

### What is Average Purchase Price?
Average Purchase Price is a cost calculation method using weighted average:
- **Formula**: `New Avg = (Current Total Value + Transaction Value) / (Current Qty + Transaction Qty)`
- Recalculates on every purchase or return to vendor
- Used to calculate profit margins accurately

### Implementation

#### 1. Database Layer (TransactionProcessorDao.kt)
Added three DAO methods:

```kotlin
// Get sum of product quantities across all warehouses
suspend fun getSumOfProductAvailableQuantity(productSlug: String?): Double?

// Get current average purchase price of a product
suspend fun getAvgPurchasePriceOfProduct(productSlug: String?): Double?

// Update the average purchase price of a product
suspend fun updateAvgPurchasePrice(productSlug: String?, newAvgPrice: Double)
```

#### 2. Business Logic (TransactionProcessor.kt)
Added methods:

**a. `updateProductAvgPurchasePrice()`**
- Called during transaction processing
- Skips services and recipes
- Calculates and updates average price for each product

**b. `calculateAvgPurchasePrice()`**
- Implements weighted average formula
- Handles purchase, vendor return, stock increase/reduce
- Includes safety checks for NaN and Infinity

**c. `reverseTransactionType()`**
- Reverses transaction type effect for updates/deletes

#### 3. Transaction Types Affected
- ✅ **Purchase** (Type 2) - Increases average
- ✅ **Vendor Return** (Type 3) - Decreases average
- ✅ **Stock Increase** (Type 14) - Increases average
- ✅ **Stock Reduce** (Type 15) - Decreases average

---

## Part 2: Profit Calculation Implementation

### What is Profit Calculation?
Profit tracks the margin between selling price and cost:
- **Formula**: `Profit = (Sale Price - Avg Purchase Price) × Quantity`
- Calculated automatically when saving transactions
- Negative profit for customer returns

### Implementation

#### 1. TransactionCalculator Utility (TransactionCalculator.kt)
Added `calculateProfit()` method:

```kotlin
fun calculateProfit(
    salePrice: Double,
    avgPurchasePrice: Double,
    quantity: Double,
    transactionType: Int
): Double {
    val profitPerUnit = salePrice - avgPurchasePrice
    
    return when (transactionType) {
        AllTransactionTypes.SALE.value -> {
            roundTo2Decimal(profitPerUnit * quantity)
        }
        AllTransactionTypes.CUSTOMER_RETURN.value -> {
            roundTo2Decimal(-1.0 * profitPerUnit * quantity)
        }
        else -> 0.0
    }
}
```

**Key Features:**
- Uses `AllTransactionTypes` enum (not hardcoded values)
- Returns 0.0 for non-sale transactions
- Negative profit for customer returns
- Rounded to 2 decimal places

#### 2. TransactionDetail Enhancement (TransactionDetail.kt)
Added helper method:

```kotlin
fun withCalculatedProfit(transactionType: Int): TransactionDetail {
    val avgPurchasePrice = product?.avgPurchasePrice ?: 0.0
    val calculatedProfit = TransactionCalculator.calculateProfit(
        salePrice = price,
        avgPurchasePrice = avgPurchasePrice,
        quantity = quantity,
        transactionType = transactionType
    )
    return copy(profit = calculatedProfit)
}
```

#### 3. Integration (TransactionsRepository.kt)
Profit calculation integrated into:
- `insertTransaction()` - New transactions
- `updateTransaction()` - Transaction updates
- `saveManufactureTransaction()` - Manufacture child transactions

All use `AllTransactionTypes` enum:
```kotlin
// Example from manufacture transaction
val ingredientsWithProfit = ingredients.map { ingredient ->
    ingredient.withCalculatedProfit(AllTransactionTypes.SALE.value)
}

val recipeWithProfit = recipeDetail.withCalculatedProfit(AllTransactionTypes.PURCHASE.value)
```

#### 4. Transaction Types Affected
- ✅ **Sale** (Type 0) - Positive profit
- ✅ **Customer Return** (Type 1) - Negative profit
- ❌ All other types - Zero profit

---

## Transaction Type Enum Usage

### AllTransactionTypes.kt
All transaction types are centralized in the `AllTransactionTypes` enum:

```kotlin
enum class AllTransactionTypes(val value: Int, val displayName: String, ...) {
    SALE(0, "Sale", ...),
    CUSTOMER_RETURN(1, "Customer Return", ...),
    PURCHASE(2, "Purchase", ...),
    VENDOR_RETURN(3, "Vendor Return", ...),
    // ... more types
}
```

### Why Use Enum Instead of Hardcoded Values?

**Before (❌ Bad):**
```kotlin
when (transactionType) {
    1 -> { /* SALE */ }  // What is 1? Need to check elsewhere
    5 -> { /* CUSTOMER_RETURN */ }  // Wrong value!
}
```

**After (✅ Good):**
```kotlin
when (transactionType) {
    AllTransactionTypes.SALE.value -> { /* SALE */ }  // Clear and correct
    AllTransactionTypes.CUSTOMER_RETURN.value -> { /* CUSTOMER_RETURN */ }
}
```

**Benefits:**
1. **Type Safety** - Compiler checks enum validity
2. **Readability** - Clear intent with named constants
3. **Maintainability** - Change once in enum, applies everywhere
4. **IDE Support** - Auto-completion and refactoring
5. **No Magic Numbers** - Self-documenting code

---

## Execution Order (Critical!)

Both features work together in a specific sequence:

```
1. Validate Transaction
   ↓
2. Insert Transaction
   ↓
3. Calculate Profit for Each Detail ← Uses CURRENT avg purchase price
   ↓
4. Insert Transaction Details with Profit
   ↓
5. Process Transaction:
   a. Update Party Balance
   b. Update Payment Method Balance
   c. Update Product Avg Purchase Price ← For FUTURE transactions
   d. Update Product Quantities
```

**Why This Order?**
- Profit must use the average price BEFORE this transaction
- Average price update happens AFTER profit calculation
- Ensures accuracy for both current and future transactions

---

## Example Scenarios

### Scenario 1: Purchase and Sale
```
Step 1: Purchase
- Buy 10 laptops @ $500 each
- Average Purchase Price = $500
- Profit = $0 (purchase transaction)

Step 2: Sale
- Sell 2 laptops @ $700 each
- Profit = ($700 - $500) × 2 = $400
- Average Purchase Price remains $500
- Stock: 8 laptops remaining
```

### Scenario 2: Multiple Purchases with Different Prices
```
Step 1: First Purchase
- Buy 5 laptops @ $500 each = $2,500
- Average Purchase Price = $500

Step 2: Second Purchase
- Buy 5 laptops @ $600 each = $3,000
- Total: 10 laptops worth $5,500
- New Average = $5,500 / 10 = $550

Step 3: Sale
- Sell 3 laptops @ $800 each
- Profit = ($800 - $550) × 3 = $750
```

### Scenario 3: Customer Return
```
Step 1: Sale
- Sell 5 laptops @ $700 each
- Profit = ($700 - $500) × 5 = $1,000

Step 2: Customer Returns 2
- Return 2 laptops @ $700 each
- Profit = -1 × ($700 - $500) × 2 = -$400
- Net Profit = $1,000 - $400 = $600
```

---

## Files Modified

### Average Purchase Price
1. `TransactionProcessorDao.kt` - Added 3 DAO methods
2. `TransactionProcessor.kt` - Added calculation logic

### Profit Calculation
1. `TransactionCalculator.kt` - Added profit calculation method
2. `TransactionDetail.kt` - Added helper method
3. `TransactionsRepository.kt` - Integrated profit calculation

### Using AllTransactionTypes Enum
- `TransactionCalculator.kt` - Import and use enum
- `TransactionsRepository.kt` - Use enum for transaction types

---

## Testing Recommendations

### Test Average Purchase Price
1. **Basic Purchase**: Verify average = purchase price
2. **Multiple Purchases**: Verify weighted average calculation
3. **Return to Vendor**: Verify average decreases correctly
4. **Transaction Update**: Verify old avg reversed, new calculated

### Test Profit Calculation
1. **Basic Sale**: Verify profit = (sale - avg) × qty
2. **Customer Return**: Verify negative profit
3. **Purchase**: Verify zero profit
4. **After Price Change**: Verify profit uses new average

### Test Integration
1. **Complete Flow**: Purchase → Sale → Return → Verify all calculations
2. **Manufacture**: Verify child transactions calculate correctly
3. **Concurrent Transactions**: Verify no race conditions

---

## Benefits

### For Business
1. **Accurate Cost Tracking** - Know true product costs
2. **Real-time Profit Margins** - See profit immediately
3. **Better Pricing Decisions** - Price based on actual costs
4. **Financial Reporting** - Accurate COGS and profit reports

### For Developers
1. **Type Safety** - Using enums prevents errors
2. **Maintainability** - Clear, self-documenting code
3. **Testability** - Easy to test with named constants
4. **Compatibility** - Matches legacy implementation exactly

---

## Notes

- Both features are integrated into transaction processing flow
- Automatic calculation - no manual intervention needed
- Uses immutable data classes for thread safety
- Fully compatible with legacy Android implementation
- Enum-based transaction types prevent magic number bugs
- Rounded to 2 decimal places for consistency
- Handles edge cases (NaN, Infinity, zero quantities)

