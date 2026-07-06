package com.example.dailyreminder

import android.app.Application
import com.example.dailyreminder.notification.NotificationHelper

class DailyReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Create notification channels on every app start.
        // This is safe to call repeatedly — Android ignores duplicate calls.
        NotificationHelper.createNotificationChannels(this)
    }
}
