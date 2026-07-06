package com.example.dailyreminder

import android.app.Application
import com.example.dailyreminder.notification.NotificationHelper

class DailyReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
