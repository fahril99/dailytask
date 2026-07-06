package com.example.dailyreminder.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit,
    onSkip: () -> Unit
) {
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
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    modifier = Modifier.size(56.dp),
                    tint = Purple
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Daily\nReminder",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 38.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aplikasi membutuhkan izin notifikasi agar dapat mengingatkan aktivitas harian Anda tepat waktu.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Dipakai tiap hari,\nkebermanfaatan setiap hari.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = TextSecondary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Izinkan Notifikasi", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSkip) {
            Text("Nanti Saja", color = TextSecondary)
        }
    }
}

@Composable
fun PermissionSuccessScreen(onContinue: () -> Unit) {
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
                    .background(com.example.dailyreminder.ui.theme.GreenSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 48.sp, color = com.example.dailyreminder.ui.theme.GreenSuccess)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Notifikasi Diizinkan",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Terima kasih.\nSemua reminder akan berjalan sesuai jadwal.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
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
