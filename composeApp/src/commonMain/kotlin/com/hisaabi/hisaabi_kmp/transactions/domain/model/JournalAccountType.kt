package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class JournalAccountType(val displayName: String) {
    EXPENSE("Expense Account"),
    EXTRA_INCOME("Extra Income Account"),
    CUSTOMER("Customer"),
    VENDOR("Vendor"),
    INVESTOR("Investor"),
    PAYMENT_METHOD("Payment Method");

    companion object {
        fun fromPartyRoleId(roleId: Int?): JournalAccountType? {
            return when (roleId) {
                0, 11 -> CUSTOMER  // Customer (0), Walk-in Customer (11)
                1, 10 -> VENDOR    // Vendor (1), Default Vendor (10)
                12 -> INVESTOR     // Investor (12)
                14 -> EXPENSE      // Expense (14)
                15 -> EXTRA_INCOME // Extra Income (15)
                else -> null
            }
        }
    }
}

