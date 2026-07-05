package com.example.dailyreminder.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TaskDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* force user to answer */ },
        title = { Text(text = "Apakah task ini sudah selesai?") },
        text = { Text(text = taskTitle) },
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
