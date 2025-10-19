package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class ExpenseIncomeType(val value: Int, val displayName: String) {
    EXPENSE(8, "Expense"),
    EXTRA_INCOME(9, "Extra Income");

    companion object {
        fun fromValue(value: Int): ExpenseIncomeType? {
            return values().find { it.value == value }
        }
    }
}

