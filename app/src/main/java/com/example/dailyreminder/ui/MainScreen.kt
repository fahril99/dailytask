package com.example.dailyreminder.ui

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.dailyreminder.model.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    taskDialogId: String?,
    onClearTaskDialog: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount

    if (taskDialogId != null) {
        val task = tasks.find { it.id == taskDialogId }
        if (task != null && !task.isCompleted) {
            TaskDialog(
                taskTitle = task.title,
                onConfirm = {
                    viewModel.completeTask(task.id)
                    onClearTaskDialog()
                },
                onDismiss = {
                    viewModel.snoozeTask(task.id)
                    onClearTaskDialog()
                }
            )
        } else {
            onClearTaskDialog()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daily Reminder") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasNotificationPermission) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Izin notifikasi belum diberikan.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onRequestPermission) {
                                Text("Izinkan Notifikasi")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Text("Progress hari ini: $completedCount / $totalCount selesai", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val pendingTasks = tasks.filter { !it.isCompleted }
            val completedTasks = tasks.filter { it.isCompleted }
            
            LazyColumn {
                if (pendingTasks.isNotEmpty()) {
                    item {
                        Text("Tugas Hari Ini", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(pendingTasks) { task ->
                        TaskRow(task = task, onCheckedChange = { isChecked ->
                            if (isChecked) viewModel.completeTask(task.id)
                        })
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
                
                if (completedTasks.isNotEmpty()) {
                    item {
                        Text("Tugas Selesai", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(completedTasks) { task ->
                        TaskRow(task = task, onCheckedChange = { isChecked ->
                            if (!isChecked) viewModel.uncompleteTask(task.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: TaskItem, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
        Text(
            text = task.timeString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
