package com.hisaabi.hisaabi_kmp.parties.domain.model

data class Party(
    val id: Int = 0,
    val name: String,
    val phone: String?,
    val address: String?,
    val balance: Double = 0.0,
    val openingBalance: Double = 0.0,
    val thumbnail: String?,
    val roleId: Int,  // UserType: 0=Customer, 1=Vendor, 12=Investor
    val personStatus: Int = 1,
    val digitalId: String?,
    val latLong: String?,
    val areaSlug: String?,
    val categorySlug: String?,
    val email: String?,
    val description: String?,
    val slug: String,
    val businessSlug: String?,
    val createdBy: String?,
    val syncStatus: Int = 0,
    val createdAt: String?,
    val updatedAt: String?
) {
    val isCustomer: Boolean
        get() = roleId == PartyType.CUSTOMER.type || roleId == PartyType.WALK_IN_CUSTOMER.type
    
    val isVendor: Boolean
        get() = roleId == PartyType.VENDOR.type || roleId == PartyType.DEFAULT_VENDOR.type
    
    val isInvestor: Boolean
        get() = roleId == PartyType.INVESTOR.type
    
    val balanceStatus: BalanceStatus
        get() = when {
            balance > 0 -> BalanceStatus.PAYABLE
            balance < 0 -> BalanceStatus.RECEIVABLE
            else -> BalanceStatus.ZERO
        }
    
    val displayName: String
        get() = name.ifEmpty { email ?: "Unknown" }
}

enum class PartyType(val type: Int) {
    CUSTOMER(0),
    VENDOR(1),
    DEFAULT_VENDOR(10),
    WALK_IN_CUSTOMER(11),
    INVESTOR(12),
    STOCK_ADJUSTER(13),
    EXPENSE(14),
    EXTRA_INCOME(15),
    MANUFACTURER(16);
    
    companion object {
        fun fromInt(value: Int): PartyType? = entries.find { it.type == value }
    }
}

enum class BalanceStatus {
    PAYABLE,    // You will pay (balance > 0)
    RECEIVABLE, // You will get (balance < 0)
    ZERO        // Settled up (balance = 0)
}

enum class PartiesFilter {
    ALL_PARTIES,
    BALANCE_RECEIVABLE,
    BALANCE_PAYABLE,
    BALANCE_ZERO
}

enum class PartySegment {
    CUSTOMER,
    VENDOR,
    INVESTOR,
    EXPENSE,
    EXTRA_INCOME;
    
    fun toPartyTypes(): List<Int> = when (this) {
        CUSTOMER -> listOf(PartyType.CUSTOMER.type, PartyType.WALK_IN_CUSTOMER.type)
        VENDOR -> listOf(PartyType.VENDOR.type, PartyType.DEFAULT_VENDOR.type)
        INVESTOR -> listOf(PartyType.INVESTOR.type)
        EXPENSE -> listOf(14) // UserTypeEnum.EXPENSE
        EXTRA_INCOME -> listOf(15) // UserTypeEnum.EXTRA_INCOME
    }
}



