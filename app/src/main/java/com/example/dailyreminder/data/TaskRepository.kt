package com.example.dailyreminder.data

import com.example.dailyreminder.DailyScheduleParser
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(private val dataStoreManager: DataStoreManager) {
    
    private fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    val tasksFlow: Flow<List<TaskItem>> = combine(
        dataStoreManager.scheduleText,
        dataStoreManager.completedTasks
    ) { scheduleText, completedIds ->
        val parsedTasks = DailyScheduleParser.parseSchedule(scheduleText)
        parsedTasks.map { task ->
            task.copy(isCompleted = completedIds.contains(task.id))
        }
    }

    val scheduleTextFlow: Flow<String> = dataStoreManager.scheduleText

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
    
    suspend fun setScheduleText(text: String) {
        dataStoreManager.setScheduleText(text)
    }
    
    suspend fun getTaskById(taskId: String): TaskItem? {
        val completedIds = dataStoreManager.completedTasks.first()
        val scheduleText = dataStoreManager.scheduleText.first()
        val parsedTasks = DailyScheduleParser.parseSchedule(scheduleText)
        val task = parsedTasks.find { it.id == taskId }
        return task?.copy(isCompleted = completedIds.contains(taskId))
    }
    
    suspend fun getAllTasksOnce(): List<TaskItem> {
        val completedIds = dataStoreManager.completedTasks.first()
        val scheduleText = dataStoreManager.scheduleText.first()
        val parsedTasks = DailyScheduleParser.parseSchedule(scheduleText)
        return parsedTasks.map { task ->
            task.copy(isCompleted = completedIds.contains(task.id))
        }
    }
}
