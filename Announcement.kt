package com.sherakbar.family.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AnnouncementCategory {
    NEWS, WEDDING, BIRTH, FUNERAL, GATHERING, URGENT
}

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: AnnouncementCategory = AnnouncementCategory.NEWS,
    val authorName: String = "",
    val authorId: String = "",
    val eventDate: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
