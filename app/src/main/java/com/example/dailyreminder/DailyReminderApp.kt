package com.example.dailyreminder

import android.app.Application
import com.example.dailyreminder.notification.NotificationHelper
import com.example.dailyreminder.reminder.ReminderManager

class DailyReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)
        
        // Initially schedule reminders if not done yet
        // A robust app would do this carefully. Since AlarmManager survives app kill, 
        // we'll schedule it on boot or when app opens (handled in ViewModel / Repository)
    }
}
