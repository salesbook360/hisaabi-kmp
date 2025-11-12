package com.hisaabi.hisaabi_kmp.reports.domain.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

actual class ShareHelper(private val context: Context) {
    
    actual fun shareFile(filePath: String, mimeType: String, title: String) {
        try {
            val file = File(filePath)
            
            if (!file.exists()) {
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, title)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

