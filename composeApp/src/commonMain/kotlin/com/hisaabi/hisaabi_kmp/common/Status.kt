package com.hisaabi.hisaabi_kmp.common

/**
 * Centralized status enum for all database entities.
 * Active = 0, Deleted = 2
 */
enum class Status(val value: Int, val displayName: String) {
    ACTIVE(0, "Active"),
    DELETED(2, "Deleted");
    
    companion object {
        fun fromInt(value: Int): Status? = entries.find { it.value == value }
    }
}

