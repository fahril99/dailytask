package com.example.dailyreminder.data

import com.example.dailyreminder.DailySchedule
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(private val dataStoreManager: DataStoreManager) {
    
    private fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    val tasksFlow: Flow<List<TaskItem>> = dataStoreManager.completedTasks.map { completedIds ->
        DailySchedule.tasks.map { task ->
            task.copy(isCompleted = completedIds.contains(task.id))
        }
    }

    suspend fun checkAndResetDailyTasks() {
        val today = getTodayDateString()
        val storedDate = dataStoreManager.lastDate.first()
        
        if (storedDate != today) {
            dataStoreManager.clearCompletedTasks()
            dataStoreManager.setLastDate(today)
        }
    }

    suspend fun setTaskCompleted(taskId: String, isCompleted: Boolean) {
        dataStoreManager.setTaskCompleted(taskId, isCompleted)
    }
    
    suspend fun getTaskById(taskId: String): TaskItem? {
        val completedIds = dataStoreManager.completedTasks.first()
        val task = DailySchedule.tasks.find { it.id == taskId }
        return task?.copy(isCompleted = completedIds.contains(taskId))
    }
    
    suspend fun getAllTasksOnce(): List<TaskItem> {
        val completedIds = dataStoreManager.completedTasks.first()
        return DailySchedule.tasks.map { task ->
            task.copy(isCompleted = completedIds.contains(task.id))
        }
    }
}
