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
 * Database migration from version 3 to version 4.
 * 
 * Changes:
 * - Renames tax column to flat_tax in InventoryTransaction table
 * - Renames discount column to flat_discount in InventoryTransaction table
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        // Rename tax to flat_tax and discount to flat_discount
        // SQLite doesn't support ALTER COLUMN RENAME directly, so we need to:
        // 1. Create a new table with the new column names
        // 2. Copy data from old table to new table
        // 3. Drop old table
        // 4. Rename new table to old name
        
        // Create new table with renamed columns
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS InventoryTransaction_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                customer_slug TEXT,
                parent_slug TEXT,
                total_bill REAL NOT NULL,
                total_paid REAL NOT NULL,
                timestamp TEXT,
                flat_discount REAL NOT NULL,
                payment_method_to_slug TEXT,
                payment_method_from_slug TEXT,
                transaction_type INTEGER NOT NULL,
                price_type_id INTEGER NOT NULL,
                additional_charges_slug TEXT,
                additional_charges_desc TEXT,
                additional_charges REAL NOT NULL,
                discount_type_id INTEGER NOT NULL,
                tax_type_id INTEGER NOT NULL,
                flat_tax REAL NOT NULL,
                description TEXT,
                shipping_address TEXT,
                status_id INTEGER NOT NULL,
                state_id INTEGER NOT NULL,
                remind_at_milliseconds INTEGER NOT NULL,
                ware_house_slug_from TEXT,
                ware_house_slug_to TEXT,
                slug TEXT,
                business_slug TEXT,
                created_by TEXT,
                sync_status INTEGER NOT NULL,
                created_at TEXT,
                updated_at TEXT
            )
        """.trimIndent())
        
        // Copy data from old table to new table
        connection.execSQL("""
            INSERT INTO InventoryTransaction_new 
            SELECT 
                id,
                customer_slug,
                parent_slug,
                total_bill,
                total_paid,
                timestamp,
                discount AS flat_discount,
                payment_method_to_slug,
                payment_method_from_slug,
                transaction_type,
                price_type_id,
                additional_charges_slug,
                additional_charges_desc,
                additional_charges,
                discount_type_id,
                tax_type_id,
                tax AS flat_tax,
                description,
                shipping_address,
                status_id,
                state_id,
                remind_at_milliseconds,
                ware_house_slug_from,
                ware_house_slug_to,
                slug,
                business_slug,
                created_by,
                sync_status,
                created_at,
                updated_at
            FROM InventoryTransaction
        """.trimIndent())
        
        // Drop old table
        connection.execSQL("DROP TABLE InventoryTransaction")
        
        // Rename new table to old name
        connection.execSQL("ALTER TABLE InventoryTransaction_new RENAME TO InventoryTransaction")
        
        // Recreate the unique index
        connection.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_InventoryTransaction_slug 
            ON InventoryTransaction (slug)
        """.trimIndent())
    }
}

/**
 * Collection of all database migrations.
 * Add new migrations here as the database schema evolves.
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_3_4
)


