# Business Selection Gate Implementation

## Overview
Implemented a mandatory business selection flow that ensures users always have a business context before accessing the application. This prevents showing incorrect data when no business is selected.

## Date
October 22, 2025

## Problem Statement
The app supports multiple businesses with all records stored per business. Without proper business selection gating:
- Dashboard would show data without business context (incorrect/mixed data)
- Users could access features without selecting which business to work with
- Data integrity risks with operations not tied to a business

## Solution: Business Selection Gate

### Flow Diagram

```
User Login
    ↓
Check Selected Business
    ↓
┌─────────────┬─────────────┐
│   Has       │    No       │
│ Selected    │  Selected   │
│ Business    │  Business   │
└─────┬───────┴──────┬──────┘
      │              │
      ↓              ↓
   Go to       Business Selection
    Home        Gate Screen
                      │
                      ├─→ Select Business → Home
                      ├─→ Create Business → Home
                      └─→ Back Press → Exit App
```

### Implementation Details

#### 1. Business Selection Gate Screen
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/business/presentation/ui/BusinessSelectionGateScreen.kt`

**Features**:
- Shows welcome message when no businesses exist
- Lists available businesses for selection
- Prominent "Create Business" button
- Instruction card explaining business selection
- Auto-navigates to home when business is selected
- Back button exits the app (doesn't go back to login)

**UI States**:

1. **Loading State**: Shows spinner while fetching businesses
2. **Empty State**: Shows welcome message and create business prompt
3. **List State**: Shows businesses with selection instructions

#### 2. Updated App Navigation
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/App.kt`

**Changes**:

##### Added Screen Type
```kotlin
enum class AppScreen {
    HOME,
    AUTH,
    BUSINESS_SELECTION_GATE, // ← New gate screen
    // ... other screens
}
```

##### Added Business Selection Check
```kotlin
// Inject business preferences
val businessPreferences: BusinessPreferencesDataSource = koinInject()
var selectedBusinessId by remember { mutableStateOf<Int?>(null) }

// Observe selected business ID
LaunchedEffect(Unit) {
    businessPreferences.observeSelectedBusinessId().collect { businessId ->
        selectedBusinessId = businessId
    }
}
```

##### Updated Initial Screen Logic
```kotlin
LaunchedEffect(isInitialized, isLoggedIn, selectedBusinessId) {
    if (isInitialized && currentScreen == null) {
        currentScreen = when {
            !isLoggedIn -> AppScreen.AUTH
            selectedBusinessId == null -> AppScreen.BUSINESS_SELECTION_GATE // ← Gate
            else -> AppScreen.HOME
        }
    }
}
```

##### Added Screen Rendering
```kotlin
AppScreen.BUSINESS_SELECTION_GATE -> {
    val myBusinessViewModel: MyBusinessViewModel = koinInject()
    BusinessSelectionGateScreen(
        viewModel = myBusinessViewModel,
        onBusinessSelected = {
            currentScreen = AppScreen.HOME
        },
        onAddBusinessClick = {
            selectedBusinessForEdit = null
            currentScreen = AppScreen.ADD_BUSINESS
        },
        onExitApp = {
            exitProcess(0) // Exit app on back press
        }
    )
}
```

#### 3. Updated Auth Navigation
**Changes in AuthNavigation callback**:
```kotlin
AppScreen.AUTH -> {
    AuthNavigation(
        onNavigateToMain = { 
            // After login, check if business is selected
            currentScreen = if (selectedBusinessId != null) {
                AppScreen.HOME
            } else {
                AppScreen.BUSINESS_SELECTION_GATE // ← Go to gate
            }
        }
    )
}
```

#### 4. Updated Add Business Navigation
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/App.kt`

```kotlin
AppScreen.ADD_BUSINESS -> {
    AddBusinessScreen(
        viewModel = koinInject(),
        businessToEdit = selectedBusinessForEdit,
        onNavigateBack = {
            businessRefreshTrigger++
            // Go back to gate if no business selected
            currentScreen = if (selectedBusinessId == null) {
                AppScreen.BUSINESS_SELECTION_GATE
            } else {
                AppScreen.MY_BUSINESS
            }
        }
    )
}
```

#### 5. Real-time Business Selection Detection
**Reactive Navigation**:
```kotlin
LaunchedEffect(isLoggedIn, selectedBusinessId) {
    if (currentScreen != null) {
        when {
            // User logged out
            !isLoggedIn && currentScreen != AppScreen.AUTH -> {
                currentScreen = AppScreen.AUTH
            }
            // User logged in but no business selected
            isLoggedIn && selectedBusinessId == null && 
            currentScreen != AppScreen.BUSINESS_SELECTION_GATE &&
            currentScreen != AppScreen.ADD_BUSINESS -> {
                currentScreen = AppScreen.BUSINESS_SELECTION_GATE
            }
            // User selected business
            isLoggedIn && selectedBusinessId != null &&
            (currentScreen == AppScreen.AUTH || 
             currentScreen == AppScreen.BUSINESS_SELECTION_GATE) -> {
                currentScreen = AppScreen.HOME
            }
        }
    }
}
```

## User Flows

### Flow 1: New User (No Businesses)
```
1. User registers/logs in
2. System checks: selectedBusinessId = null
3. → Navigate to BUSINESS_SELECTION_GATE
4. → Show "Welcome" message
5. User clicks "Create Your First Business"
6. → Navigate to ADD_BUSINESS
7. User creates business
8. Business is auto-selected (via MyBusinessViewModel)
9. → Auto-navigate to HOME
```

### Flow 2: Existing User (Has Businesses, None Selected)
```
1. User logs in
2. System checks: selectedBusinessId = null, businesses exist
3. → Navigate to BUSINESS_SELECTION_GATE
4. → Show businesses list with instructions
5. User clicks on a business card
6. Business is selected (persisted)
7. → Auto-navigate to HOME
```

### Flow 3: Existing User (Has Selected Business)
```
1. User logs in
2. System checks: selectedBusinessId = 42
3. → Navigate directly to HOME
4. ✅ User sees correct business data
```

### Flow 4: User Presses Back on Gate Screen
```
1. User is on BUSINESS_SELECTION_GATE
2. User presses back/close button
3. onExitApp() is called
4. → exitProcess(0)
5. ✅ App closes (doesn't go back to login)
```

## Benefits

### For Users
- ✅ **No Confusion**: Always know which business they're working with
- ✅ **Data Integrity**: See only relevant business data
- ✅ **Clear Onboarding**: Guided to create first business
- ✅ **Forced Selection**: Can't proceed without business context

### For Data Integrity
- ✅ **No Mixed Data**: All operations tied to selected business
- ✅ **Correct Filtering**: Dashboard/reports show right data
- ✅ **Safe Operations**: Transactions/parties saved to correct business
- ✅ **Audit Trail**: Always know business context

### For Development
- ✅ **Centralized Check**: One place handles business gating
- ✅ **Reactive**: Auto-detects selection changes
- ✅ **Type-Safe**: Compiler ensures selectedBusinessId is checked
- ✅ **Maintainable**: Clear flow control logic

## Technical Implementation

### Screen Priority Order
```
1. Is user logged in?
   NO  → AUTH screen
   YES → Continue

2. Does user have selected business?
   NO  → BUSINESS_SELECTION_GATE
   YES → HOME (or intended screen)
```

### State Management
- `selectedBusinessId` observed via Flow
- Changes trigger re-evaluation of screen
- Selection persists in database
- Survives app restart and token refresh

### Navigation Guards
**Allowed transitions without business**:
- AUTH → BUSINESS_SELECTION_GATE ✅
- BUSINESS_SELECTION_GATE → ADD_BUSINESS ✅
- ADD_BUSINESS → BUSINESS_SELECTION_GATE ✅

**Blocked transitions without business**:
- AUTH → HOME ❌ (redirected to gate)
- BUSINESS_SELECTION_GATE → HOME ❌ (need selection first)
- Any screen → HOME ❌ (if business cleared)

## Exit App Behavior

**Why Exit on Back**:
- User is on BUSINESS_SELECTION_GATE (requires business to proceed)
- Going back to login doesn't make sense (already logged in)
- Exiting app is clearer UX than navigation loop
- User can re-open app and still needs to select business

**Implementation**:
```kotlin
onExitApp = {
    exitProcess(0)
}
```

**Platform Behavior**:
- Android: Closes app properly
- iOS: Exits to home screen
- Desktop: Closes window
- Web: Closes tab (if allowed)

## Files Created
1. `BusinessSelectionGateScreen.kt` - Gate screen UI

## Files Modified
1. `App.kt` - Added gate logic and navigation
2. `AuthNavigation.kt` - Updated navigation callback (no file change needed, logic in App.kt)

## Testing Checklist

### Initial Login Flow
- [ ] New user logs in → See gate with "Create Business" prompt
- [ ] New user creates business → Auto-selected → Navigate to home
- [ ] Existing user (no selection) logs in → See gate with business list
- [ ] Existing user selects business → Navigate to home
- [ ] Existing user (has selection) logs in → Navigate directly to home

### Navigation Flow
- [ ] From gate, click business → Selects business → Go to home
- [ ] From gate, click "Create Business" → Go to ADD_BUSINESS
- [ ] From ADD_BUSINESS (no selection), save → Go back to gate
- [ ] From ADD_BUSINESS (has selection), save → Go to MY_BUSINESS
- [ ] From gate, press back → App exits

### Business Selection Persistence
- [ ] Select business → Close app → Reopen → Should go to home directly
- [ ] Token refresh happens → Selected business still persists
- [ ] Logout → Login → Selected business cleared, shows gate

### Edge Cases
- [ ] Delete selected business → Navigate to gate
- [ ] Clear selected business from More screen → Navigate to gate
- [ ] Create first business while on gate → Auto-select → Navigate to home

## Integration with Other Features

### Dashboard
```kotlin
// Dashboard now always has business context
val selectedBusiness = getSelectedBusinessUseCase()
    ?: throw IllegalStateException("No business selected") // Can't happen!

// Fetch data for selected business
loadDashboardData(selectedBusiness.id)
```

### Transactions
```kotlin
// All transactions saved with business context
transaction.businessId = selectedBusiness.id
database.save(transaction)
```

### Reports
```kotlin
// Reports filtered by selected business
val transactions = transactionsDao
    .getTransactionsByBusinessId(selectedBusiness.id)
```

## Future Enhancements

1. **Quick Switch**: Add business switcher in app bar for fast switching
2. **Business Context Indicator**: Show selected business name in header
3. **Remember Last Business**: Auto-select last used business on login
4. **Multi-Window Support**: Different windows for different businesses
5. **Offline Sync**: Queue operations until business is selected

## Known Limitations

1. **Single Platform Exit**: `exitProcess()` may behave differently on web
2. **No Confirmation**: Exits immediately without confirmation dialog
3. **State Loss**: Pending operations lost on exit (by design)

## Migration Notes

**For Existing Users**:
- First login after update will show gate screen
- They must select a business to proceed
- This is a one-time inconvenience for data integrity

**For Development**:
- All new features MUST assume a business is selected
- Use `getSelectedBusinessUseCase()` to get current business
- Filter all database queries by business ID

## Conclusion

The Business Selection Gate implementation ensures:
- ✅ Users always work in proper business context
- ✅ No mixed or incorrect data displayed
- ✅ Clear onboarding for new users
- ✅ Proper data segregation in multi-business environment

This is a critical feature for data integrity and user experience in a multi-tenant local database architecture.

