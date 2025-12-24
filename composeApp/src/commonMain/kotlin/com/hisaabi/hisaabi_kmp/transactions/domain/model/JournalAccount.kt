package com.hisaabi.hisaabi_kmp.transactions.domain.model

import com.hisaabi.hisaabi_kmp.parties.domain.model.Party
import com.hisaabi.hisaabi_kmp.paymentmethods.domain.model.PaymentMethod

/**
 * Represents a journal account entry in a journal voucher.
 * Each entry can be a pay amount or get amount to various account types.
 * 
 * @param title Display name of the account
 * @param amount Amount for this account entry
 * @param isDebit true for pay amount (pay/expense), false for get amount (get/income)
 * @param accountType Type of journal account
 * @param party Associated party (if account type is CUSTOMER, VENDOR, INVESTOR, EXPENSE, or EXTRA_INCOME)
 * @param paymentMethod Associated payment method (if account type is PAYMENT_METHOD)
 */
data class JournalAccount(
    val title: String,
    var amount: Double = 0.0,
    var isDebit: Boolean = true, // true = Pay Amount, false = Get Amount
    val accountType: JournalAccountType,
    val party: Party? = null,
    val paymentMethod: PaymentMethod? = null
) {
    /**
     * Returns the account identifier (party slug or payment method slug)
     */
    fun getAccountId(): String? {
        return when (accountType) {
            JournalAccountType.PAYMENT_METHOD -> paymentMethod?.slug
            else -> party?.slug
        }
    }

    /**
     * Checks if this is an expense or income account
     */
    fun isExpenseOrIncomeAccount(): Boolean {
        return accountType == JournalAccountType.EXPENSE || 
               accountType == JournalAccountType.EXTRA_INCOME
    }

    /**
     * Gets the label for pay amount/get amount based on account type
     */
    fun getDebitCreditLabel(): String {
        return if (isExpenseOrIncomeAccount()) {
            if (isDebit) "Add" else "Get"
        } else {
            if (isDebit) "Pay Amount" else "Get Amount"
        }
    }
}

