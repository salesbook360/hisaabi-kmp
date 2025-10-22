package com.hisaabi.hisaabi_kmp.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSHomeDirectory

actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbFile = NSHomeDirectory() + "/${AppDatabase.DATABASE_NAME}"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(*ALL_MIGRATIONS)
            .fallbackToDestructiveMigration(false)
            .build()
    }
}

actual fun getDatabaseBuilder(): DatabaseBuilder {
    return DatabaseBuilder()
}

