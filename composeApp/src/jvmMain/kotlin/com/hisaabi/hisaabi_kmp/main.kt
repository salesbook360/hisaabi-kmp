package com.hisaabi.hisaabi_kmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hisaabi.hisaabi_kmp.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Hisaabikmp",
        ) {
            App()
        }
    }
}