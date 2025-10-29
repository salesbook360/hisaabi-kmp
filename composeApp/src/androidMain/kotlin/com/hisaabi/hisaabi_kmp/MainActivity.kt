package com.hisaabi.hisaabi_kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hisaabi.hisaabi_kmp.receipt.ReceiptContextHolder
import com.hisaabi.hisaabi_kmp.settings.data.initKeyValueStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize platform-specific storage
        initKeyValueStorage(this)
        
        // Initialize receipt context
        ReceiptContextHolder.context = this

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}