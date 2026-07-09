package com.example.workdayplanner.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workdayplanner.PlannerViewModel
import com.example.workdayplanner.TrainingImportUiState
import com.example.workdayplanner.calendar.DeviceCalendar
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.CarryOverBehavior
import com.example.workdayplanner.data.LinkedShiftType
import com.example.workdayplanner.data.RepeatRule
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.TaskCategory
import com.example.workdayplanner.data.TaskPriority
import com.example.workdayplanner.data.TaskRecurrence
import com.example.workdayplanner.data.TaskScheduleClassifier
import com.example.workdayplanner.data.TaskScheduleInsight
import com.example.workdayplanner.data.TaskScheduleLabel
import com.example.workdayplanner.data.TaskTemplate
import com.example.workdayplanner.data.TaskTimingRule
import com.example.workdayplanner.data.WorkNote
import com.example.workdayplanner.data.WorkNoteKind
import com.example.workdayplanner.data.WidgetLayoutMode
import com.example.workdayplanner.data.ParsedSchedule
import com.example.workdayplanner.data.PayEstimator
import com.example.workdayplanner.data.PayEstimate
import com.example.workdayplanner.data.PayPeriodType
import com.example.workdayplanner.data.PaySettings
import com.example.workdayplanner.data.PremiumAccess
import com.example.workdayplanner.data.PremiumFeature
import com.example.workdayplanner.data.ScheduleChangeSet
import com.example.workdayplanner.data.ScheduleAwareTaskPlanner
import com.example.workdayplanner.data.ScheduleImportGuidance
import com.example.workdayplanner.data.ScheduleRisk
import com.example.workdayplanner.data.ScheduleRiskAnalyzer
import com.example.workdayplanner.data.ShiftPattern
import com.example.workdayplanner.data.ShiftPatternDay
import com.example.workdayplanner.data.ShiftPatternDayKind
import com.example.workdayplanner.data.ShiftPatternGenerator
import com.example.workdayplanner.data.TimecardCalculator
import com.example.workdayplanner.data.TimecardEntry
import com.example.workdayplanner.data.TrainingItem
import com.example.workdayplanner.data.WorkChecklistTemplate
import com.example.workdayplanner.data.WorkChecklistTemplates
import com.example.workdayplanner.data.WorkImage
import com.example.workdayplanner.data.WorkShift
import com.example.workdayplanner.data.WorkEvent
import com.example.workdayplanner.data.ShiftTemplate
import com.example.workdayplanner.data.ShiftTemplateKind
import java.io.File
import java.time.Duration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val screenPadding = 18.dp
private val sectionGap = 14.dp

private enum class TaskView(val label: String) {
    Today("Today"),
    Important("Important"),
    Overdue("Overdue"),
    Deadline("Deadline"),
    All("All")
}

private enum class TrainingView(val label: String) {
    Open("Open"),
    Associates("Associates"),
    Overdue("Overdue"),
    DueSoon("Due soon"),
    NoDate("No date"),
    Completed("Completed"),
    All("All")
}

@Composable
fun PlannerApp(
    viewModel: PlannerViewModel,
    requestedTaskId: String? = null,
    onTaskRequestHandled: () -> Unit = {},
    onNotificationPermissionNeeded: () -> Unit = {}
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val calendarMessage by viewModel.calendarMessage.collectAsStateWithLifecycle()
    val imageMessage by viewModel.imageMessage.collectAsStateWithLifecycle()
    val trainingImportState by viewModel.trainingImportState.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val topLevel = listOf(Screen.Tasks, Screen.Notes, Screen.Schedule, Screen.Manager, Screen.Settings)
    var showPremiumScreen by remember { mutableStateOf(false) }
    val showIntro = !state.onboardingCompleted && !state.hasPlannerData()

    LaunchedEffect(requestedTaskId, state.tasks) {
        val taskId = requestedTaskId?.takeIf { id -> state.tasks.any { it.id == id } } ?: return@LaunchedEffect
        navController.navigate("${Screen.TaskDetail.route}/$taskId") {
            launchSingleTop = true
        }
        onTaskRequestHandled()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = when {
                    showIntro -> "Workday Planner"
                    currentRoute.startsWith(Screen.TaskDetail.route) -> "Task"
                    currentRoute.startsWith(Screen.EventDetail.route) -> "Event"
                    currentRoute == Screen.Manager.route -> "Manager"
                    currentRoute == Screen.Notes.route -> "Notes"
                    currentRoute == Screen.Schedule.route -> "Schedule"
                    currentRoute == Screen.Import.route -> "Import"
                    currentRoute == Screen.Settings.route -> "Settings"
                    showPremiumScreen -> "Premium"
                    currentRoute == Screen.Tasks.route -> "Today"
                    else -> "Workday Planner"
                }
            )
        },
        bottomBar = {
            if (!showIntro) {
                NavigationBar {
                    topLevel.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Tasks.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, maxLines = 1) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showIntro && currentRoute == Screen.Tasks.route) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("${Screen.TaskDetail.route}/new") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New task") }
                )
            }
        }
    ) { padding ->
        if (showIntro) {
            WorkdayIntroScreen(
                modifier = Modifier.padding(padding),
                onImportSchedule = {
                    viewModel.completeOnboarding()
                    navController.navigate(Screen.Import.route) { launchSingleTop = true }
                },
                onStart = viewModel::completeOnboarding
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Screen.Tasks.route,
                modifier = Modifier.padding(padding)
            ) {
            composable(Screen.Tasks.route) {
                TaskListScreen(
                    state = state,
                    onTaskClick = { navController.navigate("${Screen.TaskDetail.route}/${it.id}") },
                    onEventClick = { navController.navigate("${Screen.EventDetail.route}/${it.id}") },
                    onAddTask = { navController.navigate("${Screen.TaskDetail.route}/new") },
                    onAddRepeatingTask = { navController.navigate("${Screen.TaskDetail.route}/new") },
                    onScheduleShortcut = { navController.navigate(Screen.Schedule.route) },
                    onImportSchedule = { navController.navigate(Screen.Import.route) },
                    onMarkTodayOff = { viewModel.addTypedDayOff(LocalDate.now(), ShiftTemplateKind.DayOff) },
                    onAddEvent = { navController.navigate("${Screen.EventDetail.route}/new") },
                    onToggleComplete = viewModel::toggleComplete,
                    onDelete = viewModel::deleteTask,
                    onDeleteEvent = viewModel::deleteEvent,
                    onClockIn = viewModel::clockIn,
                    onStartLunch = viewModel::startLunch,
                    onEndLunch = viewModel::endLunch,
                    onClockOut = viewModel::clockOut,
                    onSaveTimecardNote = viewModel::saveTimecardNote,
                    onAddChecklist = viewModel::addChecklistTemplate,
                    onOpenPremium = { showPremiumScreen = true }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    state = state,
                    onAddNote = viewModel::addWorkNote,
                    onDeleteNote = viewModel::deleteWorkNote,
                    onCreateTaskFromNote = viewModel::createTaskFromNote,
                    imageMessage = imageMessage,
                    onAddImage = viewModel::addWorkImage,
                    onDeleteImage = viewModel::deleteWorkImage
                )
            }
            composable(Screen.Manager.route) {
                ManagerScreen(
                    state = state,
                    importState = trainingImportState,
                    onRecognizeImage = viewModel::recognizeTrainingImage,
                    onTextChanged = viewModel::setTrainingImportText,
                    onImport = viewModel::importTrainingItems,
                    onAddManual = viewModel::addManualTrainingItem,
                    onToggleComplete = viewModel::toggleTrainingComplete,
                    onDelete = viewModel::deleteTrainingItem,
                    onCreateTask = viewModel::createTaskFromTrainingItem,
                    onCreateFollowUps = viewModel::createTrainingFollowUpTasks
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    state = state,
                    onAddShift = viewModel::saveShift,
                    onDeleteShift = viewModel::deleteShift,
                    onSaveShiftTemplate = viewModel::saveShiftTemplate,
                    onDeleteShiftTemplate = viewModel::deleteShiftTemplate,
                    onSaveShiftPattern = viewModel::saveShiftPattern,
                    onApplyShiftPattern = viewModel::applyShiftPattern,
                    onSetShiftPatternEnabled = viewModel::setShiftPatternEnabled,
                    onDeleteShiftPattern = viewModel::deleteShiftPattern,
                    onAddDayOff = viewModel::addDayOff,
                    onAddTypedDayOff = viewModel::addTypedDayOff,
                    onRemoveDayOff = viewModel::removeDayOff,
                    onClearSchedule = viewModel::clearSchedule,
                    onImportSchedule = { navController.navigate(Screen.Import.route) },
                    onOpenPremium = { showPremiumScreen = true }
                )
            }
            composable(Screen.Import.route) {
                ImportScreen(
                    state = state,
                    rawText = importState.rawText,
                    parsed = importState.parsed,
                    changes = importState.changes,
                    isReading = importState.isReadingImage,
                    message = importState.appliedMessage,
                    error = importState.error,
                    guidance = importState.guidance,
                    onTextChange = viewModel::setImportText,
                    onImagePicked = viewModel::recognizeScheduleImage,
                    onImageCancelled = viewModel::cancelScheduleImport,
                    onPreview = { viewModel.previewImport() },
                    onApply = { corrected -> viewModel.applyImport(corrected) },
                    onStartOver = viewModel::resetScheduleImport,
                    onOpenPremium = { showPremiumScreen = true }
                )
            }
            composable(Screen.Settings.route) {
                if (showPremiumScreen) {
                    PremiumScreen(
                        state = state,
                        onMockPremiumChanged = viewModel::setMockPremium,
                        onBack = { showPremiumScreen = false }
                    )
                } else {
                    SettingsScreen(
                        state = state,
                        onDarkModeChanged = viewModel::setDarkMode,
                        onAccentStyleChanged = viewModel::setAccentStyle,
                        onWidgetLayoutModeChanged = viewModel::setWidgetLayoutMode,
                        onPaySettingsChanged = viewModel::setPaySettings,
                        calendars = calendars,
                        calendarMessage = calendarMessage,
                        onLoadCalendars = viewModel::loadCalendars,
                        onSelectCalendar = viewModel::setSelectedCalendar,
                        onSyncCalendar = viewModel::syncShiftsToCalendar,
                        onMockPremiumChanged = viewModel::setMockPremium,
                        onOpenPremium = { showPremiumScreen = true }
                    )
                }
            }
            composable(
                route = "${Screen.TaskDetail.route}/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { entry ->
                val taskId = entry.arguments?.getString("taskId").orEmpty()
                TaskDetailScreen(
                    state = state,
                    task = state.tasks.firstOrNull { it.id == taskId },
                    onSave = {
                        viewModel.saveTask(it)
                        navController.popBackStack()
                    },
                    onSaveAndContinue = viewModel::saveTask,
                    onSaveTaskTemplate = viewModel::saveTaskTemplate,
                    onDeleteTaskTemplate = viewModel::deleteTaskTemplate,
                    onNotificationPermissionNeeded = onNotificationPermissionNeeded,
                    onOpenPremium = { showPremiumScreen = true },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "${Screen.EventDetail.route}/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { entry ->
                val eventId = entry.arguments?.getString("eventId").orEmpty()
                EventDetailScreen(
                    event = state.events.firstOrNull { it.id == eventId },
                    onSave = {
                        viewModel.saveEvent(it)
                        navController.popBackStack()
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
        }
    }
}

private fun AppState.hasPlannerData(): Boolean {
    return tasks.isNotEmpty() ||
        notes.isNotEmpty() ||
        images.isNotEmpty() ||
        events.isNotEmpty() ||
        shifts.isNotEmpty() ||
        daysOff.isNotEmpty() ||
        trainingItems.isNotEmpty() ||
        timecards.isNotEmpty()
}

@Composable
private fun WorkdayIntroScreen(
    modifier: Modifier = Modifier,
    onImportSchedule: () -> Unit,
    onStart: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Your workday command center",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Track shifts, tasks, reminders, days off, training, and estimated hours without an account or cloud upload.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            IntroFeatureCard(
                icon = Icons.Default.CalendarMonth,
                title = "Start with your schedule",
                body = "Import a schedule screenshot, review what OCR found, then save only the shifts you confirm."
            )
        }
        item {
            IntroFeatureCard(
                icon = Icons.Default.CheckCircle,
                title = "Plan around shifts",
                body = "Tasks can sit before work, after work, on workdays only, or skip marked days off."
            )
        }
        item {
            IntroFeatureCard(
                icon = Icons.Default.Settings,
                title = "Keep it local",
                body = "Your schedule, notes, photos, pay estimates, and training tracker stay on this device."
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
                Button(onClick = onImportSchedule, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import my schedule")
                }
                OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                    Text("Start from dashboard")
                }
            }
        }
    }
}

@Composable
private fun IntroFeatureCard(icon: ImageVector, title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(title: String) {
    TopAppBar(title = { Text(title, fontWeight = FontWeight.SemiBold) })
}

@Composable
private fun TaskListScreen(
    state: AppState,
    onTaskClick: (TaskItem) -> Unit,
    onEventClick: (WorkEvent) -> Unit,
    onAddTask: () -> Unit,
    onAddRepeatingTask: () -> Unit,
    onScheduleShortcut: () -> Unit,
    onImportSchedule: () -> Unit,
    onMarkTodayOff: () -> Unit,
    onAddEvent: () -> Unit,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onClockIn: () -> Unit,
    onStartLunch: () -> Unit,
    onEndLunch: () -> Unit,
    onClockOut: () -> Unit,
    onSaveTimecardNote: (String) -> Unit,
    onAddChecklist: (String) -> Unit,
    onOpenPremium: () -> Unit
) {
    val today = LocalDate.now()
    val now = LocalDateTime.now()
    var taskView by remember { mutableStateOf(TaskView.Today) }
    val tasks = state.tasks.sortedWith(
        compareBy<TaskItem> { it.completed }
            .thenByDescending { it.priority.sortWeight }
            .thenBy { it.deadline ?: LocalDateTime.MAX }
            .thenBy { it.title.lowercase() }
    )
    val skippedDayOffTasks = tasks.filter { task -> task.isSkippedBecauseDayOff(state, today) }
    val filteredTasks = tasks.filter { task ->
        if (task in skippedDayOffTasks && taskView in setOf(TaskView.Today, TaskView.Deadline)) return@filter false
        if (taskView == TaskView.Today && ScheduleAwareTaskPlanner.shouldHideOnDayOff(task, state, today)) return@filter false
        when (taskView) {
            TaskView.Today -> task.deadline?.toLocalDate() == today
            TaskView.Important -> !task.completed && task.priority.sortWeight >= TaskPriority.High.sortWeight
            TaskView.Overdue -> !task.completed && task.deadline?.isBefore(now) == true
            TaskView.Deadline -> true
            TaskView.All -> true
        }
    }
    val overdueTasks = filteredTasks.filter { !it.completed && it.deadline?.isBefore(now) == true }
    val criticalTasks = filteredTasks.filter { !it.completed && it.priority == TaskPriority.Critical && it !in overdueTasks }
    val highTasks = filteredTasks.filter { !it.completed && it.priority == TaskPriority.High && it !in overdueTasks }
    val normalTasks = filteredTasks.filter { !it.completed && it.priority.sortWeight < TaskPriority.High.sortWeight && it !in overdueTasks }
    val dueSoonTasks = filteredTasks.filter {
        !it.completed && it.deadline?.let { deadline -> !deadline.isBefore(now) && deadline.isBefore(now.plusHours(12)) } == true
    }
    val deadlineTodayTasks = filteredTasks.filter {
        !it.completed && it.deadline?.toLocalDate() == today && it !in overdueTasks && it !in dueSoonTasks
    }
    val tomorrowTasks = filteredTasks.filter { !it.completed && it.deadline?.toLocalDate() == today.plusDays(1) }
    val laterTasks = filteredTasks.filter {
        !it.completed && it.deadline?.toLocalDate()?.isAfter(today.plusDays(1)) == true
    }
    val noDeadlineTasks = filteredTasks.filter { !it.completed && it.deadline == null }
    val completedTasks = filteredTasks.filter { it.completed }
    val allOpenTasks = tasks.filterNot { it.completed }
    val allOverdueCount = allOpenTasks.count { it.deadline?.isBefore(now) == true }
    val allCriticalCount = allOpenTasks.count { it.priority == TaskPriority.Critical }
    val allDueSoonCount = allOpenTasks.count {
        it.deadline?.let { deadline -> !deadline.isBefore(now) && deadline.isBefore(now.plusHours(12)) } == true
    }
    val allTodayCount = allOpenTasks.count { it.deadline?.toLocalDate() == today && !it.isSkippedBecauseDayOff(state, today) }
    val nextTask = allOpenTasks
        .filter { it.deadline != null || it.priority.sortWeight >= TaskPriority.High.sortWeight }
        .sortedWith(
            compareBy<TaskItem> { it.deadline?.isBefore(now) != true }
                .thenByDescending { it.priority.sortWeight }
                .thenBy { it.deadline ?: LocalDateTime.MAX }
        )
        .firstOrNull()
    val events = state.events.sortedBy { it.startsAt }
    val risks = ScheduleRiskAnalyzer.risks(state, today)
    if (tasks.isEmpty() && events.isEmpty()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            CommandCenterCard(
                state = state,
                overdueTaskCount = allOverdueCount,
                todayTaskCount = allTodayCount,
                upcomingAlarmCount = upcomingAlarmCount(state, now),
                onAddTask = onAddTask,
                onAddRepeatingTask = onAddRepeatingTask,
                onScheduleShortcut = onScheduleShortcut,
                onImportSchedule = onImportSchedule,
                onMarkTodayOff = onMarkTodayOff
            )
            ScheduleRiskSection(risks = risks)
            TimecardSection(
                state = state,
                onClockIn = onClockIn,
                onStartLunch = onStartLunch,
                onEndLunch = onEndLunch,
                onClockOut = onClockOut,
                onSaveNote = onSaveTimecardNote
            )
            ChecklistTemplateSection(state = state, onAddChecklist = onAddChecklist, onOpenPremium = onOpenPremium)
            OutlinedButton(onClick = onAddEvent, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Event, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add meeting or work event")
            }
            EmptyState("Nothing planned yet", "Add a task, meeting, or import your work schedule to start building your day.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            CommandCenterCard(
                state = state,
                overdueTaskCount = allOverdueCount,
                todayTaskCount = allTodayCount,
                upcomingAlarmCount = upcomingAlarmCount(state, now),
                onAddTask = onAddTask,
                onAddRepeatingTask = onAddRepeatingTask,
                onScheduleShortcut = onScheduleShortcut,
                onImportSchedule = onImportSchedule,
                onMarkTodayOff = onMarkTodayOff
            )
        }
        if (risks.isNotEmpty()) {
            item { ScheduleRiskSection(risks = risks) }
        }
        item {
            TimecardSection(
                state = state,
                onClockIn = onClockIn,
                onStartLunch = onStartLunch,
                onEndLunch = onEndLunch,
                onClockOut = onClockOut,
                onSaveNote = onSaveTimecardNote
            )
        }
        item { ChecklistTemplateSection(state = state, onAddChecklist = onAddChecklist, onOpenPremium = onOpenPremium) }
        item {
            OutlinedButton(onClick = onAddEvent, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Event, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add meeting or work event")
            }
        }
        if (events.isNotEmpty()) {
            item { Text("Work events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(events, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    onDelete = { onDeleteEvent(event.id) }
                )
            }
        }
        item {
            TaskFocusCard(
                overdueCount = allOverdueCount,
                criticalCount = allCriticalCount,
                dueSoonCount = allDueSoonCount,
                todayCount = allTodayCount,
                nextTask = nextTask,
                onShowOverdue = { taskView = TaskView.Overdue },
                onShowImportant = { taskView = TaskView.Important },
                onShowDeadline = { taskView = TaskView.Deadline }
            )
        }
        item {
            TaskViewSelector(
                selected = taskView,
                counts = mapOf(
                    TaskView.Today to tasks.count { it.deadline?.toLocalDate() == today && !it.isSkippedBecauseDayOff(state, today) },
                    TaskView.Important to tasks.count { !it.completed && it.priority.sortWeight >= TaskPriority.High.sortWeight },
                    TaskView.Overdue to tasks.count { !it.completed && it.deadline?.isBefore(now) == true },
                    TaskView.Deadline to tasks.count { !it.completed },
                    TaskView.All to tasks.size
                ),
                onSelected = { taskView = it }
            )
        }
        if (taskView == TaskView.Today && skippedDayOffTasks.isNotEmpty()) {
            item {
                SkippedDayOffTasksCard(tasks = skippedDayOffTasks, state = state)
            }
        }
        if (filteredTasks.isEmpty()) {
            item { EmptyState("No ${taskView.label.lowercase()} tasks", "Switch views or add a task when something comes up.") }
        }
        if (taskView == TaskView.Deadline) {
            taskSection(
                title = "Overdue",
                subtitle = "Past deadline",
                tasks = overdueTasks.sortedBy { it.deadline },
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "Due soon",
                subtitle = "Next 12 hours",
                tasks = dueSoonTasks.sortedBy { it.deadline },
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "Later today",
                subtitle = "Still due today",
                tasks = deadlineTodayTasks.sortedBy { it.deadline },
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "Tomorrow",
                subtitle = today.plusDays(1).format(dateFormatter),
                tasks = tomorrowTasks.sortedBy { it.deadline },
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "Later",
                subtitle = "After tomorrow",
                tasks = laterTasks.sortedBy { it.deadline },
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "No deadline",
                subtitle = "Needs a date when ready",
                tasks = noDeadlineTasks,
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
        } else {
            taskSection(
                title = "Overdue",
                subtitle = "Past deadline",
                tasks = overdueTasks,
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "Critical",
                subtitle = "Do first",
                tasks = criticalTasks,
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = "High priority",
                subtitle = "Important work",
                tasks = highTasks,
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
            taskSection(
                title = if (taskView == TaskView.Today) "Today tasks" else "Normal priority",
                subtitle = if (taskView == TaskView.Today) "Routine work due today" else "Sorted by deadline",
                tasks = normalTasks,
                state = state,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onDelete = onDelete
            )
        }
        taskSection(
            title = "Completed",
            subtitle = "Done in this view",
            tasks = completedTasks,
            state = state,
            onTaskClick = onTaskClick,
            onToggleComplete = onToggleComplete,
            onDelete = onDelete
        )
    }
}

@Composable
private fun SkippedDayOffTasksCard(tasks: List<TaskItem>, state: AppState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Skipped because this is a day off.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            tasks.forEach { task ->
                val movedTo = TaskRecurrence.nextOccurrence(task, state)?.deadline
                Text(
                    "${task.title}: Moved to your next workday${movedTo?.let { " (${it.format(dateTimeFormatter)})" }.orEmpty()}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun TaskItem.isSkippedBecauseDayOff(state: AppState, today: LocalDate): Boolean {
    return !completed &&
        repeatRule != RepeatRule.None &&
        skipDaysOff &&
        deadline?.toLocalDate() == today &&
        TaskRecurrence.isDayOff(today, state)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskFocusCard(
    overdueCount: Int,
    criticalCount: Int,
    dueSoonCount: Int,
    todayCount: Int,
    nextTask: TaskItem?,
    onShowOverdue: () -> Unit,
    onShowImportant: () -> Unit,
    onShowDeadline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        nextTask?.focusLine().orEmpty().ifBlank { "No urgent task needs attention." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FocusChip(
                    label = "$overdueCount overdue",
                    color = Color(0xFFFF5A66),
                    enabled = overdueCount > 0,
                    onClick = onShowOverdue
                )
                FocusChip(
                    label = "$criticalCount critical",
                    color = Color(0xFFFF5A66),
                    enabled = criticalCount > 0,
                    onClick = onShowImportant
                )
                FocusChip(
                    label = "$dueSoonCount due soon",
                    color = Color(0xFFFFB020),
                    enabled = dueSoonCount > 0,
                    onClick = onShowDeadline
                )
                FocusChip(
                    label = "$todayCount today",
                    color = MaterialTheme.colorScheme.primary,
                    enabled = todayCount > 0,
                    onClick = onShowDeadline
                )
            }
        }
    }
}

@Composable
private fun FocusChip(label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.16f),
            labelColor = color,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

private fun TaskItem.focusLine(): String {
    val due = deadline?.format(dateTimeFormatter)
    val prefix = when (priority) {
        TaskPriority.Critical -> "Critical"
        TaskPriority.High -> "High priority"
        TaskPriority.Normal -> "Next"
        TaskPriority.Low -> "Low priority"
    }
    return if (due == null) "$prefix: $title" else "$prefix: $title due $due"
}

private fun androidx.compose.foundation.lazy.LazyListScope.taskSection(
    title: String,
    subtitle: String,
    tasks: List<TaskItem>,
    state: AppState,
    onTaskClick: (TaskItem) -> Unit,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (tasks.isEmpty()) return
    item { SectionHeader(title, subtitle) }
    items(tasks, key = { "$title-${it.id}" }) { task ->
        TaskCard(
            task = task,
            scheduleInsight = TaskScheduleClassifier.classify(task, state),
            onClick = { onTaskClick(task) },
            onToggleComplete = { onToggleComplete(task.id) },
            onDelete = { onDelete(task.id) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskViewSelector(
    selected: TaskView,
    counts: Map<TaskView, Int>,
    onSelected: (TaskView) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Task view", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskView.entries.forEach { view ->
                val label = "${view.label} ${counts[view] ?: 0}"
                if (view == selected) {
                    Button(onClick = { onSelected(view) }) { Text(label) }
                } else {
                    OutlinedButton(onClick = { onSelected(view) }) { Text(label) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManagerScreen(
    state: AppState,
    importState: TrainingImportUiState,
    onRecognizeImage: (Uri) -> Unit,
    onTextChanged: (String) -> Unit,
    onImport: () -> Unit,
    onAddManual: (String, String, LocalDate?) -> Unit,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCreateTask: (String) -> Unit,
    onCreateFollowUps: () -> Unit
) {
    val today = LocalDate.now()
    var searchText by remember { mutableStateOf("") }
    var trainingView by remember { mutableStateOf(TrainingView.Open) }
    var showAddTraining by remember { mutableStateOf(state.trainingItems.isEmpty()) }
    val openItems = state.trainingItems.filter { it.completedAt == null }
    val overdue = openItems.count { it.dueDate?.isBefore(today) == true }
    val dueSoon = openItems.count { it.dueDate?.let { due -> !due.isBefore(today) && !due.isAfter(today.plusDays(7)) } == true }
    val noDate = openItems.count { it.dueDate == null }
    val associateCount = openItems.map { it.associateName.lowercase() }.distinct().size
    val filteredItems = state.trainingItems
        .filter { item ->
            when (trainingView) {
                TrainingView.Open -> item.completedAt == null
                TrainingView.Associates -> item.completedAt == null
                TrainingView.Overdue -> item.completedAt == null && item.dueDate?.isBefore(today) == true
                TrainingView.DueSoon -> item.completedAt == null && item.dueDate?.let { due -> !due.isBefore(today) && !due.isAfter(today.plusDays(7)) } == true
                TrainingView.NoDate -> item.completedAt == null && item.dueDate == null
                TrainingView.Completed -> item.completedAt != null
                TrainingView.All -> true
            }
        }
        .filter { item ->
            searchText.isBlank() ||
                item.associateName.contains(searchText, ignoreCase = true) ||
                item.trainingTitle.contains(searchText, ignoreCase = true) ||
                item.sourceText.contains(searchText, ignoreCase = true)
        }
        .sortedWith(compareBy<TrainingItem> { it.completedAt != null }.thenBy { it.dueDate ?: LocalDate.MAX }.thenBy { it.associateName })
    val associateGroups = filteredItems
        .groupBy { it.associateName }
        .map { (associate, items) -> TrainingAssociateGroup(associate, items) }
        .sortedWith(compareByDescending<TrainingAssociateGroup> { it.overdueCount }.thenByDescending { it.dueSoonCount }.thenBy { it.nextDue ?: LocalDate.MAX }.thenBy { it.name })

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            ManagerDashboardCard(
                openTrainingCount = openItems.size,
                associateCount = associateCount,
                overdueCount = overdue,
                dueSoonCount = dueSoon,
                onViewOpen = { trainingView = TrainingView.Open },
                onViewAssociates = { trainingView = TrainingView.Associates },
                onViewOverdue = { trainingView = TrainingView.Overdue },
                onViewDueSoon = { trainingView = TrainingView.DueSoon },
                onCreateFollowUps = onCreateFollowUps
            )
        }
        item {
            if (showAddTraining) {
                AddTrainingCard(
                    importState = importState,
                    onAddManual = onAddManual,
                    onRecognizeImage = onRecognizeImage,
                    onTextChanged = onTextChanged,
                    onImport = onImport
                )
            } else {
                AddTrainingPrompt(onOpen = { showAddTraining = true })
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Training queue", "Filter the list, then search within the current view.")
                    TrainingViewSelector(
                        selected = trainingView,
                        counts = mapOf(
                            TrainingView.Open to openItems.size,
                            TrainingView.Associates to openItems.map { it.associateName.lowercase() }.distinct().size,
                            TrainingView.Overdue to overdue,
                            TrainingView.DueSoon to dueSoon,
                            TrainingView.NoDate to noDate,
                            TrainingView.Completed to state.trainingItems.count { it.completedAt != null },
                            TrainingView.All to state.trainingItems.size
                        ),
                        onSelected = { trainingView = it }
                    )
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search training") },
                        placeholder = { Text("associate name, CBT, food safety") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (filteredItems.isEmpty()) {
            item {
                EmptyState(
                    title = if (state.trainingItems.isEmpty()) "No training imported yet" else "No training matches",
                    body = "Take a photo of the training printout or paste OCR text to build the list."
                )
            }
        } else if (trainingView == TrainingView.Associates) {
            items(associateGroups, key = { it.name }) { group ->
                TrainingAssociateCard(group = group, today = today)
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                TrainingItemCard(
                    item = item,
                    today = today,
                    onToggleComplete = { onToggleComplete(item.id) },
                    onCreateTask = { onCreateTask(item.id) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }
    }
}

@Composable
private fun AddTrainingPrompt(onOpen: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text("Training intake", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Manual entry or printout photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onOpen) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManagerDashboardCard(
    openTrainingCount: Int,
    associateCount: Int,
    overdueCount: Int,
    dueSoonCount: Int,
    onViewOpen: () -> Unit,
    onViewAssociates: () -> Unit,
    onViewOverdue: () -> Unit,
    onViewDueSoon: () -> Unit,
    onCreateFollowUps: () -> Unit
) {
    val attentionCount = overdueCount + dueSoonCount
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Manager dashboard",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onViewOpen, label = { Text("$openTrainingCount open training") })
                AssistChip(onClick = onViewAssociates, label = { Text("$associateCount associates") })
                AssistChip(
                    onClick = onViewOverdue,
                    label = { Text("$overdueCount overdue") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFF5A66).copy(alpha = 0.16f),
                        labelColor = Color(0xFFFF5A66)
                    )
                )
                AssistChip(
                    onClick = onViewDueSoon,
                    label = { Text("$dueSoonCount due soon") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFFB020).copy(alpha = 0.16f),
                        labelColor = Color(0xFFFFB020)
                    )
                )
                AssistChip(
                    onClick = {
                        if (overdueCount > 0) onViewOverdue() else onViewDueSoon()
                    },
                    label = { Text("$attentionCount need attention") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            OutlinedButton(
                onClick = onCreateFollowUps,
                enabled = openTrainingCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add follow-up tasks")
            }
        }
    }
}

private data class TrainingAssociateGroup(
    val name: String,
    val items: List<TrainingItem>
) {
    val openCount: Int = items.count { it.completedAt == null }
    val completedCount: Int = items.count { it.completedAt != null }
    val overdueCount: Int = items.count { it.completedAt == null && it.dueDate?.isBefore(LocalDate.now()) == true }
    val dueSoonCount: Int = items.count {
        it.completedAt == null && it.dueDate?.let { due -> !due.isBefore(LocalDate.now()) && !due.isAfter(LocalDate.now().plusDays(7)) } == true
    }
    val nextItem: TrainingItem? = items
        .filter { it.completedAt == null }
        .minByOrNull { it.dueDate ?: LocalDate.MAX }
    val nextDue: LocalDate? = nextItem?.dueDate
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingAssociateCard(group: TrainingAssociateGroup, today: LocalDate) {
    val statusColor = when {
        group.overdueCount > 0 -> Color(0xFFFF5A66)
        group.dueSoonCount > 0 -> Color(0xFFFFB020)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f))
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(statusColor))
            Column(Modifier.padding(14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                group.nextItem?.let { item ->
                    Text(
                        "Next: ${item.trainingTitle}${item.dueDate?.let { " due ${it.format(dateFormatter)}" }.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("${group.openCount} open") })
                    AssistChip(
                        onClick = {},
                        label = { Text("${group.overdueCount} overdue") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFF5A66).copy(alpha = 0.16f),
                            labelColor = Color(0xFFFF5A66)
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${group.dueSoonCount} due soon") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFFB020).copy(alpha = 0.16f),
                            labelColor = Color(0xFFFFB020)
                        )
                    )
                    if (group.completedCount > 0) AssistChip(onClick = {}, label = { Text("${group.completedCount} done") })
                }
                group.items
                    .filter { it.completedAt == null }
                    .sortedBy { it.dueDate ?: LocalDate.MAX }
                    .take(3)
                    .forEach { item ->
                        Text(
                            "${item.trainingTitle}${item.dueDate?.let { " - ${trainingDueLabel(it, today)}" }.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingViewSelector(
    selected: TrainingView,
    counts: Map<TrainingView, Int>,
    onSelected: (TrainingView) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TrainingView.entries.forEach { view ->
            val label = "${view.label} ${counts[view] ?: 0}"
            if (view == selected) {
                Button(onClick = { onSelected(view) }) { Text(label) }
            } else {
                OutlinedButton(onClick = { onSelected(view) }) { Text(label) }
            }
        }
    }
}

private fun trainingDueLabel(dueDate: LocalDate, today: LocalDate): String = when {
    dueDate.isBefore(today) -> "overdue ${dueDate.format(dateFormatter)}"
    dueDate == today -> "due today"
    dueDate == today.plusDays(1) -> "due tomorrow"
    else -> "due ${dueDate.format(dateFormatter)}"
}

@Composable
private fun AddTrainingCard(
    importState: TrainingImportUiState,
    onAddManual: (String, String, LocalDate?) -> Unit,
    onRecognizeImage: (Uri) -> Unit,
    onTextChanged: (String) -> Unit,
    onImport: () -> Unit
) {
    var mode by remember { mutableStateOf("manual") }
    var associateName by remember { mutableStateOf("") }
    var trainingTitle by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(LocalDate.now().plusDays(7)) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onRecognizeImage)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Add training", "Enter one item or scan a printed training list.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (mode == "manual") {
                    Button(onClick = { mode = "manual" }, modifier = Modifier.weight(1f)) { Text("Manual") }
                } else {
                    OutlinedButton(onClick = { mode = "manual" }, modifier = Modifier.weight(1f)) { Text("Manual") }
                }
                if (mode == "photo") {
                    Button(onClick = { mode = "photo" }, modifier = Modifier.weight(1f)) { Text("Photo") }
                } else {
                    OutlinedButton(onClick = { mode = "photo" }, modifier = Modifier.weight(1f)) { Text("Photo") }
                }
            }
            if (mode == "manual") {
                OutlinedTextField(
                    value = associateName,
                    onValueChange = { associateName = it },
                    label = { Text("Associate name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trainingTitle,
                    onValueChange = { trainingTitle = it },
                    label = { Text("Training title") },
                    placeholder = { Text("CBT Food Safety") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DateOnlyRow(
                    label = "Due date",
                    value = dueDate,
                    onChanged = { dueDate = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { dueDate = null }, modifier = Modifier.weight(1f)) {
                        Text("No date")
                    }
                    Button(
                        onClick = {
                            onAddManual(associateName, trainingTitle, dueDate)
                            associateName = ""
                            trainingTitle = ""
                            dueDate = LocalDate.now().plusDays(7)
                        },
                        enabled = associateName.isNotBlank() && trainingTitle.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (importState.isReadingImage) "Reading photo..." else "Take or choose printout photo")
                }
                OutlinedTextField(
                    value = importState.rawText,
                    onValueChange = onTextChanged,
                    label = { Text("Detected training text") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                if (importState.parsedItems.isNotEmpty()) {
                    Text("${importState.parsedItems.size} rows ready to import", style = MaterialTheme.typography.labelLarge)
                    importState.parsedItems.take(3).forEach { item ->
                        Text(
                            "${item.associateName} - ${item.trainingTitle}${item.dueDate?.let { " - due ${it.format(dateFormatter)}" }.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Button(
                    onClick = onImport,
                    enabled = importState.parsedItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import training rows")
                }
            }
            importState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            importState.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingSummaryCard(
    openCount: Int,
    overdueCount: Int,
    dueSoonCount: Int,
    noDateCount: Int,
    onCreateFollowUps: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Training focus", "Prioritize what needs attention first.")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("$openCount open") })
                AssistChip(
                    onClick = {},
                    label = { Text("$overdueCount overdue") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFF5A66).copy(alpha = 0.16f),
                        labelColor = Color(0xFFFF5A66)
                    )
                )
                AssistChip(
                    onClick = {},
                    label = { Text("$dueSoonCount due 7 days") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFFFB020).copy(alpha = 0.16f),
                        labelColor = Color(0xFFFFB020)
                    )
                )
                AssistChip(onClick = {}, label = { Text("$noDateCount no date") })
            }
            OutlinedButton(
                onClick = onCreateFollowUps,
                enabled = openCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add follow-up tasks")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrainingItemCard(
    item: TrainingItem,
    today: LocalDate,
    onToggleComplete: () -> Unit,
    onCreateTask: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when {
        item.completedAt != null -> Color(0xFF54D17A)
        item.dueDate?.isBefore(today) == true -> Color(0xFFFF5A66)
        item.dueDate?.let { !it.isAfter(today.plusDays(7)) } == true -> Color(0xFFFFB020)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f))
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(statusColor))
            Column(Modifier.padding(14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.completedAt != null, onCheckedChange = { onToggleComplete() })
                    Column(Modifier.weight(1f)) {
                        Text(item.associateName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(item.trainingTitle, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.dueDate?.let {
                        AssistChip(
                            onClick = {},
                            label = { Text("Due ${it.format(dateFormatter)}") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = statusColor.copy(alpha = 0.16f),
                                labelColor = statusColor
                            )
                        )
                    } ?: AssistChip(onClick = {}, label = { Text("No due date") })
                    if (item.completedAt != null) AssistChip(onClick = {}, label = { Text("Done") })
                }
                if (item.sourceText.isNotBlank()) {
                    Text(
                        item.sourceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                OutlinedButton(onClick = onCreateTask, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Make follow-up task")
                }
            }
        }
    }
}

@Composable
private fun NotesScreen(
    state: AppState,
    onAddNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCreateTaskFromNote: (String) -> Unit,
    imageMessage: String?,
    onAddImage: (String, Uri) -> Unit,
    onDeleteImage: (String) -> Unit
) {
    val today = LocalDate.now()
    val todayNotes = state.notes.filter { it.date == today }.sortedByDescending { it.createdAt }
    val recentNotes = state.notes.filterNot { it.date == today }.sortedByDescending { it.createdAt }
    val images = state.images.sortedWith(compareByDescending<WorkImage> { it.date }.thenByDescending { it.createdAt })

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            DailyNotesSection(
                todayNotes = todayNotes,
                recentNotes = recentNotes,
                onAddNote = onAddNote,
                onDeleteNote = onDeleteNote,
                onCreateTaskFromNote = onCreateTaskFromNote
            )
        }
        item {
            WorkImagesSection(
                images = images,
                message = imageMessage,
                onAddImage = onAddImage,
                onDeleteImage = onDeleteImage
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommandCenterCard(
    state: AppState,
    overdueTaskCount: Int,
    todayTaskCount: Int,
    upcomingAlarmCount: Int,
    onAddTask: () -> Unit,
    onAddRepeatingTask: () -> Unit,
    onScheduleShortcut: () -> Unit,
    onImportSchedule: () -> Unit,
    onMarkTodayOff: () -> Unit
) {
    val today = LocalDate.now()
    val now = LocalDateTime.now()
    val todayShifts = state.shifts.filter { it.date == today }.sortedBy { it.start }
    val nextShift = state.shifts
        .filter { shift -> shift.endDateTime().isAfter(now) }
        .minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val todayStatus = today.workStatusLabel(state, todayShifts)
    val todayPay = PayEstimator.estimateDay(state, today)
    val todayTasks = state.tasks.filter { !it.completed && it.deadline?.toLocalDate() == today && !it.isSkippedBecauseDayOff(state, today) }
    val overdueTasks = state.tasks.filter { !it.completed && it.deadline?.isBefore(now) == true }
    val remindersToday = state.tasks.filter { !it.completed && it.alarmAt?.toLocalDate() == today }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Today",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                today.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            DashboardStatusPill(todayStatus)
            NextShiftDashboardCard(
                nextShift = nextShift,
                now = now,
                hasAnySchedule = state.shifts.isNotEmpty() || state.daysOff.isNotEmpty(),
                onImportSchedule = onImportSchedule,
                onScheduleShortcut = onScheduleShortcut
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardMetricChip("${todayTasks.size.coerceAtLeast(todayTaskCount)} work tasks", MaterialTheme.colorScheme.primary)
                DashboardMetricChip("${overdueTasks.size.coerceAtLeast(overdueTaskCount)} overdue", Color(0xFFE45B3C))
                DashboardMetricChip("${remindersToday.size.coerceAtLeast(upcomingAlarmCount)} reminders", Color(0xFFFFB020))
                if (state.paySettings.showPayOnDashboard && state.paySettings.hourlyRate > 0.0 && todayPay.paidHours > 0.0) {
                    DashboardMetricChip("$${todayPay.grossPay.toMoneyString()} today", MaterialTheme.colorScheme.tertiary)
                    DashboardMetricChip("${todayPay.paidHours.toSimpleString()} paid hrs", MaterialTheme.colorScheme.tertiary)
                }
            }
            if (state.paySettings.showPayOnDashboard && state.paySettings.hourlyRate > 0.0) {
                DashboardPayEstimateCard(state)
            }
            DashboardTaskPreview(
                "Today’s work tasks",
                todayTasks,
                if (state.shifts.isNotEmpty() && state.tasks.none { !it.completed }) {
                    "No tasks yet. Try an opening, closing, or truck-day task template."
                } else {
                    "No tasks due today."
                }
            )
            DashboardTaskPreview("Overdue", overdueTasks, "No overdue tasks.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardActionButton("Add shift", Icons.Default.CalendarMonth, onScheduleShortcut, Modifier.weight(1f))
                DashboardActionButton("Add task", Icons.Default.Add, onAddTask, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardActionButton("Import schedule", Icons.Default.FileUpload, onImportSchedule, Modifier.weight(1f), outlined = true)
                DashboardActionButton("Mark day off", Icons.Default.CalendarMonth, onMarkTodayOff, Modifier.weight(1f), outlined = true)
            }
            OutlinedButton(onClick = onAddRepeatingTask, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add repeating task")
            }
        }
    }
}

@Composable
private fun DashboardStatusPill(label: String) {
    val color = when (label) {
        "Workday" -> MaterialTheme.colorScheme.primary
        "Vacation" -> MaterialTheme.colorScheme.tertiary
        "Sick Day" -> Color(0xFFE45B3C)
        "Day Off" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.18f), labelColor = color)
    )
}

@Composable
private fun NextShiftDashboardCard(
    nextShift: WorkShift?,
    now: LocalDateTime,
    hasAnySchedule: Boolean,
    onImportSchedule: () -> Unit,
    onScheduleShortcut: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Next shift", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (nextShift == null) {
                Text(
                    if (hasAnySchedule) "No upcoming shift saved." else "Import a schedule screenshot or add your first shift.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onImportSchedule, modifier = Modifier.weight(1f)) { Text("Import") }
                    OutlinedButton(onClick = onScheduleShortcut, modifier = Modifier.weight(1f)) { Text("Add shift") }
                }
                return@Column
            }
            Text(nextShift.label.ifBlank { "Work" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("${nextShift.start.format(timeFormatter)} - ${nextShift.end.format(timeFormatter)}", style = MaterialTheme.typography.bodyLarge)
            Text("Starts ${nextShift.timeUntilShift(now)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            listOf(nextShift.location, nextShift.notes)
                .filter { it.isNotBlank() }
                .forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun DashboardTaskPreview(title: String, tasks: List<TaskItem>, emptyText: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val visible = tasks.take(3)
            if (visible.isEmpty()) {
                Text(emptyText, style = MaterialTheme.typography.bodyMedium)
            } else {
                visible.forEach { task ->
                    Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                if (tasks.size > visible.size) {
                    Text("+${tasks.size - visible.size} more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DashboardActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    outlined: Boolean = false
) {
    if (outlined) {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    } else {
        Button(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(label)
        }
    }
}

private fun LocalDate.workStatusLabel(state: AppState, todayShifts: List<WorkShift>): String {
    val kind = state.dayOffTypes[this]
    return when {
        kind == ShiftTemplateKind.Vacation -> "Vacation"
        kind == ShiftTemplateKind.Sick -> "Sick Day"
        this in state.daysOff || kind == ShiftTemplateKind.DayOff -> "Day Off"
        todayShifts.isNotEmpty() -> "Workday"
        state.shifts.isEmpty() && state.daysOff.isEmpty() -> "Unknown"
        else -> "Unknown"
    }
}

@Composable
private fun DashboardInfoCard(title: String, value: String, detail: String?, strong: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (strong) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            detail?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DashboardMetricChip(label: String, color: Color) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.16f),
            labelColor = color
        )
    )
}

@Composable
private fun DashboardPayEstimateCard(state: AppState) {
    val week = PayEstimator.estimateWeek(state)
    val period = PayEstimator.estimatePayPeriod(state)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Estimated gross pay before taxes and deductions.", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PayMiniMetric("This week", week, Modifier.weight(1f))
                PayMiniMetric("Pay period", period, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PayMiniMetric(label: String, estimate: PayEstimate, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$${estimate.grossPay.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("${estimate.paidHours.toSimpleString()} paid hrs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun upcomingAlarmCount(state: AppState, now: LocalDateTime): Int {
    return state.tasks.count { task ->
        !task.completed && task.alarmAt?.let { !it.isBefore(now) } == true
    }
}

private fun WorkShift.shiftTimeLabel(): String = "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"

private fun WorkShift.startDateTime(): LocalDateTime = LocalDateTime.of(date, start)

private fun WorkShift.endDateTime(): LocalDateTime {
    val endDateTime = LocalDateTime.of(date, end)
    return if (end.isBefore(start)) endDateTime.plusDays(1) else endDateTime
}

private fun WorkShift.durationMinutes(): Long = Duration.between(startDateTime(), endDateTime()).toMinutes().coerceAtLeast(0)

private fun WorkShift.durationLabel(): String {
    val minutes = durationMinutes()
    val hours = minutes / 60
    val remainder = minutes % 60
    return when {
        hours > 0 && remainder > 0 -> "${hours}h ${remainder}m"
        hours > 0 -> "${hours}h"
        else -> "${remainder}m"
    }
}

private fun WorkShift.timeUntilShift(now: LocalDateTime): String {
    val start = startDateTime()
    return if (now.isBefore(start)) "in ${Duration.between(now, start).toFriendlyDuration()}" else "now"
}

private fun shiftStatusLine(now: LocalDateTime, shift: WorkShift): String {
    val start = shift.startDateTime()
    val end = shift.endDateTime()
    return when {
        now.isBefore(start) -> "Starts in ${Duration.between(now, start).toFriendlyDuration()}"
        now.isBefore(end) -> "In shift, ${Duration.between(now, end).toFriendlyDuration()} left"
        else -> "Shift finished"
    }
}

private fun Duration.toFriendlyDuration(): String {
    val totalMinutes = toMinutes().coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

@Composable
private fun ScheduleRiskSection(risks: List<ScheduleRisk>) {
    if (risks.isEmpty()) return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Watch-outs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            risks.forEach { risk ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(risk.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(risk.detail, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TimecardSection(
    state: AppState,
    onClockIn: () -> Unit,
    onStartLunch: () -> Unit,
    onEndLunch: () -> Unit,
    onClockOut: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    val today = LocalDate.now()
    val entry = state.timecards.firstOrNull { it.date == today }
    val summary = entry?.let { TimecardCalculator.summarize(it, state.paySettings) }
    val weekSummary = TimecardCalculator.summarizeWeek(state, today)
    val scheduled = PayEstimator.estimateDay(state, today)
    var noteText by remember(entry?.id, entry?.note) { mutableStateOf(entry?.note.orEmpty()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Personal timecard", "Track what you actually worked for your own records.")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onClockIn, enabled = entry?.clockIn == null) { Text("Clock in") }
                OutlinedButton(onClick = onStartLunch, enabled = entry?.clockIn != null && entry.lunchStart == null && entry.clockOut == null) {
                    Text("Lunch start")
                }
                OutlinedButton(onClick = onEndLunch, enabled = entry?.lunchStart != null && entry.lunchEnd == null && entry.clockOut == null) {
                    Text("Lunch end")
                }
                Button(onClick = onClockOut, enabled = entry?.clockIn != null && entry.clockOut == null) { Text("Clock out") }
            }
            TimePunchRows(entry)
            if (summary != null) {
                Text(
                    "Actual today: ${summary.paidHours.toSimpleString()} paid hrs" +
                        if (state.paySettings.hourlyRate > 0.0) " / $${summary.grossPay.toMoneyString()}" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (scheduled.paidHours > 0.0) {
                    val delta = summary.paidHours - scheduled.paidHours
                    Text(
                        "Scheduled estimate: ${scheduled.paidHours.toSimpleString()} paid hrs (${delta.toSignedHours()} vs actual)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text("No actual time logged today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Actual this week: ${weekSummary.paidHours.toSimpleString()} paid hrs" +
                    if (state.paySettings.hourlyRate > 0.0) " / $${weekSummary.grossPay.toMoneyString()}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Missed punch or pay note") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(onClick = { onSaveNote(noteText) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save timecard note")
            }
        }
    }
}

@Composable
private fun TimePunchRows(entry: TimecardEntry?) {
    val rows = listOf(
        "In" to entry?.clockIn,
        "Lunch start" to entry?.lunchStart,
        "Lunch end" to entry?.lunchEnd,
        "Out" to entry?.clockOut
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { (label, value) ->
            AssistChip(onClick = {}, label = { Text("$label: ${value?.format(timeFormatter) ?: "--"}") })
        }
    }
}

private fun Double.toSignedHours(): String {
    return when {
        this > 0.0 -> "+${toSimpleString()} hrs"
        this < 0.0 -> "${toSimpleString()} hrs"
        else -> "even"
    }
}

@Composable
private fun ChecklistTemplateSection(state: AppState, onAddChecklist: (String) -> Unit, onOpenPremium: () -> Unit) {
    val unlocked = PremiumAccess.canUse(state, PremiumFeature.TaskTemplates)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Checklist templates", "Add a work routine to today's tasks.")
            if (!unlocked) {
                PremiumLockedInline(PremiumFeature.TaskTemplates, "Free tasks stay available. Premium unlocks reusable checklist templates.", onOpenPremium)
                return@Column
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkChecklistTemplates.all.forEach { template ->
                    OutlinedButton(onClick = { onAddChecklist(template.id) }) {
                        Text(template.title)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkImagesSection(
    images: List<WorkImage>,
    message: String?,
    onAddImage: (String, Uri) -> Unit,
    onDeleteImage: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            onAddImage(title, it)
            title = ""
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Work images", "Save reference photos and search labels or detected text.")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Image label") },
                placeholder = { Text("Fresh Slice plannogram") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add work image")
            }
            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search images") },
                placeholder = { Text("plannogram, fresh slice, deli") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val filteredImages = images.filter { image ->
                searchText.isBlank() ||
                    image.title.contains(searchText, ignoreCase = true) ||
                    image.detectedText.contains(searchText, ignoreCase = true) ||
                    image.tags.any { it.contains(searchText, ignoreCase = true) }
            }

            if (filteredImages.isEmpty()) {
                Text(
                    if (images.isEmpty()) "No work images saved yet." else "No images match this search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredImages.forEach { image ->
                    WorkImageCard(image = image, onDelete = { onDeleteImage(image.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkImageCard(image: WorkImage, onDelete: () -> Unit) {
    val bitmap = remember(image.imagePath) { decodeWorkImage(image.imagePath) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = image.title,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(image.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(image.date.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
            if (image.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    image.tags.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
                }
            }
            if (image.detectedText.isNotBlank()) {
                Text(
                    image.detectedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun decodeWorkImage(path: String): android.graphics.Bitmap? {
    if (!File(path).exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    val maxSide = 900
    var sampleSize = 1
    while ((bounds.outWidth / sampleSize) > maxSide || (bounds.outHeight / sampleSize) > maxSide) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DailyNotesSection(
    todayNotes: List<WorkNote>,
    recentNotes: List<WorkNote>,
    onAddNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCreateTaskFromNote: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf<WorkNoteKind?>(null) }
    var searchText by remember { mutableStateOf("") }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Daily work notes", "Smart tags organize notes automatically on this device.")
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Add a note from today") },
                placeholder = { Text("Example: Frozen order short 2 cases, follow up tomorrow") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                enabled = noteText.isNotBlank(),
                onClick = {
                    onAddNote(noteText)
                    noteText = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save note")
            }
            val notesToShow = todayNotes + recentNotes
            val issueCount = notesToShow.count { it.kind == WorkNoteKind.Issue }
            val followUpCount = notesToShow.count { it.kind == WorkNoteKind.FollowUp }
            val orderCount = notesToShow.count { it.kind == WorkNoteKind.Order }
            if (notesToShow.isNotEmpty()) {
                Text(
                    buildList {
                        if (issueCount > 0) add("$issueCount issue${if (issueCount == 1) "" else "s"}")
                        if (followUpCount > 0) add("$followUpCount follow-up${if (followUpCount == 1) "" else "s"}")
                        if (orderCount > 0) add("$orderCount order note${if (orderCount == 1) "" else "s"}")
                    }.ifEmpty { listOf("${notesToShow.size} organized note${if (notesToShow.size == 1) "" else "s"}") }
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search notes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val filterKinds = notesToShow.map { it.kind }.distinct().sortedBy { it.label }
            if (filterKinds.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedKind == null) {
                        Button(onClick = { selectedKind = null }) { Text("All") }
                    } else {
                        OutlinedButton(onClick = { selectedKind = null }) { Text("All") }
                    }
                    filterKinds.forEach { kind ->
                        if (selectedKind == kind) {
                            Button(onClick = { selectedKind = null }) { Text(kind.label) }
                        } else {
                            OutlinedButton(onClick = { selectedKind = kind }) { Text(kind.label) }
                        }
                    }
                }
            }
            val filteredByKind = selectedKind?.let { kind -> notesToShow.filter { it.kind == kind } } ?: notesToShow
            val filteredNotes = filteredByKind.filter { note ->
                searchText.isBlank() ||
                    note.text.contains(searchText, ignoreCase = true) ||
                    note.kind.label.contains(searchText, ignoreCase = true) ||
                    note.tags.any { it.contains(searchText, ignoreCase = true) }
            }
            if (filteredNotes.isEmpty()) {
                Text(
                    if (notesToShow.isEmpty()) "No notes yet today." else "No notes match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                filteredNotes.forEach { note ->
                    WorkNoteCard(
                        note = note,
                        onMakeTask = { onCreateTaskFromNote(note.id) },
                        onDelete = { onDeleteNote(note.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkNoteCard(note: WorkNote, onMakeTask: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(note.kind.label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(note.date.format(dateFormatter), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(note.text, style = MaterialTheme.typography.bodyMedium)
            if (note.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    note.tags.forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onMakeTask, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Make task")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: WorkEvent, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${event.startsAt.format(dateTimeFormatter)} - ${event.endsAt.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
            if (event.location.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Text(event.location, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=${Uri.encode(event.location)}")
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Navigate")
                    }
                }
            }
            if (event.notes.isNotBlank()) Text(event.notes, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskCard(
    task: TaskItem,
    scheduleInsight: TaskScheduleInsight,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = task.priority.priorityColor()
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (task.priority == TaskPriority.Normal) MaterialTheme.colorScheme.surfaceVariant else priorityColor.copy(alpha = 0.65f)
        )
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )
            Column(Modifier.padding(14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.completed, onCheckedChange = { onToggleComplete() })
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
                if (task.notes.isNotBlank()) Text(task.notes, style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (task.priority != TaskPriority.Normal) {
                        AssistChip(
                            onClick = {},
                            label = { Text(task.priority.label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = priorityColor.copy(alpha = 0.18f),
                                labelColor = priorityColor
                            )
                        )
                    }
                    if (task.category != TaskCategory.General) {
                        val categoryColor = task.category.categoryColor()
                        AssistChip(
                            onClick = {},
                            label = { Text(task.category.label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = categoryColor.copy(alpha = 0.16f),
                                labelColor = categoryColor
                            )
                        )
                    }
                    task.deadline?.let {
                        val deadlineColor = it.deadlineColor()
                        AssistChip(
                            onClick = {},
                            label = { Text(it.format(dateTimeFormatter)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = deadlineColor.copy(alpha = 0.14f),
                                labelColor = deadlineColor
                            )
                        )
                    }
                    if (task.repeatRule != RepeatRule.None) AssistChip(onClick = {}, label = { Text(task.repeatLabel()) })
                    scheduleInsight.labels.forEach { label ->
                        val color = label.scheduleLabelColor()
                        AssistChip(
                            onClick = {},
                            label = { Text(label.label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = color.copy(alpha = 0.14f),
                                labelColor = color
                            )
                        )
                    }
                }
                scheduleInsight.warning?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFB020), fontWeight = FontWeight.SemiBold)
                }
                scheduleInsight.suggestion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TaskDetailScreen(
    state: AppState,
    task: TaskItem?,
    onSave: (TaskItem) -> Unit,
    onSaveAndContinue: (TaskItem) -> Unit,
    onSaveTaskTemplate: (TaskTemplate) -> Unit,
    onDeleteTaskTemplate: (String) -> Unit,
    onNotificationPermissionNeeded: () -> Unit,
    onOpenPremium: () -> Unit,
    onCancel: () -> Unit
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var notes by remember(task?.id) { mutableStateOf(task?.notes.orEmpty()) }
    var category by remember(task?.id) { mutableStateOf(task?.category ?: TaskCategory.General) }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: TaskPriority.Normal) }
    var deadline by remember(task?.id) { mutableStateOf(task?.deadline ?: LocalDateTime.now().plusHours(4)) }
    var alarmAt by remember(task?.id) { mutableStateOf(task?.alarmAt ?: deadline.minusMinutes(30)) }
    var reminderEnabled by remember(task?.id) { mutableStateOf(task?.alarmAt != null) }
    var repeatRule by remember(task?.id) { mutableStateOf(task?.repeatRule ?: RepeatRule.None) }
    var repeatDays by remember(task?.id) { mutableStateOf(task?.repeatDays ?: emptySet()) }
    var skipDaysOff by remember(task?.id) { mutableStateOf(task?.skipDaysOff ?: true) }
    var workRelated by remember(task?.id) { mutableStateOf(task?.workRelated ?: true) }
    var linkedShiftId by remember(task?.id) { mutableStateOf(task?.linkedShiftId) }
    var linkedShiftType by remember(task?.id) { mutableStateOf(task?.linkedShiftType ?: LinkedShiftType.Any) }
    var timingRule by remember(task?.id) { mutableStateOf(task?.timingRule ?: TaskTimingRule.AtTime) }
    var carryOverBehavior by remember(task?.id) { mutableStateOf(task?.carryOverBehavior ?: CarryOverBehavior.None) }
    var alarmOffsetMinutes by remember(task?.id) { mutableStateOf((task?.alarmOffsetMinutes ?: 30).toString()) }
    var completed by remember(task?.id) { mutableStateOf(task?.completed ?: false) }
    var showAdvancedRepeat by remember(task?.id) { mutableStateOf(task?.repeatRule == RepeatRule.CustomDays) }
    var showMoreRules by remember(task?.id) { mutableStateOf(false) }
    val advancedRulesUnlocked = PremiumAccess.canUse(state, PremiumFeature.AdvancedTaskRules)
    val templatesUnlocked = PremiumAccess.canUse(state, PremiumFeature.TaskTemplates)
    val context = LocalContext.current
    val nextShift = remember(state.shifts) {
        val now = LocalDateTime.now()
        state.shifts
            .map { shift -> shift.date.atTime(shift.start) }
            .filter { it.isAfter(now) }
            .minOrNull()
    }
    fun resetForAnother() {
        title = ""
        notes = ""
        category = TaskCategory.General
        priority = TaskPriority.Normal
        val base = LocalDateTime.now().plusHours(4)
        deadline = base
        alarmAt = base.minusMinutes(30)
        reminderEnabled = false
        repeatRule = RepeatRule.None
        repeatDays = emptySet()
        skipDaysOff = true
        workRelated = true
        linkedShiftId = null
        linkedShiftType = LinkedShiftType.Any
        timingRule = TaskTimingRule.AtTime
        carryOverBehavior = CarryOverBehavior.None
        alarmOffsetMinutes = "30"
        completed = false
        showAdvancedRepeat = false
    }
    fun buildTask(id: String = task?.id ?: java.util.UUID.randomUUID().toString()): TaskItem {
        return TaskItem(
            id = id,
            title = title.trim(),
            notes = notes.trim(),
            category = category,
            priority = priority,
            deadline = deadline,
            alarmAt = alarmAt.takeIf { reminderEnabled },
            repeatRule = repeatRule,
            repeatDays = if (repeatRule == RepeatRule.CustomDays) repeatDays else emptySet(),
            skipDaysOff = skipDaysOff,
            workRelated = workRelated,
            linkedShiftId = linkedShiftId,
            linkedShiftType = linkedShiftType,
            timingRule = timingRule,
            carryOverBehavior = carryOverBehavior,
            alarmOffsetMinutes = alarmOffsetMinutes.toLongOrNull()?.coerceAtLeast(0) ?: 30,
            completed = completed
        )
    }
    fun requestReminderPermissionIfNeeded() {
        if (
            reminderEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            onNotificationPermissionNeeded()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(task?.let { "Edit task" } ?: "Add task", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        TaskTemplateChips(
            templates = taskTemplatesFor(state),
            onApply = { template ->
                if (!templatesUnlocked) {
                    onOpenPremium()
                    return@TaskTemplateChips
                }
                title = template.title
                notes = template.notes
                category = template.category
                priority = template.priority
                repeatRule = template.repeatRule
                repeatDays = emptySet()
                workRelated = template.workRelated
                reminderEnabled = template.reminderEnabled
                linkedShiftType = template.linkedShiftType
                timingRule = template.timingRule
                carryOverBehavior = template.carryOverBehavior
                alarmOffsetMinutes = template.alarmOffsetMinutes.toString()
            },
            onSaveTemplate = onSaveTaskTemplate,
            onDeleteTemplate = onDeleteTaskTemplate,
            premiumUnlocked = templatesUnlocked,
            onOpenPremium = onOpenPremium
        )
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                TaskCategorySelector(selected = category, onSelected = { category = it })
                TaskPrioritySelector(selected = priority, onSelected = { priority = it })
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Work-related", style = MaterialTheme.typography.bodyLarge)
                        Text("Use this for shift planning and work task labels.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = workRelated, onCheckedChange = { workRelated = it })
                }
            }
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("When", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TaskTimingRuleChips(selected = timingRule, onSelected = { selected ->
                    timingRule = selected
                    reminderEnabled = true
                    when (selected) {
                        TaskTimingRule.AtTime -> Unit
                        TaskTimingRule.BeforeNextShift -> nextShift?.let {
                            deadline = it.minusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                            alarmAt = deadline
                        }
                        TaskTimingRule.DuringShift -> nextShift?.let {
                            deadline = it.plusMinutes(30)
                            alarmAt = deadline
                        }
                        TaskTimingRule.AfterShift -> {
                            val shift = state.shifts.filter { it.date.atTime(it.start).isAfter(LocalDateTime.now()) }.minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
                            shift?.let {
                                val end = it.date.atTime(it.end).let { endTime -> if (it.end.isBefore(it.start)) endTime.plusDays(1) else endTime }
                                deadline = end.plusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                                alarmAt = deadline
                            }
                        }
                        TaskTimingRule.WorkdaysOnly -> skipDaysOff = true
                    }
                })
                if (timingRule == TaskTimingRule.AtTime || timingRule == TaskTimingRule.WorkdaysOnly) {
                    DateTimeRow("Deadline", deadline, onChanged = { deadline = it })
                } else {
                    Text("The exact reminder time will follow your saved shift schedule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Reminder", style = MaterialTheme.typography.bodyLarge)
                        Text("Workday Planner uses reminders only for tasks you create.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("You can turn reminders off anytime.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (reminderEnabled) {
                    if (timingRule == TaskTimingRule.AtTime || timingRule == TaskTimingRule.WorkdaysOnly) {
                        DateTimeRow("Reminder time", alarmAt, onChanged = { alarmAt = it })
                    }
                    OutlinedTextField(
                        value = alarmOffsetMinutes,
                        onValueChange = { alarmOffsetMinutes = it.filter(Char::isDigit).take(4) },
                        label = { Text("Alarm offset minutes") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("More rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showMoreRules = !showMoreRules }) { Text(if (showMoreRules) "Hide" else "Show") }
                }
                if (!showMoreRules) {
                    Text("Repeat, shift type, day-off, and carry-over rules.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (showMoreRules) {
                    if (!advancedRulesUnlocked) {
                        PremiumLockedInline(PremiumFeature.AdvancedTaskRules, "Basic deadlines and reminders stay free. Premium unlocks shift-aware repeat and carry-over rules.", onOpenPremium)
                        return@Column
                    }
                Text("Repeat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                RepeatRuleChips(selected = repeatRule, onSelected = {
                    repeatRule = it
                    if (it != RepeatRule.CustomDays) repeatDays = emptySet()
                    linkedShiftType = when (it) {
                        RepeatRule.OpeningShifts -> LinkedShiftType.Opening
                        RepeatRule.ClosingShifts -> LinkedShiftType.Closing
                        RepeatRule.TruckDays -> LinkedShiftType.Truck
                        else -> linkedShiftType
                    }
                })
                if (showAdvancedRepeat || repeatRule == RepeatRule.CustomDays) {
                    CustomRepeatDays(
                        selectedDays = repeatDays,
                        onToggle = { day ->
                            repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                        }
                    )
                } else {
                    TextButton(onClick = {
                        showAdvancedRepeat = true
                        repeatRule = RepeatRule.CustomDays
                    }) { Text("Choose specific days") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = skipDaysOff, onCheckedChange = { skipDaysOff = it })
                    Text("Skip days off")
                }
                LinkedShiftTypeChips(selected = linkedShiftType, onSelected = { linkedShiftType = it })
                CarryOverChips(selected = carryOverBehavior, onSelected = { carryOverBehavior = it })
                }
            }
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Shift link", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ShiftLinkChips(
                    shifts = state.shifts,
                    selectedShiftId = linkedShiftId,
                    onSelected = { linkedShiftId = it }
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Completed", style = MaterialTheme.typography.bodyLarge)
                        Text("Mark done if you are editing an existing task.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Checkbox(checked = completed, onCheckedChange = { completed = it })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = title.isNotBlank() && (repeatRule != RepeatRule.CustomDays || repeatDays.isNotEmpty()),
                onClick = {
                    requestReminderPermissionIfNeeded()
                    onSave(buildTask())
                },
                modifier = Modifier.weight(1f)
            ) { Text("Save task") }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
        }
        if (task == null) {
            OutlinedButton(
                enabled = title.isNotBlank() && (repeatRule != RepeatRule.CustomDays || repeatDays.isNotEmpty()),
                onClick = {
                    requestReminderPermissionIfNeeded()
                    onSaveAndContinue(buildTask(java.util.UUID.randomUUID().toString()))
                    resetForAnother()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save and add another")
            }
        }
    }
}

private val builtInTaskTemplates = listOf(
    TaskTemplate(id = "builtin-opening-checklist", name = "Opening shift checklist", title = "Opening shift checklist", notes = "Unlock/setup, check schedule, prep station, and note issues.", category = TaskCategory.Prep, priority = TaskPriority.High, repeatRule = RepeatRule.OpeningShifts, linkedShiftType = LinkedShiftType.Opening, timingRule = TaskTimingRule.BeforeNextShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-closing-checklist", name = "Closing shift checklist", title = "Closing shift checklist", notes = "Clean area, finish closing tasks, and leave notes for next shift.", category = TaskCategory.Cleaning, priority = TaskPriority.High, repeatRule = RepeatRule.ClosingShifts, linkedShiftType = LinkedShiftType.Closing, timingRule = TaskTimingRule.DuringShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-truck-checklist", name = "Truck/order day checklist", title = "Truck/order day checklist", notes = "Check order, truck notes, inventory gaps, and follow-up items.", category = TaskCategory.Orders, priority = TaskPriority.High, repeatRule = RepeatRule.TruckDays, linkedShiftType = LinkedShiftType.Truck, timingRule = TaskTimingRule.BeforeNextShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-inventory-checklist", name = "Inventory day checklist", title = "Inventory day checklist", notes = "Counts, outs, order review, and shrink notes.", category = TaskCategory.Orders, priority = TaskPriority.High, linkedShiftType = LinkedShiftType.Inventory, timingRule = TaskTimingRule.BeforeNextShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-manager-handoff", name = "Manager handoff checklist", title = "Manager handoff checklist", notes = "Open issues, unfinished tasks, associate follow-up, and schedule notes.", category = TaskCategory.Admin, priority = TaskPriority.High, timingRule = TaskTimingRule.AfterShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-pre-work", name = "Personal pre-work checklist", title = "Personal pre-work checklist", notes = "Uniform, lunch, keys/wallet, and anything needed before leaving.", category = TaskCategory.Personal, priority = TaskPriority.Normal, repeatRule = RepeatRule.EveryWorkday, timingRule = TaskTimingRule.BeforeNextShift, linkedShiftType = LinkedShiftType.Any, builtIn = true),
    TaskTemplate(id = "builtin-bring-uniform", name = "Bring uniform", title = "Bring uniform", category = TaskCategory.Personal, repeatRule = RepeatRule.EveryWorkday, timingRule = TaskTimingRule.BeforeNextShift, builtIn = true),
    TaskTemplate(id = "builtin-pack-lunch", name = "Pack lunch", title = "Pack lunch", category = TaskCategory.Personal, priority = TaskPriority.Low, repeatRule = RepeatRule.EveryWorkday, timingRule = TaskTimingRule.BeforeNextShift, builtIn = true),
    TaskTemplate(id = "builtin-check-schedule", name = "Check schedule", title = "Check schedule", category = TaskCategory.Admin, repeatRule = RepeatRule.Weekdays, builtIn = true),
    TaskTemplate(id = "builtin-request-day-off", name = "Request day off", title = "Submit time off request", category = TaskCategory.Admin, priority = TaskPriority.High, builtIn = true),
    TaskTemplate(id = "builtin-paycheck-check", name = "Paycheck check", title = "Check paycheck", category = TaskCategory.Admin, repeatRule = RepeatRule.Weekly, builtIn = true),
    TaskTemplate(id = "builtin-inventory-order", name = "Inventory/order reminder", title = "Inventory/order task", category = TaskCategory.Orders, priority = TaskPriority.High, builtIn = true)
)

private fun taskTemplatesFor(state: AppState): List<TaskTemplate> = builtInTaskTemplates + state.taskTemplates

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskTemplateChips(
    templates: List<TaskTemplate>,
    onApply: (TaskTemplate) -> Unit,
    onSaveTemplate: (TaskTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    premiumUnlocked: Boolean,
    onOpenPremium: () -> Unit
) {
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TaskTemplate?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Task templates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                if (!premiumUnlocked) {
                    onOpenPremium()
                    return@TextButton
                }
                editing = null
                editorOpen = !editorOpen
            }) { Text(if (editorOpen) "Hide" else "Create") }
        }
        if (!premiumUnlocked) {
            PremiumLockedInline(PremiumFeature.TaskTemplates, "Basic task entry stays free. Premium unlocks reusable templates.", onOpenPremium)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            templates.forEach { template ->
                OutlinedButton(onClick = { onApply(template) }) {
                    Text(template.name)
                }
            }
        }
        templates.filterNot { it.builtIn }.forEach { template ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(template.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = {
                    editing = template
                    editorOpen = true
                }) { Text("Edit") }
                TextButton(onClick = { onDeleteTemplate(template.id) }) { Text("Delete") }
            }
        }
        if (editorOpen) {
            TaskTemplateEditor(
                template = editing,
                onSave = {
                    onSaveTemplate(it)
                    editorOpen = false
                    editing = null
                }
            )
        }
    }
}

@Composable
private fun TaskTemplateEditor(template: TaskTemplate?, onSave: (TaskTemplate) -> Unit) {
    var name by remember(template?.id) { mutableStateOf(template?.name.orEmpty()) }
    var title by remember(template?.id) { mutableStateOf(template?.title.orEmpty()) }
    var notes by remember(template?.id) { mutableStateOf(template?.notes.orEmpty()) }
    var category by remember(template?.id) { mutableStateOf(template?.category ?: TaskCategory.General) }
    var priority by remember(template?.id) { mutableStateOf(template?.priority ?: TaskPriority.Normal) }
    var repeatRule by remember(template?.id) { mutableStateOf(template?.repeatRule ?: RepeatRule.None) }
    var reminderEnabled by remember(template?.id) { mutableStateOf(template?.reminderEnabled ?: false) }
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template?.let { "Edit task template" } ?: "Create task template", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Template name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            TaskCategorySelector(selected = category, onSelected = { category = it })
            TaskPrioritySelector(selected = priority, onSelected = { priority = it })
            RepeatRuleChips(selected = repeatRule, onSelected = { repeatRule = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                Text("Reminder on when applied")
            }
            Button(
                enabled = name.isNotBlank() && title.isNotBlank(),
                onClick = {
                    onSave(
                        TaskTemplate(
                            id = template?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            title = title.trim(),
                            notes = notes.trim(),
                            category = category,
                            priority = priority,
                            repeatRule = repeatRule,
                            reminderEnabled = reminderEnabled
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save template") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickTimingChips(
    onBeforeNextShift: (() -> Unit)?,
    onToday: () -> Unit,
    onTomorrow: () -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        onBeforeNextShift?.let {
            OutlinedButton(onClick = it) { Text("Before next shift") }
        }
        OutlinedButton(onClick = onToday) { Text("Today") }
        OutlinedButton(onClick = onTomorrow) { Text("Tomorrow") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskTimingRuleChips(selected: TaskTimingRule, onSelected: (TaskTimingRule) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskTimingRule.entries.forEach { rule ->
            FilterChip(
                selected = selected == rule,
                onClick = { onSelected(rule) },
                label = { Text(rule.label) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkedShiftTypeChips(selected: LinkedShiftType, onSelected: (LinkedShiftType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Linked shift type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinkedShiftType.entries.forEach { type ->
                FilterChip(selected = selected == type, onClick = { onSelected(type) }, label = { Text(type.label) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CarryOverChips(selected: CarryOverBehavior, onSelected: (CarryOverBehavior) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Unfinished work tasks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CarryOverBehavior.entries.forEach { behavior ->
                FilterChip(selected = selected == behavior, onClick = { onSelected(behavior) }, label = { Text(behavior.label) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepeatRuleChips(selected: RepeatRule, onSelected: (RepeatRule) -> Unit) {
    val rules = listOf(
        RepeatRule.None,
        RepeatRule.Daily,
        RepeatRule.Weekdays,
        RepeatRule.Weekly,
        RepeatRule.EveryWorkday,
        RepeatRule.OpeningShifts,
        RepeatRule.ClosingShifts,
        RepeatRule.TruckDays
    ) +
        listOfNotNull(RepeatRule.CustomDays.takeIf { selected == RepeatRule.CustomDays })
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rules.forEach { rule ->
            if (rule == selected) {
                Button(onClick = { onSelected(rule) }) { Text(rule.displayName()) }
            } else {
                OutlinedButton(onClick = { onSelected(rule) }) { Text(rule.displayName()) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftLinkChips(
    shifts: List<WorkShift>,
    selectedShiftId: String?,
    onSelected: (String?) -> Unit
) {
    val upcoming = shifts
        .filter { !it.date.isBefore(LocalDate.now().minusDays(1)) }
        .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
        .take(6)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selectedShiftId == null) {
                Button(onClick = { onSelected(null) }) { Text("No linked shift") }
            } else {
                OutlinedButton(onClick = { onSelected(null) }) { Text("No linked shift") }
            }
            upcoming.forEach { shift ->
                if (shift.id == selectedShiftId) {
                    Button(onClick = { onSelected(shift.id) }) { Text(shift.shortShiftLabel()) }
                } else {
                    OutlinedButton(onClick = { onSelected(shift.id) }) { Text(shift.shortShiftLabel()) }
                }
            }
        }
        if (upcoming.isEmpty()) {
            Text("Import a schedule to link tasks to a shift.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun WorkShift.shortShiftLabel(): String {
    return "${date.format(shortDateFormatter)} ${start.format(timeFormatter)}"
}

@Composable
private fun EventDetailScreen(event: WorkEvent?, onSave: (WorkEvent) -> Unit, onCancel: () -> Unit) {
    var title by remember(event?.id) { mutableStateOf(event?.title.orEmpty()) }
    var notes by remember(event?.id) { mutableStateOf(event?.notes.orEmpty()) }
    var location by remember(event?.id) { mutableStateOf(event?.location.orEmpty()) }
    var startsAt by remember(event?.id) { mutableStateOf(event?.startsAt ?: LocalDateTime.now().plusHours(1)) }
    var endsAt by remember(event?.id) { mutableStateOf(event?.endsAt ?: startsAt.plusHours(1)) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        DateTimeRow("Starts", startsAt, onChanged = {
            val duration = java.time.Duration.between(startsAt, endsAt)
            startsAt = it
            endsAt = it.plus(duration.takeIf { d -> !d.isNegative && !d.isZero } ?: java.time.Duration.ofHours(1))
        })
        DateTimeRow("Ends", endsAt, onChanged = { endsAt = it })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = title.isNotBlank() && endsAt.isAfter(startsAt),
                onClick = {
                    onSave(
                        WorkEvent(
                            id = event?.id ?: java.util.UUID.randomUUID().toString(),
                            title = title.trim(),
                            notes = notes.trim(),
                            location = location.trim(),
                            startsAt = startsAt,
                            endsAt = endsAt
                        )
                    )
                }
            ) { Text("Save event") }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskCategorySelector(selected: TaskCategory, onSelected: (TaskCategory) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskCategory.entries.forEach { category ->
                val color = category.categoryColor()
                if (category == selected) {
                    Button(
                        onClick = { onSelected(category) },
                        colors = ButtonDefaults.buttonColors(containerColor = color)
                    ) {
                        Text(category.label)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(category) },
                        border = BorderStroke(1.dp, color.copy(alpha = 0.65f))
                    ) {
                        Text(category.label, color = color)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskPrioritySelector(selected: TaskPriority, onSelected: (TaskPriority) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Importance", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskPriority.entries.forEach { priority ->
                val color = priority.priorityColor()
                if (priority == selected) {
                    Button(onClick = { onSelected(priority) }) { Text(priority.label) }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(priority) },
                        border = BorderStroke(1.dp, color.copy(alpha = 0.65f))
                    ) {
                        Text(priority.label, color = color)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomRepeatDays(
    selectedDays: Set<DayOfWeek>,
    onToggle: (DayOfWeek) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Repeat on specific days", style = MaterialTheme.typography.labelLarge)
        Text(
            "Choose the weekdays this task should come back.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DayOfWeek.entries.forEach { day ->
                val selected = day in selectedDays
                if (selected) {
                    Button(onClick = { onToggle(day) }) { Text(day.shortLabel()) }
                } else {
                    OutlinedButton(onClick = { onToggle(day) }) { Text(day.shortLabel()) }
                }
            }
        }
    }
}

@Composable
private fun TaskPriority.priorityColor(): Color = when (this) {
    TaskPriority.Low -> Color(0xFF7E8798)
    TaskPriority.Normal -> MaterialTheme.colorScheme.outline
    TaskPriority.High -> Color(0xFFFFB020)
    TaskPriority.Critical -> Color(0xFFFF5A66)
}

@Composable
private fun TaskCategory.categoryColor(): Color = when (this) {
    TaskCategory.General -> MaterialTheme.colorScheme.primary
    TaskCategory.Orders -> Color(0xFF4DB6FF)
    TaskCategory.Cleaning -> Color(0xFF54D17A)
    TaskCategory.Prep -> Color(0xFFFFB020)
    TaskCategory.Admin -> Color(0xFFB18CFF)
    TaskCategory.Personal -> Color(0xFFFF7AB6)
}

@Composable
private fun TaskScheduleLabel.scheduleLabelColor(): Color = when (this) {
    TaskScheduleLabel.BeforeWork -> MaterialTheme.colorScheme.primary
    TaskScheduleLabel.AfterWork -> MaterialTheme.colorScheme.secondary
    TaskScheduleLabel.DayOffTask -> Color(0xFF7E8798)
    TaskScheduleLabel.DueDuringShift -> Color(0xFFFFB020)
    TaskScheduleLabel.QuickTask -> MaterialTheme.colorScheme.outline
    TaskScheduleLabel.Overdue -> Color(0xFFFF5A66)
    TaskScheduleLabel.RepeatsNextWorkday -> Color(0xFF54D17A)
    TaskScheduleLabel.SkippedDayOff -> Color(0xFFFFB020)
}

@Composable
private fun LocalDateTime.deadlineColor(): Color {
    val now = LocalDateTime.now()
    return when {
        isBefore(now) -> Color(0xFFFF5A66)
        isBefore(now.plusHours(12)) -> Color(0xFFFFB020)
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun DateTimeRow(label: String, value: LocalDateTime, onChanged: (LocalDateTime) -> Unit) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value.format(dateTimeFormatter), style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedButton(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    onChanged(value.withYear(year).withMonth(month + 1).withDayOfMonth(day))
                },
                value.year,
                value.monthValue - 1,
                value.dayOfMonth
            ).show()
        }) { Text("Date") }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> onChanged(value.withHour(hour).withMinute(minute)) },
                value.hour,
                value.minute,
                false
            ).show()
        }) { Text("Time") }
    }
}

@Composable
private fun DateOnlyRow(label: String, value: LocalDate?, onChanged: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val pickerDate = value ?: LocalDate.now()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value?.format(dateFormatter) ?: "No due date", style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedButton(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    onChanged(LocalDate.of(year, month + 1, day))
                },
                pickerDate.year,
                pickerDate.monthValue - 1,
                pickerDate.dayOfMonth
            ).show()
        }) {
            Text("Date")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatRuleDropdown(selected: RepeatRule, onSelected: (RepeatRule) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Repeat") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RepeatRule.entries.forEach { rule ->
                DropdownMenuItem(
                    text = { Text(rule.displayName()) },
                    onClick = {
                        onSelected(rule)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScheduleHero(state: AppState) {
    val nextShift = state.shifts
        .filter { !it.date.isBefore(LocalDate.now()) }
        .minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Work calendar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                nextShift?.let {
                    "Next shift: ${it.date.format(dateFormatter)} at ${it.start.format(timeFormatter)}"
                } ?: "No upcoming shift imported",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${state.shifts.size} shifts") })
                AssistChip(onClick = {}, label = { Text("${state.daysOff.size} days off") })
            }
        }
    }
}

@Composable
private fun StyleSection(
    state: AppState,
    onDarkModeChanged: (Boolean) -> Unit,
    onAccentStyleChanged: (AccentStyle) -> Unit,
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit,
    onOpenPremium: () -> Unit
) {
    val themeUnlocked = PremiumAccess.canUse(state, PremiumFeature.ThemeCustomization)
    val widgetUnlocked = PremiumAccess.canUse(state, PremiumFeature.Widgets)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Current theme: ${state.accentStyle.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Dark mode", style = MaterialTheme.typography.bodyLarge)
                    Text("App and widgets use the same mood.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.darkMode, onCheckedChange = onDarkModeChanged)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AccentStyle.entries.forEach { style ->
                    val canSelect = themeUnlocked || style == state.accentStyle
                    if (state.accentStyle == style) {
                        Button(onClick = { onAccentStyleChanged(style) }, modifier = Modifier.weight(1f)) {
                            Text(style.label)
                        }
                    } else {
                        OutlinedButton(onClick = { if (canSelect) onAccentStyleChanged(style) else onOpenPremium() }, modifier = Modifier.weight(1f)) {
                            Text(style.label)
                        }
                    }
                }
            }
            if (!themeUnlocked) PremiumLockedInline(PremiumFeature.ThemeCustomization, "Dark mode stays free. Premium unlocks accent customization.", onOpenPremium)
            Text("Planner widget", style = MaterialTheme.typography.labelLarge)
            if (!widgetUnlocked) PremiumLockedInline(PremiumFeature.Widgets, "Premium unlocks widget customization and richer widget layouts.", onOpenPremium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WidgetLayoutMode.entries.forEach { mode ->
                    val canSelect = widgetUnlocked || mode == state.widgetLayoutMode
                    if (state.widgetLayoutMode == mode) {
                        Button(onClick = { onWidgetLayoutModeChanged(mode) }, modifier = Modifier.weight(1f)) {
                            Text(mode.label)
                        }
                    } else {
                        OutlinedButton(onClick = { if (canSelect) onWidgetLayoutModeChanged(mode) else onOpenPremium() }, modifier = Modifier.weight(1f)) {
                            Text(mode.label)
                        }
                    }
                }
            }
            Text(
                when (state.widgetLayoutMode) {
                    WidgetLayoutMode.Compact -> "Compact shows the next shift and one task."
                    WidgetLayoutMode.Standard -> "Standard shows the next shift and up to three tasks."
                    WidgetLayoutMode.Detailed -> "Detailed shows tasks plus your latest smart note."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PayEstimateSection(
    state: AppState,
    onPaySettingsChanged: (PaySettings) -> Unit,
    onOpenPremium: () -> Unit
) {
    val settings = state.paySettings
    val unlocked = PremiumAccess.canUse(state, PremiumFeature.PayEstimator)
    var hourlyRate by remember(settings.hourlyRate) { mutableStateOf(settings.hourlyRate.takeIf { it > 0.0 }?.toString() ?: "") }
    var lunchMinutes by remember(settings.unpaidLunchMinutes) { mutableStateOf(settings.unpaidLunchMinutes.toString()) }
    var overtimeThreshold by remember(settings.overtimeThresholdHours) { mutableStateOf(settings.overtimeThresholdHours.toSimpleString()) }
    var overtimeMultiplier by remember(settings.overtimeMultiplier) { mutableStateOf(settings.overtimeMultiplier.toSimpleString()) }
    var dailyOvertimeThreshold by remember(settings.dailyOvertimeThresholdHours) { mutableStateOf(settings.dailyOvertimeThresholdHours.takeIf { it > 0.0 }?.toSimpleString() ?: "") }
    var nightExtra by remember(settings.nightShiftExtraAmount) { mutableStateOf(settings.nightShiftExtraAmount.takeIf { it > 0.0 }?.toSimpleString() ?: "") }
    var weekendExtra by remember(settings.weekendExtraAmount) { mutableStateOf(settings.weekendExtraAmount.takeIf { it > 0.0 }?.toSimpleString() ?: "") }
    var customShiftLabel by remember(settings.customShiftTypeLabel) { mutableStateOf(settings.customShiftTypeLabel) }
    var customShiftExtra by remember(settings.customShiftTypeExtraAmount) { mutableStateOf(settings.customShiftTypeExtraAmount.takeIf { it > 0.0 }?.toSimpleString() ?: "") }
    var payPeriodType by remember(settings.payPeriodType) { mutableStateOf(settings.payPeriodType) }
    var customPayPeriodStart by remember(settings.customPayPeriodStart) { mutableStateOf(settings.customPayPeriodStart) }
    var showPayOnDashboard by remember(settings.showPayOnDashboard) { mutableStateOf(settings.showPayOnDashboard) }
    val todayEstimate = PayEstimator.estimateDay(state)
    val weekEstimate = PayEstimator.estimateWeek(state)
    val payPeriodEstimate = PayEstimator.estimatePayPeriod(state)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Pay estimate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Estimated gross pay before taxes and deductions. This is an estimate, not payroll advice.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!unlocked) {
                PremiumLockedInline(PremiumFeature.PayEstimator, "Your schedule stays available. Premium unlocks wage, overtime, and pay-period estimates.", onOpenPremium)
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Show on dashboard", style = MaterialTheme.typography.bodyLarge)
                    Text("Keep wage data local on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = showPayOnDashboard, onCheckedChange = { showPayOnDashboard = it })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly rate") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lunchMinutes,
                    onValueChange = { lunchMinutes = it },
                    label = { Text("Lunch min") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = overtimeThreshold,
                    onValueChange = { overtimeThreshold = it },
                    label = { Text("OT hours") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = overtimeMultiplier,
                    onValueChange = { overtimeMultiplier = it },
                    label = { Text("OT x") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dailyOvertimeThreshold,
                    onValueChange = { dailyOvertimeThreshold = it },
                    label = { Text("Daily OT hrs") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = nightExtra,
                    onValueChange = { nightExtra = it },
                    label = { Text("Night +$/hr") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = weekendExtra, onValueChange = { weekendExtra = it }, label = { Text("Weekend +$/hr") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = customShiftExtra, onValueChange = { customShiftExtra = it }, label = { Text("Custom +$/hr") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = customShiftLabel, onValueChange = { customShiftLabel = it }, label = { Text("Custom shift type") }, placeholder = { Text("Inventory") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            PayPeriodTypeChips(selected = payPeriodType, onSelected = { payPeriodType = it })
            if (payPeriodType in setOf(PayPeriodType.Biweekly, PayPeriodType.Custom)) {
                DateOnlyRow("Pay period start", customPayPeriodStart, onChanged = { customPayPeriodStart = it })
            }
            Button(
                onClick = {
                    onPaySettingsChanged(
                        PaySettings(
                            hourlyRate = hourlyRate.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            unpaidLunchMinutes = lunchMinutes.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                            overtimeThresholdHours = overtimeThreshold.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 40.0,
                            overtimeMultiplier = overtimeMultiplier.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.5,
                            dailyOvertimeThresholdHours = dailyOvertimeThreshold.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            nightShiftExtraAmount = nightExtra.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            weekendExtraAmount = weekendExtra.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            customShiftTypeLabel = customShiftLabel.trim(),
                            customShiftTypeExtraAmount = customShiftExtra.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            payPeriodType = payPeriodType,
                            customPayPeriodStart = customPayPeriodStart,
                            showPayOnDashboard = showPayOnDashboard,
                            estimatedTaxRate = settings.estimatedTaxRate,
                            estimatedDeductionRate = settings.estimatedDeductionRate
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save pay settings")
            }
            PayEstimateLine("Today", todayEstimate.grossPay, todayEstimate.paidHours, todayEstimate.overtimeHours)
            PayEstimateLine("This week", weekEstimate.grossPay, weekEstimate.paidHours, weekEstimate.overtimeHours)
            PayEstimateLine("Pay period", payPeriodEstimate.grossPay, payPeriodEstimate.paidHours, payPeriodEstimate.overtimeHours)
            EarningsSummaryCard(
                estimate = weekEstimate,
                regularHours = weekEstimate.regularHours,
                overtimeHours = weekEstimate.overtimeHours,
                paidHours = weekEstimate.paidHours
            )
            val overtimeText = if (weekEstimate.overtimeHours > 0.0) {
                "${weekEstimate.overtimeHours.toSimpleString()} overtime hours scheduled."
            } else {
                "${weekEstimate.hoursUntilOvertime.toSimpleString()} hours until overtime."
            }
            Text(overtimeText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    estimate: PayEstimate,
    regularHours: Double,
    overtimeHours: Double,
    paidHours: Double
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Earnings summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            PayBreakdownLine("Regular pay", estimate.regularPay, MaterialTheme.colorScheme.primary)
            PayBreakdownLine("Overtime pay", estimate.overtimePay, Color(0xFFFFB020))
            PayBreakdownLine("Shift differential", estimate.differentialPay, Color(0xFF54D17A))
            Divider()
            PayBreakdownLine("Estimated gross pay", estimate.grossPay, Color(0xFF4DB6FF), emphasized = true)
            Text(
                "Hours: ${paidHours.toSimpleString()} paid / ${regularHours.toSimpleString()} regular" +
                    if (overtimeHours > 0.0) " / ${overtimeHours.toSimpleString()} OT" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PayBreakdownLine(label: String, amount: Double, color: Color, emphasized: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.width(10.dp).height(10.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            "$${amount.toMoneyString()}",
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PayPeriodTypeChips(selected: PayPeriodType, onSelected: (PayPeriodType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Pay period", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PayPeriodType.entries.forEach { type ->
                FilterChip(selected = selected == type, onClick = { onSelected(type) }, label = { Text(type.label) })
            }
        }
    }
}

@Composable
private fun PayEstimateLine(label: String, grossPay: Double, paidHours: Double, overtimeHours: Double) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${paidHours.toSimpleString()} paid hours${if (overtimeHours > 0.0) ", ${overtimeHours.toSimpleString()} OT" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("$${grossPay.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun Double.toSimpleString(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
}

private fun Double.toMoneyString(): String = String.format("%.2f", this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarSyncSection(
    state: AppState,
    calendars: List<DeviceCalendar>,
    message: String?,
    onLoadCalendars: () -> Unit,
    onSelectCalendar: (Long?) -> Unit,
    onSyncCalendar: () -> Unit,
    onOpenPremium: () -> Unit
) {
    val unlocked = PremiumAccess.canUse(state, PremiumFeature.CalendarSync)
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var hasCalendarPermission by remember {
        mutableStateOf(context.hasCalendarPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasCalendarPermission = result[Manifest.permission.READ_CALENDAR] == true &&
            result[Manifest.permission.WRITE_CALENDAR] == true
        if (hasCalendarPermission) onLoadCalendars()
    }

    LaunchedEffect(hasCalendarPermission) {
        if (hasCalendarPermission) onLoadCalendars()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Google Calendar sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Sync imported shifts to a calendar on this phone. Pick your Google calendar to have it show up in Google Calendar.",
                style = MaterialTheme.typography.bodySmall
            )
            if (!unlocked) {
                PremiumLockedInline(PremiumFeature.CalendarSync, "Your schedule stays local. Premium unlocks calendar export and sync.", onOpenPremium)
                return@Column
            }
            if (!hasCalendarPermission) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    }
                ) {
                    Text("Allow calendar sync")
                }
            } else {
                val selected = calendars.firstOrNull { it.id == state.selectedCalendarId }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selected?.displayName ?: "Choose calendar",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Calendar") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        calendars.forEach { calendar ->
                            DropdownMenuItem(
                                text = { Text(calendar.displayName) },
                                onClick = {
                                    onSelectCalendar(calendar.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onSyncCalendar,
                        enabled = state.selectedCalendarId != null && state.shifts.isNotEmpty()
                    ) {
                        Text("Sync shifts")
                    }
                    OutlinedButton(onClick = onLoadCalendars) {
                        Text("Refresh")
                    }
                }
                if (calendars.isEmpty()) {
                    Text("No writable calendars found on this phone.", style = MaterialTheme.typography.bodySmall)
                }
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun android.content.Context.hasCalendarPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun ScheduleScreen(
    state: AppState,
    onAddShift: (WorkShift) -> Unit,
    onDeleteShift: (String) -> Unit,
    onSaveShiftTemplate: (ShiftTemplate) -> Unit,
    onDeleteShiftTemplate: (String) -> Unit,
    onSaveShiftPattern: (ShiftPattern) -> Unit,
    onApplyShiftPattern: (ShiftPattern, Boolean) -> Unit,
    onSetShiftPatternEnabled: (String, Boolean) -> Unit,
    onDeleteShiftPattern: (String) -> Unit,
    onAddDayOff: (LocalDate) -> Unit,
    onAddTypedDayOff: (LocalDate, ShiftTemplateKind) -> Unit,
    onRemoveDayOff: (LocalDate) -> Unit,
    onClearSchedule: () -> Unit,
    onImportSchedule: () -> Unit,
    onOpenPremium: () -> Unit
) {
    var showAddShift by remember { mutableStateOf(false) }
    var showPatternWizard by remember { mutableStateOf(false) }
    var dayOffDate by remember { mutableStateOf(LocalDate.now()) }
    var scheduleMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding)
            .padding(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        ScheduleOverviewCard(state = state)
        ScheduleQuickActions(
            showAddShift = showAddShift,
            onToggleAddShift = { showAddShift = !showAddShift },
            showPatternWizard = showPatternWizard,
            onTogglePatternWizard = {
                if (PremiumAccess.canUse(state, PremiumFeature.ShiftPatterns)) showPatternWizard = !showPatternWizard else scheduleMessage = "Shift patterns are a premium convenience feature."
            },
            dayOffDate = dayOffDate,
            onDayOffDateChanged = { dayOffDate = it },
            onAddDayOff = {
                onAddDayOff(dayOffDate)
                scheduleMessage = "Day off added."
            },
            onImportSchedule = onImportSchedule
        )
        scheduleMessage?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
        }
        if (showAddShift) {
            AddShiftCard(
                templates = shiftTemplatesFor(state),
                onAddDayOff = { date, kind ->
                    onAddTypedDayOff(date, kind)
                    scheduleMessage = "Day off added."
                    showAddShift = false
                },
                onSave = {
                    onAddShift(it)
                    showAddShift = false
                },
                onSaveTemplate = onSaveShiftTemplate,
                onDeleteTemplate = onDeleteShiftTemplate,
                onCancel = { showAddShift = false }
            )
        }
        if (showPatternWizard) {
            ShiftPatternWizard(
                state = state,
                onSavePattern = {
                    onSaveShiftPattern(it)
                    scheduleMessage = "Shift pattern saved."
                    showPatternWizard = false
                },
                onApplyPattern = { pattern, allowDuplicates ->
                    onApplyShiftPattern(pattern, allowDuplicates)
                    scheduleMessage = "Shift pattern generated for the next 30 days."
                    showPatternWizard = false
                },
                onCancel = { showPatternWizard = false }
            )
        }
        ShiftPatternSection(
            patterns = state.shiftPatterns,
            onSetEnabled = onSetShiftPatternEnabled,
            onDelete = onDeleteShiftPattern,
            premiumUnlocked = PremiumAccess.canUse(state, PremiumFeature.ShiftPatterns),
            onOpenPremium = onOpenPremium
        )
        if (!PremiumAccess.canUse(state, PremiumFeature.ShiftPatterns) && state.shiftPatterns.isEmpty()) {
            PremiumLockedCard(PremiumFeature.ShiftPatterns, "Manual shifts and days off stay free. Premium adds rotating patterns for schedules that repeat in cycles.", onOpenPremium)
        }
        if (state.shifts.isEmpty() && state.daysOff.isEmpty()) {
            ScheduleEmptyState(onImportSchedule = onImportSchedule, onAddShift = { showAddShift = true })
        } else {
            CurrentWeekSchedule(
                state = state,
                onRemoveDayOff = {
                    onRemoveDayOff(it)
                    scheduleMessage = "Day off removed."
                },
                onDeleteShift = onDeleteShift
            )
            NextSevenDaysSchedule(
                state = state,
                onRemoveDayOff = {
                    onRemoveDayOff(it)
                    scheduleMessage = "Day off removed."
                },
                onDeleteShift = onDeleteShift
            )
        }
        OutlinedButton(
            onClick = onClearSchedule,
            enabled = state.shifts.isNotEmpty() || state.daysOff.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear schedule")
        }
    }
}

@Composable
private fun ScheduleQuickActions(
    showAddShift: Boolean,
    onToggleAddShift: () -> Unit,
    showPatternWizard: Boolean,
    onTogglePatternWizard: () -> Unit,
    dayOffDate: LocalDate,
    onDayOffDateChanged: (LocalDate) -> Unit,
    onAddDayOff: () -> Unit,
    onImportSchedule: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onToggleAddShift, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (showAddShift) "Hide shift" else "Add shift")
                }
                OutlinedButton(onClick = onTogglePatternWizard, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (showPatternWizard) "Hide pattern" else "Shift pattern")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onImportSchedule, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import")
                }
                Spacer(Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                DateOnlyRow("Day off", dayOffDate, onChanged = onDayOffDateChanged, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onAddDayOff) { Text("Add day off") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleOverviewCard(state: AppState) {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val todayShifts = state.shifts.filter { it.date == today }.sortedBy { it.start }
    val nextShift = state.shifts
        .filter { it.endDateTime().isAfter(now) }
        .minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val currentWeekEnd = currentWeekStart.plusDays(6)
    val weekHours = state.shifts
        .filter { !it.date.isBefore(currentWeekStart) && !it.date.isAfter(currentWeekEnd) }
        .sumOf { it.durationMinutes() } / 60.0
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                when {
                    today in state.daysOff -> "Today is marked as a day off."
                    todayShifts.isNotEmpty() -> "Today: ${todayShifts.joinToString { it.shiftTimeLabel() }}"
                    else -> "No shift scheduled today."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                nextShift?.let { "Next shift ${it.timeUntilShift(now)}: ${it.date.format(dateFormatter)} at ${it.start.format(timeFormatter)}" }
                    ?: "No upcoming shift saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${state.shifts.count { !it.date.isBefore(today) }} upcoming shifts") })
                AssistChip(onClick = {}, label = { Text("${weekHours.toSimpleString()} hrs this week") })
                AssistChip(onClick = {}, label = { Text("${state.daysOff.count { !it.isBefore(today) }} upcoming days off") })
            }
        }
    }
}

@Composable
private fun AddShiftCard(
    templates: List<ShiftTemplate>,
    onAddDayOff: (LocalDate, ShiftTemplateKind) -> Unit,
    onSave: (WorkShift) -> Unit,
    onSaveTemplate: (ShiftTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("Work") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var startsAt by remember { mutableStateOf(LocalDate.now().atTime(9, 0)) }
    var endsAt by remember { mutableStateOf(startsAt.plusHours(8)) }
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add shift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            ShiftTemplateChips(
                templates = templates,
                onApply = { template ->
                    if (template.kind != ShiftTemplateKind.Work) {
                        onAddDayOff(startsAt.toLocalDate(), template.kind)
                        return@ShiftTemplateChips
                    }
                    title = template.label
                    location = template.location
                    notes = template.notes
                    startsAt = startsAt.toLocalDate().atTime(template.start)
                    endsAt = startsAt.toLocalDate().atTime(template.end).let { if (template.end.isBefore(template.start)) it.plusDays(1) else it }
                },
                onSaveTemplate = onSaveTemplate,
                onDeleteTemplate = onDeleteTemplate
            )
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Role/title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            DateTimeRow("Starts", startsAt, onChanged = {
                val duration = Duration.between(startsAt, endsAt).takeIf { d -> !d.isNegative && !d.isZero } ?: Duration.ofHours(8)
                startsAt = it
                endsAt = it.plus(duration)
            })
            DateTimeRow("Ends", endsAt, onChanged = { endsAt = it })
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = title.isNotBlank() && endsAt.isAfter(startsAt),
                    onClick = {
                        onSave(
                            WorkShift(
                                date = startsAt.toLocalDate(),
                                start = startsAt.toLocalTime(),
                                end = endsAt.toLocalTime(),
                                label = title.trim(),
                                location = location.trim(),
                                notes = notes.trim()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save shift") }
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            }
        }
    }
}

private val builtInShiftTemplates = listOf(
    ShiftTemplate(id = "builtin-opening-shift", name = "Opening shift", label = "Opening shift", start = LocalTime.of(6, 0), end = LocalTime.of(14, 0), notes = "Opening setup and first shift tasks.", builtIn = true),
    ShiftTemplate(id = "builtin-closing-shift", name = "Closing shift", label = "Closing shift", start = LocalTime.of(14, 0), end = LocalTime.of(22, 0), notes = "Closing cleanup and handoff.", builtIn = true),
    ShiftTemplate(id = "builtin-mid-shift", name = "Mid shift", label = "Mid shift", start = LocalTime.of(10, 0), end = LocalTime.of(18, 0), builtIn = true),
    ShiftTemplate(id = "builtin-truck-order", name = "Truck/order day", label = "Truck/order day", start = LocalTime.of(7, 0), end = LocalTime.of(15, 0), notes = "Truck, ordering, or delivery follow-up.", builtIn = true),
    ShiftTemplate(id = "builtin-inventory", name = "Inventory day", label = "Inventory day", start = LocalTime.of(8, 0), end = LocalTime.of(16, 0), notes = "Inventory count and order checks.", builtIn = true),
    ShiftTemplate(id = "builtin-day-off", name = "Day off", label = "Day off", kind = ShiftTemplateKind.DayOff, builtIn = true),
    ShiftTemplate(id = "builtin-vacation", name = "Vacation", label = "Vacation", kind = ShiftTemplateKind.Vacation, builtIn = true),
    ShiftTemplate(id = "builtin-sick-day", name = "Sick day", label = "Sick day", kind = ShiftTemplateKind.Sick, builtIn = true)
)

private fun shiftTemplatesFor(state: AppState): List<ShiftTemplate> = builtInShiftTemplates + state.shiftTemplates

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftTemplateChips(
    templates: List<ShiftTemplate>,
    onApply: (ShiftTemplate) -> Unit,
    onSaveTemplate: (ShiftTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    var editorOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ShiftTemplate?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Shift templates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                editing = null
                editorOpen = !editorOpen
            }) { Text(if (editorOpen) "Hide" else "Create") }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            templates.forEach { template ->
                OutlinedButton(onClick = { onApply(template) }) { Text(template.name) }
            }
        }
        templates.filterNot { it.builtIn }.forEach { template ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(template.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = {
                    editing = template
                    editorOpen = true
                }) { Text("Edit") }
                TextButton(onClick = { onDeleteTemplate(template.id) }) { Text("Delete") }
            }
        }
        if (editorOpen) {
            ShiftTemplateEditor(
                template = editing,
                onSave = {
                    onSaveTemplate(it)
                    editorOpen = false
                    editing = null
                }
            )
        }
    }
}

@Composable
private fun ShiftTemplateEditor(template: ShiftTemplate?, onSave: (ShiftTemplate) -> Unit) {
    var name by remember(template?.id) { mutableStateOf(template?.name.orEmpty()) }
    var label by remember(template?.id) { mutableStateOf(template?.label ?: "Work") }
    var location by remember(template?.id) { mutableStateOf(template?.location.orEmpty()) }
    var notes by remember(template?.id) { mutableStateOf(template?.notes.orEmpty()) }
    var marksDayOff by remember(template?.id) { mutableStateOf(template?.kind != null && template.kind != ShiftTemplateKind.Work) }
    var startsAt by remember(template?.id) { mutableStateOf(LocalDate.now().atTime(template?.start ?: LocalTime.of(9, 0))) }
    var endsAt by remember(template?.id) { mutableStateOf(LocalDate.now().atTime(template?.end ?: LocalTime.of(17, 0))) }
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template?.let { "Edit shift template" } ?: "Create shift template", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Template name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Role/title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = marksDayOff, onCheckedChange = { marksDayOff = it })
                Text("Marks a day off")
            }
            if (!marksDayOff) {
                DateTimeRow("Starts", startsAt, onChanged = { startsAt = it })
                DateTimeRow("Ends", endsAt, onChanged = { endsAt = it })
            }
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Button(
                enabled = name.isNotBlank() && label.isNotBlank(),
                onClick = {
                    onSave(
                        ShiftTemplate(
                            id = template?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            label = label.trim(),
                            start = startsAt.toLocalTime(),
                            end = endsAt.toLocalTime(),
                            location = location.trim(),
                            notes = notes.trim(),
                            kind = if (marksDayOff) ShiftTemplateKind.DayOff else ShiftTemplateKind.Work
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save template") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftPatternSection(
    patterns: List<ShiftPattern>,
    onSetEnabled: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    premiumUnlocked: Boolean,
    onOpenPremium: () -> Unit
) {
    if (patterns.isEmpty()) return
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Shift patterns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!premiumUnlocked) {
                PremiumLockedInline(PremiumFeature.ShiftPatterns, "Existing generated shifts remain visible. Premium is needed to create or resume patterns.", onOpenPremium)
            }
            patterns.sortedBy { it.startDate }.forEach { pattern ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(pattern.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${pattern.cycleLength}-day cycle from ${pattern.startDate.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = pattern.enabled, onCheckedChange = { if (premiumUnlocked) onSetEnabled(pattern.id, it) })
                    TextButton(onClick = { onDelete(pattern.id) }) { Text("Delete") }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftPatternWizard(
    state: AppState,
    onSavePattern: (ShiftPattern) -> Unit,
    onApplyPattern: (ShiftPattern, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val presetNames = listOf("Standard weekdays", "4 on / 4 off", "5 on / 2 off", "5 day / 5 off / 5 night", "Weekends only", "Custom")
    var pattern by remember { mutableStateOf(ShiftPatternGenerator.preset("5 on / 2 off", LocalDate.now())) }
    var name by remember { mutableStateOf(pattern.name) }
    var cycleLengthText by remember { mutableStateOf(pattern.cycleLength.toString()) }
    var hasEndDate by remember { mutableStateOf(false) }
    var allowDuplicates by remember { mutableStateOf(false) }
    val normalizedPattern = pattern.copy(
        name = name.ifBlank { "Shift pattern" },
        cycleLength = cycleLengthText.toIntOrNull()?.coerceIn(1, 30) ?: pattern.cycleLength,
        days = normalizedPatternDays(pattern.days, cycleLengthText.toIntOrNull()?.coerceIn(1, 30) ?: pattern.cycleLength),
        endDate = if (hasEndDate) pattern.endDate ?: pattern.startDate.plusMonths(1) else null
    )
    val preview = ShiftPatternGenerator.preview(normalizedPattern)
    val duplicateCount = preview.shifts.count { previewShift ->
        state.shifts.any { existing -> existing.date == previewShift.date && existing.start == previewShift.start && existing.end == previewShift.end }
    }

    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Shift Pattern", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Step 1: choose a preset, then adjust the cycle before saving.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presetNames.forEach { preset ->
                    OutlinedButton(onClick = {
                        val next = ShiftPatternGenerator.preset(preset, pattern.startDate)
                        pattern = next
                        name = next.name
                        cycleLengthText = next.cycleLength.toString()
                    }) { Text(preset) }
                }
            }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pattern name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            DateOnlyRow("Start date", normalizedPattern.startDate, onChanged = { pattern = pattern.copy(startDate = it) })
            OutlinedTextField(
                value = cycleLengthText,
                onValueChange = { value -> cycleLengthText = value.filter(Char::isDigit).take(2) },
                label = { Text("Cycle length") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasEndDate, onCheckedChange = { hasEndDate = it })
                Text("Use end date")
            }
            if (hasEndDate) {
                DateOnlyRow("End date", normalizedPattern.endDate ?: normalizedPattern.startDate.plusMonths(1), onChanged = { pattern = pattern.copy(endDate = it) })
            }
            Text("Step 2: set each cycle day.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            normalizedPattern.days.forEach { day ->
                ShiftPatternDayEditor(day = day, onChange = { changed ->
                    pattern = pattern.copy(days = normalizedPattern.days.map { if (it.index == changed.index) changed else it })
                })
            }
            Text("Step 3: preview the next 30 days.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("${preview.shifts.size} shifts and ${preview.daysOff.size} days off will be generated.", style = MaterialTheme.typography.bodyMedium)
            if (duplicateCount > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f))) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("$duplicateCount generated shifts match existing shifts.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = allowDuplicates, onCheckedChange = { allowDuplicates = it })
                            Text("Allow duplicate shifts on the same date and time")
                        }
                    }
                }
            }
            ShiftPatternPreviewList(preview = preview)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onSavePattern(normalizedPattern) }, modifier = Modifier.weight(1f)) { Text("Save pattern") }
                Button(
                    onClick = { onApplyPattern(normalizedPattern, allowDuplicates) },
                    enabled = preview.shifts.isNotEmpty() && (duplicateCount == 0 || allowDuplicates),
                    modifier = Modifier.weight(1f)
                ) { Text("Save shifts") }
            }
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

@Composable
private fun ShiftPatternDayEditor(day: ShiftPatternDay, onChange: (ShiftPatternDay) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Day ${day.index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = day.kind == ShiftPatternDayKind.Work, onCheckedChange = { checked ->
                        onChange(day.copy(kind = if (checked) ShiftPatternDayKind.Work else ShiftPatternDayKind.Off))
                    })
                    Text("Work")
                }
            }
            if (day.kind == ShiftPatternDayKind.Work) {
                OutlinedTextField(value = day.label, onValueChange = { onChange(day.copy(label = it)) }, label = { Text("Shift type") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = day.start.toString(),
                        onValueChange = { value -> parseReviewTime(value)?.let { onChange(day.copy(start = it)) } },
                        label = { Text("Start") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = day.end.toString(),
                        onValueChange = { value -> parseReviewTime(value)?.let { onChange(day.copy(end = it)) } },
                        label = { Text("End") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = day.location, onValueChange = { onChange(day.copy(location = it)) }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            } else {
                Text("Off day in this cycle", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShiftPatternPreviewList(preview: com.example.workdayplanner.data.ShiftPatternPreview) {
    val datedItems = (preview.shifts.map { it.date } + preview.daysOff).distinct().sorted().take(30)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            datedItems.take(10).forEach { date ->
                val shift = preview.shifts.firstOrNull { it.date == date }
                Text(
                    shift?.let { "${date.format(shortDateFormatter)} ${it.label}: ${it.start.format(timeFormatter)} - ${it.end.format(timeFormatter)}" }
                        ?: "${date.format(shortDateFormatter)} Day off",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (datedItems.size > 10) {
                Text("+${datedItems.size - 10} more days in preview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun normalizedPatternDays(days: List<ShiftPatternDay>, cycleLength: Int): List<ShiftPatternDay> {
    val byIndex = days.associateBy { it.index }
    return (0 until cycleLength.coerceIn(1, 30)).map { index ->
        byIndex[index] ?: ShiftPatternDay(index = index)
    }
}

@Composable
private fun ScheduleEmptyState(onImportSchedule: () -> Unit, onAddShift: () -> Unit) {
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("No schedule yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Import a screenshot or add a shift manually to build your work week.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onImportSchedule, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import")
                }
                OutlinedButton(onClick = onAddShift, modifier = Modifier.weight(1f)) { Text("Add shift") }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: AppState,
    onDarkModeChanged: (Boolean) -> Unit,
    onAccentStyleChanged: (AccentStyle) -> Unit,
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit,
    onPaySettingsChanged: (PaySettings) -> Unit,
    calendars: List<DeviceCalendar>,
    calendarMessage: String?,
    onLoadCalendars: () -> Unit,
    onSelectCalendar: (Long?) -> Unit,
    onSyncCalendar: () -> Unit,
    onMockPremiumChanged: (Boolean) -> Unit,
    onOpenPremium: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        StyleSection(
            state = state,
            onDarkModeChanged = onDarkModeChanged,
            onAccentStyleChanged = onAccentStyleChanged,
            onWidgetLayoutModeChanged = onWidgetLayoutModeChanged,
            onOpenPremium = onOpenPremium
        )
        PayEstimateSection(
            state = state,
            onPaySettingsChanged = onPaySettingsChanged,
            onOpenPremium = onOpenPremium
        )
        CalendarSyncSection(
            state = state,
            calendars = calendars,
            message = calendarMessage,
            onLoadCalendars = onLoadCalendars,
            onSelectCalendar = onSelectCalendar,
            onSyncCalendar = onSyncCalendar,
            onOpenPremium = onOpenPremium
        )
        PremiumSettingsSection(state = state, onMockPremiumChanged = onMockPremiumChanged, onOpenPremium = onOpenPremium)
    }
}

@Composable
private fun PremiumSettingsSection(
    state: AppState,
    onMockPremiumChanged: (Boolean) -> Unit,
    onOpenPremium: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Premium", "Free stays useful. Premium adds convenience for power users.")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(if (state.premium.has(PremiumFeature.UnlimitedImports)) "Premium active" else "Free plan", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Local mock entitlement for testing. Real billing can be wired later.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.premium.mockPremiumEnabled, onCheckedChange = onMockPremiumChanged)
            }
            OutlinedButton(onClick = onOpenPremium, modifier = Modifier.fillMaxWidth()) {
                Text("View premium options")
            }
        }
    }
}

@Composable
private fun PremiumScreen(
    state: AppState,
    onMockPremiumChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Workday Planner Premium", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("The free app covers basic shifts, tasks, reminders, days off, a few imports, and the Today dashboard.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Premium unlocks convenience and power-user tools without hiding existing data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        item {
            Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Suggested pricing", "No payments are implemented yet.")
                    PricingRow("Monthly", "$3.99", "For trying the power tools.")
                    PricingRow("Yearly", "$29.99", "Best fit for steady shift workers.")
                    PricingRow("Lifetime", "$59.99", "One-time unlock option.")
                }
            }
        }
        item {
            Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Premium includes")
                    PremiumFeature.entries.forEach { feature ->
                        Text("${feature.title}: ${feature.value}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Mock premium", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Local testing switch. Replace this with Play Billing entitlement later.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = state.premium.mockPremiumEnabled, onCheckedChange = onMockPremiumChanged)
                    }
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to settings") }
                }
            }
        }
    }
}

@Composable
private fun PricingRow(name: String, price: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PremiumLockedCard(feature: PremiumFeature, body: String, onOpenPremium: () -> Unit) {
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(feature.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onOpenPremium, modifier = Modifier.fillMaxWidth()) { Text("See premium options") }
        }
    }
}

@Composable
private fun PremiumLockedInline(feature: PremiumFeature, body: String, onOpenPremium: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(feature.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onOpenPremium) { Text("Premium options") }
        }
    }
}

@Composable
private fun CurrentWeekSchedule(state: AppState, onRemoveDayOff: (LocalDate) -> Unit, onDeleteShift: (String) -> Unit) {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0L..6L).map { weekStart.plusDays(it) }
    ScheduleDaySection(
        title = "Current week",
        subtitle = "${weekStart.format(dateFormatter)} - ${days.last().format(dateFormatter)}",
        dates = days,
        state = state,
        onRemoveDayOff = onRemoveDayOff,
        onDeleteShift = onDeleteShift
    )
}

@Composable
private fun NextSevenDaysSchedule(state: AppState, onRemoveDayOff: (LocalDate) -> Unit, onDeleteShift: (String) -> Unit) {
    val today = LocalDate.now()
    ScheduleDaySection(
        title = "Next 7 days",
        subtitle = "Starting ${today.format(dateFormatter)}",
        dates = (0L..6L).map { today.plusDays(it) },
        state = state,
        onRemoveDayOff = onRemoveDayOff,
        onDeleteShift = onDeleteShift
    )
}

@Composable
private fun ScheduleDaySection(
    title: String,
    subtitle: String,
    dates: List<LocalDate>,
    state: AppState,
    onRemoveDayOff: (LocalDate) -> Unit,
    onDeleteShift: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            dates.forEach { date ->
                ScheduleDayCard(
                    date = date,
                    shifts = state.shifts.filter { it.date == date }.sortedBy { it.start },
                    isDayOff = date in state.daysOff,
                    linkedTaskCount = { shift -> state.tasks.count { it.linkedShiftId == shift.id } },
                    onRemoveDayOff = onRemoveDayOff,
                    onDeleteShift = onDeleteShift
                )
            }
        }
    }
}

@Composable
private fun ScheduleDayCard(
    date: LocalDate,
    shifts: List<WorkShift>,
    isDayOff: Boolean,
    linkedTaskCount: (WorkShift) -> Int,
    onRemoveDayOff: (LocalDate) -> Unit,
    onDeleteShift: (String) -> Unit
) {
    val isToday = date == LocalDate.now()
    val container = when {
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${date.dayOfWeek.name.take(3).lowercase().replaceFirstChar(Char::uppercase)} ${date.format(shortDateFormatter)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isToday) Text("Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                if (isDayOff) {
                    TextButton(onClick = { onRemoveDayOff(date) }) { Text("Remove") }
                }
            }
            when {
                isDayOff -> DayOffCard()
                shifts.isEmpty() -> Text("No shift saved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> shifts.forEach { shift ->
                    ShiftSummaryCard(shift = shift, linkedTaskCount = linkedTaskCount(shift), onDelete = { onDeleteShift(shift.id) })
                }
            }
        }
    }
}

@Composable
private fun DayOffCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Day off", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ShiftSummaryCard(shift: WorkShift, linkedTaskCount: Int, onDelete: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(shift.label.ifBlank { "Work" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(shift.date.format(dateFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${shift.start.format(timeFormatter)} - ${shift.end.format(timeFormatter)}", style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(shift.durationLabel(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) { Text("Delete") }
                    }
                }
            }
            val detail = listOf(shift.location, shift.notes).filter { it.isNotBlank() }.joinToString(" - ")
            if (detail.isNotBlank()) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "$linkedTaskCount linked ${if (linkedTaskCount == 1) "task" else "tasks"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (shift.patternId != null) {
                Text("Generated from shift pattern", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ImportScreen(
    state: AppState,
    rawText: String,
    parsed: ParsedSchedule?,
    changes: ScheduleChangeSet?,
    isReading: Boolean,
    message: String?,
    error: String?,
    guidance: ScheduleImportGuidance?,
    onTextChange: (String) -> Unit,
    onImagePicked: (android.net.Uri) -> Unit,
    onImageCancelled: () -> Unit,
    onPreview: () -> Unit,
    onApply: (ParsedSchedule) -> Unit,
    onStartOver: () -> Unit,
    onOpenPremium: () -> Unit
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImagePicked(uri) else onImageCancelled()
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) onImagePicked(uri) else onImageCancelled()
    }
    var reviewRows by remember(parsed) { mutableStateOf(parsed?.toReviewRows().orEmpty()) }
    val remainingImports = PremiumAccess.remainingScreenshotImports(state)
    val canImport = PremiumAccess.canImportScreenshot(state)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        ImportStepHeader("1", "Pick a schedule screenshot", "Take a photo or choose an image. Your schedule image stays on your phone.")
        if (!PremiumAccess.canUse(state, PremiumFeature.UnlimitedImports)) {
            Text(
                "$remainingImports free screenshot ${if (remainingImports == 1) "import" else "imports"} left this month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!canImport) {
                PremiumLockedInline(PremiumFeature.UnlimitedImports, "Manual shifts stay free. Premium unlocks unlimited screenshot imports.", onOpenPremium)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { imagePicker.launch("image/*") }, enabled = !isReading && canImport) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isReading) "Reading screenshot..." else "Choose screenshot")
            }
            OutlinedButton(
                onClick = {
                    val photoFile = File.createTempFile("schedule-import-", ".jpg", context.cacheDir)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                },
                enabled = !isReading && canImport
            ) {
                Text("Take photo")
            }
        }
        ImportStepHeader("2", "Run on-device OCR", "Workday Planner reads the screenshot locally and does not upload images.")
        ImportStepHeader("3", "Review recognized text", "You can edit the OCR text before building the preview.")
        Button(
            onClick = onPreview,
            enabled = rawText.isNotBlank() && !isReading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (parsed == null) "Build preview from text" else "Refresh preview from edits")
        }
        OutlinedTextField(
            value = rawText,
            onValueChange = onTextChange,
            label = { Text("Recognized schedule text") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth().height(230.dp)
        )
        OutlinedButton(onClick = onPreview, enabled = rawText.isNotBlank() && !isReading, modifier = Modifier.fillMaxWidth()) {
            Text(if (parsed == null) "Review detected shifts" else "Refresh detected shifts")
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        guidance?.let { ScheduleImportGuidanceCard(it) }
        if (message != null) Text(message, color = MaterialTheme.colorScheme.secondary)
        parsed?.let {
            if (rawText.isNotBlank() && it.shifts.isEmpty() && it.daysOff.isEmpty()) {
                Text(
                    "OCR found schedule text, but we could not confidently build shifts. Try cropping the screenshot to only the schedule list, or review the detected lines below.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImportStepHeader("4", "Review detected shifts", "High confidence rows are pre-selected. Needs review rows must be confirmed before saving.")
                    val corrected = reviewRows.toParsedSchedule()
                    Text("${corrected.shifts.size} shifts, ${corrected.daysOff.size} days off confirmed")
                    changes?.let { changeSet -> ScheduleChangeSummary(changeSet, "Original OCR changes") }
                    ScheduleReviewRows(
                        rows = reviewRows,
                        onRowsChanged = { reviewRows = it },
                        onAddShift = {
                            val date = reviewRows.firstOrNull { row -> row.kind != ScheduleReviewKind.Ignored }?.date ?: LocalDate.now()
                            reviewRows = reviewRows + ScheduleReviewRow(
                                date = date,
                                start = LocalTime.of(9, 0),
                                end = LocalTime.of(17, 0),
                                title = "Work",
                                confidence = ScheduleImportConfidence.NeedsReview,
                                selected = true
                            )
                        }
                    )
                    if (reviewRows.isEmpty()) {
                        Text(
                            "No clear shifts were found. You can still use the recognized text above to add shifts manually.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "Review before saving. Nothing is saved until you confirm this review.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { onApply(corrected) },
                        enabled = corrected.shifts.isNotEmpty() || corrected.daysOff.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept and save confirmed shifts")
                    }
                    TextButton(onClick = onStartOver, modifier = Modifier.fillMaxWidth()) {
                        Text("Start over")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportStepHeader(step: String, title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("Step $step", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ScheduleImportGuidanceCard(guidance: ScheduleImportGuidance) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(guidance.issue.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(guidance.issue.body, style = MaterialTheme.typography.bodyMedium)
            guidance.detail?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            guidance.issue.tips.forEach { tip ->
                Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private enum class ScheduleReviewKind { Shift, DayOff, Ignored }

private enum class ScheduleImportConfidence(val label: String) {
    High("High confidence"),
    NeedsReview("Needs review"),
    Unclear("Could not read clearly")
}

private data class ScheduleReviewRow(
    val id: String = java.util.UUID.randomUUID().toString(),
    val kind: ScheduleReviewKind = ScheduleReviewKind.Shift,
    val date: LocalDate,
    val start: LocalTime = LocalTime.of(9, 0),
    val end: LocalTime = LocalTime.of(17, 0),
    val title: String = "Work",
    val location: String = "",
    val notes: String = "",
    val sourceText: String = "",
    val confidence: ScheduleImportConfidence = ScheduleImportConfidence.High,
    val selected: Boolean = true
)

private fun ParsedSchedule.toReviewRows(): List<ScheduleReviewRow> {
    val shiftRows = shifts.map { shift ->
        ScheduleReviewRow(
            kind = ScheduleReviewKind.Shift,
            date = shift.date,
            start = shift.start,
            end = shift.end,
            title = shift.label,
            location = shift.location,
            notes = shift.notes,
            confidence = if (shift.label.isBlank() || shift.label == "Work") ScheduleImportConfidence.NeedsReview else ScheduleImportConfidence.High,
            selected = shift.label.isNotBlank() && shift.label != "Work"
        )
    }
    val dayOffRows = daysOff.map { date ->
        ScheduleReviewRow(kind = ScheduleReviewKind.DayOff, date = date, title = "Day off", confidence = ScheduleImportConfidence.High)
    }
    val unclearRows = unparsedLines.map { line ->
        ScheduleReviewRow(
            kind = ScheduleReviewKind.Ignored,
            date = LocalDate.now(),
            title = "Unclear OCR",
            sourceText = line,
            confidence = ScheduleImportConfidence.Unclear,
            selected = false
        )
    }
    return (shiftRows + dayOffRows + unclearRows).sortedWith(compareBy<ScheduleReviewRow> { it.date }.thenBy { it.start })
}

private fun List<ScheduleReviewRow>.toParsedSchedule(): ParsedSchedule {
    val activeRows = filter { it.selected && it.kind != ScheduleReviewKind.Ignored }
    return ParsedSchedule(
        shifts = activeRows
            .filter { it.kind == ScheduleReviewKind.Shift }
            .map {
                WorkShift(
                    date = it.date,
                    start = it.start,
                    end = it.end,
                    label = it.title.ifBlank { "Work" },
                    location = it.location,
                    notes = it.notes
                )
            },
        daysOff = activeRows.filter { it.kind == ScheduleReviewKind.DayOff }.map { it.date }.toSet(),
        unparsedLines = filter { it.kind == ScheduleReviewKind.Ignored && it.sourceText.isNotBlank() }.map { it.sourceText }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleReviewRows(
    rows: List<ScheduleReviewRow>,
    onRowsChanged: (List<ScheduleReviewRow>) -> Unit,
    onAddShift: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Review rows", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onAddShift) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add shift")
            }
        }
        rows.groupBy { if (it.kind == ScheduleReviewKind.Ignored) null else it.date }.toSortedMap(compareBy<LocalDate?> { it ?: LocalDate.MAX }).forEach { (date, dateRows) ->
            Text(date?.format(dateFormatter) ?: "Unclear OCR lines", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            dateRows.forEach { row ->
                ScheduleReviewRowCard(
                    row = row,
                    onChange = { changed -> onRowsChanged(rows.map { if (it.id == row.id) changed else it }) },
                    onDelete = { onRowsChanged(rows.filterNot { it.id == row.id }) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleReviewRowCard(row: ScheduleReviewRow, onChange: (ScheduleReviewRow) -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ConfidenceChip(row.confidence)
                if (row.kind == ScheduleReviewKind.DayOff) AssistChip(onClick = {}, label = { Text("Day off") })
                if (row.kind == ScheduleReviewKind.Ignored) AssistChip(onClick = {}, label = { Text("Ignored") })
            }
            if (row.kind != ScheduleReviewKind.Ignored) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = row.selected,
                        onCheckedChange = { checked -> onChange(row.copy(selected = checked, confidence = if (checked) ScheduleImportConfidence.NeedsReview else row.confidence)) }
                    )
                    Text("Confirmed for import", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (row.sourceText.isNotBlank()) {
                Text(row.sourceText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (row.kind != ScheduleReviewKind.Ignored) {
                OutlinedTextField(
                    value = row.date.toString(),
                    onValueChange = { value -> parseReviewDate(value)?.let { onChange(row.copy(date = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) } },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (row.kind == ScheduleReviewKind.Shift) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = row.start.toString(),
                        onValueChange = { value -> parseReviewTime(value)?.let { onChange(row.copy(start = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) } },
                        label = { Text("Start") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = row.end.toString(),
                        onValueChange = { value -> parseReviewTime(value)?.let { onChange(row.copy(end = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) } },
                        label = { Text("End") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = row.title,
                    onValueChange = { onChange(row.copy(title = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = row.location,
                    onValueChange = { onChange(row.copy(location = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = row.notes,
                    onValueChange = { onChange(row.copy(notes = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (row.kind != ScheduleReviewKind.Shift) {
                    OutlinedButton(onClick = { onChange(row.copy(kind = ScheduleReviewKind.Shift, title = if (row.title == "Day off") "Work" else row.title, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) }, modifier = Modifier.weight(1f)) {
                        Text("Shift")
                    }
                }
                if (row.kind != ScheduleReviewKind.DayOff) {
                    OutlinedButton(onClick = { onChange(row.copy(kind = ScheduleReviewKind.DayOff, title = "Day off", selected = true, confidence = ScheduleImportConfidence.NeedsReview)) }, modifier = Modifier.weight(1f)) {
                        Text("Day off")
                    }
                }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun ConfidenceChip(confidence: ScheduleImportConfidence) {
    val color = when (confidence) {
        ScheduleImportConfidence.High -> Color(0xFF54D17A)
        ScheduleImportConfidence.NeedsReview -> Color(0xFFFFB020)
        ScheduleImportConfidence.Unclear -> Color(0xFFFF5A66)
    }
    AssistChip(
        onClick = {},
        label = { Text(confidence.label) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.16f), labelColor = color)
    )
}

private fun parseReviewDate(value: String): LocalDate? = runCatching { LocalDate.parse(value.trim()) }.getOrNull()

private fun parseReviewTime(value: String): LocalTime? = runCatching { LocalTime.parse(value.trim()) }.getOrNull()

@Composable
private fun ScheduleChangeSummary(changes: ScheduleChangeSet, title: String = "Schedule changes") {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (changes.hasChanges) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (!changes.hasChanges) {
                Text("No changes found against your saved schedule.", style = MaterialTheme.typography.bodyMedium)
                return@Column
            }
            changes.changedShifts.forEach { change ->
                ChangeLine(
                    "Changed",
                    "${change.date.format(dateFormatter)}: ${change.oldShift.start.format(timeFormatter)} - ${change.oldShift.end.format(timeFormatter)} to ${change.newShift.start.format(timeFormatter)} - ${change.newShift.end.format(timeFormatter)}"
                )
                if (change.oldShift.label != change.newShift.label) {
                    ChangeLine("Role", "${change.oldShift.label} to ${change.newShift.label}")
                }
            }
            changes.addedShifts.forEach { shift ->
                ChangeLine("Added", shift.displayLine())
            }
            changes.removedShifts.forEach { shift ->
                ChangeLine("Removed", shift.displayLine())
            }
            changes.newDaysOff.sorted().forEach { date ->
                ChangeLine("Day off", "${date.format(dateFormatter)} is now not scheduled")
            }
            changes.removedDaysOff.sorted().forEach { date ->
                ChangeLine("Scheduled", "${date.format(dateFormatter)} is no longer marked off")
            }
            changes.overtimeWarnings.forEach { warning ->
                ChangeLine("Hours", warning)
            }
        }
    }
}

@Composable
private fun ChangeLine(label: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

private fun WorkShift.displayLine(): String {
    return "${date.format(dateFormatter)}: ${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

@Composable
private fun ManualCorrectionRows(lines: List<String>) {
    if (lines.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Review detected lines", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        lines.take(8).forEach { line ->
            ManualCorrectionRow(line = line)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualCorrectionRow(line: String) {
    var expanded by remember(line) { mutableStateOf(false) }
    var action by remember(line) { mutableStateOf("Ignore") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(line, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = action,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.width(132.dp).menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Shift", "Day Off", "Role", "Store", "Ignore").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            action = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedScheduleRows(parsed: ParsedSchedule) {
    val dates = (parsed.daysOff + parsed.shifts.map { it.date }).sorted()
    dates.forEach { date ->
        if (date in parsed.daysOff) {
            Text("${date.format(dateFormatter)}  Not scheduled")
        }
        parsed.shifts.filter { it.date == date }.sortedBy { it.start }.forEach { shift ->
            Text("${shift.date.format(dateFormatter)}  ${shift.start.format(timeFormatter)} - ${shift.end.format(timeFormatter)}")
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Tasks : Screen("tasks", "Today", Icons.Default.CheckCircle)
    data object Notes : Screen("notes", "Notes", Icons.AutoMirrored.Filled.Notes)
    data object Schedule : Screen("schedule", "Schedule", Icons.Default.CalendarMonth)
    data object Manager : Screen("manager", "Manager", Icons.Default.Event)
    data object Import : Screen("import", "Import", Icons.Default.FileUpload)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object TaskDetail : Screen("task", "Task", Icons.Default.CheckCircle)
    data object EventDetail : Screen("event", "Event", Icons.Default.Event)
}

private fun RepeatRule.displayName(): String = when (this) {
    RepeatRule.None -> "None"
    RepeatRule.Daily -> "Daily"
    RepeatRule.Weekdays -> "Weekdays"
    RepeatRule.Weekly -> "Weekly"
    RepeatRule.CustomDays -> "Custom days"
    RepeatRule.EveryWorkday -> "Every workday"
    RepeatRule.OpeningShifts -> "Opening shifts"
    RepeatRule.ClosingShifts -> "Closing shifts"
    RepeatRule.TruckDays -> "Truck days"
}

private fun TaskItem.repeatLabel(): String {
    return if (repeatRule == RepeatRule.CustomDays && repeatDays.isNotEmpty()) {
        repeatDays.sortedBy(DayOfWeek::getValue).joinToString(", ") { it.shortLabel() }
    } else {
        repeatRule.displayName()
    }
}

private fun DayOfWeek.shortLabel(): String = when (this) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}
