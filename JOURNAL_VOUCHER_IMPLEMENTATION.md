# Journal Voucher Implementation - Multiple Transactions

## Overview
Journal vouchers in Hisaabi are compound transactions that create multiple related transactions to maintain proper accounting records. When you save a journal voucher, it creates one parent transaction and multiple child transactions.

## How Journal Vouchers Work

### Example Scenario
You have:
- 1 Customer (Ali) - $50 **debit**
- 1 Vendor (Ahmed) - $50 **credit**
- Payment Method: Cash

### What Gets Created:
1. **Journal Voucher Transaction** (Parent) - Type 19
   - Total Debit: $50
   - Total Credit: $50
   - Payment Method: Cash
   - Description: "Transfer from customer to vendor"

2. **Get Cash from Customer** (Child) - Type 7
   - Parent Slug: Journal Voucher's slug
   - Customer: Ali
   - Amount: $50
   - Payment Method: Cash

3. **Pay Cash to Vendor** (Child) - Type 4
   - Parent Slug: Journal Voucher's slug
   - Vendor: Ahmed
   - Amount: $50
   - Payment Method: Cash

## Transaction Types Mapping

### Party-Based Transactions

| Party Type | Debit (Pay Cash) | Credit (Get Cash) |
|------------|------------------|-------------------|
| **Customer** (roleId = 1) | Type 6: Pay to Customer | Type 7: Get from Customer |
| **Vendor** (roleId = 2) | Type 4: Pay to Vendor | Type 5: Get from Vendor |
| **Investor** (roleId = 7) | Type 12: Investment Withdraw | Type 11: Investment Deposit |
| **Expense** (roleId = 5) | Type 8: Expense (positive amount) | Type 8: Expense (negative amount) |
| **Extra Income** (roleId = 6) | Type 9: Extra Income (positive amount) | Type 9: Extra Income (negative amount) |

### Payment Method Transactions
- **Payment Method**: Type 10 (Cash Transfer)
  - Debit: Money OUT from selected payment method TO the account's payment method
  - Credit: Money IN from the account's payment method TO selected payment method

## Implementation Details

### Step 1: Create Parent Transaction
```kotlin
val journalTransaction = Transaction(
    transactionType = 19, // TRANSACTION_TYPE_JOURNAL
    totalPaid = totalDebit,
    totalBill = totalDebit,
    description = description,
    paymentMethodToSlug = selectedPaymentMethod.slug,
    ...
)
```

### Step 2: Create Child Transactions for Each Account

#### For Party Accounts:
```kotlin
val transactionType = getTransactionTypeFromParty(party.roleId, isDebit)
val childTransaction = Transaction(
    parentSlug = parentSlug, // Links to journal voucher
    transactionType = transactionType,
    customerSlug = party.slug,
    party = party,
    totalPaid = amount,
    paymentMethodToSlug = selectedPaymentMethod.slug,
    ...
)
```

#### For Payment Method Accounts:
```kotlin
val childTransaction = Transaction(
    parentSlug = parentSlug,
    transactionType = 10, // CASH_TRANSFER
    paymentMethodFromSlug = if (isDebit) selectedPM.slug else accountPM.slug,
    paymentMethodToSlug = if (isDebit) accountPM.slug else selectedPM.slug,
    ...
)
```

### Step 3: Timestamps
Each child transaction gets a slightly incremented timestamp:
```kotlin
childTimestamp = parentTimestamp + timestampCounter
```
This ensures proper ordering when syncing to server.

## Complete Flow Example

### Journal Voucher Entry:
- Customer "Ali": $100 debit
- Vendor "Ahmed": $50 credit  
- Investor "Sara": $50 credit
- Payment Method: Cash

### Creates 4 Transactions:

1. **Journal Voucher** (Type 19)
   - Debit: $100
   - Credit: $100
   - PM: Cash

2. **Get Cash from Ali** (Type 7)
   - Parent: Journal Voucher
   - Customer: Ali
   - Amount: $100
   - PM: Cash

3. **Pay Cash to Ahmed** (Type 4)
   - Parent: Journal Voucher
   - Vendor: Ahmed
   - Amount: $50
   - PM: Cash

4. **Investment Deposit from Sara** (Type 11)
   - Parent: Journal Voucher
   - Investor: Sara
   - Amount: $50
   - PM: Cash

## Benefits of This Approach

✅ **Proper Accounting**: Each party's balance is updated correctly  
✅ **Transaction History**: Clear trail of money movement  
✅ **Parent-Child Relationship**: Easy to see all related transactions  
✅ **Payment Method Tracking**: Accurate tracking of cash/bank movements  
✅ **Reporting**: Detailed reports per party, payment method, and transaction type

## Transaction Detail Screen

When viewing a journal voucher detail:
- Shows the main journal voucher (Type 19)
- Shows payment method used
- Shows debit/credit totals
- Shows description

When viewing child transactions:
- Shows parent journal voucher reference
- Shows party details
- Shows amount paid/received
- Shows payment method

## Testing

To test the implementation:

1. **Create a Simple Journal Voucher**:
   - Add 1 customer with $50 debit
   - Add 1 vendor with $50 credit
   - Select payment method
   - Save
   - Check: Should create 3 transactions

2. **Create a Complex Journal Voucher**:
   - Add multiple customers and vendors
   - Add investors, expenses, and income
   - Save
   - Check: Should create 1 + N transactions (1 parent + N children)

3. **View Transactions List**:
   - Should see all transactions created
   - Each child should have parent_slug set

4. **View Transaction Details**:
   - Journal voucher should show totals
   - Child transactions should show party info and amounts

## Database Queries

### Get all child transactions of a journal voucher:
```sql
SELECT * FROM transactions WHERE parent_slug = 'JOURNAL_SLUG'
```

### Get journal voucher with children:
```sql
SELECT * FROM transactions 
WHERE slug = 'JOURNAL_SLUG' 
OR parent_slug = 'JOURNAL_SLUG'
ORDER BY timestamp
```

## Code Location

- **ViewModel**: `AddJournalVoucherViewModel.kt`
- **Helper Function**: `getTransactionTypeFromParty()`
- **Transaction Factory**: Similar to Android Native's `TransactionFactory.createDbTransactionFromParty()`

## Migration from Old Implementation

If you have existing journal vouchers that only created a single transaction:
- They will still be visible
- They won't show party information (no customer_slug set)
- **New journal vouchers** will use the correct multi-transaction approach
- Consider migrating old journal vouchers by:
  1. Reading the accounts from stored data
  2. Creating the child transactions
  3. Updating the parent transaction

## Summary

The journal voucher now works exactly like the Android Native version:
- **1 parent transaction** (Journal Voucher)
- **N child transactions** (one for each account)
- **Proper parent-child linking** via `parent_slug`
- **Correct transaction types** based on party role and debit/credit
- **Accurate balance updates** for all parties involved

