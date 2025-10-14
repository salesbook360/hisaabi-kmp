package com.hisaabi.hisaabi_kmp.database

/**
 * WasmJS implementation - Room is not yet supported on WasmJS
 * This is a stub implementation that throws an error if database is accessed
 */
actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        throw UnsupportedOperationException(
            "Room database is not yet supported on WasmJS platform. " +
            "Please use Android, iOS, or JVM targets for database functionality."
        )
    }
}

actual fun getDatabaseBuilder(): DatabaseBuilder {
    return DatabaseBuilder()
}

