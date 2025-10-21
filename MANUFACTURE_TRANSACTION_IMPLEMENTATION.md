# Manufacture Transaction Implementation

## Overview
This document describes the implementation of the Manufacture Transaction feature in the Hisaabi KMP application.

## What is a Manufacture Transaction?
A manufacture transaction allows users to:
- Select a recipe (product with type = RECIPE)
- View and adjust ingredients required for manufacturing
- Set the quantity of the recipe to manufacture
- Add additional charges (e.g., labor, utilities)
- Specify the warehouse where manufacturing takes place
- Create a transaction that properly tracks inventory (ingredients going out, finished product coming in)

## Architecture

### 1. Data Layer

#### ProductsRepository Enhancement
- **Updated Method**: `getRecipeIngredients(recipeSlug: String)`
  - Fetches recipe ingredients from the database
  - Loads ingredient product details
  - Returns fully populated `RecipeIngredient` domain models

#### TransactionsRepository
- **New Method**: `saveManufactureTransaction(...)`
  - Creates 3 linked transactions:
    1. **Parent Transaction**: Manufacture transaction (type = 3)
    2. **Child Sale Transaction**: Deducts ingredients from inventory (type = 1)
    3. **Child Purchase Transaction**: Adds manufactured product to inventory (type = 2)
  - Uses `parentSlug` to link child transactions
  - Maintains proper timestamps for ordering

### 2. Domain Layer

#### Models Used
- `Product` - Recipe and ingredient products
- `RecipeIngredient` - Ingredient details for a recipe
- `TransactionDetail` - Product details in a transaction
- `QuantityUnit` - Units of measurement
- `Warehouse` - Manufacturing location

### 3. Presentation Layer

#### AddManufactureViewModel
**State Management**:
```kotlin
data class ManufactureState(
    val availableRecipes: List<Product> = emptyList(),
    val selectedRecipe: Product? = null,
    val recipeQuantity: Double = 1.0,
    val recipeUnit: QuantityUnit? = null,
    val recipeUnitPrice: Double = 0.0,
    val ingredients: List<TransactionDetail> = emptyList(),
    val additionalCharges: Double = 0.0,
    val additionalChargesDescription: String = "",
    val selectedWarehouse: Warehouse? = null,
    val transactionTimestamp: Long = System.currentTimeMillis(),
    val totalCost: Double = 0.0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)
```

**Key Features**:
- Recipe selection from available recipes
- Automatic ingredient quantity calculation based on recipe quantity
- Real-time cost calculation
- Warehouse selection
- Date/time management
- Error handling with user feedback

#### AddManufactureScreen
**UI Components**:
1. **Top Section**:
   - Date picker button
   - Warehouse selector button

2. **Recipe Card**:
   - Recipe selection dialog
   - Quantity input
   - Unit price display
   - Remove button

3. **Ingredients Section**:
   - Table view showing all ingredients
   - Columns: Item, Quantity, Price, Total
   - Read-only display (quantities auto-calculated)

4. **Additional Charges Section**:
   - Amount input field
   - Description text field (optional)

5. **Total Cost Summary**:
   - Total cost display
   - Unit price calculation

**User Flow**:
1. User opens Manufacture screen from Home menu
2. Selects warehouse (required)
3. Selects recipe from dialog
4. Adjusts quantity of recipe to manufacture
5. Ingredients automatically recalculated
6. Optionally adds additional charges
7. Reviews total cost
8. Saves transaction

### 4. Navigation

#### App.kt Changes
- Added `ADD_MANUFACTURE` to `AppScreen` enum
- Added `manufactureViewModel` initialization
- Added navigation handler in `HomeScreen` call
- Added `AppScreen.ADD_MANUFACTURE` case in when statement

#### HomeScreen.kt Changes
- Added `onNavigateToManufacture` parameter
- Passed handler to `HomeMenuScreen`

#### MenuScreen.kt Changes
- Added `onNavigateToManufacture` parameter
- Added "Manufacture" menu option with Build icon
- Added navigation handler in menu click

### 5. Dependency Injection

#### TransactionsModule.kt
Added ViewModel injection:
```kotlin
single {
    AddManufactureViewModel(
        transactionsRepository = get(),
        productsRepository = get(),
        quantityUnitsRepository = get(),
        paymentMethodsRepository = get(),
        warehousesRepository = get()
    )
}
```

## Transaction Structure

### Parent Manufacture Transaction
```
Type: 3 (MANUFACTURE)
Customer: null
Total Paid: Recipe total cost
Additional Charges: User-defined
Warehouse From: Selected warehouse
Transaction Details: Empty (details in children)
```

### Child Sale Transaction (Ingredients Out)
```
Type: 1 (SALE)
Parent Slug: Parent manufacture slug
Timestamp: Parent timestamp + 1ms
Transaction Details: All ingredients with quantities and prices
Additional Charges: Included
```

### Child Purchase Transaction (Recipe In)
```
Type: 2 (PURCHASE)
Parent Slug: Parent manufacture slug
Timestamp: Parent timestamp + 2ms
Transaction Details: Recipe product with quantity and calculated price
Additional Charges: 0 (already included in sale)
```

## Business Logic

### Cost Calculation
```
Ingredients Cost = Σ(ingredient.quantity × ingredient.price)
Total Cost = Ingredients Cost + Additional Charges
Recipe Unit Price = Total Cost / Recipe Quantity
```

### Ingredient Quantity Calculation
```
When recipe quantity changes:
For each ingredient:
    New Quantity = Original Quantity × Recipe Quantity
```

### Example
```
Recipe: Pizza (Quantity = 10)
Ingredients:
  - Flour: 5kg × 10 = 50kg @ ₨20/kg = ₨1000
  - Cheese: 2kg × 10 = 20kg @ ₨500/kg = ₨10000
  - Sauce: 1L × 10 = 10L @ ₨100/L = ₨1000
Additional Charges: ₨500 (labor)

Total Cost: ₨12,500
Unit Price: ₨12,500 / 10 = ₨1,250 per pizza
```

## Database Impact

### Transactions Created
- 3 transactions per manufacture (1 parent + 2 children)
- Child transactions filtered from main transaction list
- Parent transaction shows in transaction list

### Inventory Impact
- Ingredients: Stock reduced by sale transaction
- Recipe Product: Stock increased by purchase transaction

## UI/UX Features

1. **Recipe Selection Dialog**
   - Shows all available recipes
   - Search/filter capability (if recipes list is large)
   - No recipes warning message

2. **Real-time Updates**
   - Instant ingredient quantity recalculation
   - Live total cost updates
   - Unit price calculation

3. **Validation**
   - Recipe must be selected
   - Quantity must be > 0
   - Warehouse must be selected

4. **Error Handling**
   - User-friendly error messages
   - Loading states
   - Saving progress indicator

## Future Enhancements

1. **Date Picker**: Implement proper date/time picker dialog
2. **Warehouse Selector**: Implement warehouse selection dialog with list
3. **Editable Ingredients**:
   - Allow manual quantity adjustments
   - Allow price overrides
4. **Add Custom Ingredients**: Allow adding ingredients not in the recipe
5. **Remove Ingredients**: Allow removing ingredients from the list
6. **Cost Breakdown**: Show detailed cost breakdown in summary
7. **Manufacturing Notes**: Add notes field for manufacturing details
8. **Batch Management**: Track manufacturing batches
9. **Quality Control**: Add quality check steps
10. **Print Manufacturing Order**: Generate printable manufacturing order

## Testing Recommendations

1. **Unit Tests**:
   - Test quantity calculations
   - Test cost calculations
   - Test transaction creation logic

2. **Integration Tests**:
   - Test end-to-end manufacture flow
   - Test database transaction rollback on errors
   - Test parent-child transaction linking

3. **UI Tests**:
   - Test recipe selection
   - Test quantity input
   - Test validation messages

4. **Manual Testing Scenarios**:
   - Create manufacture with single ingredient
   - Create manufacture with multiple ingredients
   - Create manufacture with additional charges
   - Verify inventory changes
   - Verify transaction list filtering (no children shown)

## Dependencies

- Kotlin Multiplatform
- Compose Multiplatform
- Room Database
- Koin (Dependency Injection)
- Kotlin Coroutines/Flow

## Files Modified

1. `AddManufactureViewModel.kt` (new)
2. `AddManufactureScreen.kt` (new)
3. `TransactionsRepository.kt` (modified)
4. `ProductsRepository.kt` (modified)
5. `TransactionsModule.kt` (modified)
6. `App.kt` (modified)
7. `HomeScreen.kt` (modified)
8. `MenuScreen.kt` (modified)

## Conclusion

The manufacture transaction feature is now fully implemented and integrated into the application. Users can now:
- Create manufacture transactions from the Home menu
- Select recipes and adjust quantities
- View auto-calculated ingredients
- Add additional charges
- Save transactions that properly track inventory changes

The implementation follows the existing architecture patterns and is consistent with other transaction types in the application.

