# Test Data Guide - Populate Dashboard

## 🎯 Quick Start: Add Test Data to See Dashboard in Action

Since your database is currently empty, the Dashboard will show all zeros. Here's how to add test data to see the Dashboard with real metrics.

---

## Option 1: Create a Data Seeder (Recommended)

Create this file to populate test data:

### **`DataSeeder.kt`**

```kotlin
package com.hisaabi.hisaabi_kmp.database

import com.hisaabi.hisaabi_kmp.database.dao.*
import com.hisaabi.hisaabi_kmp.database.entity.*
import kotlinx.datetime.Clock

class DataSeeder(
    private val partyDao: PartyDao,
    private val productDao: ProductDao,
    private val inventoryTransactionDao: InventoryTransactionDao,
    private val transactionDetailDao: TransactionDetailDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val productQuantitiesDao: ProductQuantitiesDao,
    private val categoryDao: CategoryDao
) {
    
    private val businessSlug = "default_business"
    private val currentTime = Clock.System.now().toEpochMilliseconds()
    
    suspend fun seedDatabase() {
        seedPaymentMethods()
        seedCategories()
        seedParties()
        seedProducts()
        seedTransactions()
        println("✅ Test data seeded successfully!")
    }
    
    private suspend fun seedPaymentMethods() {
        paymentMethodDao.insertPaymentMethod(
            PaymentMethodEntity(
                title = "Cash",
                description = "Cash in hand",
                amount = 50000.0,
                opening_amount = 50000.0,
                status_id = 1,
                slug = "payment-cash",
                business_slug = businessSlug
            )
        )
        
        paymentMethodDao.insertPaymentMethod(
            PaymentMethodEntity(
                title = "Bank Account",
                description = "Main bank account",
                amount = 100000.0,
                opening_amount = 100000.0,
                status_id = 1,
                slug = "payment-bank",
                business_slug = businessSlug
            )
        )
    }
    
    private suspend fun seedCategories() {
        categoryDao.insertCategory(
            CategoryEntity(
                title = "Electronics",
                type_id = 1,
                slug = "cat-electronics",
                business_slug = businessSlug
            )
        )
        
        categoryDao.insertCategory(
            CategoryEntity(
                title = "Clothing",
                type_id = 1,
                slug = "cat-clothing",
                business_slug = businessSlug
            )
        )
    }
    
    private suspend fun seedParties() {
        // Customers
        for (i in 1..10) {
            partyDao.insertParty(
                PartyEntity(
                    name = "Customer $i",
                    phone = "+92300000000$i",
                    address = "Address $i",
                    balance = (i * -1000.0), // Negative = they owe us
                    opening_balance = 0.0,
                    role_id = 1, // Customer
                    person_status = 1,
                    slug = "customer-$i",
                    business_slug = businessSlug,
                    created_at = currentTime.toString()
                )
            )
        }
        
        // Vendors/Suppliers
        for (i in 1..5) {
            partyDao.insertParty(
                PartyEntity(
                    name = "Supplier $i",
                    phone = "+92310000000$i",
                    address = "Supplier Address $i",
                    balance = (i * 2000.0), // Positive = we owe them
                    opening_balance = 0.0,
                    role_id = 2, // Vendor
                    person_status = 1,
                    slug = "vendor-$i",
                    business_slug = businessSlug,
                    created_at = currentTime.toString()
                )
            )
        }
    }
    
    private suspend fun seedProducts() {
        for (i in 1..20) {
            // Add Product
            productDao.insertProduct(
                ProductEntity(
                    title = "Product $i",
                    description = "Description for product $i",
                    retail_price = (i * 100.0),
                    wholesale_price = (i * 80.0),
                    purchase_price = (i * 60.0),
                    status_id = 1,
                    category_slug = if (i % 2 == 0) "cat-electronics" else "cat-clothing",
                    slug = "product-$i",
                    business_slug = businessSlug
                )
            )
            
            // Add Stock
            productQuantitiesDao.insertProductQuantity(
                ProductQuantitiesEntity(
                    product_slug = "product-$i",
                    warehouse_slug = "warehouse-main",
                    current_quantity = (100.0 - i * 2), // Some will be low stock
                    minimum_quantity = 10.0,
                    maximum_quantity = 500.0,
                    opening_quantity = 100.0,
                    business_slug = businessSlug
                )
            )
        }
    }
    
    private suspend fun seedTransactions() {
        // Add Sales Transactions
        for (i in 1..15) {
            val transactionSlug = "sale-$i"
            
            // Insert transaction
            inventoryTransactionDao.insertTransaction(
                InventoryTransactionEntity(
                    customer_slug = "customer-${i % 10 + 1}",
                    total_bill = (i * 5000.0),
                    total_paid = (i * 3000.0),
                    discount = (i * 100.0),
                    tax = (i * 200.0),
                    additional_charges = 0.0,
                    transaction_type = 1, // SALE
                    status_id = 1,
                    timestamp = (currentTime - (i * 24 * 60 * 60 * 1000)).toString(), // Spread over days
                    slug = transactionSlug,
                    business_slug = businessSlug
                )
            )
            
            // Insert transaction details
            transactionDetailDao.insertTransactionDetail(
                TransactionDetailEntity(
                    transaction_slug = transactionSlug,
                    product_slug = "product-${i % 20 + 1}",
                    quantity = (i * 2.0),
                    price = (i * 100.0),
                    profit = (i * 40.0),
                    flat_tax = (i * 20.0),
                    tax_type = 1,
                    discount_type = 0,
                    flat_discount = 0.0,
                    slug = "detail-sale-$i",
                    business_slug = businessSlug
                )
            )
        }
        
        // Add Purchase Transactions
        for (i in 1..10) {
            inventoryTransactionDao.insertTransaction(
                InventoryTransactionEntity(
                    customer_slug = "vendor-${i % 5 + 1}",
                    total_bill = (i * 4000.0),
                    total_paid = (i * 4000.0),
                    transaction_type = 4, // PURCHASE
                    status_id = 1,
                    timestamp = (currentTime - (i * 24 * 60 * 60 * 1000)).toString(),
                    slug = "purchase-$i",
                    business_slug = businessSlug
                )
            )
        }
        
        // Add Payment Transactions
        for (i in 1..5) {
            // Payment Received
            inventoryTransactionDao.insertTransaction(
                InventoryTransactionEntity(
                    customer_slug = "customer-${i % 10 + 1}",
                    total_paid = (i * 2000.0),
                    transaction_type = 7, // PAYMENT_IN
                    status_id = 1,
                    timestamp = (currentTime - (i * 12 * 60 * 60 * 1000)).toString(),
                    slug = "payment-in-$i",
                    business_slug = businessSlug
                )
            )
            
            // Payment Made
            inventoryTransactionDao.insertTransaction(
                InventoryTransactionEntity(
                    customer_slug = "vendor-${i % 5 + 1}",
                    total_paid = (i * 1500.0),
                    transaction_type = 8, // PAYMENT_OUT
                    status_id = 1,
                    timestamp = (currentTime - (i * 12 * 60 * 60 * 1000)).toString(),
                    slug = "payment-out-$i",
                    business_slug = businessSlug
                )
            )
        }
    }
}
```

### **How to Use the Seeder**

1. Add DataSeeder to Koin DI in `DatabaseModule.kt`:
```kotlin
single { 
    DataSeeder(
        partyDao = get(),
        productDao = get(),
        inventoryTransactionDao = get(),
        transactionDetailDao = get(),
        paymentMethodDao = get(),
        productQuantitiesDao = get(),
        categoryDao = get()
    )
}
```

2. Call it once when app starts (in MainActivity or App.kt):
```kotlin
val dataSeeder: DataSeeder = koinInject()
LaunchedEffect(Unit) {
    dataSeeder.seedDatabase()
}
```

---

## Option 2: Import from Existing Database

If you want to copy data from your existing `database_db.db`:

### **Using Android Studio**

1. Copy `database_db.db` to Android app's database directory
2. App will use existing data
3. Room will recognize the schema

### **Programmatic Import**

Create a migration script to copy data:
```kotlin
// Read from old SQLite DB
// Insert into Room DB via DAOs
```

---

## Option 3: Manual Testing via Debug Console

Add this temporary button to test data insertion:

```kotlin
Button(onClick = {
    viewModelScope.launch {
        // Quick test transaction
        inventoryTransactionDao.insertTransaction(
            InventoryTransactionEntity(
                total_bill = 1000.0,
                total_paid = 800.0,
                transaction_type = 1,
                status_id = 1,
                business_slug = "default_business",
                slug = "test-${Clock.System.now().toEpochMilliseconds()}",
                timestamp = Clock.System.now().toEpochMilliseconds().toString()
            )
        )
    }
}) {
    Text("Add Test Transaction")
}
```

---

## 📊 Expected Dashboard After Seeding

With the seeder data above, you should see:

### **Balance Overview**
- Total Balance: ₹150,000 (from payment methods)
- Customers: ₹10,000+ receivable
- Vendors: ₹10,000+ payable
- Net Balance: ~₹0

### **Payment Overview (This Month)**
- Total Received: ₹30,000+
- Total Paid: ₹22,500+
- Net Received: ₹7,500+

### **Sales Overview (This Month)**
- Total Sales: 15 transactions
- Total Revenue: ₹300,000+
- Total Cost: ₹180,000+
- Total Profit: ₹120,000+

### **Purchase Overview (This Month)**
- No. of Purchase: 10
- Purchase Cost: ₹200,000+
- Purchase Orders: 0
- Returns: 0

### **Inventory Summary**
- Qty in Hand: ~1,560 units
- Will be Received: 0
- Low Stock Products: ~10

### **Parties Summary**
- Total Customers: 10
- Total Suppliers: 5
- Total Investors: 0

### **Products Summary**
- Total Products: 20
- Low Stock Products: ~10
- Categories: 2

---

## 🔧 Troubleshooting

### **Dashboard Shows All Zeros**
- ✅ This is normal if database is empty
- Add test data using seeder
- Or wait for real data from API sync

### **Data Not Updating**
- Tap refresh button in Dashboard toolbar
- Check business_slug matches ("default_business")
- Verify transactions have status_id = 1 (active)

### **Build Errors**
- Make sure you're using Java 17
- Run `./gradlew clean build`
- Check all imports are correct

---

## 📝 Next Steps

1. **Add DataSeeder class** (code above)
2. **Run the seeder** once to populate database
3. **Open the app** and navigate to Dashboard
4. **See real metrics** calculated from database
5. **Test filters** by changing time periods
6. **Test refresh** by tapping refresh icon

---

**The Dashboard is ready to display real data as soon as you populate the database!** 🎊

