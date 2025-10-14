package com.hisaabi.hisaabi_kmp.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbFile = File(System.getProperty("java.io.tmpdir"), AppDatabase.DATABASE_NAME)
        return Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

actual fun getDatabaseBuilder(): DatabaseBuilder {
    return DatabaseBuilder()
}

