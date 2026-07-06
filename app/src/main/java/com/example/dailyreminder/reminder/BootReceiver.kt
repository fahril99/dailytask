package com.example.dailyreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dailyreminder.data.TaskRepository
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BootReceiver reschedules all pending alarms after a device restart.
 *
 * AlarmManager alarms do NOT survive reboots — this receiver restores them.
 * It reads the saved schedule and completed tasks from DataStore and
 * re-creates all alarms for tasks that haven't been completed yet.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            return
        }

        Log.d(TAG, "Boot completed — rescheduling alarms")

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val dataStoreManager = DataStoreManager(context)
                val taskRepository = TaskRepository(dataStoreManager)

                // Reset daily progress if the date changed during the downtime
                taskRepository.checkAndResetDailyTasks()

                val tasks = taskRepository.getAllTasksOnce()
                val staged = taskRepository.getStagedRemindersEnabled()

                Log.d(TAG, "Rescheduling ${tasks.size} tasks (staged=$staged)")
                ReminderManager.scheduleAllTasks(context, tasks, staged)
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms on boot: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
