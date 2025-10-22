# Quick Test Guide: Authentication Persistence

## Overview
This guide helps you quickly test the new persistent authentication storage implementation.

## What Changed?
User authentication data (profile + tokens) now persists in the Room database instead of memory. Users will remain logged in after app restarts.

## Quick Test Steps

### Test 1: Login Persistence ✅
1. **Login** to the app with valid credentials
2. **Close** the app completely (kill the process)
3. **Reopen** the app
4. **Expected Result:** You should still be logged in, no need to enter credentials again

### Test 2: Logout Clears Data ✅
1. **Login** to the app
2. **Logout** from the app
3. **Close** and **reopen** the app
4. **Expected Result:** You should see the login screen, not logged in

### Test 3: Token Persistence ✅
1. **Login** to the app
2. Navigate through the app (tokens should be used for API calls)
3. **Close** and **reopen** the app
4. Navigate again (should work without re-login)
5. **Expected Result:** API calls should work with persisted tokens

### Test 4: Profile Data Persistence ✅
1. **Login** to the app
2. Go to Profile/Settings screen and note your profile information
3. **Close** and **reopen** the app
4. Go back to Profile/Settings screen
5. **Expected Result:** Profile information should be displayed immediately without API call

## Database Migration

### First Time Running After Update

When you first run the app after this update, the database will be migrated from version 1 to version 2. This happens automatically.

**What happens:**
- A new `user_auth` table is created
- Existing data is preserved
- No user data is lost

**If you encounter issues:**
1. Check logcat/console for migration errors
2. Try uninstalling and reinstalling the app (dev/test only)
3. Check that `ALL_MIGRATIONS` is properly imported in DatabaseBuilder files

## Platform-Specific Testing

### Android
```bash
./gradlew :composeApp:installDebug
# Or run from Android Studio
```
Database location: `/data/data/com.hisaabi.hisaabi_kmp/databases/hisaabi_database.db`

### iOS
```bash
# Open Xcode and run the iosApp target
```
Database location: Check NSHomeDirectory in iOS simulator

### Desktop (JVM)
```bash
./gradlew :composeApp:run
```
Database location: System temp directory

### Web (WasmJS)
**Note:** WasmJS database is not yet implemented. Auth will work but won't persist on this platform.

## Debugging Tips

### Check if User is in Database
Add debug logs in `AuthLocalDataSourceImpl`:
```kotlin
override suspend fun isLoggedIn(): Boolean {
    val result = userAuthDao.isLoggedIn()
    println("DEBUG: isLoggedIn = $result")
    return result
}
```

### Check Stored Tokens
Add debug logs:
```kotlin
override suspend fun getAccessToken(): String? {
    val token = userAuthDao.getAccessToken()
    println("DEBUG: Retrieved access token: ${token?.take(20)}...")
    return token
}
```

### Check Database File
**Android:**
```bash
adb shell
cd /data/data/com.hisaabi.hisaabi_kmp/databases/
ls -la
```

**Desktop:**
Check `System.getProperty("java.io.tmpdir")`

## Common Issues & Solutions

### Issue: App crashes on first launch after update
**Cause:** Migration not found or failed
**Solution:** Check that `ALL_MIGRATIONS` is imported in DatabaseBuilder files

### Issue: User not staying logged in
**Cause:** Database not being created or accessed
**Solution:** 
- Check Koin DI is providing `UserAuthDao`
- Check `AuthLocalDataSource` is getting the DAO injected
- Add debug logs to verify database calls

### Issue: Tokens not updating after refresh
**Cause:** `saveUser()` not being called with updated tokens
**Solution:** Check `AuthRepository.refreshToken()` calls `localDataSource.saveUser()`

### Issue: Old data appearing after login
**Cause:** Logout not clearing database
**Solution:** Check `clearAuthData()` is being called on logout

## Code Verification Checklist

### Files Created ✅
- [x] `UserAuthEntity.kt` - Database entity
- [x] `UserAuthDao.kt` - Database access object
- [x] `DatabaseMigrations.kt` - Migration script
- [x] Documentation files

### Files Modified ✅
- [x] `AppDatabase.kt` - Added entity and DAO
- [x] `DatabaseModule.kt` - Added DAO to DI
- [x] `AuthLocalDataSource.kt` - Uses database instead of memory
- [x] `AuthModule.kt` - Injects DAO dependency
- [x] `DatabaseBuilder.android.kt` - Added migration
- [x] `DatabaseBuilder.ios.kt` - Added migration
- [x] `DatabaseBuilder.jvm.kt` - Added migration

### Verify Koin DI ✅
Ensure these are in the DI graph:
- `AppDatabase` (from `databaseModule`)
- `UserAuthDao` (from `databaseModule`)
- `AuthLocalDataSource` with `UserAuthDao` injected (from `authModule`)

## Success Criteria

You'll know the implementation is working correctly when:

1. ✅ User stays logged in after app restart
2. ✅ Profile information appears immediately without API call
3. ✅ API calls work with persisted tokens
4. ✅ Logout clears all data from database
5. ✅ No crashes or errors in logs
6. ✅ Works on all target platforms (Android, iOS, Desktop)

## Next Steps After Testing

Once testing is successful:

1. **Remove debug logs** if you added any
2. **Consider adding encryption** for sensitive token storage
3. **Implement biometric authentication** for additional security
4. **Add token expiration checks** before using stored tokens
5. **Monitor performance** with database operations

## Need Help?

If you encounter issues:
1. Check the detailed documentation: `AUTH_PERSISTENT_STORAGE_IMPLEMENTATION.md`
2. Review migration script: `DatabaseMigrations.kt`
3. Check Koin DI configuration: `AuthModule.kt` and `DatabaseModule.kt`
4. Look for errors in logs/logcat

## Summary

The authentication persistence is now fully implemented using Room database. Test thoroughly on all platforms before deploying to production. The implementation follows clean architecture principles and integrates seamlessly with the existing codebase.

Happy Testing! 🚀

