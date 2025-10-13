package com.hisaabi.hisaabi_kmp.auth.domain.model

data class User(
    val id: Int,
    val name: String,
    val address: String,
    val email: String,
    val phone: String,
    val slug: String,
    val firebaseId: String,
    val pic: String? = null  // Made nullable as API can return null
) {
    val displayName: String
        get() = name.ifEmpty { email }
}
