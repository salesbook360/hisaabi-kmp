package com.hisaabi.hisaabi_kmp.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.hisaabi.hisaabi_kmp.auth.presentation.rememberGoogleSignInHelper
import com.hisaabi.hisaabi_kmp.auth.presentation.ui.ForgotPasswordScreen
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
    
    // Get platform-specific Google Sign-In helper (null if not supported)
    val googleSignInHelper = rememberGoogleSignInHelper()
    
    var showRegister by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    
    when {
        isLoggedIn -> {
            ProfileScreen(
                viewModel = authViewModel,
                onLogout = {
                    showRegister = false
                    showForgotPassword = false
                }
            )
        }
        showForgotPassword -> {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { showForgotPassword = false }
            )
        }
        showRegister -> {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { 
                    showRegister = false
                    showForgotPassword = false
                },
                onRegisterSuccess = onNavigateToMain
            )
        }
        else -> {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { showRegister = true },
                onNavigateToForgotPassword = { showForgotPassword = true },
                onLoginSuccess = onNavigateToMain,
                googleSignInHelper = googleSignInHelper
            )
        }
    }
}
