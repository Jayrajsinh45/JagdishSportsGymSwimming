package com.jagdishsports.gymswimming.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

data class CameraPhotoTarget(
    val uri: Uri,
    val path: String
)

object MemberPhotoStorage {
    private const val PHOTO_DIR = "member_photos"

    fun createCameraPhotoTarget(context: Context): CameraPhotoTarget {
        val file = createPhotoFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return CameraPhotoTarget(uri = uri, path = file.absolutePath)
    }

    fun copyGalleryPhoto(context: Context, sourceUri: Uri): String {
        val file = createPhotoFile(context)
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open selected photo.")
        return file.absolutePath
    }

    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) {
            return
        }
        runCatching {
            File(path).takeIf { it.exists() && it.isFile }?.delete()
        }
    }

    private fun createPhotoFile(context: Context): File {
        val directory = File(context.filesDir, PHOTO_DIR).apply {
            mkdirs()
        }
        return File(directory, "member-${UUID.randomUUID()}.jpg")
    }
}
