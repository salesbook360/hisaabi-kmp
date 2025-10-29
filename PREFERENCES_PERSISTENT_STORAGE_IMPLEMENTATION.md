# Preferences Persistent Storage Implementation

## Overview

The `PreferencesManager` has been upgraded from in-memory storage to platform-specific persistent storage. All user preferences (transaction settings, receipt config, dashboard config, app settings) now persist across app restarts.

## Implementation Details

### Architecture

The implementation uses the **expect/actual** pattern to provide platform-specific storage implementations while maintaining a common interface.

```
KeyValueStorage (interface)
    ├── AndroidKeyValueStorage (SharedPreferences)
    ├── IOSKeyValueStorage (NSUserDefaults)
    ├── JVMKeyValueStorage (Properties file)
    └── WasmJsKeyValueStorage (localStorage)
```

### Files Created

#### 1. Common Interface
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/KeyValueStorage.kt`

Defines the platform-agnostic interface for key-value storage:
- `getString()`, `putString()` - String operations
- `getLong()`, `putLong()` - Long operations  
- `getBoolean()`, `putBoolean()` - Boolean operations
- `getInt()`, `putInt()` - Integer operations
- `remove()`, `clear()`, `contains()` - Management operations

Also contains the `expect fun createKeyValueStorage()` factory function.

#### 2. Android Implementation
**File**: `composeApp/src/androidMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/KeyValueStorage.android.kt`

Uses **SharedPreferences** for storage:
- Preferences file name: `hisaabi_preferences`
- All operations use `apply()` for asynchronous saves
- Requires initialization with Android Context

**Initialization**: Call `initKeyValueStorage(context)` from `MainActivity.onCreate()` or `Application.onCreate()`

#### 3. iOS Implementation
**File**: `composeApp/src/iosMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/KeyValueStorage.ios.kt`

Uses **NSUserDefaults** for storage:
- Uses standard user defaults (`NSUserDefaults.standardUserDefaults`)
- Automatically syncs after each write operation
- No initialization required - works out of the box

#### 4. JVM Implementation
**File**: `composeApp/src/jvmMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/KeyValueStorage.jvm.kt`

Uses **Properties file** for storage:
- File location: `~/.hisaabi/preferences.properties`
- Creates directory if it doesn't exist
- Saves to file after each operation
- No initialization required

#### 5. WasmJS Implementation
**File**: `composeApp/src/wasmJsMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/KeyValueStorage.wasmJs.kt`

Uses **browser localStorage** for storage:
- Uses native browser localStorage API
- All values stored as strings
- No initialization required

### Updated Files

#### PreferencesManager
**File**: `composeApp/src/commonMain/kotlin/com/hisaabi/hisaabi_kmp/settings/data/PreferencesManager.kt`

**Changes**:
1. Now accepts `KeyValueStorage` as constructor parameter (with default)
2. All settings are loaded from storage on initialization
3. All save operations persist to storage
4. Uses JSON serialization for complex objects (TransactionSettings, ReceiptConfig, DashboardConfig)
5. Uses direct storage for simple types (Boolean, String, Long)

**Persisted Data**:
- `transaction_settings` (JSON) - All transaction settings
- `receipt_config` (JSON) - Receipt configuration
- `dashboard_config` (JSON) - Dashboard visibility settings
- `biometric_auth_enabled` (Boolean) - Biometric auth preference
- `selected_language` (String) - Selected language code
- `selected_currency` (String) - Selected currency code
- Custom long values for sync and other features

#### MainActivity (Android)
**File**: `composeApp/src/androidMain/kotlin/com/hisaabi/hisaabi_kmp/MainActivity.kt`

**Changes**:
Added initialization call in `onCreate()`:
```kotlin
initKeyValueStorage(this)
```

Must be called before any PreferencesManager usage (before `setContent`).

## Usage

### Basic Usage (No changes required)

The `PreferencesManager` works exactly as before - storage is transparent:

```kotlin
// In ViewModel or Repository
class MyViewModel(private val preferencesManager: PreferencesManager) {
    
    fun saveSettings() {
        // Automatically persists to storage
        preferencesManager.saveTransactionSettings(newSettings)
    }
    
    fun loadSettings() {
        // Automatically loads from storage
        preferencesManager.transactionSettings.collect { settings ->
            // Use settings
        }
    }
}
```

### Android Initialization

In `MainActivity.onCreate()` or custom `Application` class:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize storage before using PreferencesManager
    initKeyValueStorage(this)
    
    // Rest of initialization...
}
```

### Direct Storage Access (Advanced)

If you need direct access to storage (rare):

```kotlin
val storage = createKeyValueStorage()
storage.putString("custom_key", "value")
val value = storage.getString("custom_key")
```

## Data Persistence

### What Gets Persisted

1. **Transaction Settings** (JSON):
   - All 26 transaction settings
   - Cash in/out, products, customers, services toggles
   - Decimal places, tax settings, number formatting
   - Performance and display options

2. **Receipt Configuration** (JSON):
   - Receipt generation preferences
   - Thermal printer settings
   - Invoice field visibility
   - Customer/business details
   - Logo and custom messages

3. **Dashboard Configuration** (JSON):
   - All section visibility toggles
   - Individual data point toggles
   - 9 dashboard sections with sub-options

4. **App Settings** (Direct):
   - Biometric authentication enabled/disabled
   - Selected language (language code)
   - Selected currency (currency code)

5. **Custom Long Values** (Direct):
   - Used by sync module and other features
   - Accessed via `getLong()`, `setLong()`, `observeLong()`

### Data Format

**Complex Objects** (Settings, Configs):
- Stored as JSON strings
- Uses kotlinx.serialization
- Backward compatible (ignores unknown keys)
- Falls back to defaults on parse errors

**Simple Values** (Boolean, String, Long):
- Stored in native format
- Direct storage/retrieval
- No serialization overhead

## Storage Locations by Platform

| Platform | Storage Type | Location |
|----------|-------------|----------|
| Android | SharedPreferences | `/data/data/com.hisaabi.hisaabi_kmp/shared_prefs/hisaabi_preferences.xml` |
| iOS | UserDefaults | Standard iOS user defaults domain |
| JVM Desktop | Properties File | `~/.hisaabi/preferences.properties` |
| WasmJS/Web | localStorage | Browser's localStorage (per domain) |

## Migration from In-Memory Storage

**Automatic Migration**: None required!

Since the old implementation was in-memory only, there's no existing data to migrate. On first launch after this update:
1. Storage will be empty
2. All settings will load with DEFAULT values
3. User can reconfigure their preferences
4. New preferences will persist across restarts

## Error Handling

### Load Errors
If loading from storage fails (corrupted data, parse errors):
- Falls back to DEFAULT values silently
- No crash or error shown to user
- User can reconfigure settings

### Save Errors
Platform implementations handle errors gracefully:
- Android: SharedPreferences operations rarely fail
- iOS: NSUserDefaults sync failures are silent
- JVM: File I/O exceptions are caught and logged
- WasmJS: localStorage has quota limits (rare to hit)

## Testing

### Manual Testing

1. **Save and Restart Test**:
   - Change transaction settings
   - Kill app completely
   - Restart app
   - Verify settings persisted

2. **Multiple Settings Test**:
   - Change transaction settings
   - Change receipt config
   - Change dashboard config
   - Change language/currency
   - Restart app
   - Verify all persisted

3. **Default Values Test**:
   - Clear app data (Android) or reinstall
   - Launch app
   - Verify defaults are loaded
   - Change settings
   - Verify new values persist

### Platform-Specific Testing

**Android**:
```bash
# View SharedPreferences file
adb shell run-as com.hisaabi.hisaabi_kmp cat /data/data/com.hisaabi.hisaabi_kmp/shared_prefs/hisaabi_preferences.xml

# Clear storage
adb shell pm clear com.hisaabi.hisaabi_kmp
```

**iOS**:
```swift
// View all UserDefaults in debugger
po UserDefaults.standard.dictionaryRepresentation()

// Clear storage
UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
```

**JVM**:
```bash
# View preferences file
cat ~/.hisaabi/preferences.properties

# Clear storage
rm -rf ~/.hisaabi/
```

**WasmJS**:
```javascript
// View all localStorage in browser console
console.log(localStorage);

// Clear storage
localStorage.clear();
```

## Performance Considerations

1. **Load Performance**:
   - All settings loaded once on PreferencesManager creation
   - Cached in memory (StateFlow)
   - Subsequent reads from memory (fast)

2. **Save Performance**:
   - JSON serialization is fast for small objects
   - Storage operations are async (non-blocking)
   - No UI lag when saving settings

3. **Memory Usage**:
   - Minimal - settings stored in memory and on disk
   - JSON strings are small (~1-10 KB total)
   - No memory leaks - storage lives as long as needed

## Future Enhancements

1. **Encryption**: Add encryption for sensitive settings
2. **Cloud Sync**: Sync preferences across devices
3. **Import/Export**: Backup and restore preferences
4. **Migration**: Handle schema changes in future versions
5. **Compression**: Compress JSON for very large configs
6. **Batching**: Batch multiple saves to reduce I/O

## Troubleshooting

### Android: "KeyValueStorage not initialized" Error

**Problem**: Calling PreferencesManager before initializing storage.

**Solution**: Ensure `initKeyValueStorage(context)` is called in `MainActivity.onCreate()` before any Koin injection or PreferencesManager usage.

### Settings Not Persisting

**Check**:
1. Is storage initialization called? (Android only)
2. Are save methods being called? (Check logs)
3. Is app being killed properly? (Not force-stopped during save)
4. Is storage quota available? (Web only)

### Settings Reset on App Update

**Expected**: On some platforms, app data is cleared on updates.

**Solution**: Implement cloud sync or export/import functionality.

## Dependencies

**No new dependencies added!**

All implementations use platform-standard APIs:
- Android: `android.content.SharedPreferences` (built-in)
- iOS: `platform.Foundation.NSUserDefaults` (built-in)
- JVM: `java.util.Properties` (built-in)
- WasmJS: `kotlinx.browser.localStorage` (kotlin-wrappers)

## Conclusion

The PreferencesManager now provides robust, platform-specific persistent storage with:
- ✅ Zero breaking changes to existing code
- ✅ Automatic persistence on all platforms
- ✅ Type-safe API with Flow support
- ✅ Error handling with fallback to defaults
- ✅ No new dependencies
- ✅ Production-ready implementation

All user preferences now survive app restarts, providing a better user experience.

