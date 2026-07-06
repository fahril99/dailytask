package com.example.dailyreminder.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.DividerDark
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    stagedRemindersEnabled: Boolean,
    powerNapMinutes: Int,
    scheduleText: String,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onStagedRemindersToggle: (Boolean) -> Unit,
    onPowerNapChange: (Int) -> Unit,
    onEditSchedule: () -> Unit
) {
    var showPowerNapDialog by remember { mutableStateOf(false) }
    var powerNapInput by remember(powerNapMinutes) { mutableStateOf(powerNapMinutes.toString()) }

    if (showPowerNapDialog) {
        AlertDialog(
            onDismissRequest = { showPowerNapDialog = false },
            title = { Text("Atur Power Nap Default") },
            text = {
                OutlinedTextField(
                    value = powerNapInput,
                    onValueChange = { powerNapInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Menit") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = powerNapInput.toIntOrNull()
                    if (mins != null && mins > 0) {
                        onPowerNapChange(mins)
                    }
                    showPowerNapDialog = false
                }) { Text("Simpan", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { showPowerNapDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Jadwal Section
        SettingsSection(title = "Jadwal") {
            SettingsClickableItem(
                icon = Icons.Default.CalendarToday,
                title = "Edit Jadwal",
                subtitle = "Ubah jadwal reminder harian",
                onClick = onEditSchedule
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pengingat Section
        SettingsSection(title = "Pengingat") {
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Suara Notifikasi",
                subtitle = "Aktifkan suara saat notifikasi muncul",
                checked = soundEnabled,
                onToggle = onSoundToggle
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerDark)
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Getaran",
                subtitle = "Aktifkan getaran saat notifikasi muncul",
                checked = vibrationEnabled,
                onToggle = onVibrationToggle
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerDark)
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Reminder Bertahap",
                subtitle = "Aktifkan pengingat -5, 0, +5, +15, +30 menit",
                checked = stagedRemindersEnabled,
                onToggle = onStagedRemindersToggle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Waktu Istirahat Section
        SettingsSection(title = "Waktu Istirahat") {
            SettingsClickableItem(
                icon = Icons.Default.SelfImprovement,
                title = "Power Nap Default",
                subtitle = "Durasi istirahat: $powerNapMinutes menit",
                onClick = { showPowerNapDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tentang Section
        SettingsSection(title = "Tentang Aplikasi") {
            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "Versi Aplikasi",
                subtitle = "1.0.0"
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = Purple,
                uncheckedTrackColor = DividerDark
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        IconButton(onClick = onClick) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Purple, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
