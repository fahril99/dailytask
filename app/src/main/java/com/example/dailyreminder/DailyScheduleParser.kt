package com.example.dailyreminder

import com.example.dailyreminder.model.TaskItem

object DailyScheduleParser {
    
    fun parseSchedule(text: String): List<TaskItem> {
        val tasks = mutableListOf<TaskItem>()
        val lines = text.lines()
        
        for (line in lines) {
            if (line.trim().isEmpty()) continue
            
            // Expected format: HH:mm | Title | Description
            // Description is optional
            val parts = line.split("|").map { it.trim() }
            if (parts.size >= 2) {
                val timeStr = parts[0]
                val title = parts[1]
                val description = if (parts.size > 2) parts[2] else null
                
                val timeParts = timeStr.split(":")
                if (timeParts.size == 2) {
                    val hour = timeParts[0].toIntOrNull()
                    val minute = timeParts[1].toIntOrNull()
                    
                    if (hour != null && minute != null) {
                        // Generate deterministic ID so completion status maps correctly after reboot
                        val id = "task_${hour}_${minute}_${title.hashCode()}"
                        tasks.add(
                            TaskItem(
                                id = id,
                                title = title,
                                description = description?.takeIf { it.isNotEmpty() },
                                hour = hour,
                                minute = minute
                            )
                        )
                    }
                }
            }
        }
        return tasks.sortedWith(compareBy({ it.hour }, { it.minute }))
    }
}
