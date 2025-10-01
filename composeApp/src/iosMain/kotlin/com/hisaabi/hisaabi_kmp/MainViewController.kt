package com.hisaabi.hisaabi_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.hisaabi.hisaabi_kmp.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) {

    App()
}