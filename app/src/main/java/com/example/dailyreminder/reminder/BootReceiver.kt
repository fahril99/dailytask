package com.example.dailyreminder.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dailyreminder.data.TaskRepository
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            val dataStoreManager = DataStoreManager(context)
            val taskRepository = TaskRepository(dataStoreManager)

            CoroutineScope(Dispatchers.IO).launch {
                taskRepository.checkAndResetDailyTasks()
                val tasks = taskRepository.getAllTasksOnce()
                val staged = taskRepository.getStagedRemindersEnabled()
                ReminderManager.scheduleAllTasks(context, tasks, staged)
            }
        }
    }
}
