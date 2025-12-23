# Environment Configuration

This module provides environment-specific configuration for the application, including API base URLs.

## Environments

The application supports three environments:

- **DEV**: Development environment (`http://10.68.53.161:3000`)
- **STAGE**: Staging environment (`http://52.20.167.4:5000`)
- **LIVE**: Production/Live environment (`http://52.20.167.4:5000`)

## Configuration Files

Environment property files are located in `resources/`:
- `env.dev.properties` - Development environment
- `env.stage.properties` - Staging environment  
- `env.live.properties` - Live/Production environment

## Usage

The `AppConfig` interface is injected via Koin and provides the base URL for the current environment. All data sources use this configuration instead of hardcoded URLs.

### Default Environment

By default, the application uses the **STAGE** environment. To change the default, modify `getDefaultEnvironment()` in `AppConfig.kt`.

### Changing Environment

You can override the environment in several ways:

1. **Via Dependency Injection**: Override the `AppConfig` binding in your platform-specific Koin module
2. **Via Build Config** (Android): Add `BuildConfig.ENVIRONMENT` and read it in `getDefaultEnvironment()`
3. **Via Info.plist** (iOS): Add an environment key and read it in `getDefaultEnvironment()`
4. **Via System Properties**: Read from system properties or environment variables in `getDefaultEnvironment()`

### Example: Override in Platform-Specific Code

```kotlin
// In your platform module (Android/iOS/JVM)
single<AppConfig> {
    val environment = when {
        BuildConfig.DEBUG -> Environment.DEV
        else -> Environment.LIVE
    }
    AppConfigImpl(environment)
}
```

## Files Using Configuration

All remote data sources now use `AppConfig`:

- `AuthRemoteDataSource`
- `BusinessRemoteDataSource`
- `ProfileRepository`
- `SyncRemoteDataSource`

The base URL is injected via constructor injection and accessed through the `baseUrl` property.

