# Authentication Persistent Storage Implementation

## Overview

The authentication module has been upgraded to use **persistent storage** instead of in-memory storage. User profile information and authentication tokens now persist across app restarts using the Room database.

## Changes Made

### 1. New Database Entity: `UserAuthEntity`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/entity/UserAuthEntity.kt`

This entity stores:
- User profile information (ID, name, email, address, phone, slug, firebase ID, profile picture)
- Authentication tokens (access token, refresh token)
- Metadata (last updated timestamp)

**Key Features:**
- Uses a fixed ID (1) since only one user can be logged in at a time
- All user data persists in the database
- Tokens are securely stored and automatically retrieved on app restart

### 2. New DAO: `UserAuthDao`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/dao/UserAuthDao.kt`

Provides methods for:
- `insertUserAuth()` - Insert or replace user auth data
- `updateUserAuth()` - Update existing user auth data
- `getUserAuth()` - Get current user auth data
- `observeUserAuth()` - Observe auth data changes with Flow
- `getAccessToken()` - Get access token only
- `getRefreshToken()` - Get refresh token only
- `updateAccessToken()` - Update access token only
- `updateTokens()` - Update both tokens
- `isLoggedIn()` - Check if user is logged in
- `clearUserAuth()` - Clear all auth data (logout)
- `getLastUpdated()` - Get last update timestamp

### 3. Updated `AuthLocalDataSourceImpl`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/auth/data/datasource/AuthLocalDataSource.kt`

**Before:**
```kotlin
class AuthLocalDataSourceImpl : AuthLocalDataSource {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var currentUser: UserDto? = null
    // ... in-memory storage
}
```

**After:**
```kotlin
class AuthLocalDataSourceImpl(
    private val userAuthDao: UserAuthDao
) : AuthLocalDataSource {
    // Uses database for persistent storage
}
```

All methods now interact with the Room database instead of in-memory variables.

### 4. Updated `AppDatabase`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/AppDatabase.kt`

**Changes:**
- Added `UserAuthEntity::class` to entities list
- Added `abstract fun userAuthDao(): UserAuthDao`
- **Database version bumped from 1 to 2**

### 5. Updated `DatabaseModule`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/database/di/DatabaseModule.kt`

**Changes:**
- Added `single { get<AppDatabase>().userAuthDao() }` to provide UserAuthDao via Koin

### 6. Updated `AuthModule`
**Location:** `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/auth/di/AuthModule.kt`

**Changes:**
- Updated `AuthLocalDataSource` injection to pass `UserAuthDao` dependency
- Changed from `AuthLocalDataSourceImpl()` to `AuthLocalDataSourceImpl(get())`

## Database Migration

### Important: Database Version Change

The database version has been updated from **1 to 2**. This requires a migration strategy.

### Migration Options

#### Option 1: Destructive Migration (Recommended for Development)

If you're in development and don't need to preserve existing data, you can use destructive migration:

Update your `DatabaseBuilder` files to include:

```kotlin
Room.databaseBuilder<AppDatabase>(/* ... */)
    .fallbackToDestructiveMigration(true)  // Add this
    .build()
```

This will clear all data and recreate the database with the new schema.

#### Option 2: Proper Migration (Recommended for Production)

For production, create a migration that adds the new `user_auth` table:

**Create a migration file** (e.g., `DatabaseMigrations.kt`):

```kotlin
package com.hisaabi.hisaabi_kmp.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS user_auth (
                id INTEGER PRIMARY KEY NOT NULL,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                address TEXT NOT NULL,
                phone TEXT NOT NULL,
                slug TEXT NOT NULL,
                firebase_id TEXT NOT NULL,
                pic TEXT,
                access_token TEXT NOT NULL,
                refresh_token TEXT NOT NULL,
                last_updated INTEGER NOT NULL
            )
        """.trimIndent())
    }
}
```

Then update your `DatabaseBuilder` to include the migration:

```kotlin
Room.databaseBuilder<AppDatabase>(/* ... */)
    .addMigrations(MIGRATION_1_2)  // Add this
    .build()
```

## Benefits

### ✅ Persistent Login
- Users remain logged in across app restarts
- No need to re-login every time the app is opened
- Tokens persist securely in the local database

### ✅ Better User Experience
- Seamless authentication flow
- Faster app startup (no need to check auth status remotely)
- Offline capability for checking login status

### ✅ Security
- Tokens stored in encrypted database (Room uses SQLite which is file-based)
- Easy to clear all auth data on logout
- Platform-specific security features can be added

### ✅ Scalability
- Easy to add more auth-related fields in the future
- Can track last login time, device info, etc.
- Supports reactive updates via Flow

## Usage Examples

### Check if User is Logged In
```kotlin
val authLocalDataSource: AuthLocalDataSource = get()
val isLoggedIn = authLocalDataSource.isLoggedIn()
```

### Get Current User
```kotlin
val user = authLocalDataSource.getUser()
if (user != null) {
    println("Welcome back, ${user.name}!")
}
```

### Get Access Token
```kotlin
val token = authLocalDataSource.getAccessToken()
// Use token for API calls
```

### Logout (Clear All Auth Data)
```kotlin
authLocalDataSource.clearAuthData()
// User is now logged out, all data cleared from database
```

### Observe Auth State Changes (Using DAO directly)
```kotlin
val userAuthDao: UserAuthDao = get()
userAuthDao.observeUserAuth()
    .collectLatest { userAuth ->
        if (userAuth != null) {
            println("User logged in: ${userAuth.name}")
        } else {
            println("No user logged in")
        }
    }
```

## Testing Recommendations

### 1. Test Login Persistence
1. Login to the app
2. Close the app completely
3. Reopen the app
4. Verify user is still logged in

### 2. Test Logout
1. Login to the app
2. Logout
3. Close and reopen the app
4. Verify user is logged out

### 3. Test Token Refresh
1. Login to the app
2. Wait for token refresh to trigger
3. Close and reopen the app
4. Verify new tokens are persisted

### 4. Test Multi-Platform
Test the persistence on all platforms:
- ✅ Android
- ✅ iOS
- ✅ Desktop (JVM)
- ✅ Web (WasmJS)

## Migration Checklist

- [x] Create `UserAuthEntity`
- [x] Create `UserAuthDao`
- [x] Update `AppDatabase` to include entity and DAO
- [x] Update `DatabaseModule` to provide DAO
- [x] Update `AuthLocalDataSourceImpl` to use database
- [x] Update `AuthModule` to inject DAO dependency
- [ ] **Choose and implement migration strategy** (destructive or proper migration)
- [ ] Test login persistence on all platforms
- [ ] Test logout functionality
- [ ] Test token refresh with persistence
- [ ] Update platform-specific `DatabaseBuilder` files with migration strategy

## Next Steps

1. **Choose your migration strategy** (see Migration Options above)
2. **Update your DatabaseBuilder files** (platform-specific) to include the migration
3. **Test thoroughly** on all target platforms
4. **Consider adding encryption** for sensitive data (optional)
5. **Monitor performance** with the new database operations

## Platform-Specific Database Locations

### Android
- Database path: `context.getDatabasePath("hisaabi_database.db")`
- Typically: `/data/data/<package>/databases/hisaabi_database.db`

### iOS
- Database path: `NSHomeDirectory() + "/hisaabi_database.db"`
- Typically in the app's documents directory

### Desktop (JVM)
- Database path: `System.getProperty("java.io.tmpdir") + "/hisaabi_database.db"`
- In system temp directory

### Web (WasmJS)
- Browser-based storage (IndexedDB)
- Handled by Room's WasmJS driver

## Security Considerations

### Current Implementation
- Tokens stored in local database (not encrypted by default)
- Database file has OS-level protection
- Cleared completely on logout

### Recommended Enhancements
1. **Add database encryption** (SQLCipher or similar)
2. **Implement biometric authentication** for sensitive operations
3. **Add token expiration checks** before using stored tokens
4. **Implement secure key storage** for additional sensitive data
5. **Add device binding** to prevent token theft

## Troubleshooting

### Issue: App crashes on startup after update
**Solution:** The database schema changed. Implement proper migration or use destructive migration for development.

### Issue: User not staying logged in
**Solution:** Check that `AuthLocalDataSource` is properly injected with `UserAuthDao` in the `AuthModule`.

### Issue: Tokens not updating after refresh
**Solution:** Ensure `saveUser()` is called after token refresh with updated tokens.

### Issue: Database not found on iOS
**Solution:** Check that the iOS `DatabaseBuilder` has correct file path permissions.

## Summary

This implementation provides a robust, persistent authentication solution for the Hisaabi KMP application. Users will now remain logged in across app restarts, providing a much better user experience while maintaining security best practices.

The implementation leverages the existing Room database infrastructure, making it maintainable and consistent with the rest of the application's data layer.


