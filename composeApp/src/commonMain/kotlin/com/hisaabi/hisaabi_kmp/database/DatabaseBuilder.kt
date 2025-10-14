package com.hisaabi.hisaabi_kmp.database

import androidx.room.RoomDatabase

/**
 * Expect function to build the database instance for each platform
 */
expect class DatabaseBuilder {
    fun build(): AppDatabase
}

/**
 * Helper function to get the database builder
 */
expect fun getDatabaseBuilder(): DatabaseBuilder

