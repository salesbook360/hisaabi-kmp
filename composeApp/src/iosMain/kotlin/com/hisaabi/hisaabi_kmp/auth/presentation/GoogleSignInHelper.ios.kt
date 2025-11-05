package com.hisaabi.hisaabi_kmp.auth.presentation

actual fun getGoogleSignInHelper(): GoogleSignInHelper {
   return object : GoogleSignInHelper{
        override fun signIn(
            onSuccess: (String) -> Unit,
            onFailure: (String) -> Unit
        ) {
            TODO("Not yet implemented")
        }
    }
}