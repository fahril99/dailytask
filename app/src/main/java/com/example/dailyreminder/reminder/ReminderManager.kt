package com.example.dailyreminder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.dailyreminder.model.TaskItem
import java.util.Calendar

object ReminderManager {

    fun scheduleAllTasks(context: Context, tasks: List<TaskItem>, stagedReminders: Boolean = true) {
        tasks.forEach { task ->
            if (!task.isCompleted) {
                scheduleTaskReminders(context, task, stagedReminders)
            } else {
                cancelTaskReminders(context, task)
            }
        }
    }

    fun scheduleTaskReminders(context: Context, task: TaskItem, stagedReminders: Boolean = true) {
        val offsets = if (stagedReminders) listOf(-5, 0, 5, 15, 30) else listOf(0)

        offsets.forEachIndexed { index, offset ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, task.hour)
                set(Calendar.MINUTE, task.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, offset)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val requestCode = getRequestCode(task.id, index)
            setAlarm(context, calendar.timeInMillis, task, requestCode)
        }
    }

    fun scheduleSnooze(context: Context, task: TaskItem) {
        val snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000)
        val requestCode = getSnoozeRequestCode(task.id)
        setAlarm(context, snoozeTime, task, requestCode)
    }

    private fun setAlarm(context: Context, timeInMillis: Long, task: TaskItem, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", task.title)
            if (task.description != null) putExtra("TASK_DESC", task.description)
            putExtra("NOTIFICATION_ID", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun cancelTaskReminders(context: Context, task: TaskItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val offsetsCount = 5
        for (index in 0 until offsetsCount) {
            val requestCode = getRequestCode(task.id, index)
            cancelAlarm(context, alarmManager, requestCode)
        }
        cancelAlarm(context, alarmManager, getSnoozeRequestCode(task.id))
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getRequestCode(taskId: String, index: Int): Int {
        return Math.abs(taskId.hashCode()) + index
    }

    private fun getSnoozeRequestCode(taskId: String): Int {
        return Math.abs(taskId.hashCode()) + 100
    }
}
