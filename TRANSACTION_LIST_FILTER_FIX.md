# Transaction List Filter - Child Transactions Hidden

## Problem
The transactions list was showing ALL transactions, including child transactions created by journal vouchers and other compound transactions. This cluttered the list and showed duplicate/related entries.

## Solution
Updated database queries to filter out child transactions from the main transaction lists.

## What Changed

### InventoryTransactionDao.kt

Updated all queries that return lists of transactions to exclude child transactions:

#### 1. getAllTransactions()
```kotlin
// Before
@Query("SELECT * FROM InventoryTransaction ORDER BY timestamp DESC")

// After  
@Query("SELECT * FROM InventoryTransaction WHERE parent_slug IS NULL OR parent_slug = '' ORDER BY timestamp DESC")
```

#### 2. getTransactionsByCustomer()
```kotlin
// Before
@Query("SELECT * FROM InventoryTransaction WHERE customer_slug = :customerSlug ORDER BY timestamp DESC")

// After
@Query("SELECT * FROM InventoryTransaction WHERE customer_slug = :customerSlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
```

#### 3. getTransactionsByType()
```kotlin
// Before
@Query("SELECT * FROM InventoryTransaction WHERE transaction_type = :transactionType ORDER BY timestamp DESC")

// After
@Query("SELECT * FROM InventoryTransaction WHERE transaction_type = :transactionType AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
```

#### 4. getTransactionsByBusiness()
```kotlin
// Before
@Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug ORDER BY timestamp DESC")

// After
@Query("SELECT * FROM InventoryTransaction WHERE business_slug = :businessSlug AND (parent_slug IS NULL OR parent_slug = '') ORDER BY timestamp DESC")
```

### New Helper Queries

Added queries to fetch child transactions when needed:

```kotlin
// Get child transactions as Flow
@Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug ORDER BY timestamp ASC")
fun getChildTransactions(parentSlug: String): Flow<List<InventoryTransactionEntity>>

// Get child transactions as List
@Query("SELECT * FROM InventoryTransaction WHERE parent_slug = :parentSlug ORDER BY timestamp ASC")
suspend fun getChildTransactionsList(parentSlug: String): List<InventoryTransactionEntity>
```

## How It Works

### Parent vs Child Transactions

**Parent Transactions** (shown in list):
- `parent_slug` is NULL or empty string
- Examples: Journal Vouchers, Sales, Purchases, Stock Adjustments

**Child Transactions** (hidden from list):
- `parent_slug` contains the parent transaction's slug
- Examples: Individual party payments from journal vouchers

### Filter Logic

```sql
WHERE parent_slug IS NULL OR parent_slug = ''
```

This filters out any transaction that has a parent, showing only top-level transactions.

## Transaction Types in Lists

### What You'll See:
✅ Journal Voucher (Type 19) - The parent  
✅ Sale (Type 1)  
✅ Purchase (Type 3)  
✅ Payment Transfer (Type 10) - If standalone  
✅ Records (Types 21-25)  
✅ Stock Adjustments (Types 13-15)  

### What You Won't See:
❌ Pay to Customer (Type 6) - If it's a child of journal voucher  
❌ Get from Vendor (Type 5) - If it's a child of journal voucher  
❌ Investment transactions - If they're children  
❌ Any other child transaction with a parent_slug set

## Viewing Child Transactions

Child transactions can still be accessed:

### 1. Transaction Detail Screen
When viewing a journal voucher detail, you can use `getChildTransactions()` to show all related transactions.

### 2. Party Ledger
Party-specific transactions will show parent entries, and detail views can expand to show children.

### 3. Reports
Custom reports can include or exclude child transactions as needed using the appropriate queries.

## Example Scenario

### Journal Voucher Created:
- Customer (Ali): $100 debit
- Vendor (Ahmed): $100 credit

### Transactions Created:
1. **Journal Voucher** (Type 19) - `parent_slug = NULL` ✅ SHOWS IN LIST
2. **Get from Customer** (Type 7) - `parent_slug = "JOURNAL_SLUG"` ❌ HIDDEN
3. **Pay to Vendor** (Type 4) - `parent_slug = "JOURNAL_SLUG"` ❌ HIDDEN

### What User Sees:
- Transactions List: Only 1 transaction (Journal Voucher)
- Journal Voucher Detail: Shows the parent + 2 child transactions

## Dashboard Queries

Dashboard queries are **not affected** because they:
- Filter by specific transaction types
- Child transactions have different types than what dashboards look for
- Example: Sales dashboard looks for Type 1, won't find Type 6 or 7

## Benefits

✅ **Cleaner List**: Shows only meaningful parent transactions  
✅ **No Duplication**: Avoids showing the same money movement multiple times  
✅ **Better UX**: Users see logical transactions, not internal bookkeeping  
✅ **Proper Hierarchy**: Parent-child relationship is maintained  
✅ **Accurate Counts**: Transaction count reflects actual business transactions  

## Migration Notes

- **Existing Data**: Old transactions without parent_slug will continue to show normally
- **New Journal Vouchers**: Will create parent + children, but only parent shows in list
- **No Data Loss**: All transactions remain in database, just filtered in display
- **Backward Compatible**: Queries handle both NULL and empty string for parent_slug

## Testing

1. **Create a Journal Voucher**:
   - Add 2 parties
   - Save
   - Check: Should see only 1 transaction in list (the journal voucher)

2. **View Journal Voucher Detail**:
   - Click on the journal voucher
   - Should see parent transaction info
   - (Future) Will show child transactions

3. **Filter by Transaction Type**:
   - Filter for "Journal Voucher"
   - Should only see parent journal vouchers
   - Child payment transactions won't appear

4. **Check Other Transaction Types**:
   - Create standalone Sale, Purchase, etc.
   - Should all appear normally (they have no parent)

## Files Modified

- `InventoryTransactionDao.kt` - Updated all list queries to filter parent_slug
  
## Summary

The transaction list now shows only parent transactions, providing a clean and logical view of business transactions while maintaining the complete parent-child relationship structure in the database for accurate accounting.

