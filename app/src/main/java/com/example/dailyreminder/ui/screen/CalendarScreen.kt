package com.example.dailyreminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.DividerDark
import com.example.dailyreminder.ui.theme.GreenSuccess
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.RedError
import com.example.dailyreminder.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    historyMap: Map<String, Set<String>>,
    onDateSelected: (LocalDate) -> List<TaskItem>
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val selectedDayTasks = remember(selectedDate, historyMap) { onDateSelected(selectedDate) }
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kalender",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Month Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = Purple)
                    }
                    Text(
                        text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale("id"))} ${selectedMonth.year}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Purple)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = DividerDark)
                Spacer(modifier = Modifier.height(12.dp))

                // Day of week headers
                val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayHeaders.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val firstDayOfMonth = selectedMonth.atDay(1)
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // 0=Sun
                val daysInMonth = selectedMonth.lengthOfMonth()
                val totalCells = firstDayOfWeek + daysInMonth
                val weeks = (totalCells + 6) / 7

                repeat(weeks) { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { dayOfWeek ->
                            val dayNum = week * 7 + dayOfWeek - firstDayOfWeek + 1
                            val isValid = dayNum in 1..daysInMonth
                            val date = if (isValid) selectedMonth.atDay(dayNum) else null
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val hasHistory = date != null && historyMap[date.toString()]?.isNotEmpty() == true

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Purple
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                                        color = if (isToday && !isSelected) Purple else androidx.compose.ui.graphics.Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = isValid) { date?.let { selectedDate = it } },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isValid) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontSize = 13.sp,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (hasHistory) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else GreenSuccess)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected day detail
        val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("id"))
        Text(
            text = "${selectedDate.dayOfMonth} $monthName ${selectedDate.year}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada aktivitas", color = TextSecondary)
            }
        } else {
            LazyColumn {
                items(selectedDayTasks) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (task.isCompleted) "✔" else "❌",
                            fontSize = 16.sp,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (task.isCompleted) GreenSuccess else RedError
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}
