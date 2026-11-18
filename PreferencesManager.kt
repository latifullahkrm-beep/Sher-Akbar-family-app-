package com.sherakbar.family.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("sher_akbar_prefs", Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _languageCode = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _lastSyncEpoch = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncEpoch: StateFlow<Long> = _lastSyncEpoch.asStateFlow()

    fun getCurrentUserUid(): String? = prefs.getString(KEY_USER_UID, null)

    fun setCurrentUserUid(uid: String?) {
        prefs.edit().putString(KEY_USER_UID, uid).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _darkMode.value = enabled
    }

    fun setLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE, code).apply()
        _languageCode.value = code
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun updateLastSync(epochMs: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, epochMs).apply()
        _lastSyncEpoch.value = epochMs
    }

    companion object {
        private const val KEY_USER_UID = "current_user_uid"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language_code"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_LAST_SYNC = "last_sync_epoch"
    }
}
