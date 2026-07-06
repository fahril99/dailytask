package com.example.dailyreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.dailyreminder.model.TaskItem
import java.util.Calendar

/**
 * ReminderManager schedules and cancels exact alarms via AlarmManager.
 *
 * IMPORTANT: All alarm scheduling is done here and is completely independent
 * of MainActivity, ViewModel, and Compose. Alarms are set with RTC_WAKEUP so
 * they fire even when the screen is off or the app is in the background.
 *
 * Alarm offsets per task: -5 min, exact, +5 min, +15 min, +30 min (staged).
 * Each offset uses a unique PendingIntent requestCode derived from taskId + offsetIndex.
 */
object ReminderManager {

    private const val TAG = "ReminderManager"

    // Offset indices map:
    // 0 → -5 min, 1 → 0 (exact), 2 → +5 min, 3 → +15 min, 4 → +30 min
    private val STAGED_OFFSETS = listOf(-5, 0, 5, 15, 30)
    private val SINGLE_OFFSET = listOf(0)

    // -------- Public API --------

    fun scheduleAllTasks(context: Context, tasks: List<TaskItem>, stagedReminders: Boolean = true) {
        Log.d(TAG, "scheduleAllTasks: ${tasks.size} tasks, staged=$stagedReminders")
        tasks.forEach { task ->
            if (!task.isCompleted) {
                scheduleTaskReminders(context, task, stagedReminders)
            } else {
                cancelTaskReminders(context, task)
            }
        }
    }

    /**
     * Schedules all staged (or single) alarms for a task.
     * Alarms that have already passed today are scheduled for the same time tomorrow.
     */
    fun scheduleTaskReminders(context: Context, task: TaskItem, stagedReminders: Boolean = true) {
        val offsets = if (stagedReminders) STAGED_OFFSETS else SINGLE_OFFSET
        offsets.forEachIndexed { index, offsetMinutes ->
            val triggerAt = buildTriggerTime(task.hour, task.minute, offsetMinutes)
            val requestCode = buildRequestCode(task.id, index)
            setExactAlarm(context, triggerAt, task, requestCode)
            Log.d(TAG, "Scheduled alarm for '${task.title}' offset=${offsetMinutes}min at ${java.util.Date(triggerAt)}")
        }
    }

    /**
     * Schedules a single snooze alarm 5 minutes from now.
     */
    fun scheduleSnooze(context: Context, task: TaskItem) {
        val triggerAt = System.currentTimeMillis() + 5L * 60_000L
        val requestCode = buildSnoozeRequestCode(task.id)
        setExactAlarm(context, triggerAt, task, requestCode)
        Log.d(TAG, "Snooze alarm for '${task.title}' at ${java.util.Date(triggerAt)}")
    }

    /**
     * Cancels all scheduled alarms for a task (staged + snooze).
     */
    fun cancelTaskReminders(context: Context, task: TaskItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (index in STAGED_OFFSETS.indices) {
            cancelAlarm(context, alarmManager, buildRequestCode(task.id, index))
        }
        cancelAlarm(context, alarmManager, buildSnoozeRequestCode(task.id))
        Log.d(TAG, "Cancelled all alarms for '${task.title}'")
    }

    // -------- Private helpers --------

    /**
     * Builds the trigger time (epoch ms) for a task at [hour]:[minute] + [offsetMinutes].
     * If that moment is already in the past, it returns the same time tomorrow.
     */
    private fun buildTriggerTime(hour: Int, minute: Int, offsetMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }
        // If the moment has already passed, move to tomorrow
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    /**
     * Sets an exact, battery-optimisation-bypassing alarm.
     * Uses setExactAndAllowWhileIdle (API 23+) which fires even in Doze mode.
     * On API 31+ checks canScheduleExactAlarms() first.
     */
    private fun setExactAlarm(context: Context, triggerAt: Long, task: TaskItem, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = buildAlarmIntent(context, task, requestCode)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                    } else {
                        // Fallback: inexact but still wakeup-capable
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                        Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted; using inexact alarm for requestCode=$requestCode")
                    }
                }
                else -> {
                    // API 23-30: setExactAndAllowWhileIdle fires even in Doze
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException setting alarm: ${e.message}")
            // Last resort fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return  // already cancelled or never set
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun buildAlarmIntent(context: Context, task: TaskItem, requestCode: Int): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            task.description?.let { putExtra("TASK_DESC", it) }
            putExtra("NOTIFICATION_ID", requestCode)
        }

    /**
     * Deterministic, collision-resistant request codes.
     * We shift taskId hash left so that the offset index (0..4) doesn't collide
     * with the snooze code (+100).
     */
    private fun buildRequestCode(taskId: String, offsetIndex: Int): Int {
        // Ensure positive and leave room for snooze (+100)
        val base = (taskId.hashCode() and 0x7FFFFFFF) % 10_000_000
        return base * 200 + offsetIndex   // max = 9_999_999 * 200 + 4 ≈ 2B (within Int)
    }

    private fun buildSnoozeRequestCode(taskId: String): Int {
        val base = (taskId.hashCode() and 0x7FFFFFFF) % 10_000_000
        return base * 200 + 100
    }
}
