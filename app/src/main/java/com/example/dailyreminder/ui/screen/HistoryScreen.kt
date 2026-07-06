package com.example.dailyreminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.DividerDark
import com.example.dailyreminder.ui.theme.GreenSuccess
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.RedError
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun HistoryScreen(tasks: List<TaskItem>) {
    var selectedTab by remember { mutableStateOf(0) }
    val completedTasks = tasks.filter { it.isCompleted }
    val pendingTasks = tasks.filter { !it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Riwayat",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardDark,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Purple,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = DividerDark) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Selesai", fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal) },
                selectedContentColor = Purple,
                unselectedContentColor = TextSecondary
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Belum Selesai", fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal) },
                selectedContentColor = Purple,
                unselectedContentColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hari Ini",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        val displayList = if (selectedTab == 0) completedTasks else pendingTasks

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "Belum ada tugas selesai" else "Semua tugas selesai!",
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn {
                items(displayList) { task ->
                    HistoryTaskRow(task = task, isCompleted = selectedTab == 0)
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun HistoryTaskRow(task: TaskItem, isCompleted: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.timeString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) GreenSuccess else TextSecondary,
                modifier = Modifier.width(60.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description != null) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (isCompleted) "Done" else "Not Done",
                tint = if (isCompleted) GreenSuccess else RedError,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
