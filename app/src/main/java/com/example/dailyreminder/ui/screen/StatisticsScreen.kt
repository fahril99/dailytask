package com.example.dailyreminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyreminder.ui.components.BarChart
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.GreenSuccess
import com.example.dailyreminder.ui.theme.OrangeWarning
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.RedError
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun StatisticsScreen(
    weeklyCompleted: Int,
    weeklyMissed: Int,
    longestStreak: Int,
    longestStreakDate: String,
    weeklyStats: List<Pair<String, Int>>
) {
    val total = weeklyCompleted + weeklyMissed
    val percentage = if (total == 0) 0 else ((weeklyCompleted.toFloat() / total) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Statistik",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Minggu Ini",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Selesai",
                value = weeklyCompleted.toString(),
                color = GreenSuccess,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Belum Selesai",
                value = weeklyMissed.toString(),
                color = RedError,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Persentase",
                value = "$percentage%",
                color = Purple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bar Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Penyelesaian Harian",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(data = weeklyStats)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Longest Streak Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Streak Terpanjang",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "$longestStreak Hari",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (longestStreakDate.isNotEmpty()) {
                        Text(
                            text = longestStreakDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
