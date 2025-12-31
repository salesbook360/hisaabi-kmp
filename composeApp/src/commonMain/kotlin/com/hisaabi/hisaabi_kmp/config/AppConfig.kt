package com.hisaabi.hisaabi_kmp.config

/**
 * Application configuration that provides environment-specific settings
 */
interface AppConfig {
    val baseUrl: String
    val environment: Environment
}

class AppConfigImpl(
    override val environment: Environment
) : AppConfig {
    override val baseUrl: String by lazy {
        when (environment) {
            Environment.DEV -> "http://10.0.0.12:3000"
            Environment.STAGE -> "http://52.20.167.4:5000"
            Environment.LIVE -> "http://52.20.167.4:5000"
        }
    }
}

/**
 * Default environment - can be changed via build config or runtime settings
 * 
 * Defaults to STAGE environment (http://52.20.167.4:5000)
 * This can be overridden via:
 * - Build config (Android: BuildConfig.ENVIRONMENT)
 * - Info.plist or build settings (iOS)
 * - System properties or environment variables
 * - Dependency injection override
 */
fun getDefaultEnvironment(): Environment {
    // You can read from build config, system property, or environment variable here
    // For example, in Android: BuildConfig.ENVIRONMENT
    // For iOS: Info.plist or build settings
    // For now, defaulting to STAGE (production URL)
    return Environment.LIVE
}

