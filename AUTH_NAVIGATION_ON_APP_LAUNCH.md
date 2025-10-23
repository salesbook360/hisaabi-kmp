# Authentication Navigation on App Launch

## Overview
This document describes the implementation of authentication-based navigation on app launch. The app now checks the user's authentication status when it starts and navigates to either the Login screen (if not logged in) or the Home screen (if logged in).

## Implementation Details

### Changes Made

#### 1. App.kt - Main Navigation Logic
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/App.kt`

**Key Changes:**
- Added `AuthViewModel` injection at the app level to check authentication state
- Changed `currentScreen` from a non-nullable enum to nullable (`AppScreen?`) to handle initialization state
- Added `hasInitialized` flag to distinguish between initial screen setup and subsequent auth state changes
- Implemented `LaunchedEffect(isLoggedIn)` to:
  - Set initial screen based on auth state on first run
  - Handle subsequent auth state changes (login/logout)
  - Navigate to HOME when user logs in from AUTH screen
  - Navigate to AUTH when user logs out from any screen

**Implementation:**
```kotlin
// Check authentication state on app launch
val authViewModel: AuthViewModel = koinInject()
val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

// Set initial screen based on authentication state
var currentScreen by remember { mutableStateOf<AppScreen?>(null) }
var hasInitialized by remember { mutableStateOf(false) }

// Set initial screen and handle auth state changes
LaunchedEffect(isLoggedIn) {
    if (!hasInitialized) {
        // Set initial screen based on auth state
        currentScreen = if (isLoggedIn) AppScreen.HOME else AppScreen.AUTH
        hasInitialized = true
    } else {
        // Handle subsequent auth state changes
        // Navigate to HOME when user logs in
        if (isLoggedIn && currentScreen == AppScreen.AUTH) {
            currentScreen = AppScreen.HOME
        }
        // Navigate to AUTH when user logs out (from any screen)
        else if (!isLoggedIn && currentScreen != AppScreen.AUTH) {
            currentScreen = AppScreen.AUTH
        }
    }
}

// Show content only when currentScreen is initialized
currentScreen?.let { screen ->
    when (screen) {
        // ... navigation logic
    }
}
```

### How It Works

1. **On App Launch:**
   - `AuthViewModel` is created and injected
   - `AuthViewModel.init` block runs, which calls:
     - `checkAuthState()` - checks if user is logged in from database
     - `observeAuthState()` - continuously observes auth state changes
   - The `isLoggedIn` StateFlow emits the current auth state
   - `LaunchedEffect(isLoggedIn)` detects the first emission
   - Sets `currentScreen` to either `HOME` (if logged in) or `AUTH` (if not logged in)
   - Sets `hasInitialized = true`

2. **On User Login:**
   - User completes login on AUTH screen
   - `AuthViewModel` updates `isLoggedIn` StateFlow to `true`
   - `LaunchedEffect(isLoggedIn)` detects the change
   - Navigates from AUTH to HOME

3. **On User Logout:**
   - User logs out from any screen
   - `AuthViewModel` updates `isLoggedIn` StateFlow to `false`
   - `LaunchedEffect(isLoggedIn)` detects the change
   - Navigates to AUTH screen

### Authentication State Management

The authentication state is managed by:

1. **AuthViewModel** (`auth/presentation/viewmodel/AuthViewModel.kt`):
   - Maintains `isLoggedIn` StateFlow
   - Checks auth state on initialization
   - Observes database changes for auth state

2. **AuthRepository** (`auth/data/repository/AuthRepository.kt`):
   - Provides `isLoggedIn()` method
   - Provides `observeAuthState()` Flow

3. **AuthLocalDataSource** (`auth/data/datasource/AuthLocalDataSource.kt`):
   - Checks database for user authentication record
   - Observes database changes using Room's Flow

4. **UserAuthDao** (database):
   - Stores user authentication data persistently
   - Provides reactive queries via Flow

### Benefits

1. **Persistent Authentication**: User stays logged in across app restarts
2. **Automatic Navigation**: No manual navigation needed - app automatically shows correct screen
3. **Reactive**: Auth state changes are immediately reflected in navigation
4. **Secure**: User must login to access the app

## Testing

To test the implementation:

1. **First Launch (No User):**
   - Launch the app for the first time
   - Should show Login screen

2. **Login and Restart:**
   - Login with valid credentials
   - Close the app completely
   - Relaunch the app
   - Should show Home screen (user stays logged in)

3. **Logout:**
   - From any screen in the app
   - Tap logout
   - Should navigate to Login screen

4. **Login from Auth Screen:**
   - Enter credentials and login
   - Should navigate to Home screen

## Technical Notes

- The nullable `currentScreen` prevents rendering anything until auth state is determined
- The `hasInitialized` flag ensures we distinguish between initial setup and subsequent changes
- The implementation uses `collectAsState()` for reactive UI updates
- The auth check is fast (database query) so there's minimal delay

### Fix: Removed Profile Screen Flash

**Issue**: After login or on app launch (when already logged in), the profile screen would briefly flash before navigating to the home screen.

**Root Cause**: The `AuthNavigation` component was showing `ProfileScreen` when `isLoggedIn` was true, before the app-level navigation could switch to `HOME`.

**Solution**: Modified `AuthNavigation` to:
1. Use `LaunchedEffect` to immediately call `onNavigateToMain()` when `isLoggedIn` becomes true
2. Only render auth screens (Login, Register, ForgotPassword) when `!isLoggedIn`
3. Removed the `ProfileScreen` case from `AuthNavigation` entirely

**Code Change** (`AuthNavigation.kt`):
```kotlin
// Navigate to main immediately when user is logged in
// This prevents the profile screen flash after login
LaunchedEffect(isLoggedIn) {
    if (isLoggedIn) {
        onNavigateToMain()
    }
}

// Only show auth screens when not logged in
if (!isLoggedIn) {
    when {
        showForgotPassword -> ForgotPasswordScreen(...)
        showRegister -> RegisterScreen(...)
        else -> LoginScreen(...)
    }
}
```

This ensures smooth navigation without any visual flicker.

### Fix: Added Splash Screen to Prevent Login Screen Flash

**Issue**: On cold app start, the login screen would briefly flash before navigating to home screen (if user was already logged in).

**Root Cause**: The `AuthViewModel` initializes `isLoggedIn` as `false` and performs the auth check asynchronously. During this brief moment, the app would show the LOGIN screen before the auth check completed.

**Solution**: Added initialization tracking and splash screen:

1. **Added `isInitialized` StateFlow** to `AuthViewModel`:
```kotlin
private val _isInitialized = MutableStateFlow(false)
val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

private fun checkAuthState() {
    viewModelScope.launch {
        val isLoggedIn = isLoggedInUseCase()
        val currentUser = if (isLoggedIn) getCurrentUserUseCase() else null
        
        _uiState.value = _uiState.value.copy(
            isLoggedIn = isLoggedIn,
            currentUser = currentUser
        )
        _isLoggedIn.value = isLoggedIn
        _isInitialized.value = true // Mark as initialized after first check
    }
}
```

2. **Created `SplashScreen` component** (`SplashScreen.kt`):
```kotlin
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Shows blank screen to prevent flash
        // Can be enhanced with logo/loading indicator
    }
}
```

3. **Modified `App.kt`** to show splash screen during initialization:
```kotlin
val isInitialized by authViewModel.isInitialized.collectAsState()

// Set initial screen after auth check is complete
LaunchedEffect(isInitialized, isLoggedIn) {
    if (isInitialized && currentScreen == null) {
        currentScreen = if (isLoggedIn) AppScreen.HOME else AppScreen.AUTH
    }
}

// Show splash screen while checking auth state
if (currentScreen == null) {
    SplashScreen()
} else {
    when (currentScreen!!) {
        // ... app screens
    }
}
```

**Result**: 
- No more login screen flash on cold start
- Smooth transition from splash to appropriate screen
- User sees blank screen (splash) while auth state is being checked

## Future Enhancements

Potential improvements:
1. Enhance splash screen with app logo and/or loading indicator
2. Add session timeout functionality
3. Add biometric authentication option
4. Add remember me / auto-login preferences
5. Add animated transitions between screens

