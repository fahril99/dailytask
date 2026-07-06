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
 * It uses a chained scheduling approach:
 * - Initially, only the FIRST stage of the reminder is scheduled.
 * - When AlarmReceiver fires, it schedules the NEXT stage.
 */
object ReminderManager {

    private const val TAG = "ReminderManager"

    // Offset indices map:
    // 0 → 0 (exact), 1 → +5 min, 2 → +15 min, 3 → +30 min
    // Removed -5 as it complicates chaining and is not explicitly in user's prompt chain.
    // Wait, the user explicitly said:
    // "Saat ini seluruh reminder -5, 0, +5, +15, +30 dibuat sekaligus... Yang saya inginkan adalah 07.00 -> notif -> Belum selesai -> +5"
    // So the chain is: 0 (exact), +5, +15, +30.
    val STAGED_OFFSETS = listOf(0, 5, 15, 30)
    private val SINGLE_OFFSET = listOf(0)

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
     * Schedules the FIRST alarm for a task.
     */
    fun scheduleTaskReminders(context: Context, task: TaskItem, stagedReminders: Boolean = true) {
        val offsetMinutes = if (stagedReminders) STAGED_OFFSETS[0] else SINGLE_OFFSET[0]
        val stageIndex = 0
        
        val triggerAt = buildTriggerTime(task.hour, task.minute, offsetMinutes)
        val requestCode = buildRequestCode(task.id, stageIndex)
        setExactAlarm(context, triggerAt, task, requestCode, stageIndex, stagedReminders)
        Log.d(TAG, "Alarm Scheduled for '${task.title}' stage=$stageIndex at ${java.util.Date(triggerAt)}")
    }

    /**
     * Called by AlarmReceiver to schedule the NEXT stage in the chain.
     */
    fun scheduleNextStage(context: Context, task: TaskItem, currentStageIndex: Int) {
        val nextStageIndex = currentStageIndex + 1
        if (nextStageIndex < STAGED_OFFSETS.size) {
            val offsetMinutes = STAGED_OFFSETS[nextStageIndex]
            
            // To ensure we don't accidentally schedule for "tomorrow" if we are a bit late,
            // we calculate the absolute time for today.
            var cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, task.hour)
                set(Calendar.MINUTE, task.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, offsetMinutes)
            }
            
            // If somehow this next stage is already in the past (e.g., missed alarm), we still try to set it for tomorrow.
            // But usually this is called exactly when the previous stage fires, so it should be in the future.
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val triggerAt = cal.timeInMillis
            val requestCode = buildRequestCode(task.id, nextStageIndex)
            setExactAlarm(context, triggerAt, task, requestCode, nextStageIndex, true)
            Log.d(TAG, "Next Stage Alarm Scheduled for '${task.title}' stage=$nextStageIndex at ${java.util.Date(triggerAt)}")
        }
    }

    fun scheduleSnooze(context: Context, task: TaskItem) {
        val triggerAt = System.currentTimeMillis() + 5L * 60_000L
        val requestCode = buildSnoozeRequestCode(task.id)
        // Snooze is effectively a standalone stage -1
        setExactAlarm(context, triggerAt, task, requestCode, -1, false)
        Log.d(TAG, "Alarm Scheduled (Snooze) for '${task.title}' at ${java.util.Date(triggerAt)}")
    }

    fun cancelTaskReminders(context: Context, task: TaskItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (index in STAGED_OFFSETS.indices) {
            cancelAlarm(context, alarmManager, buildRequestCode(task.id, index))
        }
        cancelAlarm(context, alarmManager, buildSnoozeRequestCode(task.id))
        Log.d(TAG, "Cancelled all alarms for '${task.title}'")
    }

    private fun buildTriggerTime(hour: Int, minute: Int, offsetMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun setExactAlarm(
        context: Context,
        triggerAt: Long,
        task: TaskItem,
        requestCode: Int,
        stageIndex: Int,
        stagedReminders: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = buildAlarmIntent(context, task, requestCode, stageIndex, stagedReminders)
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
                        // Fallback ke inexact alarm jika user belum memberi izin
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                        Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted; using inexact alarm fallback")
                    }
                }
                else -> {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException setting alarm: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pi)
        pi.cancel()
    }

    private fun buildAlarmIntent(
        context: Context,
        task: TaskItem,
        requestCode: Int,
        stageIndex: Int,
        stagedReminders: Boolean
    ): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            task.description?.let { putExtra("TASK_DESC", it) }
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("STAGE_INDEX", stageIndex)
            putExtra("STAGED_REMINDERS", stagedReminders)
        }

    private fun buildRequestCode(taskId: String, offsetIndex: Int): Int {
        val base = (taskId.hashCode() and 0x7FFFFFFF) % 10_000_000
        return base * 200 + offsetIndex
    }

    private fun buildSnoozeRequestCode(taskId: String): Int {
        val base = (taskId.hashCode() and 0x7FFFFFFF) % 10_000_000
        return base * 200 + 100
    }
}
