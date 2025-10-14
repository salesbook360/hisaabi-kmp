package com.hisaabi.hisaabi_kmp.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual class DatabaseBuilder(private val context: Context) {
    actual fun build(): AppDatabase {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

actual fun getDatabaseBuilder(): DatabaseBuilder {
    // This will be injected by Koin with Android context
    throw IllegalStateException("DatabaseBuilder should be provided by Koin DI with Android context")
}

