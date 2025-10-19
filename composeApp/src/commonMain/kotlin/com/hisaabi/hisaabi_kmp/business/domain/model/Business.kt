package com.hisaabi.hisaabi_kmp.business.domain.model

data class Business(
    val id: Int = 0,
    val title: String,
    val email: String?,
    val address: String?,
    val phone: String?,
    val logo: String?,
    val slug: String?
) {
    val displayName: String
        get() = title.ifEmpty { "Unknown" }
}

