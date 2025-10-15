package com.hisaabi.hisaabi_kmp.auth.presentation

/**
 * Interface for Google Sign-In functionality
 * Platform-specific implementations are required
 */
interface GoogleSignInHelper {
    /**
     * Initiates Google Sign-In flow
     * @param onSuccess Called with the Google ID token when sign-in succeeds
     * @param onFailure Called with error message when sign-in fails
     */
    fun signIn(onSuccess: (String) -> Unit, onFailure: (String) -> Unit)
}

/**
 * Expect function to get platform-specific GoogleSignInHelper
 * Implementations should be provided in each platform's source set
 */
expect fun getGoogleSignInHelper(): GoogleSignInHelper


