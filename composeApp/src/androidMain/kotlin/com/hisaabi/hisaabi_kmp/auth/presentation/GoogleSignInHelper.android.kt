package com.hisaabi.hisaabi_kmp.auth.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Android implementation of GoogleSignInHelper using Compose
 */
class AndroidGoogleSignInHelper(
    private val googleSignInClient: GoogleSignInClient,
    private val launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) : GoogleSignInHelper {
    
    override fun signIn(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        println("=== Starting Google Sign-In ===")
        
        // Store callbacks temporarily
        // Note: In a real app, you'd want to handle this more robustly
        AndroidGoogleSignInHelper.onSuccessCallback = onSuccess
        AndroidGoogleSignInHelper.onFailureCallback = onFailure
        
        println("Callbacks stored")
        
        val signInIntent = googleSignInClient.signInIntent
        println("Launching sign-in intent")
        launcher.launch(signInIntent)
    }
    
    companion object {
        // Replace with your actual Web Client ID from Google Cloud Console
        const val WEB_CLIENT_ID = "107630732978-95o7huv64pimhu2ptf57ngk439qqdb29.apps.googleusercontent.com"
        
        // Temporary storage for callbacks - not ideal but works for single sign-in flow
        var onSuccessCallback: ((String) -> Unit)? = null
        var onFailureCallback: ((String) -> Unit)? = null
    }
}

/**
 * Actual implementation for Android - Composable version
 */
@Composable
actual fun rememberGoogleSignInHelper(): GoogleSignInHelper? {
    val context = LocalContext.current
    
    // Create GoogleSignInClient
    val googleSignInClient = remember(context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(AndroidGoogleSignInHelper.WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    // Use Compose's rememberLauncherForActivityResult instead of registerForActivityResult
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        println("=== Google Sign-In Result Received ===")
        println("Result code: ${result.resultCode}")
        println("Result data: ${result.data}")
        
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            println("Task obtained from intent")
            
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            println("Account: ${account.email}")
            
            val idToken = account.idToken
            println("ID Token: ${idToken?.take(50)}...")
            
            if (idToken != null) {
                println("Invoking success callback")
                AndroidGoogleSignInHelper.onSuccessCallback?.invoke(idToken)
            } else {
                println("ID token is null")
                AndroidGoogleSignInHelper.onFailureCallback?.invoke("Failed to get ID token")
            }
        } catch (e: ApiException) {
            println("ApiException: ${e.statusCode} - ${e.message}")
            e.printStackTrace()
            AndroidGoogleSignInHelper.onFailureCallback?.invoke("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
            AndroidGoogleSignInHelper.onFailureCallback?.invoke("Unexpected error: ${e.message}")
        } finally {
            // Clear callbacks after use
            println("Clearing callbacks")
            AndroidGoogleSignInHelper.onSuccessCallback = null
            AndroidGoogleSignInHelper.onFailureCallback = null
        }
    }
    
    return remember(googleSignInClient, launcher) {
        AndroidGoogleSignInHelper(googleSignInClient, launcher)
    }
}

/**
 * Deprecated - use rememberGoogleSignInHelper() instead
 */
actual fun getGoogleSignInHelper(): GoogleSignInHelper {
    throw UnsupportedOperationException("Use rememberGoogleSignInHelper() in Composable context instead")
}

