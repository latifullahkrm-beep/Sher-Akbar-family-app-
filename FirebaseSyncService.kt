package com.sherakbar.family.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.PhotoAlbum
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Online sync bridge for Firebase Auth, Cloud Firestore, and Firebase Storage.
 * Automatically activates when google-services.json is configured; safely degrades
 * to offline-first Room mode when unconfigured or offline.
 */
class FirebaseSyncService(private val context: Context) {

    val isConfigured: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    private val auth: FirebaseAuth?
        get() = if (isConfigured) FirebaseAuth.getInstance() else null

    private val firestore: FirebaseFirestore?
        get() = if (isConfigured) FirebaseFirestore.getInstance() else null

    private val storage: FirebaseStorage?
        get() = if (isConfigured) FirebaseStorage.getInstance() else null

    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase not configured"))
        return runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid ?: error("Authentication succeeded without UID")
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<String> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase not configured"))
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid ?: error("Registration succeeded without UID")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase not configured"))
        return runCatching {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Unit
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    suspend fun pushMember(member: FamilyMember) {
        firestore?.collection("members")?.document(member.id)?.set(member, SetOptions.merge())?.await()
    }

    suspend fun deleteMember(memberId: String) {
        firestore?.collection("members")?.document(memberId)?.delete()?.await()
    }

    suspend fun pushAnnouncement(announcement: Announcement) {
        firestore?.collection("announcements")?.document(announcement.id)?.set(announcement, SetOptions.merge())?.await()
    }

    suspend fun deleteAnnouncement(id: String) {
        firestore?.collection("announcements")?.document(id)?.delete()?.await()
    }

    suspend fun pushAlbum(album: PhotoAlbum) {
        firestore?.collection("albums")?.document(album.id)?.set(album, SetOptions.merge())?.await()
    }

    suspend fun pushPhoto(photo: GalleryPhoto) {
        firestore?.collection("photos")?.document(photo.id)?.set(photo, SetOptions.merge())?.await()
    }

    suspend fun deletePhoto(photoId: String) {
        firestore?.collection("photos")?.document(photoId)?.delete()?.await()
    }

    suspend fun uploadImageIfConfigured(localUri: Uri, folder: String): String {
        val storageRef = storage ?: return localUri.toString()
        val ref = storageRef.reference.child("$folder/${UUID.randomUUID()}.jpg")
        ref.putFile(localUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun pullAllMembers(): List<FamilyMember> {
        val db = firestore ?: return emptyList()
        val snapshot = db.collection("members").get().await()
        return snapshot.toObjects(FamilyMember::class.java)
    }

    suspend fun pullAllAnnouncements(): List<Announcement> {
        val db = firestore ?: return emptyList()
        val snapshot = db.collection("announcements").get().await()
        return snapshot.toObjects(Announcement::class.java)
    }
}
