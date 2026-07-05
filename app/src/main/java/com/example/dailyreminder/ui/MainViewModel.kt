package com.example.dailyreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyreminder.data.TaskRepository
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.reminder.ReminderManager
import com.example.dailyreminder.storage.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository = TaskRepository(DataStoreManager(application))
    
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.checkAndResetDailyTasks()
            
            taskRepository.tasksFlow.collect { taskList ->
                _tasks.value = taskList
                ReminderManager.scheduleAllTasks(getApplication(), taskList)
            }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, true)
        }
    }

    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, false)
        }
    }

    fun snoozeTask(taskId: String) {
        viewModelScope.launch {
            val task = _tasks.value.find { it.id == taskId }
            if (task != null && !task.isCompleted) {
                ReminderManager.scheduleSnooze(getApplication(), task)
            }
        }
    }
}
