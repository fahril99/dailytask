package com.example.dailyreminder.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.GreenSuccess
import com.example.dailyreminder.ui.theme.OrangeWarning
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.RedError
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun ConfirmationScreen(
    task: TaskItem,
    onComplete: () -> Unit,
    onSnooze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏃", fontSize = 44.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = task.timeString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = task.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (task.description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sudah selesai?",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
        ) {
            Text("Sudah Selesai", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSnooze,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = RedError)
        ) {
            Text("Belum Selesai", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun TaskSuccessScreen(onContinue: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GreenSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 48.sp, color = GreenSuccess)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bagus!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tugas ditandai selesai.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Lanjutkan", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SnoozeScreen(onOk: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(OrangeWarning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🕒", fontSize = 44.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Baik!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Saya akan mengingatkan lagi\n5 menit.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onOk,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Oke", fontWeight = FontWeight.SemiBold)
        }
    }
}
