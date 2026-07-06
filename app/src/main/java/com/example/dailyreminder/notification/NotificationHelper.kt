package com.example.dailyreminder.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.dailyreminder.MainActivity
import com.example.dailyreminder.R

object NotificationHelper {

    const val CHANNEL_ID_REMINDER = "daily_reminder_high"
    const val CHANNEL_ID_SILENT = "daily_reminder_silent"

    /**
     * Creates notification channels.
     * Must be called on app start (Application.onCreate).
     * Sound/vibration settings are read from DataStore at runtime per notification.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // High-importance channel (sound + vibration)
            val highChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Pengingat Harian",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat aktivitas harian"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(defaultSound, audioAttributes)
            }

            // Silent channel (no sound, no vibration)
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Pengingat Harian (Senyap)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat tanpa suara dan getaran"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            nm.createNotificationChannel(highChannel)
            nm.createNotificationChannel(silentChannel)
        }
    }

    /**
     * Shows a heads-up / high-priority notification for a task reminder.
     * Sound and vibration are controlled via the channel ID chosen based on settings.
     *
     * @param soundEnabled  whether to use sound channel
     * @param vibrationEnabled whether to add vibration (only meaningful pre-O; on O+ it's channel-level)
     * @param customSoundUri optional custom sound URI saved by user
     */
    fun showTaskNotification(
        context: Context,
        taskId: String,
        taskTitle: String,
        taskDescription: String?,
        notificationId: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        customSoundUri: String? = null
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tap opens MainActivity, which will show ConfirmationScreen via intent extra
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_TASK_DIALOG", taskId)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (!taskDescription.isNullOrEmpty()) {
            "$taskDescription\nKlik untuk membuka aplikasi."
        } else {
            "Saatnya melakukan $taskTitle.\nKlik untuk membuka aplikasi."
        }

        // Choose channel: use silent channel if both sound AND vibration are off
        val channelId = if (soundEnabled || vibrationEnabled) CHANNEL_ID_REMINDER else CHANNEL_ID_SILENT

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ $taskTitle")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_MAX)  // heads-up on pre-O
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(tapPendingIntent)

        // Pre-O: apply sound/vibration manually
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (soundEnabled) {
                val soundUri: Uri = if (!customSoundUri.isNullOrEmpty()) {
                    Uri.parse(customSoundUri)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
                builder.setSound(soundUri)
            } else {
                builder.setSound(null)
            }
            if (vibrationEnabled) {
                builder.setVibrate(longArrayOf(0, 300, 200, 300))
            } else {
                builder.setVibrate(null)
            }
        }

        nm.notify(notificationId, builder.build())
    }

    // Legacy compatibility alias used by AlarmReceiver
    fun showNotification(
        context: Context,
        taskId: String,
        taskTitle: String,
        taskDescription: String?,
        notificationId: Int,
        soundEnabled: Boolean = true,
        vibrationEnabled: Boolean = true
    ) = showTaskNotification(
        context, taskId, taskTitle, taskDescription, notificationId, soundEnabled, vibrationEnabled
    )
}
