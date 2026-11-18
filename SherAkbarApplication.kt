package com.sherakbar.family

import android.app.Application
import com.google.firebase.FirebaseApp
import com.sherakbar.family.data.local.BackupManager
import com.sherakbar.family.data.local.PreferencesManager
import com.sherakbar.family.data.local.SherAkbarDatabase
import com.sherakbar.family.data.remote.FirebaseSyncService
import com.sherakbar.family.data.repository.FamilyRepository
import com.sherakbar.family.notifications.NotificationHelper

class SherAkbarApplication : Application() {
    lateinit var database: SherAkbarDatabase
        private set
    lateinit var preferences: PreferencesManager
        private set
    lateinit var repository: FamilyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        runCatching { FirebaseApp.initializeApp(this) }
        NotificationHelper.createChannels(this)

        database = SherAkbarDatabase.getInstance(this)
        preferences = PreferencesManager(this)
        repository = FamilyRepository(
            context = this,
            db = database,
            prefs = preferences,
            firebase = FirebaseSyncService(this),
            backupManager = BackupManager(this)
        )
    }
}
