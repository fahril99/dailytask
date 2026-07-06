package com.example.dailyreminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.GreenSuccess
import com.example.dailyreminder.ui.theme.OrangeWarning
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    scheduleText: String,
    tasks: List<TaskItem>,
    nextTask: TaskItem?,
    minutesUntilNext: Long,
    currentStreak: Int,
    onSaveSchedule: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val hasSchedule = tasks.isNotEmpty()
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editorText by remember(scheduleText) { mutableStateOf(scheduleText) }
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Daily Reminder",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Progress Hari Ini",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$completedCount / $totalCount selesai",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Purple,
                        trackColor = Purple.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional Content
            if (hasSchedule) {
                // Next Task Card
                if (nextTask != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Jadwal Berikutnya",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = nextTask.timeString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Purple
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = nextTask.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (nextTask.description != null) {
                                        Text(
                                            text = nextTask.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (minutesUntilNext > 0) "${minutesUntilNext} menit lagi" else "Sekarang",
                                style = MaterialTheme.typography.bodySmall,
                                color = OrangeWarning
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Streak Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Streak",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$currentStreak Hari",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pertahankan konsistensimu.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Schedule Editor Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Kode Jadwal Reminder",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editorText,
                            onValueChange = { editorText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            placeholder = {
                                Text(
                                    "04:00 | Bangun | Bangun, wudhu, salat Subuh\n05:00 | Push Up | 70 kali\n06:00 | Membaca Buku | Minimal 30 menit\n07:00 | Berangkat Sekolah\n12:00 | Makan Siang\n22:00 | Tidur",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Purple,
                                unfocusedBorderColor = com.example.dailyreminder.ui.theme.DividerDark,
                                cursorColor = Purple,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveSchedule(editorText)
                        val lines = editorText.lines().filter { it.isNotBlank() }
                        scope.launch {
                            snackbarHostState.showSnackbar("${lines.size} Reminder berhasil dibuat.")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) {
                    Text("Konfirmasi & Terapkan Jadwal", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Always show editor when schedule exists (editable)
            if (hasSchedule) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Kode Jadwal Reminder",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editorText,
                            onValueChange = { editorText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 300.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Purple,
                                unfocusedBorderColor = com.example.dailyreminder.ui.theme.DividerDark,
                                cursorColor = Purple,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onSaveSchedule(editorText)
                                val lines = editorText.lines().filter { it.isNotBlank() }
                                scope.launch {
                                    snackbarHostState.showSnackbar("${lines.size} Reminder diperbarui.")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)
                        ) {
                            Text("Perbarui Jadwal", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
