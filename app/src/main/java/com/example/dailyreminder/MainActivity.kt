package com.example.dailyreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.core.content.ContextCompat
import com.example.dailyreminder.model.TaskItem
import com.example.dailyreminder.ui.MainViewModel
import com.example.dailyreminder.ui.screen.CalendarScreen
import com.example.dailyreminder.ui.screen.ConfirmationScreen
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
import com.example.dailyreminder.ui.theme.DarkSurface
import com.example.dailyreminder.ui.theme.Purple
import com.example.dailyreminder.ui.theme.TextSecondary
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var hasNotificationPermission by mutableStateOf(false)
    private var permissionJustGranted by mutableStateOf(false)

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
        handleIntent(intent)

        setContent {
            DailyReminderTheme {
                AppContent()
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
        val powerNapMinutes by viewModel.powerNapMinutes.collectAsState()

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
            powerNapMinutes = powerNapMinutes,
            nextTask = viewModel.getNextUpcomingTask(),
            minutesUntilNext = viewModel.getNextUpcomingTask()?.let { viewModel.getMinutesUntilTask(it) } ?: 0,
            onSaveSchedule = { viewModel.saveSchedule(it) },
            onSoundToggle = { viewModel.updateSoundEnabled(it) },
            onVibrationToggle = { viewModel.updateVibrationEnabled(it) },
            onStagedRemindersToggle = { viewModel.updateStagedRemindersEnabled(it) },
            onPowerNapChange = { viewModel.updatePowerNapMinutes(it) },
            onDateSelected = { viewModel.getHistoryForDate(it) }
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
        powerNapMinutes: Int,
        nextTask: TaskItem?,
        minutesUntilNext: Long,
        onSaveSchedule: (String) -> Unit,
        onSoundToggle: (Boolean) -> Unit,
        onVibrationToggle: (Boolean) -> Unit,
        onStagedRemindersToggle: (Boolean) -> Unit,
        onPowerNapChange: (Int) -> Unit,
        onDateSelected: (LocalDate) -> List<TaskItem>
    ) {
        data class NavItem(val label: String, val icon: ImageVector, val index: Int)

        val navItems = listOf(
            NavItem("Beranda", Icons.Default.Home, 0),
            NavItem("Kalender", Icons.Default.CalendarMonth, 1),
            NavItem("Riwayat", Icons.Default.History, 2),
            NavItem("Statistik", Icons.Default.BarChart, 3),
            NavItem("Pengaturan", Icons.Default.Settings, 4)
        )

        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        var editScheduleFromSettings by remember { mutableStateOf(false) }

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
                            powerNapMinutes = powerNapMinutes,
                            scheduleText = scheduleText,
                            onSoundToggle = onSoundToggle,
                            onVibrationToggle = onVibrationToggle,
                            onStagedRemindersToggle = onStagedRemindersToggle,
                            onPowerNapChange = onPowerNapChange,
                            onEditSchedule = { selectedTab = 0 }
                        )
                    }
                }
            }
        }
    }
}
