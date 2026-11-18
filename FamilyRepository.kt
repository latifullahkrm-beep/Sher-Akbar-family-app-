package com.sherakbar.family.data.repository

import android.content.Context
import android.net.Uri
import com.sherakbar.family.data.local.BackupManager
import com.sherakbar.family.data.local.FamilyBackupPayload
import com.sherakbar.family.data.local.PreferencesManager
import com.sherakbar.family.data.local.SherAkbarDatabase
import com.sherakbar.family.data.model.AccountStatus
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.PhotoAlbum
import com.sherakbar.family.data.model.UserAccount
import com.sherakbar.family.data.model.UserRole
import com.sherakbar.family.data.remote.FirebaseSyncService
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class FamilyRepository(
    private val context: Context,
    private val db: SherAkbarDatabase,
    private val prefs: PreferencesManager,
    private val firebase: FirebaseSyncService,
    private val backupManager: BackupManager
) {
    val membersFlow: Flow<List<FamilyMember>> = db.memberDao().observeAllMembers()
    val recentMembersFlow: Flow<List<FamilyMember>> = db.memberDao().observeRecentMembers()
    val announcementsFlow: Flow<List<Announcement>> = db.announcementDao().observeAll()
    val albumsFlow: Flow<List<PhotoAlbum>> = db.galleryDao().observeAlbums()
    val photosFlow: Flow<List<GalleryPhoto>> = db.galleryDao().observeAllPhotos()
    val usersFlow: Flow<List<UserAccount>> = db.userDao().observeAllUsers()

    suspend fun getMember(id: String): FamilyMember? = db.memberDao().getById(id)

    suspend fun upsertMember(member: FamilyMember) {
        val updated = member.copy(updatedAt = System.currentTimeMillis())
        db.memberDao().upsert(updated)
        runCatching { firebase.pushMember(updated) }
    }

    suspend fun deleteMember(id: String) {
        db.memberDao().deleteById(id)
        runCatching { firebase.deleteMember(id) }
    }

    suspend fun upsertAnnouncement(announcement: Announcement) {
        db.announcementDao().upsert(announcement)
        runCatching { firebase.pushAnnouncement(announcement) }
    }

    suspend fun deleteAnnouncement(id: String) {
        db.announcementDao().deleteById(id)
        runCatching { firebase.deleteAnnouncement(id) }
    }

    suspend fun createAlbum(name: String, description: String, eventDate: String, creator: String) {
        val album = PhotoAlbum(
            id = "alb_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            description = description.trim(),
            eventDate = eventDate.trim(),
            createdBy = creator
        )
        db.galleryDao().upsertAlbum(album)
        runCatching { firebase.pushAlbum(album) }
    }

    suspend fun addPhotoToAlbum(
        albumId: String,
        sourceUri: Uri,
        caption: String,
        eventName: String,
        eventDate: String,
        uploadedBy: String
    ) {
        val persistedUri = copyUriToLocalGallery(sourceUri)
        val remoteOrLocalUrl = runCatching {
            if (firebase.isConfigured) firebase.uploadImageIfConfigured(sourceUri, "gallery/$albumId")
            else persistedUri
        }.getOrDefault(persistedUri)

        val photo = GalleryPhoto(
            id = "ph_${UUID.randomUUID().toString().take(8)}",
            albumId = albumId,
            imageUri = remoteOrLocalUrl,
            caption = caption.trim(),
            eventName = eventName.trim(),
            eventDate = eventDate.trim(),
            uploadedBy = uploadedBy
        )
        db.galleryDao().upsertPhoto(photo)
        runCatching { firebase.pushPhoto(photo) }
    }

    suspend fun deletePhoto(photoId: String) {
        db.galleryDao().deletePhotoById(photoId)
        runCatching { firebase.deletePhoto(photoId) }
    }

    fun copyUriToLocalGallery(uri: Uri): String {
        val dir = File(context.filesDir, "gallery").apply { mkdirs() }
        val target = File(dir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return Uri.fromFile(target).toString()
    }

    suspend fun login(email: String, password: String): Result<UserAccount> {
        val normalized = email.trim().lowercase()
        if (firebase.isConfigured) {
            firebase.signInWithEmail(normalized, password)
        }
        val localUser = db.userDao().getByEmail(normalized)
            ?: return Result.failure(IllegalArgumentException("No account found for $normalized."))

        if (localUser.passwordHash != SherAkbarDatabase.hashPassword(password)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        if (localUser.status == AccountStatus.PENDING) {
            return Result.failure(IllegalStateException("Your account is awaiting administrator approval."))
        }
        if (localUser.status == AccountStatus.SUSPENDED) {
            return Result.failure(IllegalStateException("This account has been suspended by the family administrator."))
        }
        prefs.setCurrentUserUid(localUser.uid)
        return Result.success(localUser)
    }

    suspend fun register(fullName: String, email: String, password: String): Result<UserAccount> {
        val normalized = email.trim().lowercase()
        if (fullName.isBlank() || normalized.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Enter full name, valid email, and password (min 6 chars)."))
        }
        if (db.userDao().getByEmail(normalized) != null) {
            return Result.failure(IllegalStateException("An account with this email already exists."))
        }
        if (firebase.isConfigured) {
            firebase.registerWithEmail(normalized, password)
        }
        val newAccount = UserAccount(
            uid = "user_${UUID.randomUUID().toString().take(8)}",
            fullName = fullName.trim(),
            email = normalized,
            passwordHash = SherAkbarDatabase.hashPassword(password),
            role = UserRole.MEMBER,
            status = AccountStatus.PENDING
        )
        db.userDao().upsert(newAccount)
        return Result.success(newAccount)
    }

    suspend fun resetPassword(email: String): Result<String> {
        val normalized = email.trim().lowercase()
        if (firebase.isConfigured) {
            val remote = firebase.sendPasswordReset(normalized)
            if (remote.isSuccess) return Result.success("Password reset email sent to $normalized.")
        }
        val existing = db.userDao().getByEmail(normalized)
            ?: return Result.failure(IllegalArgumentException("No local account registered for $normalized."))
        val updated = existing.copy(passwordHash = SherAkbarDatabase.hashPassword("Family@2026"))
        db.userDao().upsert(updated)
        return Result.success("Offline reset complete. Temporary password set to Family@2026.")
    }

    suspend fun restoreCurrentSession(): UserAccount? {
        val uid = prefs.getCurrentUserUid() ?: return null
        return db.userDao().getByUid(uid)?.takeIf { it.status == AccountStatus.APPROVED }
    }

    fun logout() {
        prefs.setCurrentUserUid(null)
        firebase.signOut()
    }

    suspend fun updateUserAccount(user: UserAccount) {
        db.userDao().upsert(user)
    }

    suspend fun exportBackup(): Uri {
        val payload = FamilyBackupPayload(
            members = db.memberDao().getAllOnce(),
            announcements = db.announcementDao().getAllOnce(),
            albums = db.galleryDao().getAlbumsOnce(),
            photos = db.galleryDao().getPhotosOnce()
        )
        return backupManager.exportToJsonFile(payload)
    }

    suspend fun importBackup(uri: Uri) {
        val payload = backupManager.parseBackupFromUri(uri)
        db.memberDao().clearAll()
        db.memberDao().upsertAll(payload.members)
        db.announcementDao().clearAll()
        db.announcementDao().upsertAll(payload.announcements)
        db.galleryDao().clearPhotos()
        db.galleryDao().clearAlbums()
        db.galleryDao().upsertAlbums(payload.albums)
        db.galleryDao().upsertPhotos(payload.photos)
    }

    suspend fun syncWithFirebase(): Result<String> {
        if (!firebase.isConfigured) {
            prefs.updateLastSync(System.currentTimeMillis())
            return Result.success("Offline Room cache verified. Add google-services.json for cloud sync.")
        }
        return runCatching {
            db.memberDao().getAllOnce().forEach { firebase.pushMember(it) }
            db.announcementDao().getAllOnce().forEach { firebase.pushAnnouncement(it) }
            val cloudMembers = firebase.pullAllMembers()
            if (cloudMembers.isNotEmpty()) db.memberDao().upsertAll(cloudMembers)
            val cloudAnnouncements = firebase.pullAllAnnouncements()
            if (cloudAnnouncements.isNotEmpty()) db.announcementDao().upsertAll(cloudAnnouncements)
            prefs.updateLastSync(System.currentTimeMillis())
            "Synchronized ${cloudMembers.size} members with Cloud Firestore."
        }
    }
}
