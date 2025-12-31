package com.hisaabi.hisaabi_kmp

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hisaabi.hisaabi_kmp.di.initKoin

fun main() {
    initKoin()
    application {
        val windowState = rememberWindowState(
            size = DpSize(1280.dp, 800.dp),
            position = WindowPosition(Alignment.Center)
        )
        
        Window(
            onCloseRequest = ::exitApplication,
            title = "Hisaabi - Business Management",
            state = windowState,
        ) {
            App()
        }
    }
}