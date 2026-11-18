package com.sherakbar.family.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SherAkbarMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "Sher Akbar Family"
        val body = message.notification?.body ?: return
        NotificationHelper.showAnnouncementNotification(applicationContext, title, body)
    }
}
