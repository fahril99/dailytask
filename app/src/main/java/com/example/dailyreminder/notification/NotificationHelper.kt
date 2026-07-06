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

    fun createNotificationChannels(context: Context) {
        // We no longer pre-create static channels because they lock the sound/vibration settings.
        // Channels will be created dynamically just-in-time.
    }

    private fun getDynamicChannelId(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        customSoundUri: String?
    ): String {
        if (!soundEnabled && !vibrationEnabled) {
            return "daily_reminder_silent"
        }
        val hash = customSoundUri?.hashCode() ?: "default".hashCode()
        return "reminder_${soundEnabled}_${vibrationEnabled}_$hash"
    }

    private fun createChannelIfNeeded(
        context: Context,
        channelId: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        customSoundUri: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (nm.getNotificationChannel(channelId) != null) {
                return // Channel already exists
            }

            val channelName = if (!soundEnabled && !vibrationEnabled) "Pengingat Harian (Senyap)" else "Pengingat Harian"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat aktivitas harian"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)

                if (vibrationEnabled) {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 200, 300)
                } else {
                    enableVibration(false)
                    vibrationPattern = null
                }

                if (soundEnabled) {
                    val soundUri: Uri = if (!customSoundUri.isNullOrEmpty()) {
                        Uri.parse(customSoundUri)
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }

            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a heads-up / high-priority notification for a task reminder.
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

        val channelId = getDynamicChannelId(soundEnabled, vibrationEnabled, customSoundUri)
        createChannelIfNeeded(context, channelId, soundEnabled, vibrationEnabled, customSoundUri)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ $taskTitle")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(tapPendingIntent)

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
