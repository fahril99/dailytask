package com.example.dailyreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dailyreminder.data.TaskRepository
import com.example.dailyreminder.notification.NotificationHelper
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("TASK_ID") ?: return
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: return
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        
        val dataStoreManager = DataStoreManager(context)
        val taskRepository = TaskRepository(dataStoreManager)
        
        CoroutineScope(Dispatchers.IO).launch {
            val task = taskRepository.getTaskById(taskId)
            if (task != null && !task.isCompleted) {
                NotificationHelper.showNotification(context, taskId, taskTitle, notificationId)
            }
        }
    }
}
