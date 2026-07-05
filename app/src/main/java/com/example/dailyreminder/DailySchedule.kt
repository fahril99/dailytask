package com.example.dailyreminder

import com.example.dailyreminder.model.TaskItem

object DailySchedule {
    // List of static tasks based on user request.
    // Modify this file to change the schedule.
    val tasks = listOf(
        TaskItem(id = "task_01", title = "Bangun Tidur", hour = 5, minute = 0),
        TaskItem(id = "task_02", title = "Sholat Subuh", hour = 5, minute = 10),
        TaskItem(id = "task_03", title = "Membaca Buku", hour = 5, minute = 40),
        TaskItem(id = "task_04", title = "Olahraga", hour = 6, minute = 0),
        TaskItem(id = "task_05", title = "Mandi & Siap-siap", hour = 6, minute = 30),
        TaskItem(id = "task_06", title = "Sarapan", hour = 7, minute = 0),
        TaskItem(id = "task_07", title = "Berangkat Kerja/Sekolah", hour = 7, minute = 30),
        TaskItem(id = "task_08", title = "Makan Siang", hour = 12, minute = 0),
        TaskItem(id = "task_09", title = "Pulang", hour = 17, minute = 0),
        TaskItem(id = "task_10", title = "Tidur", hour = 22, minute = 0)
    )
}
