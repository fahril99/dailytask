package com.example.dailyreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import android.app.AlarmManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings as SettingsIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.MainViewModel
import com.example.dailyreminder.ui.screen.BatteryOptimizationScreen
import com.example.dailyreminder.ui.screen.CalendarScreen
import com.example.dailyreminder.ui.screen.ConfirmationScreen
import com.example.dailyreminder.ui.screen.DeviceOptimizationGuideScreen
import com.example.dailyreminder.ui.screen.HistoryScreen
import com.example.dailyreminder.ui.screen.HomeScreen
import com.example.dailyreminder.ui.screen.PermissionScreen
import com.example.dailyreminder.ui.screen.PermissionSuccessScreen
import com.example.dailyreminder.ui.screen.SettingsScreen
import com.example.dailyreminder.ui.screen.SnoozeScreen
import com.example.dailyreminder.ui.screen.StatisticsScreen
import com.example.dailyreminder.ui.screen.TaskSuccessScreen
import com.example.dailyreminder.ui.theme.CardDark
import com.example.dailyreminder.ui.theme.DailyReminderTheme
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var hasNotificationPermission by mutableStateOf(false)
    private var permissionJustGranted by mutableStateOf(false)

    // Battery optimization
    private var isBatteryOptimizationIgnored by mutableStateOf(true)
    private var batteryOptSkipped by mutableStateOf(false)
    private var showDeviceGuide by mutableStateOf(false)
    private var showExactAlarmDialog by mutableStateOf(false)

    // App navigation state
    private var taskDialogId by mutableStateOf<String?>(null)
    private var showConfirmationResult by mutableStateOf<ConfirmResult?>(null)

    enum class ConfirmResult { DONE, SNOOZE }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        permissionJustGranted = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()
        checkBatteryOptimization()
        handleIntent(intent)

        setContent {
            DailyReminderTheme {
                AppContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh status baterai dan izin saat kembali dari Settings
        checkBatteryOptimization()
        checkExactAlarmPermission()
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmDialog = true
            } else {
                showExactAlarmDialog = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("SHOW_TASK_DIALOG")?.let { taskId ->
            taskDialogId = taskId
            intent.removeExtra("SHOW_TASK_DIALOG")
        }
    }

    private fun checkNotificationPermission() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            isBatteryOptimizationIgnored = pm.isIgnoringBatteryOptimizations(packageName)
        } else {
            isBatteryOptimizationIgnored = true
        }
    }

    @Composable
    fun AppContent() {
        val tasks by viewModel.tasks.collectAsState()
        val scheduleText by viewModel.scheduleText.collectAsState()
        val historyMap by viewModel.historyMap.collectAsState()
        val currentStreak by viewModel.currentStreak.collectAsState()
        val longestStreak by viewModel.longestStreak.collectAsState()
        val weeklyStats by viewModel.weeklyStats.collectAsState()
        val weeklyCompleted by viewModel.weeklyCompleted.collectAsState()
        val weeklyMissed by viewModel.weeklyMissed.collectAsState()
        val soundEnabled by viewModel.soundEnabled.collectAsState()
        val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
        val stagedRemindersEnabled by viewModel.stagedRemindersEnabled.collectAsState()

        // Exact Alarm Dialog Overlay
        if (showExactAlarmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showExactAlarmDialog = false },
                title = { Text("Izin Alarm Tepat Waktu") },
                text = { Text("Aplikasi membutuhkan izin ini agar reminder bekerja secara akurat di latar belakang.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        }
                        showExactAlarmDialog = false
                    }) {
                        Text("Buka Pengaturan")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showExactAlarmDialog = false }) {
                        Text("Nanti")
                    }
                }
            )
        }

        // Overlay screens take priority
        if (taskDialogId != null) {
            val task = tasks.find { it.id == taskDialogId }
            if (task != null && !task.isCompleted) {
                when (showConfirmationResult) {
                    ConfirmResult.DONE -> TaskSuccessScreen(onContinue = {
                        showConfirmationResult = null
                        taskDialogId = null
                    })
                    ConfirmResult.SNOOZE -> SnoozeScreen(onOk = {
                        showConfirmationResult = null
                        taskDialogId = null
                    })
                    null -> ConfirmationScreen(
                        task = task,
                        onComplete = {
                            viewModel.completeTask(task.id)
                            showConfirmationResult = ConfirmResult.DONE
                        },
                        onSnooze = {
                            viewModel.snoozeTask(task.id)
                            showConfirmationResult = ConfirmResult.SNOOZE
                        }
                    )
                }
                return
            } else {
                // Task already completed or not found; clear
                taskDialogId = null
                showConfirmationResult = null
            }
        }

        // Permission flow (only on Android 13+ and if not yet granted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            if (permissionJustGranted) {
                PermissionSuccessScreen(onContinue = { permissionJustGranted = false })
            } else {
                PermissionScreen(
                    onRequestPermission = {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onSkip = { hasNotificationPermission = true } // let user proceed without permission
                )
            }
            return
        }

        if (showDeviceGuide) {
            DeviceOptimizationGuideScreen(onBack = { showDeviceGuide = false })
            return
        }

        if (!isBatteryOptimizationIgnored && !batteryOptSkipped) {
            BatteryOptimizationScreen(
                onRequestOptimization = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                startActivity(intent)
                            } catch (e2: Exception) {
                                batteryOptSkipped = true
                            }
                        }
                    } else {
                        batteryOptSkipped = true
                    }
                },
                onSkip = { batteryOptSkipped = true }
            )
            return
        }

        // Main App with Bottom Navigation
        MainAppScaffold(
            tasks = tasks,
            scheduleText = scheduleText,
            historyMap = historyMap,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyStats = weeklyStats,
            weeklyCompleted = weeklyCompleted,
            weeklyMissed = weeklyMissed,
            soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled,
            stagedRemindersEnabled = stagedRemindersEnabled,
            nextTask = viewModel.getNextUpcomingTask(),
            minutesUntilNext = viewModel.getNextUpcomingTask()?.let { viewModel.getMinutesUntilTask(it) } ?: 0,
            onSaveSchedule = { viewModel.saveSchedule(it) },
            onSoundToggle = { viewModel.updateSoundEnabled(it) },
            onVibrationToggle = { viewModel.updateVibrationEnabled(it) },
            onStagedRemindersToggle = { viewModel.updateStagedRemindersEnabled(it) },
            onDateSelected = { viewModel.getHistoryForDate(it) },
            onShowDeviceGuide = { showDeviceGuide = true }
        )
    }

    @Composable
    fun MainAppScaffold(
        tasks: List<TaskItem>,
        scheduleText: String,
        historyMap: Map<String, Set<String>>,
        currentStreak: Int,
        longestStreak: Int,
        weeklyStats: List<Pair<String, Int>>,
        weeklyCompleted: Int,
        weeklyMissed: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        stagedRemindersEnabled: Boolean,
        nextTask: TaskItem?,
        minutesUntilNext: Long,
        onSaveSchedule: (String) -> Unit,
        onSoundToggle: (Boolean) -> Unit,
        onVibrationToggle: (Boolean) -> Unit,
        onStagedRemindersToggle: (Boolean) -> Unit,
        onDateSelected: (LocalDate) -> List<TaskItem>,
        onShowDeviceGuide: () -> Unit
    ) {
        data class NavItem(val label: String, val icon: ImageVector, val index: Int)

        val navItems = listOf(
            NavItem("Beranda", Icons.Default.Home, 0),
            NavItem("Kalender", Icons.Default.CalendarMonth, 1),
            NavItem("Riwayat", Icons.Default.History, 2),
            NavItem("Statistik", Icons.Default.BarChart, 3),
            NavItem("Pengaturan", Icons.Default.SettingsIcon, 4)
        )

        var selectedTab by rememberSaveable { mutableIntStateOf(0) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = CardDark,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = selectedTab == item.index,
                            onClick = { selectedTab = item.index },
                            icon = {
                                Icon(imageVector = item.icon, contentDescription = item.label)
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == item.index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple,
                                selectedTextColor = Purple,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Purple.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> HomeScreen(
                            scheduleText = scheduleText,
                            tasks = tasks,
                            nextTask = nextTask,
                            minutesUntilNext = minutesUntilNext,
                            currentStreak = currentStreak,
                            onSaveSchedule = onSaveSchedule,
                            onNavigateToSettings = { selectedTab = 4 }
                        )
                        1 -> CalendarScreen(
                            historyMap = historyMap,
                            onDateSelected = onDateSelected
                        )
                        2 -> HistoryScreen(tasks = tasks)
                        3 -> StatisticsScreen(
                            weeklyCompleted = weeklyCompleted,
                            weeklyMissed = weeklyMissed,
                            longestStreak = longestStreak,
                            longestStreakDate = "",
                            weeklyStats = weeklyStats
                        )
                        4 -> SettingsScreen(
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            stagedRemindersEnabled = stagedRemindersEnabled,
                            scheduleText = scheduleText,
                            onSoundToggle = onSoundToggle,
                            onVibrationToggle = onVibrationToggle,
                            onStagedRemindersToggle = onStagedRemindersToggle,
                            onEditSchedule = { selectedTab = 0 },
                            onShowOptimizationGuide = onShowDeviceGuide
                        )
                    }
                }
            }
        }
    }
}
