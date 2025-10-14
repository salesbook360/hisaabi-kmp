# Home Screen Implementation

## Overview

The Hisaabi KMP app now features a modern home screen with bottom navigation, providing easy access to all main features of the application.

## Architecture

### Navigation Structure

```
App (Entry Point)
├── HomeScreen (Default Landing Screen)
│   ├── Dashboard (Tab 1)
│   ├── Menu (Tab 2)
│   └── More (Tab 3)
└── AuthNavigation (Login/Register Screens)
```

## Screens

### 1. Dashboard Screen 📊

The main dashboard providing an overview of business metrics and quick actions.

**Features:**
- **Summary Cards**
  - Total Sales
  - Total Purchases
  - Customer Count
  - Product Count

- **Quick Actions Grid**
  - New Sale
  - New Purchase
  - Add Product
  - Add Customer
  - Reports
  - Settings

- **Recent Transactions**
  - Shows recent business transactions
  - Empty state when no transactions exist

### 2. Menu Screen 📋

Organized access to all main features grouped by category.

**Sections:**

#### Transactions
- Sales - View all sales transactions
- Purchases - View all purchases
- Payments Received - Track incoming payments
- Payments Made - Track outgoing payments

#### Inventory
- Products - Manage product catalog
- Stock - View stock levels
- Categories - Manage categories
- Units - Manage units of measurement

#### Parties
- Customers - Manage customers
- Suppliers - Manage suppliers
- Ledger - View account ledger

#### Reports
- Sales Report - Detailed sales analysis
- Purchase Report - Purchase analytics
- Inventory Report - Stock analysis
- Profit & Loss - P&L statement

### 3. More Screen ⚙️

Settings, user profile, and additional options.

**Features:**

#### Profile Section
- Business name display
- Email display
- Profile avatar with business initial

#### Settings
- Business Profile
- User Management
- Warehouses
- Payment Methods
- Tax Settings
- Preferences

#### Support
- Help & FAQ
- Contact Support
- Rate App
- Share App

#### About
- Privacy Policy
- Terms of Service
- Licenses

#### Actions
- Logout button (navigates to Auth screens)
- Version information

## Bottom Navigation

The bottom navigation bar provides quick switching between main sections:

| Icon | Label | Screen |
|------|-------|--------|
| 🏠 Home | Dashboard | Main dashboard with metrics |
| 📋 Menu | Menu | All features organized |
| ⋮ More | More | Settings and profile |

## Navigation Flow

### Default Flow
1. **App Launch** → HomeScreen (Dashboard Tab)
2. User can navigate between tabs using bottom navigation
3. From More screen → Logout → AuthNavigation

### Auth Flow
1. From More screen → Click Logout
2. Navigate to AuthNavigation
3. After Login/Register → Return to HomeScreen

## File Structure

```
home/
├── HomeScreen.kt          # Main container with bottom navigation
├── DashboardScreen.kt     # Dashboard with metrics & quick actions
├── MenuScreen.kt          # Organized menu of all features
└── MoreScreen.kt          # Settings, profile, and more options
```

## Code Examples

### Using the Home Screen

```kotlin
// In App.kt
@Composable
fun App() {
    MaterialTheme {
        KoinContext {
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToAuth = { currentScreen = AppScreen.AUTH }
                    )
                }
                AppScreen.AUTH -> {
                    AuthNavigation(
                        onNavigateToMain = { currentScreen = AppScreen.HOME }
                    )
                }
            }
        }
    }
}
```

### Customizing Summary Cards

```kotlin
SummaryCard(
    title = "Total Sales",
    value = "₹12,500",
    icon = Icons.Default.ShoppingCart,
    modifier = Modifier.weight(1f)
)
```

### Adding Menu Items

```kotlin
val customMenuItems = listOf(
    MenuItem(
        title = "Custom Feature",
        subtitle = "Description here",
        icon = Icons.Default.Star
    )
)
```

## Key Features

### ✅ Implemented Features

1. **Bottom Navigation**
   - 3 main tabs (Dashboard, Menu, More)
   - Selected state indication
   - Material Design 3 styling

2. **Dashboard**
   - Summary cards for key metrics
   - Quick action grid
   - Recent transactions section
   - Empty states

3. **Menu**
   - Organized by category
   - Section headers
   - Subtitles for context
   - Chevron navigation indicators

4. **More Screen**
   - User profile display
   - Settings categories
   - Support options
   - Logout functionality
   - Version display

### 🎨 Design Features

- **Material Design 3** components throughout
- **Responsive layouts** that work on all screen sizes
- **Color-coded sections** for better organization
- **Icons** for visual clarity
- **Cards and surfaces** for content hierarchy
- **Proper spacing** and padding

### 🔄 Navigation

- **Tab-based navigation** using bottom navigation bar
- **State management** with Compose remember
- **Navigation callbacks** for screen transitions
- **Logout flow** to auth screens

## Customization

### Changing Default Landing Screen

Currently defaults to Dashboard. To change:

```kotlin
// In HomeScreen.kt
var selectedTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Menu, 2 = More
```

### Modifying Colors

```kotlin
// Use MaterialTheme colors
colors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.primaryContainer
)
```

### Adding New Menu Items

1. Add to the appropriate list in MenuScreen.kt or MoreScreen.kt:

```kotlin
val myMenuItems = listOf(
    MenuItem("New Feature", "Description", Icons.Default.Star),
    // ... existing items
)
```

2. The UI will automatically update

## Future Enhancements

### Planned Features
- [ ] Actual navigation to feature screens
- [ ] Dynamic data from database
- [ ] Search functionality
- [ ] Notifications badge
- [ ] Recent activity timeline
- [ ] Charts and graphs on dashboard
- [ ] Pull-to-refresh
- [ ] Swipe gestures
- [ ] Custom themes
- [ ] Widgets for quick actions

### Potential Improvements
- [ ] Add drawer navigation for larger screens
- [ ] Implement deep linking
- [ ] Add onboarding flow for first-time users
- [ ] Implement search across all features
- [ ] Add floating action button for quick add
- [ ] Progressive disclosure of menu items
- [ ] Customizable dashboard widgets

## Testing

The home screen has been tested to:
- ✅ Build successfully on all platforms
- ✅ Display correctly with Material Design 3
- ✅ Handle tab switching smoothly
- ✅ Navigate to auth screens
- ✅ Show placeholder data properly

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Android | ✅ Full Support | Tested and working |
| iOS | ✅ Full Support | Compose Multiplatform |
| Desktop | ✅ Full Support | Compose Multiplatform |
| Web | ✅ Full Support | WasmJS |

## Troubleshooting

### Common Issues

**Issue**: Bottom navigation not showing
- **Solution**: Make sure Scaffold is properly configured with bottomBar parameter

**Issue**: Icons not displaying
- **Solution**: Ensure all icon imports are from Material Icons Extended

**Issue**: Tab state not persisting
- **Solution**: Check that remember is used correctly for selectedTab state

## Performance Notes

- Lazy loading used for lists (LazyColumn, LazyVerticalGrid)
- State hoisting for efficient recomposition
- Remember used to prevent unnecessary recompositions
- Material Design 3 optimized components

## Resources

- [Material Design 3](https://m3.material.io/)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Navigation in Compose](https://developer.android.com/jetpack/compose/navigation)

---

**Created**: October 14, 2025  
**Last Updated**: October 14, 2025  
**Version**: 1.0.0

