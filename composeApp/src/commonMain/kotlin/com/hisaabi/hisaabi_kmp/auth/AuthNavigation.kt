package com.hisaabi.hisaabi_kmp.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.hisaabi.hisaabi_kmp.auth.presentation.ui.LoginScreen
import com.hisaabi.hisaabi_kmp.auth.presentation.ui.ProfileScreen
import com.hisaabi.hisaabi_kmp.auth.presentation.ui.RegisterScreen
import com.hisaabi.hisaabi_kmp.auth.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject

@Composable
fun AuthNavigation(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit
) {
    val authViewModel: AuthViewModel = koinInject()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    var showRegister by remember { mutableStateOf(false) }
    
    when {
        isLoggedIn -> {
            ProfileScreen(
                viewModel = authViewModel,
                onLogout = {
                    showRegister = false
                }
            )
        }
        showRegister -> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { showRegister = false },
                onRegisterSuccess = onNavigateToMain
            )
        }
        else -> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { showRegister = true },
                onLoginSuccess = onNavigateToMain
            )
        }
    }
}
