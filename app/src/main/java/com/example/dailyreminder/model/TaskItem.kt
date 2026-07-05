package com.example.dailyreminder.model

data class TaskItem(
    val id: String,
    val title: String,
    val hour: Int,
    val minute: Int,
    var isCompleted: Boolean = false
) {
    fun timeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
