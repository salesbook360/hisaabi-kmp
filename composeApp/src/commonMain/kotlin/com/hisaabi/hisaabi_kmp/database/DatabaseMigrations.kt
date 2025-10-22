package com.hisaabi.hisaabi_kmp.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Database migration from version 1 to version 2.
 * 
 * Changes:
 * - Adds user_auth table for persistent authentication storage
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        // Create the user_auth table for storing authenticated user data
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

/**
 * Collection of all database migrations.
 * Add new migrations here as the database schema evolves.
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2
)

