package com.example.dailyreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dailyreminder.data.TaskRepository
import com.example.dailyreminder.notification.NotificationHelper
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * AlarmReceiver is the core of the reminder system.
 *
 * It runs completely independently of MainActivity, ViewModel, and Compose.
 * AlarmManager fires this receiver even when:
 *   - The app is in the background
 *   - The app has been removed from recent apps (as long as Android hasn't killed it)
 *   - The screen is off
 *   - The device is in Doze mode (because we use setExactAndAllowWhileIdle)
 *
 * After showing a notification, the receiver also self-reschedules the NEXT
 * staged alarm for the same task if the user hasn't completed it yet.
 * This ensures the reminder chain continues without relying on the UI.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("TASK_ID") ?: run {
            Log.w(TAG, "Received alarm with no TASK_ID")
            return
        }
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
        val taskDesc = intent.getStringExtra("TASK_DESC")
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", taskId.hashCode())

        Log.d(TAG, "Alarm Received: taskId=$taskId, notificationId=$notificationId")

        // Use a SupervisorJob so a failure doesn't cancel other coroutines.
        // goAsync() would be ideal but increases complexity; DataStore reads are fast.
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        scope.launch {
            try {
                val dataStoreManager = DataStoreManager(context)
                val taskRepository = TaskRepository(dataStoreManager)

                val task = taskRepository.getTaskById(taskId)

                if (task == null) {
                    Log.d(TAG, "Task $taskId not found in schedule — skipping notification")
                    return@launch
                }

                if (task.isCompleted) {
                    Log.d(TAG, "Task '${task.title}' already completed — skipping notification")
                    return@launch
                }

                // Read settings directly from DataStore (no ViewModel needed)
                val soundEnabled = dataStoreManager.soundEnabled.first()
                val vibrationEnabled = dataStoreManager.vibrationEnabled.first()

                NotificationHelper.showTaskNotification(
                    context = context,
                    taskId = taskId,
                    taskTitle = taskTitle,
                    taskDescription = taskDesc,
                    notificationId = notificationId,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled
                )

                Log.d(TAG, "Notification Sent for '${task.title}'")
            } catch (e: Exception) {
                Log.e(TAG, "Error in AlarmReceiver coroutine: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
