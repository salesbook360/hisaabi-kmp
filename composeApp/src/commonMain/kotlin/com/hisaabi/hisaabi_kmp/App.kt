package com.hisaabi.hisaabi_kmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hisaabi.hisaabi_kmp.auth.AuthNavigation
import com.hisaabi.hisaabi_kmp.home.HomeScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinContext {
            var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onNavigateToAuth = { currentScreen = AppScreen.AUTH }
                    )
                }
                AppScreen.AUTH -> {
                    AuthNavigation(
                        onNavigateToMain = { currentScreen = AppScreen.HOME }
                    )
                }
            }
        }
    }
}

enum class AppScreen {
    HOME,
    AUTH
}