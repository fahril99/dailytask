package com.example.dailyreminder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun BarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = Purple
) {
    val maxValue = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barCount = data.size
            if (barCount == 0) return@Canvas
            val spacing = 12.dp.toPx()
            val totalSpacing = spacing * (barCount + 1)
            val barWidth = (size.width - totalSpacing) / barCount

            data.forEachIndexed { index, (_, value) ->
                val barHeight = if (maxValue > 0) {
                    (value.toFloat() / maxValue) * (size.height - 20.dp.toPx())
                } else {
                    0f
                }
                val x = spacing + index * (barWidth + spacing)
                val y = size.height - barHeight - 10.dp.toPx()

                // Bar
                drawRoundRect(
                    color = if (value > 0) barColor else barColor.copy(alpha = 0.15f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
