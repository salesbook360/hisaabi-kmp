package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class RecordType(val value: Int, val displayName: String) {
    MEETING(21, "Meeting"),
    TASK(22, "Task"),
    CLIENT_NOTE(23, "Client Note"),
    SELF_NOTE(24, "Self Note"),
    CASH_REMINDER(25, "Cash Reminder");
    
    companion object {
        fun fromValue(value: Int): RecordType? {
            return values().find { it.value == value }
        }
        
        fun requiresParty(recordType: RecordType): Boolean {
            return when (recordType) {
                MEETING, TASK, CLIENT_NOTE, CASH_REMINDER -> true
                SELF_NOTE -> false
            }
        }
        
        fun showsAmountField(recordType: RecordType): Boolean {
            return recordType == CASH_REMINDER
        }
    }
}

