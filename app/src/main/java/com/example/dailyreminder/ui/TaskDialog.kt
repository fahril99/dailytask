package com.example.dailyreminder.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.dailyreminder.model.TaskItem

@Composable
fun TaskDialog(
    task: TaskItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* force user to answer */ },
        title = { Text(text = task.title) },
        text = { 
            if (task.description != null) {
                Text(text = "Target:\n${task.description}\n\nSudah selesai?")
            } else {
                Text(text = "Sudah selesai?")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sudah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Belum")
            }
        }
    )
}
