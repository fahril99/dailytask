package com.example.dailyreminder.data

import com.example.dailyreminder.DailyScheduleParser
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.storage.DataStoreManager
import com.example.dailyreminder.storage.parseHistoryJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
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

    val historyMapFlow: Flow<Map<String, Set<String>>> = dataStoreManager.historyJson.map { json ->
        parseHistoryJson(json)
    }

    // Settings flows
    val soundEnabled: Flow<Boolean> = dataStoreManager.soundEnabled
    val vibrationEnabled: Flow<Boolean> = dataStoreManager.vibrationEnabled
    val stagedRemindersEnabled: Flow<Boolean> = dataStoreManager.stagedRemindersEnabled

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
        if (isCompleted) {
            dataStoreManager.addToHistory(taskId, getTodayDateString())
        }
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

    suspend fun getStagedRemindersEnabled(): Boolean {
        return dataStoreManager.stagedRemindersEnabled.first()
    }

    // --- Streak computation ---

    fun computeCurrentStreak(history: Map<String, Set<String>>): Int {
        var streak = 0
        var date = LocalDate.now()
        val todayKey = date.toString()

        if (history.containsKey(todayKey) && history[todayKey]!!.isNotEmpty()) {
            streak = 1
            date = date.minusDays(1)
        } else {
            date = date.minusDays(1)
        }

        while (true) {
            val key = date.toString()
            if (history.containsKey(key) && history[key]!!.isNotEmpty()) {
                streak++
                date = date.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    fun computeLongestStreak(history: Map<String, Set<String>>): Int {
        if (history.isEmpty()) return 0
        val dates = history.keys
            .filter { history[it]?.isNotEmpty() == true }
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .sorted()
        if (dates.isEmpty()) return 0

        var maxStreak = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                if (current > maxStreak) maxStreak = current
            } else {
                current = 1
            }
        }
        return maxStreak
    }

    // --- Weekly stats ---

    fun computeWeeklyStats(history: Map<String, Set<String>>): List<Pair<String, Int>> {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)

        return (0L..6L).map { offset ->
            val date = startOfWeek.plusDays(offset)
            val key = date.toString()
            val count = history[key]?.size ?: 0
            val dayName = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Sen"
                DayOfWeek.TUESDAY -> "Sel"
                DayOfWeek.WEDNESDAY -> "Rab"
                DayOfWeek.THURSDAY -> "Kam"
                DayOfWeek.FRIDAY -> "Jum"
                DayOfWeek.SATURDAY -> "Sab"
                DayOfWeek.SUNDAY -> "Min"
                else -> ""
            }
            dayName to count
        }
    }

    // --- Weekly total stats ---

    fun computeWeeklyTotalCompleted(history: Map<String, Set<String>>): Int {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        var total = 0
        for (offset in 0L..6L) {
            val key = startOfWeek.plusDays(offset).toString()
            total += history[key]?.size ?: 0
        }
        return total
    }

    fun computeWeeklyTotalMissed(history: Map<String, Set<String>>, totalTasksPerDay: Int): Int {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        var missed = 0
        for (offset in 0L..6L) {
            val date = startOfWeek.plusDays(offset)
            if (date.isAfter(today)) continue
            val key = date.toString()
            val completed = history[key]?.size ?: 0
            missed += maxOf(0, totalTasksPerDay - completed)
        }
        return missed
    }

    // --- Settings updates ---

    suspend fun setSoundEnabled(enabled: Boolean) = dataStoreManager.setSoundEnabled(enabled)
    suspend fun setVibrationEnabled(enabled: Boolean) = dataStoreManager.setVibrationEnabled(enabled)
    suspend fun setStagedRemindersEnabled(enabled: Boolean) = dataStoreManager.setStagedRemindersEnabled(enabled)
}
