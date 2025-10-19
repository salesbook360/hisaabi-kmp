package com.hisaabi.hisaabi_kmp.transactions.domain.model

enum class PayGetCashType(val value: Int, val displayName: String) {
    GET_CASH(1, "Get Payment"),
    PAY_CASH(2, "Pay Payment");
    
    companion object {
        fun fromValue(value: Int): PayGetCashType? {
            return values().find { it.value == value }
        }
    }
}

enum class PartyTypeForCash(val displayName: String) {
    CUSTOMER("Customer"),
    VENDOR("Vendor"),
    INVESTOR("Investor");
    
    fun getTransactionType(payGetCashType: PayGetCashType): Int {
        return when (this) {
            CUSTOMER -> if (payGetCashType == PayGetCashType.GET_CASH) 7 else 6 // GET_FROM_CUSTOMER or PAY_TO_CUSTOMER
            VENDOR -> if (payGetCashType == PayGetCashType.GET_CASH) 5 else 4 // GET_FROM_VENDOR or PAY_TO_VENDOR
            INVESTOR -> if (payGetCashType == PayGetCashType.GET_CASH) 12 else 11 // INVESTMENT_WITHDRAW or INVESTMENT_DEPOSIT
        }
    }
}

