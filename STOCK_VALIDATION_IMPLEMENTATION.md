# Stock Validation Implementation

## Overview
This document describes the implementation of stock validation to prevent negative inventory in the Hisaabi KMP application. The validation ensures that stock cannot go negative when saving or updating any transaction.

## Implementation Details

### 1. TransactionProcessor Validation

#### Location
`composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/data/repository/TransactionProcessor.kt`

#### New Methods

**`validateTransaction(transaction: Transaction, oldTransaction: Transaction?): Result<Unit>`**
- Main validation method called before processing any transaction
- Accepts both new transactions and old transactions (for updates)
- Returns `Result.success` if valid, `Result.failure` with error message if invalid
- Currently validates stock, but designed to be extensible for other validations

**`validateStock(transaction: Transaction, oldTransaction: Transaction?): Result<Unit>`**
- Private method that performs the actual stock validation
- Checks each product in transaction details
- Skips service products and recipes (no physical stock)
- Calculates net stock change (for updates, considers both old and new quantities)
- Gets current stock from database
- Returns detailed error message if stock would go negative

#### Validation Logic

1. **For New Transactions:**
   - Calculates how much stock will be adjusted
   - Checks current stock levels
   - Ensures `current_stock + adjustment >= 0`

2. **For Transaction Updates:**
   - Calculates old adjustment (what the old transaction did)
   - Calculates new adjustment (what the new transaction will do)
   - Net change = new adjustment - old adjustment
   - Validates that current stock + net change >= 0

3. **Error Messages:**
   ```
   Insufficient stock for 'Product Name'.
   Available: X, Required: Y, Short by: Z
   ```

### 2. TransactionsRepository Integration

#### Location
`composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/data/repository/TransactionsRepository.kt`

#### Updated Methods

**`insertTransaction(transaction: Transaction): Result<String>`**
- Calls `transactionProcessor.validateTransaction(transaction, null)` BEFORE any database changes
- Returns validation error immediately if validation fails
- Proceeds with insertion only if validation passes

**`updateTransaction(transaction: Transaction): Result<Unit>`**
- Loads old transaction first
- Calls `transactionProcessor.validateTransaction(transaction, oldTransaction)` BEFORE any database changes
- Returns validation error immediately if validation fails
- Proceeds with update only if validation passes

**`saveManufactureTransaction(...): Result<String>`**
- Creates a temporary sale transaction to validate ingredient availability
- Validates BEFORE creating any of the 3 transactions (parent, sale, purchase)
- Returns validation error immediately if validation fails
- This prevents manufacturing when ingredients are insufficient

### 3. Validation Order

The validation occurs in the following order (CRITICAL):

```
1. validateTransaction() called
   ↓
2. validateStock() checks all products
   ↓
3. If validation fails → return error immediately (no database changes)
   ↓
4. If validation passes → proceed with transaction
   ↓
5. Insert/update transaction in database
   ↓
6. Process transaction (update balances)
```

**Key Point:** Validation happens BEFORE:
- Any database inserts/updates
- Any balance updates (party, payment method, stock)
- Any transaction processing

### 4. Transaction Types Affected

The validation applies to all transaction types that affect stock:

#### Stock Reducing Transactions (require validation):
- **SALE** (type: 1) - Reduces stock
- **VENDOR_RETURN** (type: 10) - Reduces stock
- **STOCK_REDUCE** (type: 8) - Reduces stock
- **STOCK_TRANSFER** (type: 9) - Reduces stock from source warehouse
- **MANUFACTURE** (child sale transaction) - Reduces ingredient stock

#### Stock Increasing Transactions (no validation needed):
- **PURCHASE** (type: 2) - Increases stock
- **CUSTOMER_RETURN** (type: 4) - Increases stock
- **STOCK_INCREASE** (type: 7) - Increases stock

#### Non-Stock Transactions (validation skipped):
- Service products (no physical stock)
- Recipe products (no physical stock)
- All other transaction types (records, cash transactions, etc.)

### 5. Use Case Integration

All transaction creation flows go through validated repository methods:

- `AddTransactionUseCase` → `insertTransaction()` ✓
- `UpdateTransactionUseCase` → `updateTransaction()` ✓
- Manufacture transactions → `saveManufactureTransaction()` ✓
- All other transactions → standard insert/update methods ✓

### 6. ViewModel Integration

All ViewModels use the use cases, which means validation is automatically applied:

- `AddTransactionViewModel` - Sale/Purchase transactions
- `StockAdjustmentViewModel` - Stock increase/reduce/transfer
- `AddManufactureViewModel` - Manufacturing transactions
- `AddExpenseIncomeViewModel` - Expense/Income transactions (no stock)
- `PayGetCashViewModel` - Cash transactions (no stock)
- `PaymentTransferViewModel` - Payment transfers (no stock)
- `AddRecordViewModel` - Records (no stock)
- `AddJournalVoucherViewModel` - Journal vouchers (no stock)

## Error Handling

### User-Facing Error Messages

When validation fails, the user sees a clear error message:

```
Insufficient stock for 'Laptop'.
Available: 5.0, Required: 10.0, Short by: 5.0
```

### Error Propagation

1. `TransactionProcessor.validateTransaction()` returns `Result.failure(Exception(message))`
2. `TransactionsRepository` catches the failure and returns it
3. `UseCase` propagates the Result to ViewModel
4. `ViewModel` displays error in UI state
5. User sees the error message in the UI

## Testing Scenarios

### Scenario 1: New Sale with Insufficient Stock
- Current stock: 10 units
- Try to sell: 15 units
- **Result:** Error - "Insufficient stock... Short by: 5.0"

### Scenario 2: Update Transaction Increasing Quantity
- Old transaction: Sold 5 units
- Update to: Sold 10 units
- Current stock: 2 units
- Net change: -5 units (additional reduction)
- **Result:** Error if current stock + (-5) < 0

### Scenario 3: Manufacturing with Insufficient Ingredients
- Recipe requires: Ingredient A (10 units), Ingredient B (5 units)
- Available: Ingredient A (8 units), Ingredient B (5 units)
- **Result:** Error - "Insufficient stock for 'Ingredient A'... Short by: 2.0"

### Scenario 4: Stock Transfer with Insufficient Source Stock
- Transfer 20 units from Warehouse A to Warehouse B
- Warehouse A stock: 15 units
- **Result:** Error - "Insufficient stock... Short by: 5.0"

### Scenario 5: Service Products (No Validation)
- Sell service product (consulting)
- **Result:** Success (services have no physical stock)

### Scenario 6: Recipe Products (No Validation)
- Sell recipe product as finished good
- **Result:** Success (recipe itself doesn't require stock validation during sale)

## Database Structure

### ProductQuantities Table
```sql
CREATE TABLE ProductQuantities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_slug TEXT,
    warehouse_slug TEXT,
    opening_quantity REAL DEFAULT 0.0,
    current_quantity REAL DEFAULT 0.0,  -- Used for validation
    minimum_quantity REAL DEFAULT 0.0,
    maximum_quantity REAL DEFAULT 0.0,
    business_slug TEXT,
    sync_status INTEGER DEFAULT 0,
    UNIQUE(product_slug, warehouse_slug)
)
```

## Benefits

1. **Data Integrity:** Prevents negative stock in the database
2. **User Experience:** Clear error messages guide users
3. **Business Logic:** Ensures accurate inventory tracking
4. **Audit Trail:** Stock issues caught before database changes
5. **Extensibility:** Validation framework can be extended for other validations

## Future Enhancements

Potential additions to the validation framework:

1. **Payment Method Balance Validation:** Prevent negative balances in payment methods
2. **Party Credit Limit Validation:** Enforce credit limits for customers
3. **Batch/Serial Number Validation:** Validate batch numbers for products
4. **Expiry Date Validation:** Warn about selling expired products
5. **Minimum Stock Warnings:** Alert when stock falls below minimum threshold

## Technical Notes

### Performance
- Validation queries are simple SELECT statements
- No loops over large datasets
- Each product is validated individually
- Minimal performance impact

### Concurrency
- Database transactions ensure atomicity
- Validation and processing happen in same transaction
- No race conditions between validation and processing

### Error Recovery
- No partial transactions (all-or-nothing)
- Database remains consistent on validation failure
- Users can retry after adjusting quantities

## Conclusion

The stock validation implementation successfully prevents negative inventory by:
1. Validating ALL transactions before database changes
2. Considering both new transactions and updates
3. Providing clear error messages to users
4. Maintaining data integrity and consistency

All transaction flows now include stock validation, ensuring accurate inventory management across the entire application.


