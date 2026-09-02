package com.ravanbarvar.patientmanager.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalTime

object DocumentUtils {
    fun queryDisplayName(context: Context, uri: Uri): String {
        var name = uri.lastPathSegment ?: "مدرک"
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) cursor.getString(idx)?.let { name = it }
            }
        }
        return name
    }

    fun createCaptureUri(context: Context, patientId: Long): Uri {
        val dir = File(context.filesDir, "documents/$patientId").apply { mkdirs() }
        val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** [key] can be a saved patient's id, or any stable placeholder for a not-yet-saved one. */
    fun createAvatarCaptureUri(context: Context, key: String): Uri {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "avatar_${key}_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

fun currentMinutesOfDay(): Int {
    val now = LocalTime.now()
    return now.hour * 60 + now.minute
}
