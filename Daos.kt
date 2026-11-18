package com.sherakbar.family.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.PhotoAlbum
import com.sherakbar.family.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM family_members ORDER BY generation ASC, fullName ASC")
    fun observeAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members ORDER BY createdAt DESC LIMIT 6")
    fun observeRecentMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members")
    suspend fun getAllOnce(): List<FamilyMember>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: FamilyMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(members: List<FamilyMember>)

    @Delete
    suspend fun delete(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM family_members")
    suspend fun clearAll()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, createdAt DESC")
    fun observeAll(): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements")
    suspend fun getAllOnce(): List<Announcement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(announcement: Announcement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<Announcement>)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM announcements")
    suspend fun clearAll()
}

@Dao
interface GalleryDao {
    @Query("SELECT * FROM photo_albums ORDER BY createdAt DESC")
    fun observeAlbums(): Flow<List<PhotoAlbum>>

    @Query("SELECT * FROM gallery_photos ORDER BY createdAt DESC")
    fun observeAllPhotos(): Flow<List<GalleryPhoto>>

    @Query("SELECT * FROM gallery_photos WHERE albumId = :albumId ORDER BY createdAt DESC")
    fun observePhotosByAlbum(albumId: String): Flow<List<GalleryPhoto>>

    @Query("SELECT * FROM photo_albums")
    suspend fun getAlbumsOnce(): List<PhotoAlbum>

    @Query("SELECT * FROM gallery_photos")
    suspend fun getPhotosOnce(): List<GalleryPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbum(album: PhotoAlbum)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlbums(albums: List<PhotoAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhoto(photo: GalleryPhoto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhotos(photos: List<GalleryPhoto>)

    @Query("DELETE FROM gallery_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Query("DELETE FROM photo_albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)

    @Query("DELETE FROM gallery_photos")
    suspend fun clearPhotos()

    @Query("DELETE FROM photo_albums")
    suspend fun clearAlbums()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserAccount)

    @Query("DELETE FROM user_accounts WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}
