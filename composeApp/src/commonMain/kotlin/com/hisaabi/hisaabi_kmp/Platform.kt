package com.hisaabi.hisaabi_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform