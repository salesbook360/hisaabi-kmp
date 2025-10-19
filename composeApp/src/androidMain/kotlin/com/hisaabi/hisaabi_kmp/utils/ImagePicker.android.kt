package com.hisaabi.hisaabi_kmp.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

actual class ImagePicker {
    @Composable
    actual fun PickImage(onImageSelected: (String) -> Unit) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                onImageSelected(it.toString())
            }
        }
        
        // Return a function that can be called to launch the picker
        launcher.launch("image/*")
    }
}

