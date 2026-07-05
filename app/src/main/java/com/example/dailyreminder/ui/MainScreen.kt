package com.example.dailyreminder.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.dailyreminder.model.TaskItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    taskDialogId: String?,
    onClearTaskDialog: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tasks by viewModel.tasks.collectAsState()
    val scheduleText by viewModel.scheduleText.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (taskDialogId != null) {
        val task = tasks.find { it.id == taskDialogId }
        if (task != null && !task.isCompleted) {
            TaskDialog(
                task = task,
                onConfirm = {
                    viewModel.completeTask(task.id)
                    onClearTaskDialog()
                },
                onDismiss = {
                    viewModel.snoozeTask(task.id)
                    Toast.makeText(context, "Baik. Saya akan mengingatkan lagi 5 menit.", Toast.LENGTH_SHORT).show()
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Completed") },
                    label = { Text("Completed") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedTab == 0) {
                HomeScreen(
                    scheduleText = scheduleText,
                    onSaveSchedule = { newText ->
                        viewModel.saveSchedule(newText)
                        val count = newText.lines().filter { it.isNotBlank() }.size
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("$count Reminder berhasil dibuat.")
                        }
                    },
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestPermission = onRequestPermission
                )
            } else {
                CompletedScreen(
                    tasks = tasks.filter { it.isCompleted },
                    onUncomplete = { viewModel.uncompleteTask(it) }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    scheduleText: String,
    onSaveSchedule: (String) -> Unit,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var text by remember(scheduleText) { mutableStateOf(scheduleText) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Aplikasi membutuhkan izin notifikasi agar dapat mengingatkan jadwal harian.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRequestPermission) {
                            Text("Izinkan Notifikasi")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Notifikasi belum diaktifkan. Reminder tidak dapat bekerja sebelum izin diberikan.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Text("✅ Notifikasi Aktif", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Kode Jadwal Reminder", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("04:00 | Bangun Tidur | Rapikan tempat tidur\n05:00 | Push Up 100 Kali | Target 100 repetisi") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onSaveSchedule(text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Konfirmasi & Terapkan Jadwal")
        }
    }
}

@Composable
fun CompletedScreen(
    tasks: List<TaskItem>,
    onUncomplete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tugas Selesai", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            textDecoration = TextDecoration.LineThrough
                        )
                        if (task.description != null) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                    Text(
                        text = task.timeString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
