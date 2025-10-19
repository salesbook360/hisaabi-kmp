package com.hisaabi.hisaabi_kmp.utils

import androidx.compose.runtime.Composable

/**
 * Platform-specific image picker
 * Returns the selected image URI/path as a String
 */
expect class ImagePicker() {
    @Composable
    fun PickImage(
        onImageSelected: (String) -> Unit
    )
}

