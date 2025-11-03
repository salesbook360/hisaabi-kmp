# Transaction Processor Implementation

## Overview
This document describes the centralized transaction processor implementation for the Hisaabi POS application. The processor handles all balance updates for customers, vendors, products, and payment methods.

## Architecture

### Core Components

#### 1. TransactionProcessorDao
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/dao/TransactionProcessorDao.kt`

Centralized DAO for all balance update operations:
- **Product Stock Operations**: Update quantities in warehouses
- **Payment Method Operations**: Update payment method balances
- **Party Balance Operations**: Update customer/vendor balances

Key methods:
- `updateProductQuantity(productSlug, warehouseSlug, quantityToAdd)`
- `updatePaymentMethodAmount(paymentMethodSlug, amountToAdd)`
- `updatePartyBalance(partySlug, addUpBalance)`
- `getAvailableQuantity(productSlug, warehouseSlug)`
- `getCurrentPartyBalance(partySlug)`
- `getPaymentMethodBalance(paymentMethodSlug)`

#### 2. TransactionProcessor
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/transactions/data/repository/TransactionProcessor.kt`

Centralized transaction processing logic that handles:
- Party balance calculations
- Payment method balance calculations
- Product stock quantity calculations
- Transaction reversal (for updates and deletes)
- Nested transaction processing

Key methods:
- `processTransaction(transaction, isReverse)` - Main processing method
- `processNestedTransaction(transaction, isReverse)` - For child transactions
- `reverseTransaction(transaction)` - Reverse transaction effects
- `updatePartyBalance(transaction, isReverse)` - Update party balances
- `updatePaymentMethodBalance(transaction, isReverse)` - Update payment methods
- `updateProductQuantities(transaction, isReverse)` - Update stock

#### 3. Integration with TransactionsRepository

The TransactionProcessor is integrated into the TransactionsRepository and is automatically called for:
- **Insert**: After inserting transaction and details, processes to update balances
- **Update**: Reverses old transaction, applies new transaction
- **Delete**: Reverses transaction before deletion
- **Manufacture**: Processes child Sale and Purchase transactions

## Balance Calculation Logic

### Party Balance Adjustments

Based on transaction type, party balance is adjusted as follows:

**INCREASE party balance** (customer/vendor owes less):
- SALE (0)
- GET_FROM_CUSTOMER (7)
- VENDOR_RETURN (3)
- GET_FROM_VENDOR (5)

**DECREASE party balance** (customer/vendor owes more):
- PURCHASE (2)
- PAY_TO_VENDOR (4)
- CUSTOMER_RETURN (1)
- PAY_TO_CUSTOMER (6)

Formula: `balanceAdjustment = -(totalBill - totalPaid)` for increases, `+(totalBill - totalPaid)` for decreases

### Payment Method Balance Adjustments

Based on transaction type, payment method balance is adjusted as follows:

**DECREASE payment method** (money goes out):
- CUSTOMER_RETURN (1)
- PURCHASE (2)
- PAY_TO_VENDOR (4)
- PAY_TO_CUSTOMER (6)
- EXPENSE (8)
- INVESTMENT_WITHDRAW (12)

**INCREASE payment method** (money comes in):
- INVESTMENT_DEPOSIT (11)
- EXTRA_INCOME (9)
- PAYMENT_TRANSFER (10)
- GET_FROM_CUSTOMER (7)
- GET_FROM_VENDOR (5)
- VENDOR_RETURN (3)
- SALE (0)

Formula: `amountAdjustment = -totalPaid` for decreases, `+totalPaid` for increases

**Special Case - Payment Transfer**: Affects both `paymentMethodFrom` (negative) and `paymentMethodTo` (positive)

### Product Stock Quantity Adjustments

Based on transaction type, product quantity is adjusted as follows:

**INCREASE stock**:
- PURCHASE (2)
- CUSTOMER_RETURN (1)
- STOCK_INCREASE (14)

**DECREASE stock**:
- SALE (0)
- VENDOR_RETURN (3)
- STOCK_REDUCE (15)
- STOCK_TRANSFER (13)

Formula: `quantityAdjustment = +quantity` for increases, `-quantity` for decreases

**Special Cases**:
- **STOCK_TRANSFER**: Affects both `warehouseFrom` (negative) and `warehouseTo` (positive)
- **Service products**: Skipped (no physical stock)
- **Recipes**: Skipped (no physical stock)

## Transaction Processing Flow

### Normal Transaction (Insert)

```
1. Generate slugs for transaction and details
2. Insert transaction entity
3. Insert transaction details entities
4. Load transaction with all details
5. Process transaction (update balances)
```

### Transaction Update

```
1. Load old transaction with all details
2. Reverse old transaction (remove balance effects)
3. Update transaction entity
4. Delete old details
5. Insert new details
6. Load updated transaction with all details
7. Process updated transaction (apply new balance effects)
```

### Transaction Delete

```
1. Load transaction with all details
2. Reverse transaction (remove balance effects)
3. Delete transaction details
4. Delete transaction entity
```

### Manufacture Transaction

```
1. Create parent Manufacture transaction
2. Create child Sale transaction (ingredients)
3. Create child Purchase transaction (recipe)
4. Process child Sale transaction (reduce stock)
5. Process child Purchase transaction (increase stock)
```

### Journal Voucher Transaction

```
1. Create parent Journal Voucher transaction
2. Create child transactions for each account:
   - Party accounts (Customer, Vendor, Investor, Expense, Income)
   - Payment method transfers
3. Each child transaction is processed automatically
```

## Database Schema Updates

### AppDatabase
- **Version**: Updated from 2 to 3
- **New DAO**: `transactionProcessorDao()`

### TransactionProcessorDao
- Extends Room DAO
- Provides atomic balance update operations
- Uses direct SQL for performance

## Dependencies

### Modules
- `databaseModule` - Provides TransactionProcessorDao
- `transactionsModule` - Provides TransactionProcessor
- `TransactionsRepository` - Integrates TransactionProcessor

### DI Setup

```kotlin
val transactionsModule = module {
    // Transaction Processor
    single {
        TransactionProcessor(
            transactionProcessorDao = get(),
            productQuantitiesDao = get()
        )
    }
    
    // Repository (with processor)
    single { 
        TransactionsRepository(
            localDataSource = get(),
            partiesRepository = get(),
            paymentMethodsRepository = get(),
            warehousesRepository = get(),
            productsRepository = get(),
            quantityUnitsRepository = get(),
            slugGenerator = get(),
            transactionProcessor = get()
        ) 
    }
}
```

## Testing Considerations

When testing the transaction processor:

1. **Balance Accuracy**: Verify balances update correctly for all transaction types
2. **Reversal Logic**: Test that updates and deletes properly reverse and reapply
3. **Nested Transactions**: Verify Manufacture and Journal Voucher child transactions
4. **Edge Cases**: 
   - Zero balances
   - Negative balances
   - Missing entities (null checks)
   - Stock transfers between warehouses
   - Payment transfers between methods
5. **Concurrency**: Ensure transaction processing is atomic and thread-safe

## Legacy Compatibility

This implementation is based on the working legacy implementation:
- **Location**: `HisaabiAndroidNative/app/src/main/java/com/hisaabi/mobileapp/utils/TransactionProcessorImp.kt`
- **Logic**: All balance calculation logic matches the legacy implementation
- **Types**: All transaction type values match the legacy system

## Key Features

✅ Centralized transaction processing
✅ Automatic balance updates for all transaction types
✅ Support for nested transactions (Manufacture, Journal Voucher)
✅ Transaction reversal for updates and deletes
✅ Stock transfer between warehouses
✅ Payment transfer between methods
✅ Atomic database operations
✅ Thread-safe implementation

## Future Enhancements

Potential improvements:
- Transaction validation before processing
- Stock level checking before processing
- Audit logging of balance changes
- Batch transaction processing
- Rollback support for failed transactions


