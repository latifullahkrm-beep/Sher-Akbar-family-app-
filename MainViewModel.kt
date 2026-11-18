package com.sherakbar.family.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sherakbar.family.SherAkbarApplication
import com.sherakbar.family.data.model.AccountStatus
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.AnnouncementCategory
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.FamilyStats
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.Gender
import com.sherakbar.family.data.model.MemberCategory
import com.sherakbar.family.data.model.MemberSortOrder
import com.sherakbar.family.data.model.PhotoAlbum
import com.sherakbar.family.data.model.UserAccount
import com.sherakbar.family.data.model.UserRole
import com.sherakbar.family.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as SherAkbarApplication
    private val repo = app.repository
    private val prefs = app.preferences

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    val isAdmin: StateFlow<Boolean> = _currentUser
        .map { it?.role == UserRole.ADMIN && it.status == AccountStatus.APPROVED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _authInitialized = MutableStateFlow(false)
    val authInitialized: StateFlow<Boolean> = _authInitialized.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val darkMode: StateFlow<Boolean> = prefs.darkMode
    val languageCode: StateFlow<String> = prefs.languageCode
    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled
    val lastSyncEpoch: StateFlow<Long> = prefs.lastSyncEpoch

    val allMembers: StateFlow<List<FamilyMember>> = repo.membersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMembers: StateFlow<List<FamilyMember>> = repo.recentMembersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyStats: StateFlow<FamilyStats> = allMembers.map { list ->
        FamilyStats(
            totalMembers = list.size,
            maleMembers = list.count { it.gender == Gender.MALE },
            femaleMembers = list.count { it.gender == Gender.FEMALE },
            totalGenerations = list.maxOfOrNull { it.generation } ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyStats())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MemberCategory.ALL)
    val selectedCategory: StateFlow<MemberCategory> = _selectedCategory.asStateFlow()

    private val _sortOrder = MutableStateFlow(MemberSortOrder.NAME_ASC)
    val sortOrder: StateFlow<MemberSortOrder> = _sortOrder.asStateFlow()

    val filteredMembers: StateFlow<List<FamilyMember>> = combine(
        allMembers, _searchQuery, _selectedCategory, _sortOrder
    ) { members, query, category, sort ->
        val byCat = when (category) {
            MemberCategory.ALL -> members
            MemberCategory.MALE -> members.filter { it.gender == Gender.MALE }
            MemberCategory.FEMALE -> members.filter { it.gender == Gender.FEMALE }
            MemberCategory.CHILDREN -> members.filter { it.isChild || it.generation >= 4 }
            MemberCategory.ELDERS -> members.filter { it.isElder || it.generation == 1 }
            MemberCategory.DECEASED -> members.filter { it.isDeceased || !it.dateOfDeath.isNullOrBlank() }
        }
        val q = query.trim().lowercase()
        val byQuery = if (q.isBlank()) byCat else {
            byCat.filter {
                it.fullName.lowercase().contains(q) ||
                    it.fullNameUrdu.contains(q) ||
                    it.fatherName.lowercase().contains(q) ||
                    it.address.lowercase().contains(q)
            }
        }
        when (sort) {
            MemberSortOrder.NAME_ASC -> byQuery.sortedBy { it.fullName.lowercase() }
            MemberSortOrder.NAME_DESC -> byQuery.sortedByDescending { it.fullName.lowercase() }
            MemberSortOrder.GENERATION_ASC -> byQuery.sortedWith(compareBy({ it.generation }, { it.fullName }))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = repo.announcementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<PhotoAlbum>> = repo.albumsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhotos: StateFlow<List<GalleryPhoto>> = repo.photosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserAccount>> = repo.usersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _currentUser.value = repo.restoreCurrentSession()
            _authInitialized.value = true
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectCategory(category: MemberCategory) { _selectedCategory.value = category }
    fun updateSortOrder(sort: MemberSortOrder) { _sortOrder.value = sort }
    fun clearMessages() { _statusMessage.value = null; _errorMessage.value = null }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.login(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _errorMessage.value = null
                    _statusMessage.value = "Welcome back, ${user.fullName}"
                }
                .onFailure { _errorMessage.value = it.message ?: "Unable to sign in." }
            _isLoading.value = false
        }
    }

    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.register(fullName, email, password)
                .onSuccess {
                    _errorMessage.value = null
                    _statusMessage.value = "Registration submitted! An administrator will approve your account."
                }
                .onFailure { _errorMessage.value = it.message ?: "Registration failed." }
            _isLoading.value = false
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repo.resetPassword(email)
                .onSuccess { msg -> _statusMessage.value = msg }
                .onFailure { _errorMessage.value = it.message ?: "Could not reset password." }
            _isLoading.value = false
        }
    }

    fun logout() {
        repo.logout()
        _currentUser.value = null
    }

    fun saveMember(member: FamilyMember, localPhotoUri: Uri? = null) {
        if (!isAdmin.value) {
            _errorMessage.value = "Only family administrators can add or modify member records."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val finalUri = localPhotoUri?.let { repo.copyUriToLocalGallery(it) } ?: member.photoUri
            val finalMember = member.copy(
                id = member.id.ifBlank { "m_${UUID.randomUUID().toString().take(8)}" },
                photoUri = finalUri
            )
            repo.upsertMember(finalMember)
            _statusMessage.value = "${finalMember.fullName} saved successfully."
            _isLoading.value = false
        }
    }

    fun deleteMember(memberId: String) {
        if (!isAdmin.value) {
            _errorMessage.value = "Only administrators can delete records."
            return
        }
        viewModelScope.launch {
            repo.deleteMember(memberId)
            _statusMessage.value = "Member record removed."
        }
    }

    fun publishAnnouncement(
        id: String?,
        title: String,
        content: String,
        category: AnnouncementCategory,
        eventDate: String,
        isPinned: Boolean
    ) {
        if (!isAdmin.value) {
            _errorMessage.value = "Only administrators can publish announcements."
            return
        }
        viewModelScope.launch {
            val author = _currentUser.value
            val ann = Announcement(
                id = id ?: "ann_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                content = content.trim(),
                category = category,
                authorName = author?.fullName ?: "Sher Akbar Admin",
                authorId = author?.uid ?: "admin_1",
                eventDate = eventDate.trim(),
                isPinned = isPinned
            )
            repo.upsertAnnouncement(ann)
            if (notificationsEnabled.value) {
                NotificationHelper.showAnnouncementNotification(app, ann.title, ann.content)
            }
            _statusMessage.value = "Announcement published."
        }
    }

    fun deleteAnnouncement(id: String) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            repo.deleteAnnouncement(id)
            _statusMessage.value = "Announcement deleted."
        }
    }

    fun createAlbum(name: String, description: String, eventDate: String) {
        viewModelScope.launch {
            val creator = _currentUser.value?.fullName ?: "Family Member"
            repo.createAlbum(name, description, eventDate, creator)
            _statusMessage.value = "Album '$name' created."
        }
    }

    fun uploadPhoto(albumId: String, uri: Uri, caption: String, eventName: String, eventDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val uploader = _currentUser.value?.fullName ?: "Family Member"
            repo.addPhotoToAlbum(albumId, uri, caption, eventName, eventDate, uploader)
            _statusMessage.value = "Photo added to gallery."
            _isLoading.value = false
        }
    }

    fun deletePhoto(photoId: String) {
        if (!isAdmin.value) {
            _errorMessage.value = "Only administrators can delete gallery photos."
            return
        }
        viewModelScope.launch {
            repo.deletePhoto(photoId)
            _statusMessage.value = "Photo removed."
        }
    }

    fun approveUser(user: UserAccount) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            repo.updateUserAccount(user.copy(status = AccountStatus.APPROVED))
            _statusMessage.value = "${user.fullName} approved."
        }
    }

    fun toggleUserRole(user: UserAccount) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            val nextRole = if (user.role == UserRole.ADMIN) UserRole.MEMBER else UserRole.ADMIN
            repo.updateUserAccount(user.copy(role = nextRole))
            _statusMessage.value = "${user.fullName} role updated to ${nextRole.name}."
        }
    }

    fun suspendUser(user: UserAccount) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            repo.updateUserAccount(user.copy(status = AccountStatus.SUSPENDED))
            _statusMessage.value = "${user.fullName} suspended."
        }
    }

    fun exportBackup(onReady: (Uri) -> Unit) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            runCatching { repo.exportBackup() }
                .onSuccess { uri -> onReady(uri) }
                .onFailure { _errorMessage.value = "Backup export failed: ${it.message}" }
        }
    }

    fun importBackup(uri: Uri) {
        if (!isAdmin.value) return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repo.importBackup(uri) }
                .onSuccess { _statusMessage.value = "Family archive restored from backup." }
                .onFailure { _errorMessage.value = "Restore failed: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _isLoading.value = true
            repo.syncWithFirebase()
                .onSuccess { _statusMessage.value = it }
                .onFailure { _errorMessage.value = it.message ?: "Sync failed." }
            _isLoading.value = false
        }
    }

    fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    fun setLanguage(code: String) = prefs.setLanguageCode(code)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.setNotificationsEnabled(enabled)

    fun getChildrenOf(member: FamilyMember, pool: List<FamilyMember>): List<FamilyMember> {
        return pool.filter {
            it.fatherId == member.id || it.motherId == member.id ||
                (it.fatherName.isNotBlank() && it.fatherName == member.fullName) ||
                (it.motherName.isNotBlank() && it.motherName == member.fullName)
        }
    }

    fun getSiblingsOf(member: FamilyMember, pool: List<FamilyMember>): List<FamilyMember> {
        if (member.fatherId == null && member.fatherName.isBlank()) return emptyList()
        return pool.filter {
            it.id != member.id && (
                (member.fatherId != null && it.fatherId == member.fatherId) ||
                    (member.fatherName.isNotBlank() && it.fatherName == member.fatherName)
                )
        }
    }
}
