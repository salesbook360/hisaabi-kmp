# Auth Module for Hisaabi KMP

This document describes the authentication module implemented in the Hisaabi Kotlin Multiplatform project.

## Architecture

The auth module follows Clean Architecture principles with the following layers:

### 1. Data Layer
- **Models**: `AuthRequest.kt`, `AuthResponse.kt` - Data transfer objects for API communication
- **Data Sources**: 
  - `AuthRemoteDataSource.kt` - Handles API calls for authentication
  - `AuthLocalDataSource.kt` - Manages local storage of auth tokens and user data
- **Repository**: `AuthRepository.kt` - Coordinates between remote and local data sources

### 2. Domain Layer
- **Models**: `User.kt`, `AuthResult.kt` - Domain models and result wrappers
- **Use Cases**: 
  - `LoginUseCase.kt` - Handles user login with validation
  - `RegisterUseCase.kt` - Handles user registration with validation
  - `LogoutUseCase.kt` - Handles user logout
  - `GetCurrentUserUseCase.kt` - Retrieves current user data
  - `IsLoggedInUseCase.kt` - Checks authentication status

### 3. Presentation Layer
- **ViewModels**: `AuthViewModel.kt` - Manages UI state and business logic
- **UI Screens**:
  - `LoginScreen.kt` - Login form with email/password
  - `RegisterScreen.kt` - Registration form with validation
  - `ProfileScreen.kt` - User profile display and logout
- **Navigation**: `AuthNavigation.kt` - Handles navigation between auth screens

### 4. Dependency Injection
- **Module**: `AuthModule.kt` - Koin module for dependency injection

## Features

### Authentication Features
- ✅ User login with email/password
- ✅ User registration with validation
- ✅ User logout
- ✅ Token-based authentication
- ✅ Automatic token refresh
- ✅ Persistent login state
- ✅ Profile management

### Validation
- ✅ Email format validation
- ✅ Password strength requirements
- ✅ Required field validation
- ✅ Password confirmation matching

### UI/UX
- ✅ Material Design 3 components
- ✅ Loading states
- ✅ Error handling and display
- ✅ Form validation feedback
- ✅ Responsive design

## Dependencies

The auth module uses the following key dependencies:

- **Ktor**: HTTP client for API communication
- **Kotlinx Serialization**: JSON serialization/deserialization
- **Koin**: Dependency injection
- **Kotlinx Coroutines**: Asynchronous programming
- **Compose Multiplatform**: UI framework

## API Endpoints

The module expects the following API endpoints:

- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `POST /auth/refresh` - Token refresh
- `POST /auth/forgot-password` - Password reset request
- `POST /auth/reset-password` - Password reset
- `POST /auth/logout` - User logout

## Usage

### 1. Initialize the Module

The auth module is automatically initialized when the app starts through Koin dependency injection.

### 2. Use in UI

```kotlin
@Composable
fun MyApp() {
    KoinApplication(modules = listOf(authModule)) {
        AuthNavigation(
            onNavigateToMain = { /* Navigate to main app */ }
        )
    }
}
```

### 3. Access Auth State

```kotlin
@Composable
fun SomeScreen() {
    val authViewModel: AuthViewModel = koinInject()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUser by authViewModel.uiState.collectAsState()
    
    // Use auth state in your UI
}
```

## Security Considerations

- Passwords are validated for strength (uppercase, lowercase, numbers)
- Tokens are stored locally and automatically refreshed
- API calls include proper authentication headers
- Input validation prevents common security issues

## Future Enhancements

- [ ] Biometric authentication
- [ ] Social login (Google, Apple)
- [ ] Two-factor authentication
- [ ] Remember me functionality
- [ ] Password change functionality
- [ ] Account deletion
- [ ] Email verification flow

## Testing

The module is designed to be easily testable with:
- Repository pattern for easy mocking
- Use cases for business logic testing
- ViewModels for UI logic testing
- Dependency injection for test doubles

## Platform Support

This auth module works across all KMP targets:
- ✅ Android
- ✅ iOS
- ✅ Desktop (JVM)
- ✅ Web (WasmJs)
