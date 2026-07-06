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
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository = TaskRepository(DataStoreManager(application))

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _scheduleText = MutableStateFlow("")
    val scheduleText: StateFlow<String> = _scheduleText.asStateFlow()

    private val _historyMap = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val historyMap: StateFlow<Map<String, Set<String>>> = _historyMap.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val weeklyStats: StateFlow<List<Pair<String, Int>>> = _weeklyStats.asStateFlow()

    private val _weeklyCompleted = MutableStateFlow(0)
    val weeklyCompleted: StateFlow<Int> = _weeklyCompleted.asStateFlow()

    private val _weeklyMissed = MutableStateFlow(0)
    val weeklyMissed: StateFlow<Int> = _weeklyMissed.asStateFlow()

    // Settings
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _stagedRemindersEnabled = MutableStateFlow(true)
    val stagedRemindersEnabled: StateFlow<Boolean> = _stagedRemindersEnabled.asStateFlow()

    private val _powerNapMinutes = MutableStateFlow(20)
    val powerNapMinutes: StateFlow<Int> = _powerNapMinutes.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.checkAndResetDailyTasks()

            launch {
                taskRepository.scheduleTextFlow.collect { text ->
                    _scheduleText.value = text
                }
            }

            launch {
                taskRepository.tasksFlow.collect { taskList ->
                    _tasks.value = taskList
                    val staged = taskRepository.getStagedRemindersEnabled()
                    ReminderManager.scheduleAllTasks(getApplication(), taskList, staged)
                }
            }

            launch {
                taskRepository.historyMapFlow.collect { history ->
                    _historyMap.value = history
                    _currentStreak.value = taskRepository.computeCurrentStreak(history)
                    _longestStreak.value = taskRepository.computeLongestStreak(history)
                    _weeklyStats.value = taskRepository.computeWeeklyStats(history)
                    _weeklyCompleted.value = taskRepository.computeWeeklyTotalCompleted(history)
                    val totalPerDay = _tasks.value.size
                    _weeklyMissed.value = taskRepository.computeWeeklyTotalMissed(history, totalPerDay)
                }
            }

            launch { taskRepository.soundEnabled.collect { _soundEnabled.value = it } }
            launch { taskRepository.vibrationEnabled.collect { _vibrationEnabled.value = it } }
            launch { taskRepository.stagedRemindersEnabled.collect { _stagedRemindersEnabled.value = it } }
            launch { taskRepository.powerNapMinutes.collect { _powerNapMinutes.value = it } }
        }
    }

    fun saveSchedule(text: String) {
        viewModelScope.launch {
            taskRepository.setScheduleText(text)
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

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { taskRepository.setSoundEnabled(enabled) }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { taskRepository.setVibrationEnabled(enabled) }
    }

    fun updateStagedRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { taskRepository.setStagedRemindersEnabled(enabled) }
    }

    fun updatePowerNapMinutes(minutes: Int) {
        viewModelScope.launch { taskRepository.setPowerNapMinutes(minutes) }
    }

    // --- Helpers ---

    fun getNextUpcomingTask(): TaskItem? {
        val now = LocalTime.now()
        return _tasks.value
            .filter { !it.isCompleted }
            .filter { LocalTime.of(it.hour, it.minute).isAfter(now) }
            .minByOrNull { it.hour * 60 + it.minute }
    }

    fun getMinutesUntilTask(task: TaskItem): Long {
        val now = LocalTime.now()
        val taskTime = LocalTime.of(task.hour, task.minute)
        return ChronoUnit.MINUTES.between(now, taskTime)
    }

    fun getHistoryForDate(date: LocalDate): List<TaskItem> {
        val key = date.toString()
        val completedIds = _historyMap.value[key] ?: emptySet()
        return _tasks.value.map { task ->
            task.copy(isCompleted = completedIds.contains(task.id))
        }
    }
}
