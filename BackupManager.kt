package com.sherakbar.family.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.PhotoAlbum
import java.io.File

data class FamilyBackupPayload(
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val members: List<FamilyMember>,
    val announcements: List<Announcement>,
    val albums: List<PhotoAlbum>,
    val photos: List<GalleryPhoto>
)

class BackupManager(private val context: Context) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJsonFile(payload: FamilyBackupPayload): Uri {
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val file = File(dir, "sher_akbar_family_backup_${System.currentTimeMillis()}.json")
        file.writeText(gson.toJson(payload))
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun parseBackupFromUri(uri: Uri): FamilyBackupPayload {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("Unable to open selected backup file.")
        return gson.fromJson(json, FamilyBackupPayload::class.java)
            ?: throw IllegalArgumentException("Invalid Sher Akbar backup JSON format.")
    }
}
