package com.sherakbar.family.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_albums")
data class PhotoAlbum(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val description: String = "",
    val eventDate: String = "",
    val coverUri: String? = null,
    val coverDrawableResName: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_photos")
data class GalleryPhoto(
    @PrimaryKey val id: String = "",
    val albumId: String = "",
    val imageUri: String? = null,
    val drawableResName: String? = null,
    val caption: String = "",
    val eventName: String = "",
    val eventDate: String = "",
    val uploadedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
