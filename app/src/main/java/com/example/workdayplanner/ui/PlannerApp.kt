package com.example.workdayplanner.ui

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.provider.AlarmClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.animation.animateContentSize
import com.example.workdayplanner.PlannerViewModel
import com.example.workdayplanner.TrainingImportUiState
import com.example.workdayplanner.alarm.AlarmScheduler
import com.example.workdayplanner.calendar.DeviceCalendar
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AlarmDelivery
import com.example.workdayplanner.data.AlarmDispatchStatus
import com.example.workdayplanner.data.AlarmSettings
import com.example.workdayplanner.data.AppearanceMode
import com.example.workdayplanner.data.AppThemeStyle
import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.CarryOverBehavior
import com.example.workdayplanner.data.LinkedShiftType
import com.example.workdayplanner.data.RepeatRule
import com.example.workdayplanner.data.ReminderType
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
import com.example.workdayplanner.data.ShiftAlarmSettings
import com.example.workdayplanner.data.TimecardCalculator
import com.example.workdayplanner.data.TimecardEntry
import com.example.workdayplanner.data.TrainingItem
import com.example.workdayplanner.data.WorkChecklistTemplate
import com.example.workdayplanner.data.WorkChecklistTemplates
import com.example.workdayplanner.data.WorkImage
import com.example.workdayplanner.data.WorkShift
import com.example.workdayplanner.data.WorkEvent
import com.example.workdayplanner.data.WorkNoteOrganizer
import com.example.workdayplanner.data.WorkNoteTemplates
import com.example.workdayplanner.data.WorkVoiceCaptureParser
import com.example.workdayplanner.data.WorkVoiceCaptureType
import com.example.workdayplanner.data.VoiceTaskParser
import com.example.workdayplanner.data.VoiceTaskParseResult
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
import kotlinx.coroutines.delay

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

private enum class VoicePanelState {
    Hidden,
    Listening,
    Ambiguous,
    Duplicate,
    Success,
    Error
}

@Composable
private fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}

@Composable
private fun Modifier.workdayPressScale(enabled: Boolean = true): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) 0.985f else 1f,
        animationSpec = tween(110),
        label = "pressScale"
    )
    return this.scale(scale)
}

@Composable
private fun WorkdayAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberMotionEnabled()
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (motionEnabled) fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 10 } else fadeIn(tween(80)),
        exit = if (motionEnabled) fadeOut(tween(140)) + shrinkVertically(tween(160)) else fadeOut(tween(80))
    ) {
        content()
    }
}

private val workNoteKinds = listOf(
    WorkNoteKind.ShiftNote,
    WorkNoteKind.ManagerHandoff,
    WorkNoteKind.OrderNote,
    WorkNoteKind.TruckNote,
    WorkNoteKind.InventoryNote,
    WorkNoteKind.EmployeeTrainingNote,
    WorkNoteKind.ReminderNote,
    WorkNoteKind.PayTimecardNote
)

private fun noteSort(): Comparator<WorkNote> =
    compareByDescending<WorkNote> { it.pinned }
        .thenByDescending { it.date }
        .thenByDescending { it.createdAt }

@Composable
fun PlannerApp(
    viewModel: PlannerViewModel,
    requestedTaskId: String? = null,
    voiceTaskLaunchRequest: Int = 0,
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
    var openImportAfterIntro by remember { mutableStateOf(false) }
    val showIntro = !state.onboardingCompleted && !state.hasPlannerData()
    val motionEnabled = rememberMotionEnabled()

    LaunchedEffect(requestedTaskId, state.tasks) {
        val taskId = requestedTaskId?.takeIf { id -> state.tasks.any { it.id == id } } ?: return@LaunchedEffect
        navController.navigate("${Screen.TaskDetail.route}/$taskId") {
            launchSingleTop = true
        }
        onTaskRequestHandled()
    }

    LaunchedEffect(showIntro, openImportAfterIntro) {
        if (!showIntro && openImportAfterIntro) {
            openImportAfterIntro = false
            navController.navigate(Screen.Import.route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(voiceTaskLaunchRequest) {
        if (voiceTaskLaunchRequest > 0) {
            if (showIntro) viewModel.completeOnboarding()
            navController.navigate(Screen.Tasks.route) {
                launchSingleTop = true
            }
        }
    }

    fun openPremium() {
        showPremiumScreen = true
        navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = when {
                    showIntro -> "Workday Planner"
                    currentRoute.startsWith(Screen.TaskDetail.route) -> "Task"
                    currentRoute.startsWith(Screen.EventDetail.route) -> "Event"
                    currentRoute == Screen.Manager.route -> "Manager"
                    currentRoute == Screen.Notes.route -> "Notes"
                    currentRoute == Screen.WeeklyReview.route -> "Weekly Review"
                    currentRoute == Screen.Schedule.route -> "Schedule"
                    currentRoute == Screen.Import.route -> "Import"
                    showPremiumScreen -> "Premium"
                    currentRoute == Screen.Settings.route -> "Settings"
                    currentRoute == Screen.Tasks.route -> "Today"
                    else -> "Workday Planner"
                }
            )
        },
        bottomBar = {
            if (!showIntro) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    topLevel.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                showPremiumScreen = false
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Tasks.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, maxLines = 1) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!showIntro && currentRoute == Screen.Tasks.route) {
                VoiceTaskFloatingCapture(
                    state = state,
                    launchRequest = voiceTaskLaunchRequest,
                    onSaveTask = viewModel::saveTask,
                    onDeleteTask = viewModel::deleteTask,
                    onEditTask = { navController.navigate("${Screen.TaskDetail.route}/${it.id}") }
                )
            }
        }
    ) { padding ->
        if (showIntro) {
            WorkdayIntroScreen(
                modifier = Modifier.padding(padding),
                onImportSchedule = {
                    openImportAfterIntro = true
                    viewModel.completeOnboarding()
                },
                onStart = viewModel::completeOnboarding
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Screen.Tasks.route,
                modifier = Modifier.padding(padding),
                enterTransition = {
                    if (motionEnabled) fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 12 } else fadeIn(tween(80))
                },
                exitTransition = {
                    if (motionEnabled) fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { -it / 18 } else fadeOut(tween(80))
                },
                popEnterTransition = {
                    if (motionEnabled) fadeIn(tween(180)) + slideInHorizontally(tween(220)) { -it / 12 } else fadeIn(tween(80))
                },
                popExitTransition = {
                    if (motionEnabled) fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { it / 18 } else fadeOut(tween(80))
                }
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
                    onOpenNotes = { navController.navigate(Screen.Notes.route) },
                    onMarkTodayOff = { viewModel.addTypedDayOff(LocalDate.now(), ShiftTemplateKind.DayOff) },
                    onWeeklyReview = { navController.navigate(Screen.WeeklyReview.route) },
                    onAddEvent = { navController.navigate("${Screen.EventDetail.route}/new") },
                    onToggleComplete = viewModel::toggleComplete,
                    onDelete = viewModel::deleteTask,
                    onDeleteEvent = viewModel::deleteEvent,
                    onClockIn = viewModel::clockIn,
                    onStartLunch = viewModel::startLunch,
                    onEndLunch = viewModel::endLunch,
                    onClockOut = viewModel::clockOut,
                    onSaveTimecardEntry = viewModel::saveTimecardEntry,
                    onAddChecklist = viewModel::addChecklistTemplate,
                    onSaveVoiceTask = viewModel::saveTask,
                    onOpenPremium = ::openPremium
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    state = state,
                    onSaveNote = viewModel::saveWorkNote,
                    onDeleteNote = viewModel::deleteWorkNote,
                    onCreateTaskFromNote = viewModel::createTaskFromNote,
                    onCreateChecklistFromNote = viewModel::createChecklistFromNote,
                    onTogglePinned = viewModel::toggleWorkNotePinned,
                    onToggleArchived = viewModel::toggleWorkNoteArchived,
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
            composable(Screen.WeeklyReview.route) {
                WeeklyReviewScreen(
                    state = state,
                    onSaveTimecardEntry = viewModel::saveTimecardEntry,
                    onAddTaskNextWeek = { navController.navigate("${Screen.TaskDetail.route}/new") },
                    onReviewSchedule = { navController.navigate(Screen.Schedule.route) },
                    onClearCompletedTasks = viewModel::clearCompletedTasks
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    state = state,
                    onAddShift = viewModel::saveShift,
                    onAddTask = { viewModel.saveTask(it) },
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
                    onOpenPremium = ::openPremium
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
                    onApply = { corrected ->
                        viewModel.applyImport(corrected)
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.Tasks.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onStartOver = viewModel::resetScheduleImport,
                    onOpenPremium = ::openPremium
                )
            }
            composable(Screen.Settings.route) {
                if (showPremiumScreen) {
                    PremiumScreen(
                        state = state,
                        onTesterModeChanged = viewModel::setMockPremium,
                        onBack = { showPremiumScreen = false }
                    )
                } else {
                    SettingsScreen(
                        state = state,
                        onAppearanceModeChanged = viewModel::setAppearanceMode,
                        onAccentStyleChanged = viewModel::setAccentStyle,
                        onWidgetLayoutModeChanged = viewModel::setWidgetLayoutMode,
                        onPaySettingsChanged = viewModel::setPaySettings,
                        onShiftAlarmSettingsChanged = viewModel::setShiftAlarmSettings,
                        onAlarmSettingsChanged = viewModel::setAlarmSettings,
                        calendars = calendars,
                        calendarMessage = calendarMessage,
                        onLoadCalendars = viewModel::loadCalendars,
                        onSelectCalendar = viewModel::setSelectedCalendar,
                        onSyncCalendar = viewModel::syncShiftsToCalendar,
                        onNotificationPermissionNeeded = onNotificationPermissionNeeded,
                        onTesterModeChanged = viewModel::setMockPremium,
                        onOpenPremium = ::openPremium
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
                    onSaveAndContinue = { viewModel.saveTask(it) },
                    onSaveTaskTemplate = viewModel::saveTaskTemplate,
                    onDeleteTaskTemplate = viewModel::deleteTaskTemplate,
                    onNotificationPermissionNeeded = onNotificationPermissionNeeded,
                    onOpenPremium = ::openPremium,
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
private fun VoiceTaskFloatingCapture(
    state: AppState,
    launchRequest: Int,
    onSaveTask: (TaskItem) -> TaskItem,
    onDeleteTask: (String) -> Unit,
    onEditTask: (TaskItem) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var listening by remember { mutableStateOf(false) }
    var processing by remember { mutableStateOf(false) }
    var partialTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var pendingPermissionStart by remember { mutableStateOf(false) }
    var createdVoiceTasks by remember { mutableStateOf<List<TaskItem>>(emptyList()) }
    var ambiguousVoiceTask by remember { mutableStateOf<VoiceTaskParseResult?>(null) }
    var duplicateCandidate by remember { mutableStateOf<TaskItem?>(null) }
    var lastFingerprint by remember { mutableStateOf<String?>(null) }
    var lastCreatedAt by remember { mutableStateOf<LocalDateTime?>(null) }
    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember(recognizerAvailable) {
        if (recognizerAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingPermissionStart = true
        } else {
            voiceError = "Microphone permission denied. You can still add tasks by typing."
        }
    }

    fun saveParsedTasks(parsed: List<VoiceTaskParseResult>) {
        val tasksToSave = parsed.map { it.toTask(alarmDelivery = state.alarmSettings.defaultAlarmDelivery) }
        val firstDuplicate = tasksToSave.firstOrNull { task ->
            isRecentVoiceDuplicate(state.tasks, task) || isImmediateDuplicate(lastFingerprint, lastCreatedAt, task)
        }
        if (firstDuplicate != null) {
            duplicateCandidate = firstDuplicate
            voiceError = null
            return
        }
        val saved = tasksToSave.map(onSaveTask)
        createdVoiceTasks = saved
        lastFingerprint = saved.lastOrNull()?.voiceTaskFingerprint()
        lastCreatedAt = LocalDateTime.now()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun applyCorrection(text: String): Boolean {
        val latest = createdVoiceTasks.lastOrNull() ?: return false
        if (latest.createdAt.isBefore(LocalDateTime.now().minusSeconds(20))) return false
        val corrected = applyVoiceTaskCorrection(latest, text) ?: return false
        val saved = onSaveTask(corrected)
        createdVoiceTasks = listOf(saved)
        lastFingerprint = saved.voiceTaskFingerprint()
        lastCreatedAt = LocalDateTime.now()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        return true
    }

    fun processTranscript(text: String) {
        val transcript = text.trim()
        processing = true
        if (transcript.isBlank()) {
            processing = false
            voiceError = "Couldn't hear that. Tap to try again."
            return
        }
        if (applyCorrection(transcript)) {
            processing = false
            return
        }
        if (transcript.equals("cancel that", ignoreCase = true) && createdVoiceTasks.isNotEmpty()) {
            createdVoiceTasks.forEach { onDeleteTask(it.id) }
            createdVoiceTasks = emptyList()
            processing = false
            return
        }
        val parsed = parseVoiceTaskResults(transcript, state)
        val first = parsed.firstOrNull()
        if (parsed.size == 1 && first != null && "AM/PM unclear" in first.ambiguityReasons && first.dueAt != null) {
            ambiguousVoiceTask = first
            processing = false
            return
        }
        saveParsedTasks(parsed)
        processing = false
    }

    fun startVoiceCapture() {
        val recognizer = speechRecognizer
        if (!recognizerAvailable || recognizer == null) {
            voiceError = "Speech recognition is not available on this device."
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            voiceError = "Workday Planner needs microphone access to turn speech into a task."
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        partialTranscript = ""
        voiceError = null
        duplicateCandidate = null
        ambiguousVoiceTask = null
        listening = true
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        runCatching {
            recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak one work task")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            })
        }.onFailure {
            listening = false
            voiceError = "Couldn't hear that. Tap to try again."
        }
    }

    fun stopVoiceCapture() {
        speechRecognizer?.stopListening()
        listening = false
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun cancelVoiceCapture() {
        speechRecognizer?.cancel()
        listening = false
        partialTranscript = ""
        voiceError = null
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceError = null
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                listening = false
            }

            override fun onError(error: Int) {
                listening = false
                voiceError = voiceTaskErrorMessage(error, partialTranscript)
                if (partialTranscript.isNotBlank() && error == SpeechRecognizer.ERROR_NO_MATCH) {
                    processTranscript(partialTranscript)
                }
            }

            override fun onResults(results: Bundle?) {
                listening = false
                processTranscript(results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty())
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialTranscript = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { speechRecognizer?.destroy() }
    }

    LaunchedEffect(pendingPermissionStart) {
        if (pendingPermissionStart) {
            pendingPermissionStart = false
            startVoiceCapture()
        }
    }
    LaunchedEffect(launchRequest) {
        if (launchRequest > 0) startVoiceCapture()
    }
    LaunchedEffect(createdVoiceTasks) {
        if (createdVoiceTasks.isNotEmpty()) {
            delay(8000)
            createdVoiceTasks = emptyList()
        }
    }

    val voicePanelState = when {
        listening || processing -> VoicePanelState.Listening
        ambiguousVoiceTask != null -> VoicePanelState.Ambiguous
        duplicateCandidate != null -> VoicePanelState.Duplicate
        createdVoiceTasks.isNotEmpty() -> VoicePanelState.Success
        voiceError != null -> VoicePanelState.Error
        else -> VoicePanelState.Hidden
    }
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(340.dp)) {
        AnimatedContent(
            targetState = voicePanelState,
            transitionSpec = {
                (fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 8 })
                    .togetherWith(fadeOut(tween(120)) + slideOutVertically(tween(140)) { -it / 10 })
            },
            label = "voicePanel"
        ) { panel ->
            when (panel) {
                VoicePanelState.Hidden -> Spacer(Modifier.height(0.dp))
                VoicePanelState.Listening -> VoiceTaskListeningCard(
                    transcript = if (processing) "Creating task..." else partialTranscript,
                    error = voiceError,
                    onCancel = ::cancelVoiceCapture,
                    onStop = ::stopVoiceCapture
                )
                VoicePanelState.Ambiguous -> ambiguousVoiceTask?.let {
                    VoiceTaskAmbiguityCard(
                        result = it,
                        onChoose = { adjusted ->
                            saveParsedTasks(listOf(adjusted))
                            ambiguousVoiceTask = null
                        },
                        onCancel = { ambiguousVoiceTask = null }
                    )
                }
                VoicePanelState.Duplicate -> VoiceTaskDuplicateCard(
                    onKeep = {
                        val saved = onSaveTask(duplicateCandidate!!)
                        createdVoiceTasks = listOf(saved)
                        duplicateCandidate = null
                    },
                    onDismiss = { duplicateCandidate = null }
                )
                VoicePanelState.Success -> VoiceTaskBatchConfirmationCard(
                    tasks = createdVoiceTasks,
                    onUndo = {
                        createdVoiceTasks.forEach { onDeleteTask(it.id) }
                        createdVoiceTasks = emptyList()
                    },
                    onEdit = { createdVoiceTasks.lastOrNull()?.let(onEditTask) }
                )
                VoicePanelState.Error -> VoiceTaskErrorCard(message = voiceError.orEmpty(), onRetry = ::startVoiceCapture)
            }
        }
        FloatingActionButton(
            onClick = { if (listening) stopVoiceCapture() else startVoiceCapture() },
            containerColor = if (listening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (listening) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                if (listening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (listening) "Stop listening for voice task" else "Start voice task"
            )
        }
    }
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
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
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
    onOpenNotes: () -> Unit,
    onMarkTodayOff: () -> Unit,
    onWeeklyReview: () -> Unit,
    onAddEvent: () -> Unit,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onClockIn: () -> Unit,
    onStartLunch: () -> Unit,
    onEndLunch: () -> Unit,
    onClockOut: () -> Unit,
    onSaveTimecardEntry: (TimecardEntry) -> Unit,
    onAddChecklist: (String) -> Unit,
    onSaveVoiceTask: (TaskItem) -> TaskItem,
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
                onOpenNotes = onOpenNotes,
                onMarkTodayOff = onMarkTodayOff,
                onWeeklyReview = onWeeklyReview,
                onSaveVoiceTask = onSaveVoiceTask,
                onEditVoiceTask = onTaskClick,
                onUndoVoiceTask = onDelete
            )
            TimecardSection(
                state = state,
                onClockIn = onClockIn,
                onStartLunch = onStartLunch,
                onEndLunch = onEndLunch,
                onClockOut = onClockOut,
                onSaveEntry = onSaveTimecardEntry
            )
            ChecklistTemplateSection(onAddChecklist = onAddChecklist)
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
                onOpenNotes = onOpenNotes,
                onMarkTodayOff = onMarkTodayOff,
                onWeeklyReview = onWeeklyReview,
                onSaveVoiceTask = onSaveVoiceTask,
                onEditVoiceTask = onTaskClick,
                onUndoVoiceTask = onDelete
            )
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
        item {
            TimecardSection(
                state = state,
                onClockIn = onClockIn,
                onStartLunch = onStartLunch,
                onEndLunch = onEndLunch,
                onClockOut = onClockOut,
                onSaveEntry = onSaveTimecardEntry
            )
        }
        item { ChecklistTemplateSection(onAddChecklist = onAddChecklist) }
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
                    modifier = Modifier.animateItem(),
                    onClick = { onEventClick(event) },
                    onDelete = { onDeleteEvent(event.id) }
                )
            }
        }
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
                    color = MaterialTheme.colorScheme.error,
                    enabled = overdueCount > 0,
                    onClick = onShowOverdue
                )
                FocusChip(
                    label = "$criticalCount critical",
                    color = MaterialTheme.colorScheme.error,
                    enabled = criticalCount > 0,
                    onClick = onShowImportant
                )
                FocusChip(
                    label = "$dueSoonCount due soon",
                    color = MaterialTheme.colorScheme.warning,
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
            modifier = Modifier.animateItem(),
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
                    Button(onClick = { onSelected(view) }, modifier = Modifier.animateContentSize(animationSpec = tween(150))) {
                        AnimatedContent(targetState = label, label = "taskViewLabel") { value -> Text(value) }
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(view) }, modifier = Modifier.animateContentSize(animationSpec = tween(150))) {
                        AnimatedContent(targetState = label, label = "taskViewLabel") { value -> Text(value) }
                    }
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
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Manager dashboard",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onViewOpen, label = { Text("$openTrainingCount open training") })
                AssistChip(onClick = onViewAssociates, label = { Text("$associateCount associates") })
                AssistChip(
                    onClick = onViewOverdue,
                    label = { Text("$overdueCount overdue") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.dangerContainer,
                        labelColor = MaterialTheme.colorScheme.onDangerContainer
                    )
                )
                AssistChip(
                    onClick = onViewDueSoon,
                    label = { Text("$dueSoonCount due soon") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.warningContainer,
                        labelColor = MaterialTheme.colorScheme.onWarningContainer
                    )
                )
                AssistChip(
                    onClick = {
                        if (overdueCount > 0) onViewOverdue() else onViewDueSoon()
                    },
                    label = { Text("$attentionCount need attention") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
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
        group.overdueCount > 0 -> MaterialTheme.colorScheme.error
        group.dueSoonCount > 0 -> MaterialTheme.colorScheme.warning
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
                            containerColor = MaterialTheme.colorScheme.dangerContainer,
                            labelColor = MaterialTheme.colorScheme.onDangerContainer
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${group.dueSoonCount} due soon") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.warningContainer,
                            labelColor = MaterialTheme.colorScheme.onWarningContainer
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
                            containerColor = MaterialTheme.colorScheme.dangerContainer,
                            labelColor = MaterialTheme.colorScheme.onDangerContainer
                        )
                    )
                AssistChip(
                    onClick = {},
                        label = { Text("$dueSoonCount due 7 days") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.warningContainer,
                            labelColor = MaterialTheme.colorScheme.onWarningContainer
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
        item.completedAt != null -> MaterialTheme.colorScheme.success
        item.dueDate?.isBefore(today) == true -> MaterialTheme.colorScheme.error
        item.dueDate?.let { !it.isAfter(today.plusDays(7)) } == true -> MaterialTheme.colorScheme.warning
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
    onSaveNote: (WorkNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCreateTaskFromNote: (String) -> Unit,
    onCreateChecklistFromNote: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleArchived: (String) -> Unit,
    imageMessage: String?,
    onAddImage: (String, Uri) -> Unit,
    onDeleteImage: (String) -> Unit
) {
    val today = LocalDate.now()
    val visibleNotes = state.notes.filterNot { it.archived }
    val todayNotes = visibleNotes.filter { it.date == today }.sortedWith(noteSort())
    val recentNotes = visibleNotes.filterNot { it.date == today }.sortedWith(noteSort())
    val images = state.images.sortedWith(compareByDescending<WorkImage> { it.date }.thenByDescending { it.createdAt })

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            DailyNotesSection(
                todayNotes = todayNotes,
                recentNotes = recentNotes,
                shifts = state.shifts,
                onSaveNote = onSaveNote,
                onDeleteNote = onDeleteNote,
                onCreateTaskFromNote = onCreateTaskFromNote,
                onCreateChecklistFromNote = onCreateChecklistFromNote,
                onTogglePinned = onTogglePinned,
                onToggleArchived = onToggleArchived
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
    onOpenNotes: () -> Unit,
    onMarkTodayOff: () -> Unit,
    onWeeklyReview: () -> Unit,
    onSaveVoiceTask: (TaskItem) -> TaskItem,
    onEditVoiceTask: (TaskItem) -> Unit,
    onUndoVoiceTask: (String) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val today = LocalDate.now()
    val now = LocalDateTime.now()
    var listening by remember { mutableStateOf(false) }
    var partialTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var pendingPermissionStart by remember { mutableStateOf(false) }
    var createdVoiceTask by remember { mutableStateOf<TaskItem?>(null) }
    var ambiguousVoiceTask by remember { mutableStateOf<VoiceTaskParseResult?>(null) }
    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember(recognizerAvailable) {
        if (recognizerAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingPermissionStart = true
        } else {
            voiceError = "Microphone permission denied. You can still add tasks by typing."
        }
    }
    val todayShifts = state.shifts.filter { it.date == today }.sortedBy { it.start }
    val nextShift = state.shifts
        .filter { shift -> shift.endDateTime().isAfter(now) }
        .minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val todayStatus = today.workStatusLabel(state, todayShifts)
    val todayTasks = state.tasks.filter { !it.completed && it.deadline?.toLocalDate() == today && !it.isSkippedBecauseDayOff(state, today) }
    val overdueTasks = state.tasks.filter { !it.completed && it.deadline?.isBefore(now) == true }
    val remindersToday = state.tasks.filter { !it.completed && it.alarmAt?.toLocalDate() == today }
    val weekHours = PayEstimator.estimateWeek(state, today).paidHours
    val watchOuts = dashboardWatchOuts(state, today, now, nextShift, overdueTasks)

    fun createTaskFromTranscript(text: String) {
        val transcript = text.trim()
        if (transcript.isBlank()) {
            voiceError = "Couldn't hear that. Tap to try again."
            return
        }
        val parsed = parseVoiceTaskResults(transcript, state).firstOrNull() ?: VoiceTaskParser.parse(transcript)
        if ("AM/PM unclear" in parsed.ambiguityReasons && parsed.dueAt != null) {
            ambiguousVoiceTask = parsed
            partialTranscript = transcript
            voiceError = null
            return
        }
        val task = parsed.toTask(alarmDelivery = state.alarmSettings.defaultAlarmDelivery)
        val savedTask = onSaveVoiceTask(task)
        createdVoiceTask = savedTask
        partialTranscript = transcript
        voiceError = null
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun startVoiceCapture() {
        val recognizer = speechRecognizer
        if (!recognizerAvailable || recognizer == null) {
            voiceError = "Speech recognition is not available on this device."
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            voiceError = "Workday Planner needs microphone access to turn speech into a task."
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        partialTranscript = ""
        voiceError = null
        createdVoiceTask = null
        ambiguousVoiceTask = null
        listening = true
        runCatching {
            recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak one work task")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            })
        }.onFailure {
            listening = false
            voiceError = "Couldn't hear that. Tap to try again."
        }
    }

    fun stopVoiceCapture() {
        speechRecognizer?.stopListening()
        listening = false
    }

    fun cancelVoiceCapture() {
        speechRecognizer?.cancel()
        listening = false
        partialTranscript = ""
        voiceError = null
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceError = null
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                listening = false
            }

            override fun onError(error: Int) {
                listening = false
                voiceError = voiceTaskErrorMessage(error, partialTranscript)
                if (partialTranscript.isNotBlank() && error == SpeechRecognizer.ERROR_NO_MATCH) {
                    createTaskFromTranscript(partialTranscript)
                }
            }

            override fun onResults(results: Bundle?) {
                listening = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                partialTranscript = text
                createTaskFromTranscript(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialTranscript = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { speechRecognizer?.destroy() }
    }

    LaunchedEffect(pendingPermissionStart) {
        if (pendingPermissionStart) {
            pendingPermissionStart = false
            startVoiceCapture()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(today.format(dateFormatter), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DashboardStatusPill(todayStatus)
        }
        NextShiftDashboardCard(
            nextShift = nextShift,
            now = now,
            hasAnySchedule = state.shifts.isNotEmpty() || state.daysOff.isNotEmpty(),
            onImportSchedule = onImportSchedule,
            onScheduleShortcut = onScheduleShortcut
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard(
                    label = "Work tasks",
                    value = todayTasks.size.coerceAtLeast(todayTaskCount).toString(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    label = "Overdue",
                    value = overdueTasks.size.coerceAtLeast(overdueTaskCount).toString(),
                    containerColor = MaterialTheme.colorScheme.dangerContainer,
                    contentColor = MaterialTheme.colorScheme.onDangerContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                DashboardStatCard(
                    label = "Reminders",
                    value = remindersToday.size.coerceAtLeast(upcomingAlarmCount).toString(),
                    containerColor = MaterialTheme.colorScheme.warningContainer,
                    contentColor = MaterialTheme.colorScheme.onWarningContainer,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    label = "Hours this week",
                    value = weekHours.toSimpleString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        DashboardNotesCard(state = state, onOpenNotes = onOpenNotes)
        if (state.paySettings.showPayOnDashboard && state.paySettings.hourlyRate > 0.0) {
            DashboardPayEstimateCard(state)
        }
        TodayWorkTasksCard(
            tasks = todayTasks,
            hasSchedule = state.shifts.isNotEmpty(),
            onAddFromTemplate = onAddRepeatingTask,
            onAddTask = onAddTask
        )
        if (watchOuts.isNotEmpty()) {
            DashboardWatchOutsCard(watchOuts)
        }
        Button(onClick = ::startVoiceCapture, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Speak task")
        }
        if (listening || partialTranscript.isNotBlank() && createdVoiceTask == null && ambiguousVoiceTask == null) {
            VoiceTaskListeningCard(
                transcript = partialTranscript,
                error = voiceError,
                onCancel = ::cancelVoiceCapture,
                onStop = ::stopVoiceCapture
            )
        } else if (voiceError != null) {
            VoiceTaskErrorCard(message = voiceError.orEmpty(), onRetry = ::startVoiceCapture)
        }
        ambiguousVoiceTask?.let { result ->
            VoiceTaskAmbiguityCard(
                result = result,
                onChoose = { adjusted ->
                    val task = adjusted.toTask(alarmDelivery = state.alarmSettings.defaultAlarmDelivery)
                    val savedTask = onSaveVoiceTask(task)
                    createdVoiceTask = savedTask
                    ambiguousVoiceTask = null
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onCancel = { ambiguousVoiceTask = null }
            )
        }
        createdVoiceTask?.let { task ->
            VoiceTaskConfirmationCard(
                task = task,
                onUndo = {
                    onUndoVoiceTask(task.id)
                    createdVoiceTask = null
                },
                onEdit = { onEditVoiceTask(task) }
            )
        }
        SectionHeader("Quick actions")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardActionButton("Add shift", Icons.Default.CalendarMonth, onScheduleShortcut, Modifier.weight(1f), outlined = true)
            DashboardActionButton("New task", Icons.Default.Add, onAddTask, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardActionButton("Import", Icons.Default.FileUpload, onImportSchedule, Modifier.weight(1f), outlined = true)
            DashboardActionButton("Mark day off", Icons.Default.CalendarMonth, onMarkTodayOff, Modifier.weight(1f), outlined = true)
        }
        OutlinedButton(onClick = onWeeklyReview, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Weekly review")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegacyCommandCenterCard(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Today",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                today.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                DashboardMetricChip(
                    "${todayTasks.size.coerceAtLeast(todayTaskCount)} work tasks",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                DashboardMetricChip(
                    "${overdueTasks.size.coerceAtLeast(overdueTaskCount)} overdue",
                    MaterialTheme.colorScheme.dangerContainer,
                    MaterialTheme.colorScheme.onDangerContainer
                )
                DashboardMetricChip(
                    "${remindersToday.size.coerceAtLeast(upcomingAlarmCount)} reminders",
                    MaterialTheme.colorScheme.warningContainer,
                    MaterialTheme.colorScheme.onWarningContainer
                )
                if (state.paySettings.showPayOnDashboard && state.paySettings.hourlyRate > 0.0 && todayPay.paidHours > 0.0) {
                    DashboardMetricChip("$${todayPay.grossPay.toMoneyString()} today", MaterialTheme.colorScheme.successContainer, MaterialTheme.colorScheme.onSuccessContainer)
                    DashboardMetricChip("${todayPay.paidHours.toSimpleString()} paid hrs", MaterialTheme.colorScheme.successContainer, MaterialTheme.colorScheme.onSuccessContainer)
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
private fun DashboardStatCard(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(colors = CardDefaults.cardColors(containerColor = containerColor), modifier = modifier) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 9.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TodayWorkTasksCard(
    tasks: List<TaskItem>,
    hasSchedule: Boolean,
    onAddFromTemplate: () -> Unit,
    onAddTask: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Today’s work tasks", "Work items due during this shift day.")
            val visible = tasks.take(3)
            if (visible.isEmpty()) {
                Text(
                    if (hasSchedule) "No tasks due today. Add a checklist or one-off reminder when needed." else "No tasks due today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visible.forEach { task ->
                    Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                if (tasks.size > visible.size) {
                    Text("+${tasks.size - visible.size} more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onAddFromTemplate, modifier = Modifier.weight(1f)) {
                    Text("Add from template")
                }
                Button(onClick = onAddTask, modifier = Modifier.weight(1f)) {
                    Text("New task")
                }
            }
        }
    }
}

@Composable
private fun DashboardStatusPill(label: String) {
    val containerColor = when (label) {
        "Workday" -> MaterialTheme.colorScheme.primaryContainer
        "Vacation", "Day Off" -> MaterialTheme.colorScheme.secondaryContainer
        "Sick Day" -> MaterialTheme.colorScheme.dangerContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = when (label) {
        "Workday" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Vacation", "Day Off" -> MaterialTheme.colorScheme.onSecondaryContainer
        "Sick Day" -> MaterialTheme.colorScheme.onDangerContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor, labelColor = labelColor)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
            OutlinedButton(onClick = onScheduleShortcut, modifier = Modifier.fillMaxWidth()) {
                Text("Edit shift")
            }
        }
    }
}

private data class DashboardWatchOut(val title: String, val detail: String, val severity: WatchSeverity)

private enum class WatchSeverity { Warning, Danger }

private fun dashboardWatchOuts(
    state: AppState,
    today: LocalDate,
    now: LocalDateTime,
    nextShift: WorkShift?,
    overdueTasks: List<TaskItem>
): List<DashboardWatchOut> {
    val week = PayEstimator.estimateWeek(state, today)
    val todayEntry = state.timecards.firstOrNull { it.date == today }
    val endedShiftWithoutClockOut = state.shifts.any { shift ->
        shift.date == today &&
            shift.endDateTime().isBefore(now) &&
            todayEntry?.clockIn != null &&
            todayEntry.clockOut == null
    }
    val startsSoon = nextShift?.startDateTime()?.let { start ->
        !start.isBefore(now) && Duration.between(now, start).toMinutes() in 0..60
    } == true
    return buildList {
        if (overdueTasks.isNotEmpty()) {
            add(DashboardWatchOut("Overdue task", "${overdueTasks.size} task${if (overdueTasks.size == 1) "" else "s"} past deadline.", WatchSeverity.Danger))
        }
        if (endedShiftWithoutClockOut) {
            add(DashboardWatchOut("Possible missed punch", "A shift has ended and your personal timecard is still open.", WatchSeverity.Danger))
        }
        if (week.overtimeHours > 0.0) {
            add(DashboardWatchOut("Overtime estimated", "${week.overtimeHours.toSimpleString()} overtime hrs this week.", WatchSeverity.Warning))
        } else if (week.hoursUntilOvertime in 0.01..4.0) {
            add(DashboardWatchOut("Close to overtime", "${week.hoursUntilOvertime.toSimpleString()} hrs until weekly overtime.", WatchSeverity.Warning))
        }
        if (startsSoon && todayEntry?.clockIn == null) {
            add(DashboardWatchOut("Shift starts soon", "${nextShift?.label?.ifBlank { "Work" } ?: "Shift"} starts ${nextShift?.timeUntilShift(now)}.", WatchSeverity.Warning))
        }
        ScheduleRiskAnalyzer.risks(state, today).take(2).forEach { risk ->
            add(DashboardWatchOut(risk.title, risk.detail, WatchSeverity.Warning))
        }
    }.distinctBy { it.title to it.detail }.take(4)
}

@Composable
private fun DashboardWatchOutsCard(watchOuts: List<DashboardWatchOut>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.warningContainer.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionHeader("Watch-outs")
            watchOuts.forEach { watch ->
                val color = if (watch.severity == WatchSeverity.Danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onWarningContainer
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(watch.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = color)
                    Text(watch.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onWarningContainer)
                }
            }
        }
    }
}

@Composable
private fun BeforeShiftBriefCard(
    state: AppState,
    nextShift: WorkShift?,
    now: LocalDateTime,
    onAddTask: () -> Unit,
    onScheduleShortcut: () -> Unit
) {
    if (nextShift == null) return
    val shiftStart = nextShift.startDateTime()
    val prepTasks = state.tasks
        .filter { task ->
            !task.completed &&
                task.workRelated &&
                task.deadline != null &&
                !task.deadline.isBefore(now) &&
                !task.deadline.isAfter(shiftStart)
        }
        .sortedWith(compareBy<TaskItem> { it.deadline }.thenByDescending { it.priority.sortWeight })
        .take(4)
    val settings = state.shiftAlarmSettings
    val alarmLine = if (settings.enabled) {
        val alarmAt = shiftStart.minusMinutes(settings.offsetMinutes.toLong())
        "Shift alarm: ${alarmAt.format(dateTimeFormatter)} (${shiftAlarmOffsetLabel(settings.offsetMinutes)} before)."
    } else {
        "Shift alarm is off. Turn it on in Settings when you want a wake-up alarm."
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Before shift", "What matters before ${nextShift.start.format(timeFormatter)}.")
            Text(alarmLine, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (prepTasks.isEmpty()) {
                Text(
                    "No prep tasks before this shift. Common ones: uniform, lunch, keys, check schedule.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                prepTasks.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            task.deadline?.let {
                                Text("Due ${it.format(dateTimeFormatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        AssistChip(onClick = {}, label = { Text(task.priority.label) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onScheduleShortcut, modifier = Modifier.weight(1f)) { Text("Review schedule") }
                Button(onClick = onAddTask, modifier = Modifier.weight(1f)) { Text("Add prep task") }
            }
        }
    }
}

@Composable
private fun DashboardNotesCard(state: AppState, onOpenNotes: () -> Unit) {
    val today = LocalDate.now()
    val priorityKinds = setOf(
        WorkNoteKind.Manager,
        WorkNoteKind.ManagerHandoff,
        WorkNoteKind.EmployeeTrainingNote,
        WorkNoteKind.ReminderNote,
        WorkNoteKind.PayTimecardNote,
        WorkNoteKind.Issue,
        WorkNoteKind.FollowUp,
        WorkNoteKind.Meeting
    )
    val visibleNotes = state.notes.filterNot { it.archived }
    val todayNotes = visibleNotes.filter { it.date == today }
    val priorityNotes = visibleNotes
        .filter { it.pinned || it.date == today || it.kind in priorityKinds }
        .sortedWith(noteSort())
        .take(3)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Work notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            visibleNotes.isEmpty() -> "Shift notes, handoffs, orders, and pay notes."
                            todayNotes.isNotEmpty() -> "${todayNotes.size} note${if (todayNotes.size == 1) "" else "s"} from today"
                            else -> "${visibleNotes.size} saved work note${if (visibleNotes.size == 1) "" else "s"}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = onOpenNotes,
                    label = { Text("Open") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) }
                )
            }
            if (priorityNotes.isEmpty()) {
                Text(
                    "No work notes yet. Add a quick shift note or voice capture when something needs remembered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                priorityNotes.forEach { note ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(note.kind.label) }
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                note.title.ifBlank { note.text.lineSequence().firstOrNull().orEmpty() }.take(120),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (note.pinned) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                listOf(
                                    note.date.format(shortDateFormatter),
                                    if (note.pinned) "Pinned" else null,
                                    note.tags.firstOrNull()
                                ).filterNotNull().distinct().joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            OutlinedButton(onClick = onOpenNotes, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (visibleNotes.isEmpty()) "Add work note" else "View all notes")
            }
        }
    }
}

@Composable
private fun DashboardTaskPreview(title: String, tasks: List<TaskItem>, emptyText: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
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

@Composable
private fun VoiceTaskListeningCard(
    transcript: String,
    error: String?,
    onCancel: () -> Unit,
    onStop: () -> Unit
) {
    val motionEnabled = rememberMotionEnabled()
    val pulse = if (motionEnabled) {
        val transition = rememberInfiniteTransition(label = "voicePulse")
        transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "voiceMicPulse"
        ).value
    } else {
        1f
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(200))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Listening",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                )
                Column(Modifier.weight(1f)) {
                    Text("Listening...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        transcript.ifBlank { "Say one task, like \"Box meat truck order at 11:30 AM.\"" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            error?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(onClick = onStop, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun VoiceTaskErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}

@Composable
private fun VoiceTaskConfirmationCard(task: TaskItem, onUndo: () -> Unit, onEdit: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.successContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.success.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSuccessContainer)
            Text(
                voiceTaskSummary(task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSuccessContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onUndo, modifier = Modifier.weight(1f)) {
                    Text("Undo")
                }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
            }
        }
    }
}

private fun voiceTaskSummary(task: TaskItem): String {
    val due = task.deadline?.let { "Due ${it.format(timeFormatter)}" }
    val alarm = task.alarmAt?.let {
        when (task.alarmDispatchStatus) {
            AlarmDispatchStatus.SentToSystemClock -> "Clock alarm sent for ${it.format(timeFormatter)}"
            AlarmDispatchStatus.SystemClockFallbackScheduled -> "Clock unavailable; Workday Planner full alarm set for ${it.format(timeFormatter)}"
            AlarmDispatchStatus.ExactAlarmAccessNeeded -> "Alarm access needed before ${it.format(timeFormatter)} can ring"
            AlarmDispatchStatus.ScheduledInApp -> "Workday Planner full alarm set for ${it.format(timeFormatter)}"
            AlarmDispatchStatus.ScheduledNotification -> "Notification reminder set for ${it.format(timeFormatter)}"
            AlarmDispatchStatus.SkippedPastAlarm -> "Alarm time already passed"
            else -> "Alarm set for ${it.format(timeFormatter)}"
        }
    }
    return listOfNotNull(due, alarm).ifEmpty { listOf("Task saved without an alarm") }.joinToString(" • ")
}

@Composable
private fun VoiceTaskBatchConfirmationCard(tasks: List<TaskItem>, onUndo: () -> Unit, onEdit: () -> Unit) {
    if (tasks.size == 1) {
        VoiceTaskConfirmationCard(task = tasks.first(), onUndo = onUndo, onEdit = onEdit)
        return
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.successContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.success.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${tasks.size} tasks created", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSuccessContainer)
            tasks.take(3).forEach {
                Text(it.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSuccessContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onUndo, modifier = Modifier.weight(1f)) { Text("Undo all") }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
            }
        }
    }
}

@Composable
private fun VoiceTaskDuplicateCard(onKeep: () -> Unit, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.warningContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This task was just created.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onWarningContainer)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Dismiss") }
                Button(onClick = onKeep, modifier = Modifier.weight(1f)) { Text("Keep duplicate") }
            }
        }
    }
}

@Composable
private fun VoiceTaskAmbiguityCard(
    result: VoiceTaskParseResult,
    onChoose: (VoiceTaskParseResult) -> Unit,
    onCancel: () -> Unit
) {
    val due = result.dueAt ?: return
    val hour12 = due.hour % 12
    val displayHour = if (hour12 == 0) 12 else hour12
    val amDue = due.withHour(if (displayHour == 12) 0 else displayHour)
    val pmDue = due.withHour(if (displayHour == 12) 12 else displayHour + 12)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Which time did you mean?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(result.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { onChoose(result.withDueTime(amDue)) }, modifier = Modifier.weight(1f)) {
                    Text(amDue.format(timeFormatter))
                }
                Button(onClick = { onChoose(result.withDueTime(pmDue)) }, modifier = Modifier.weight(1f)) {
                    Text(pmDue.format(timeFormatter))
                }
            }
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text("Cancel")
            }
        }
    }
}

internal fun parseVoiceTaskResults(transcript: String, state: AppState): List<VoiceTaskParseResult> {
    val shiftAware = applyShiftRelativeVoiceTerms(transcript, state)
    val parts = splitClearMultiTaskSentence(shiftAware)
    return parts.map { VoiceTaskParser.parse(it) }
}

private fun splitClearMultiTaskSentence(transcript: String): List<String> {
    val normalized = transcript.trim()
    // Alarm/reminder phrases often contain a second "at" time for the same task:
    // "produce order due at 7:30 alarm at 7:20". Do not split those into
    // separate tasks, or the title and alarm time get assigned incorrectly.
    if (Regex("\\b(alarm|reminder|remind me)\\s+at\\b", RegexOption.IGNORE_CASE).containsMatchIn(normalized)) {
        return listOf(normalized)
    }
    val markers = Regex("\\bat\\s+(\\d{1,2}|noon|midnight)\\b", RegexOption.IGNORE_CASE).findAll(normalized).toList()
    if (markers.size < 2) return listOf(normalized)
    val starts = markers.map { it.range.first }
    return starts.mapIndexed { index, start ->
        val end = starts.getOrNull(index + 1) ?: normalized.length
        normalized.substring(start, end)
            .replace(Regex("^(and\\s+)?", RegexOption.IGNORE_CASE), "")
            .trim(' ', ',', '.')
    }.filter { it.isNotBlank() }
}

internal fun applyShiftRelativeVoiceTerms(transcript: String, state: AppState): String {
    val lower = transcript.lowercase()
    val targetDate = spokenDateForShiftRelativePhrase(lower)
    val shift = state.shifts
        .filter {
            when (targetDate) {
                null -> it.date == LocalDate.now() || it.date == LocalDate.now().plusDays(1)
                else -> it.date == targetDate
            }
        }
        .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
        .firstOrNull() ?: return transcript
    val closing = shift.endDateTime()
    val start = shift.startDateTime()
    val replacementTime = when {
        "one hour before closing" in lower -> closing.minusHours(1)
        "halfway through my shift" in lower -> start.plusMinutes(Duration.between(start, closing).toMinutes() / 2)
        "at the start of my shift" in lower || "before my shift" in lower -> start
        "after work" in lower || "after my shift" in lower || "after shift" in lower -> closing
        "at closing" in lower || "before closing" in lower -> closing
        "after lunch" in lower -> start.plusHours(5)
        "before lunch" in lower -> start.plusHours(4)
        else -> null
    } ?: return transcript
    val rewritten = transcript
        .replace(Regex("one hour before closing", RegexOption.IGNORE_CASE), "at ${replacementTime.format(timeFormatter)}")
        .replace(Regex("halfway through my shift", RegexOption.IGNORE_CASE), "at ${replacementTime.format(timeFormatter)}")
        .replace(Regex("at the start of my shift|before my shift|after work|after my shift|after shift|at closing|before closing|after lunch|before lunch", RegexOption.IGNORE_CASE), "at ${replacementTime.format(timeFormatter)}")
    return if (targetDate != null) {
        rewritten.replace(
            Regex("\\b(next\\s+)?(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|tues|wed|thu|thur|thurs|fri|sat|sun)\\b", RegexOption.IGNORE_CASE)
        ) { match ->
            if (match.groupValues[1].isNotBlank()) match.value else "next ${match.groupValues[2]}"
        }
    } else {
        rewritten
    }
}

internal fun spokenDateForShiftRelativePhrase(lower: String, today: LocalDate = LocalDate.now()): LocalDate? {
    Regex("\\b(next\\s+)?(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|tues|wed|thu|thur|thurs|fri|sat|sun)\\b", RegexOption.IGNORE_CASE)
        .find(lower)
        ?.let { match ->
            val day = when (match.groupValues[2].lowercase()) {
                "monday", "mon" -> DayOfWeek.MONDAY
                "tuesday", "tue", "tues" -> DayOfWeek.TUESDAY
                "wednesday", "wed" -> DayOfWeek.WEDNESDAY
                "thursday", "thu", "thur", "thurs" -> DayOfWeek.THURSDAY
                "friday", "fri" -> DayOfWeek.FRIDAY
                "saturday", "sat" -> DayOfWeek.SATURDAY
                else -> DayOfWeek.SUNDAY
            }
            var daysUntil = (day.value - today.dayOfWeek.value + 7) % 7
            if (daysUntil == 0 || match.groupValues[1].isNotBlank()) daysUntil += 7
            return today.plusDays(daysUntil.toLong())
        }
    return when {
        Regex("\\btomorrow\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower) -> today.plusDays(1)
        Regex("\\btoday\\b", RegexOption.IGNORE_CASE).containsMatchIn(lower) -> today
        else -> null
    }
}

private fun applyVoiceTaskCorrection(task: TaskItem, transcript: String): TaskItem? {
    val lower = transcript.lowercase().trim()
    val due = task.deadline
    return when {
        lower == "am" && due != null -> task.withCorrectedDue(due.withHour(if (due.hour % 12 == 0) 0 else due.hour % 12))
        lower == "pm" && due != null -> {
            val hour = due.hour % 12
            task.withCorrectedDue(due.withHour(if (hour == 0) 12 else hour + 12))
        }
        lower.contains("change it to noon") && due != null -> task.withCorrectedDue(due.withHour(12).withMinute(0))
        lower.contains("make that tomorrow") && due != null -> task.withCorrectedDue(due.plusDays(1))
        lower.contains("remind me an hour before") && due != null -> task.copy(alarmOffsetMinutes = 60, alarmAt = due.minusHours(1))
        lower.contains("repeat every friday") -> task.copy(repeatRule = RepeatRule.Weekly, repeatDays = setOf(DayOfWeek.FRIDAY))
        lower.contains("make it high priority") -> task.copy(priority = TaskPriority.High)
        else -> null
    }
}

private fun TaskItem.withCorrectedDue(newDue: LocalDateTime): TaskItem {
    val newAlarm = if (reminderType == ReminderType.None) null else newDue.minusMinutes(alarmOffsetMinutes)
    return copy(deadline = newDue, alarmAt = newAlarm)
}

private fun TaskItem.voiceTaskFingerprint(): String {
    return "${title.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()}|${deadline ?: "none"}|voice"
}

private fun isRecentVoiceDuplicate(tasks: List<TaskItem>, candidate: TaskItem): Boolean {
    val fingerprint = candidate.voiceTaskFingerprint()
    val cutoff = LocalDateTime.now().minusSeconds(90)
    return tasks.any {
        it.createdUsingVoice &&
            it.createdAt.isAfter(cutoff) &&
            it.voiceTaskFingerprint() == fingerprint
    }
}

private fun isImmediateDuplicate(lastFingerprint: String?, lastCreatedAt: LocalDateTime?, candidate: TaskItem): Boolean {
    return lastFingerprint == candidate.voiceTaskFingerprint() &&
        lastCreatedAt?.isAfter(LocalDateTime.now().minusSeconds(15)) == true
}

private fun VoiceTaskParseResult.withDueTime(newDueAt: LocalDateTime): VoiceTaskParseResult {
    val newAlarmAt = when {
        reminderType == com.example.workdayplanner.data.ReminderType.None -> null
        reminderOffsetMinutes == 0L -> newDueAt
        else -> newDueAt.minusMinutes(reminderOffsetMinutes)
    }
    return copy(
        dueAt = newDueAt,
        alarmAt = newAlarmAt,
        ambiguityReasons = ambiguityReasons - "AM/PM unclear",
        confidence = confidence.coerceAtLeast(0.9)
    )
}

private fun voiceTaskErrorMessage(error: Int, partialTranscript: String): String {
    if (partialTranscript.isNotBlank() && error == SpeechRecognizer.ERROR_NO_MATCH) {
        return ""
    }
    return when (error) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied. You can still add tasks by typing."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Microphone is busy. Tap to try again."
        SpeechRecognizer.ERROR_AUDIO -> "Couldn't hear that. Tap to try again."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition is unavailable right now. Tap to try again."
        SpeechRecognizer.ERROR_CLIENT -> "Voice task cancelled."
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Couldn't hear that. Tap to try again."
        else -> "Couldn't hear that. Tap to try again."
    }
}

private fun LocalDate.workStatusLabel(state: AppState, todayShifts: List<WorkShift>): String {
    val kind = state.dayOffTypes[this]
    return when {
        kind == ShiftTemplateKind.Vacation -> "Vacation"
        kind == ShiftTemplateKind.Sick -> "Sick Day"
        this in state.daysOff || kind == ShiftTemplateKind.DayOff -> "Day Off"
        todayShifts.isNotEmpty() -> "Workday"
        state.shifts.isEmpty() && state.daysOff.isEmpty() -> "No schedule"
        else -> "No shift"
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
private fun DashboardMetricChip(label: String, containerColor: Color, labelColor: Color) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        )
    )
}

@Composable
private fun DashboardPayEstimateCard(state: AppState) {
    val scheduledWeek = PayEstimator.estimateWeek(state)
    val actualWeek = PayEstimator.estimateActualWeek(state)
    val period = PayEstimator.estimatePayPeriod(state)
    val payWatchOuts = payDashboardWatchOuts(state, scheduledWeek, actualWeek)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Estimated gross pay before taxes and deductions.", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PayHoursMetric("Scheduled", "${scheduledWeek.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
                PayHoursMetric("Actual", "${actualWeek.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PayMoneyMetric("Regular", actualWeek.regularPay.takeIf { actualWeek.paidHours > 0.0 } ?: scheduledWeek.regularPay, Modifier.weight(1f))
                PayMoneyMetric("Overtime", actualWeek.overtimePay.takeIf { actualWeek.paidHours > 0.0 } ?: scheduledWeek.overtimePay, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PayMoneyMetric("Gross", actualWeek.grossPay.takeIf { actualWeek.paidHours > 0.0 } ?: scheduledWeek.grossPay, Modifier.weight(1f))
                PayHoursMetric("Until OT", (actualWeek.hoursUntilOvertime.takeIf { actualWeek.paidHours > 0.0 } ?: scheduledWeek.hoursUntilOvertime).toSimpleString() + " hrs", Modifier.weight(1f))
            }
            Text("Pay period scheduled estimate: $${period.grossPay.toMoneyString()} / ${period.paidHours.toSimpleString()} paid hrs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            payWatchOuts.forEach { watch ->
                Text(watch, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PayHoursMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PayMoneyMetric(label: String, value: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$${value.toMoneyString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun payDashboardWatchOuts(state: AppState, scheduledWeek: PayEstimate, actualWeek: PayEstimate): List<String> {
    val active = if (actualWeek.paidHours > 0.0) actualWeek else scheduledWeek
    val missedPunch = state.timecards.any { it.clockIn != null && it.clockOut == null }
    val longShift = state.shifts.any { it.durationMinutes() >= 10 * 60 } ||
        state.timecards.any { entry -> TimecardCalculator.summarize(entry, state.paySettings).workedHours >= 10.0 }
    return buildList {
        when {
            active.overtimeHours > 0.0 -> add("Overtime likely: ${active.overtimeHours.toSimpleString()} OT hrs estimated.")
            active.hoursUntilOvertime in 0.01..4.0 -> add("Close to overtime: ${active.hoursUntilOvertime.toSimpleString()} hrs remaining.")
        }
        if (scheduledWeek.paidHours > 0.0 && scheduledWeek.paidHours < state.paySettings.overtimeThresholdHours * 0.5) {
            add("Lower hours than normal: scheduled paid hrs are under half your OT threshold.")
        }
        if (longShift) add("Long shift watch-out: one shift or punch is 10+ hrs.")
        if (missedPunch) add("Missed punch may affect estimate.")
        if (state.paySettings.deductUnpaidBreaks) {
            add("Breaks deducted: ${state.paySettings.unpaidLunchMinutes} min per scheduled shift.")
        }
    }.take(4)
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

private fun speechErrorMessage(error: Int): String = when (error) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio capture failed. Retry or type the note."
    SpeechRecognizer.ERROR_CLIENT -> "Speech capture was cancelled."
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice notes."
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service needs a working recognizer. Try again or type the note."
    SpeechRecognizer.ERROR_NO_MATCH -> "No clear speech was detected. Retry or type the note."
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again."
    SpeechRecognizer.ERROR_SERVER -> "Speech service failed. The transcript was not saved; type it instead."
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected."
    else -> "Speech recognition failed. Retry or type the note."
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.warningContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Watch-outs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onWarningContainer)
            risks.forEach { risk ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(risk.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onWarningContainer)
                    Text(risk.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onWarningContainer)
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
    onSaveEntry: (TimecardEntry) -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val entry = state.timecards.firstOrNull { it.date == today }
    val editableEntry = entry ?: TimecardEntry(date = today)
    val summary = entry?.let { TimecardCalculator.summarize(it, state.paySettings) }
    val weekSummary = TimecardCalculator.summarizeWeek(state, today)
    val scheduled = PayEstimator.estimateDay(state, today)
    val scheduledWeek = PayEstimator.estimateWeek(state, today)
    val watchOuts = timecardWatchOuts(state, today, entry, summary, weekSummary, scheduled)
    var editingEntry by remember(entry?.id, entry) { mutableStateOf(editableEntry) }
    var showManualEdit by remember(entry?.id) { mutableStateOf(false) }
    var noteText by remember(entry?.id, entry?.note) { mutableStateOf(editableEntry.note) }
    var missedPunchNote by remember(entry?.id, entry?.missedPunchNote) { mutableStateOf(editableEntry.missedPunchNote) }
    var payIssueNote by remember(entry?.id, entry?.payIssueNote) { mutableStateOf(editableEntry.payIssueNote) }
    val exportText = remember(state.timecards, state.shifts, state.paySettings, today) {
        buildWeeklyTimecardExport(state, today)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Personal timecard", "For your personal records only.")
            TimecardStatusBanner(state = state, entry = entry, scheduled = scheduled)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onClockIn, enabled = entry?.clockIn == null) { Text("Clock in") }
                OutlinedButton(onClick = onStartLunch, enabled = entry?.clockIn != null && entry.lunchStart == null && entry.clockOut == null) {
                    Text("Lunch start")
                }
                OutlinedButton(onClick = onEndLunch, enabled = entry?.lunchStart != null && entry.lunchEnd == null && entry.clockOut == null) {
                    Text("Lunch end")
                }
                OutlinedButton(onClick = onClockOut, enabled = entry?.clockIn != null && entry.clockOut == null) { Text("Clock out") }
            }
            Text(
                punchButtonHint(entry),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TimePunchRows(entry)
            if (summary != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TimecardMetric("Actual", "${summary.workedHours.toSimpleString()} hrs", Modifier.weight(1f))
                    TimecardMetric("Paid", "${summary.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
                }
                val delta = summary.paidHours - scheduled.paidHours
                Text(
                    if (scheduled.paidHours > 0.0) {
                        "Scheduled vs actual: ${scheduled.paidHours.toSimpleString()} scheduled paid hrs (${delta.toSignedHours()})."
                    } else {
                        "No scheduled shift estimate for today."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("No actual time logged today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (watchOuts.isNotEmpty()) {
                TimecardWatchOuts(watchOuts)
            }
            TextButton(onClick = { showManualEdit = !showManualEdit }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showManualEdit) "Hide manual correction" else "Manual edit times")
            }
            if (showManualEdit) {
                ManualTimecardEditor(
                    entry = editingEntry,
                    onEntryChange = { editingEntry = it },
                    onSave = {
                        onSaveEntry(
                            editingEntry.copy(
                                note = noteText.trim(),
                                missedPunchNote = missedPunchNote.trim(),
                                payIssueNote = payIssueNote.trim()
                            )
                        )
                        showManualEdit = false
                    }
                )
            }
            OutlinedTextField(
                value = missedPunchNote,
                onValueChange = { missedPunchNote = it },
                label = { Text("Missed punch note") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = payIssueNote,
                onValueChange = { payIssueNote = it },
                label = { Text("Pay issue note") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Manual correction note") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onSaveEntry(editableEntry.copy(note = noteText.trim(), missedPunchNote = missedPunchNote.trim(), payIssueNote = payIssueNote.trim()))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save notes")
            }
            TimecardWeeklySummaryCard(
                weekSummary = weekSummary,
                scheduledWeek = scheduledWeek,
                notesCount = state.timecards.count { it.hasIssueNote() },
                settings = state.paySettings
            )
            OutlinedButton(
                onClick = {
                    val share = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, "Workday Planner weekly timecard")
                        .putExtra(Intent.EXTRA_TEXT, exportText)
                    context.startActivity(Intent.createChooser(share, "Share weekly timecard"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export/share weekly timecard")
            }
        }
    }
}

@Composable
private fun TimecardMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ManualTimecardEditor(entry: TimecardEntry, onEntryChange: (TimecardEntry) -> Unit, onSave: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Manual correction", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Use this when you missed a punch or need your own corrected record.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TimecardDateTimeEdit("Clock in", entry.date, entry.clockIn) { onEntryChange(entry.copy(clockIn = it)) }
            TimecardDateTimeEdit("Lunch start", entry.date, entry.lunchStart) { onEntryChange(entry.copy(lunchStart = it)) }
            TimecardDateTimeEdit("Lunch end", entry.date, entry.lunchEnd) { onEntryChange(entry.copy(lunchEnd = it)) }
            TimecardDateTimeEdit("Clock out", entry.date, entry.clockOut) { onEntryChange(entry.copy(clockOut = it)) }
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Save corrected times")
            }
        }
    }
}

@Composable
private fun TimecardDateTimeEdit(label: String, date: LocalDate, value: LocalDateTime?, onChanged: (LocalDateTime?) -> Unit) {
    if (value == null) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelLarge)
                Text("Not set", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = { onChanged(date.atTime(LocalTime.now().withSecond(0).withNano(0))) }) {
                Text("Add")
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DateTimeRow(label, value, onChanged = { onChanged(it) })
            TextButton(onClick = { onChanged(null) }, modifier = Modifier.align(Alignment.End)) {
                Text("Clear $label")
            }
        }
    }
}

@Composable
private fun TimecardWatchOuts(watchOuts: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.warningContainer.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.warning.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Timecard watch-outs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onWarningContainer)
            watchOuts.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onWarningContainer)
            }
        }
    }
}

@Composable
private fun TimecardWeeklySummaryCard(
    weekSummary: com.example.workdayplanner.data.TimecardSummary,
    scheduledWeek: PayEstimate,
    notesCount: Int,
    settings: PaySettings
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Weekly timecard summary")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TimecardMetric("Scheduled", "${scheduledWeek.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
                TimecardMetric("Actual", "${weekSummary.workedHours.toSimpleString()} hrs", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TimecardMetric("Paid", "${weekSummary.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
                TimecardMetric("Breaks", "${weekSummary.lunchHours.toSimpleString()} hrs", Modifier.weight(1f))
            }
            val gross = if (settings.hourlyRate > 0.0) " / $${weekSummary.grossPay.toMoneyString()} est." else ""
            Text("Overtime estimate: ${weekSummary.overtimeHours.toSimpleString()} hrs$gross", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Notes/issues: $notesCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TimecardStatusBanner(state: AppState, entry: TimecardEntry?, scheduled: PayEstimate) {
    val today = LocalDate.now()
    val now = LocalDateTime.now()
    val nextTodayShift = state.shifts
        .filter { it.date == today && it.endDateTime().isAfter(now) }
        .minWithOrNull(compareBy<WorkShift> { it.start })
    val status = when {
        entry?.clockOut != null -> "Clocked out"
        entry?.lunchStart != null && entry.lunchEnd == null -> "On lunch"
        entry?.clockIn != null -> "Clocked in"
        nextTodayShift != null -> "Not clocked in"
        else -> "No shift punch started"
    }
    val detail = when {
        entry?.clockOut != null -> "Today's punches are complete. Add a note if payroll needs context."
        entry?.lunchStart != null && entry.lunchEnd == null -> "Lunch is running. Tap Lunch end when you return."
        entry?.clockIn != null -> "You are tracking actual time for today."
        nextTodayShift != null -> "Next shift starts ${nextTodayShift.timeUntilShift(now)}."
        scheduled.paidHours > 0.0 -> "Scheduled estimate: ${scheduled.paidHours.toSimpleString()} paid hrs."
        else -> "Use this only for your own records. It is not an employer clock."
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(status, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (scheduled.paidHours > 0.0) {
                AssistChip(onClick = {}, label = { Text("${scheduled.paidHours.toSimpleString()} hrs") })
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

private fun punchButtonHint(entry: TimecardEntry?): String {
    return when {
        entry?.clockIn == null -> "Lunch and clock out unlock after you clock in."
        entry.lunchStart == null -> "Lunch start is available. Lunch end unlocks after lunch starts."
        entry.lunchEnd == null && entry.clockOut == null -> "Lunch end is available while lunch is open."
        entry.clockOut == null -> "Clock out is available. Add a missed punch note if you need to correct anything."
        else -> "Today's punches are complete. You can still edit times manually."
    }
}

private fun timecardWatchOuts(
    state: AppState,
    today: LocalDate,
    entry: TimecardEntry?,
    todaySummary: com.example.workdayplanner.data.TimecardSummary?,
    weekSummary: com.example.workdayplanner.data.TimecardSummary,
    scheduled: PayEstimate
): List<String> {
    val now = LocalDateTime.now()
    val currentShift = state.shifts
        .filter { it.date == today }
        .minByOrNull { kotlin.math.abs(Duration.between(now, it.startDateTime()).toMinutes()) }
    return buildList {
        if (entry?.clockIn != null && entry.clockOut == null && currentShift?.endDateTime()?.isBefore(now) == true) {
            add("Forgot to clock out? Your scheduled shift has already ended.")
        }
        if (todaySummary != null && todaySummary.workedHours >= 10.0) {
            add("Long shift: ${todaySummary.workedHours.toSimpleString()} actual hrs today.")
        }
        val hoursUntilOvertime = state.paySettings.overtimeThresholdHours - weekSummary.paidHours
        if (weekSummary.overtimeHours > 0.0) {
            add("Overtime estimate: ${weekSummary.overtimeHours.toSimpleString()} paid hrs over threshold.")
        } else if (hoursUntilOvertime in 0.01..4.0) {
            add("Close to overtime: ${hoursUntilOvertime.toSimpleString()} paid hrs remaining.")
        }
        if (todaySummary != null && scheduled.paidHours > 0.0 && kotlin.math.abs(todaySummary.paidHours - scheduled.paidHours) >= 0.25) {
            add("Actual paid hours differ from scheduled by ${(todaySummary.paidHours - scheduled.paidHours).toSignedHours()}.")
        }
        if (todaySummary != null && todaySummary.workedHours >= 6.0 && todaySummary.lunchHours == 0.0 && entry?.missedPunchNote.isNullOrBlank()) {
            add("No lunch recorded on a ${todaySummary.workedHours.toSimpleString()} hr shift. Add a note if lunch was missed or forgotten.")
        }
    }.take(5)
}

private fun buildWeeklyTimecardExport(state: AppState, dateInWeek: LocalDate): String {
    val weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val weekEntries = state.timecards
        .filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        .sortedBy { it.date }
    val actual = TimecardCalculator.summarizeWeek(state, dateInWeek)
    val scheduled = PayEstimator.estimateWeek(state, dateInWeek)
    return buildString {
        appendLine("Workday Planner weekly timecard")
        appendLine("For your personal records only.")
        appendLine("${weekStart.format(shortDateFormatter)} - ${weekEnd.format(shortDateFormatter)}")
        appendLine()
        appendLine("Scheduled hours: ${scheduled.paidHours.toSimpleString()}")
        appendLine("Actual hours: ${actual.workedHours.toSimpleString()}")
        appendLine("Paid hours: ${actual.paidHours.toSimpleString()}")
        appendLine("Unpaid break time: ${actual.lunchHours.toSimpleString()}")
        appendLine("Overtime estimate: ${actual.overtimeHours.toSimpleString()}")
        if (state.paySettings.hourlyRate > 0.0) {
            appendLine("Estimated gross pay: $${actual.grossPay.toMoneyString()}")
        }
        appendLine()
        if (weekEntries.isEmpty()) {
            appendLine("No actual timecard entries this week.")
        } else {
            weekEntries.forEach { entry ->
                val summary = TimecardCalculator.summarize(entry, state.paySettings)
                appendLine("${entry.date.format(dateFormatter)}")
                appendLine("  In: ${entry.clockIn?.format(timeFormatter) ?: "--"}")
                appendLine("  Lunch: ${entry.lunchStart?.format(timeFormatter) ?: "--"} - ${entry.lunchEnd?.format(timeFormatter) ?: "--"}")
                appendLine("  Out: ${entry.clockOut?.format(timeFormatter) ?: "--"}")
                appendLine("  Paid: ${summary.paidHours.toSimpleString()} hrs")
                listOf(
                    "Correction" to entry.note,
                    "Missed punch" to entry.missedPunchNote,
                    "Pay issue" to entry.payIssueNote
                ).filter { it.second.isNotBlank() }.forEach { (label, note) ->
                    appendLine("  $label note: $note")
                }
            }
        }
    }
}

private fun TimecardEntry.hasIssueNote(): Boolean {
    return note.isNotBlank() || missedPunchNote.isNotBlank() || payIssueNote.isNotBlank()
}

@Composable
private fun WeeklyReviewScreen(
    state: AppState,
    onSaveTimecardEntry: (TimecardEntry) -> Unit,
    onAddTaskNextWeek: () -> Unit,
    onReviewSchedule: () -> Unit,
    onClearCompletedTasks: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val nextWeekStart = weekStart.plusWeeks(1)
    val nextWeekEnd = nextWeekStart.plusDays(6)
    val weekShifts = state.shifts.filter { it.date in weekStart..weekEnd }.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val nextWeekShifts = state.shifts.filter { it.date in nextWeekStart..nextWeekEnd }.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val weekDaysOff = state.daysOff.filter { it in weekStart..weekEnd }.sorted()
    val nextWeekDaysOff = state.daysOff.filter { it in nextWeekStart..nextWeekEnd }.sorted()
    val weekTasks = state.tasks.filter { task ->
        task.deadline?.toLocalDate()?.let { it in weekStart..weekEnd } == true ||
            task.completionHistory.any { it.toLocalDate() in weekStart..weekEnd }
    }
    val completedTasks = weekTasks.filter { task -> task.completed || task.completionHistory.any { it.toLocalDate() in weekStart..weekEnd } }
    val missedTasks = weekTasks.filter { !it.completed && taskIsOverdueInWeek(it, weekEnd) }
    val weekTimecards = state.timecards.filter { it.date in weekStart..weekEnd }.sortedBy { it.date }
    val timecardNotes = weekTimecards.filter { it.note.isNotBlank() || it.missedPunchNote.isNotBlank() || it.payIssueNote.isNotBlank() }
    val payNotes = state.notes.filter {
        it.date in weekStart..weekEnd && it.kind == WorkNoteKind.PayTimecardNote
    }
    val scheduled = PayEstimator.estimateWeek(state, today)
    val actual = TimecardCalculator.summarizeWeek(state, today)
    val actualPay = PayEstimator.estimateActualWeek(state, today)
    var missingPunchNote by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Weekly Review", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${weekStart.format(shortDateFormatter)} - ${weekEnd.format(shortDateFormatter)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("For your personal records only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            WeeklySectionCard("This week") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    WeeklyMetric("Shifts", weekShifts.size.toString(), Modifier.weight(1f))
                    WeeklyMetric("Days off", weekDaysOff.size.toString(), Modifier.weight(1f))
                }
                if (weekShifts.isEmpty() && weekDaysOff.isEmpty()) {
                    Text("No schedule saved for this week.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    weekShifts.take(5).forEach { shift ->
                        Text("${shift.date.format(shortDateFormatter)} ${shift.start.format(timeFormatter)}-${shift.end.format(timeFormatter)} ${shift.label}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (weekDaysOff.isNotEmpty()) {
                        Text("Days off: ${weekDaysOff.joinToString { it.format(shortDateFormatter) }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        item {
            WeeklySectionCard("Hours and pay") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    WeeklyMetric("Scheduled", "${scheduled.scheduledHours.toSimpleString()} hrs", Modifier.weight(1f))
                    WeeklyMetric("Actual", "${actual.workedHours.toSimpleString()} hrs", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    WeeklyMetric("Paid", "${actual.paidHours.toSimpleString()} hrs", Modifier.weight(1f))
                    WeeklyMetric("Overtime", "${maxOf(scheduled.overtimeHours, actual.overtimeHours).toSimpleString()} hrs", Modifier.weight(1f))
                }
                val gross = if (actual.paidHours > 0.0) actualPay.grossPay else scheduled.grossPay
                WeeklyMetric("Estimated gross pay", "$${gross.toMoneyString()}", Modifier.fillMaxWidth())
                Text("Estimated gross pay before taxes and deductions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            WeeklySectionCard("Tasks") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    WeeklyMetric("Completed", completedTasks.size.toString(), Modifier.weight(1f))
                    WeeklyMetric("Missed/overdue", missedTasks.size.toString(), Modifier.weight(1f))
                }
                if (missedTasks.isNotEmpty()) {
                    missedTasks.take(4).forEach { task ->
                        Text("${task.title} - due ${task.deadline?.format(dateTimeFormatter) ?: "no date"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text("No missed tasks found for this week.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            WeeklySectionCard("Notes/issues") {
                if (timecardNotes.isEmpty() && payNotes.isEmpty()) {
                    Text("No timecard or pay issue notes this week.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    timecardNotes.forEach { entry ->
                        val notes = listOf(entry.note, entry.missedPunchNote, entry.payIssueNote).filter { it.isNotBlank() }
                        Text("${entry.date.format(shortDateFormatter)}: ${notes.joinToString(" | ")}", style = MaterialTheme.typography.bodyMedium)
                    }
                    payNotes.forEach { note ->
                        Text("${note.date.format(shortDateFormatter)}: ${note.title.ifBlank { note.text.take(40) }}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = missingPunchNote,
                    onValueChange = { missingPunchNote = it },
                    label = { Text("Add missing punch note") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    enabled = missingPunchNote.isNotBlank(),
                    onClick = {
                        val entry = state.timecards.firstOrNull { it.date == today } ?: TimecardEntry(date = today)
                        onSaveTimecardEntry(entry.copy(missedPunchNote = missingPunchNote.trim()))
                        missingPunchNote = ""
                        message = "Missing punch note saved."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save punch note")
                }
            }
        }
        item {
            WeeklySectionCard("Next week") {
                if (nextWeekShifts.isEmpty() && nextWeekDaysOff.isEmpty()) {
                    Text("No shifts saved for next week yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    nextWeekShifts.take(6).forEach { shift ->
                        Text("${shift.date.format(shortDateFormatter)} ${shift.start.format(timeFormatter)}-${shift.end.format(timeFormatter)} ${shift.label}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (nextWeekDaysOff.isNotEmpty()) {
                        Text("Days off: ${nextWeekDaysOff.joinToString { it.format(shortDateFormatter) }}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        item {
            WeeklySectionCard("Actions") {
                Button(
                    onClick = {
                        shareText(context, "Workday Planner weekly summary", buildWeeklyReviewExport(state, today))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export/share weekly summary")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onAddTaskNextWeek, modifier = Modifier.weight(1f)) { Text("Add task") }
                    OutlinedButton(onClick = onReviewSchedule, modifier = Modifier.weight(1f)) { Text("Review schedule") }
                }
                OutlinedButton(onClick = onClearCompletedTasks, modifier = Modifier.fillMaxWidth()) {
                    Text("Clear completed tasks")
                }
                message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary) }
            }
        }
    }
}

@Composable
private fun WeeklySectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun WeeklyMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun buildWeeklyReviewExport(state: AppState, dateInWeek: LocalDate): String {
    val weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)
    val nextWeekStart = weekStart.plusWeeks(1)
    val nextWeekEnd = nextWeekStart.plusDays(6)
    val weekShifts = state.shifts.filter { it.date in weekStart..weekEnd }
    val nextWeekShifts = state.shifts.filter { it.date in nextWeekStart..nextWeekEnd }.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val daysOff = state.daysOff.filter { it in weekStart..weekEnd }.sorted()
    val weekTasks = state.tasks.filter { task ->
        task.deadline?.toLocalDate()?.let { it in weekStart..weekEnd } == true ||
            task.completionHistory.any { it.toLocalDate() in weekStart..weekEnd }
    }
    val completed = weekTasks.count { task -> task.completed || task.completionHistory.any { it.toLocalDate() in weekStart..weekEnd } }
    val missed = weekTasks.count { !it.completed && taskIsOverdueInWeek(it, weekEnd) }
    val scheduled = PayEstimator.estimateWeek(state, dateInWeek)
    val actual = TimecardCalculator.summarizeWeek(state, dateInWeek)
    val actualPay = PayEstimator.estimateActualWeek(state, dateInWeek)
    val noteLines = state.timecards.filter { it.date in weekStart..weekEnd && it.hasIssueNote() }.flatMap { entry ->
        listOf(entry.note, entry.missedPunchNote, entry.payIssueNote)
            .filter { it.isNotBlank() }
            .map { "${entry.date.format(shortDateFormatter)}: $it" }
    }
    return buildString {
        appendLine("Workday Planner weekly review")
        appendLine("For your personal records only.")
        appendLine("${weekStart.format(shortDateFormatter)} - ${weekEnd.format(shortDateFormatter)}")
        appendLine()
        appendLine("This week")
        appendLine("Shifts scheduled/worked: ${weekShifts.size}")
        appendLine("Days off: ${daysOff.size}")
        appendLine("Scheduled hours: ${scheduled.scheduledHours.toSimpleString()}")
        appendLine("Actual hours: ${actual.workedHours.toSimpleString()}")
        appendLine("Paid hours: ${actual.paidHours.toSimpleString()}")
        appendLine("Estimated pay: $${(if (actual.paidHours > 0.0) actualPay.grossPay else scheduled.grossPay).toMoneyString()}")
        appendLine("Overtime estimate: ${maxOf(scheduled.overtimeHours, actual.overtimeHours).toSimpleString()}")
        appendLine()
        appendLine("Tasks")
        appendLine("Completed: $completed")
        appendLine("Missed/overdue: $missed")
        appendLine()
        appendLine("Notes/issues")
        if (noteLines.isEmpty()) appendLine("None") else noteLines.forEach(::appendLine)
        appendLine()
        appendLine("Next week")
        if (nextWeekShifts.isEmpty()) {
            appendLine("No shifts saved.")
        } else {
            nextWeekShifts.take(8).forEach { shift ->
                appendLine("${shift.date.format(shortDateFormatter)} ${shift.start.format(timeFormatter)}-${shift.end.format(timeFormatter)} ${shift.label}")
            }
        }
    }
}

private fun taskIsOverdueInWeek(task: TaskItem, weekEnd: LocalDate): Boolean {
    val deadline = task.deadline ?: return false
    return deadline.toLocalDate() <= weekEnd && deadline.isBefore(LocalDateTime.now())
}

private operator fun ClosedRange<LocalDate>.contains(date: LocalDate): Boolean =
    !date.isBefore(start) && !date.isAfter(endInclusive)

private fun shareText(context: android.content.Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

private fun Double.toSignedHours(): String {
    return when {
        this > 0.0 -> "+${toSimpleString()} hrs"
        this < 0.0 -> "${toSimpleString()} hrs"
        else -> "even"
    }
}

@Composable
private fun ChecklistTemplateSection(onAddChecklist: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Checklist templates", "Add a work routine to today's tasks.")
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
    shifts: List<WorkShift>,
    onSaveNote: (WorkNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCreateTaskFromNote: (String) -> Unit,
    onCreateChecklistFromNote: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleArchived: (String) -> Unit
) {
    val context = LocalContext.current
    var noteTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var noteKind by remember { mutableStateOf(WorkNoteKind.ShiftNote) }
    var voiceType by remember { mutableStateOf(WorkVoiceCaptureType.ShiftNote) }
    var rawVoiceTranscript by remember { mutableStateOf("") }
    var linkedShiftId by remember { mutableStateOf<String?>(null) }
    var pinned by remember { mutableStateOf(false) }
    var selectedKind by remember { mutableStateOf<WorkNoteKind?>(null) }
    var searchText by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var liveVoiceText by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var startVoiceAfterPermission by remember { mutableStateOf(false) }
    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember(recognizerAvailable) {
        if (recognizerAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startVoiceAfterPermission = true
        } else {
            voiceError = "Microphone permission denied. You can still type the note."
        }
    }

    fun applyVoiceTranscript(text: String) {
        if (text.isBlank()) return
        rawVoiceTranscript = text.trim()
        val result = WorkVoiceCaptureParser.format(rawVoiceTranscript, voiceType)
        noteTitle = result.title
        noteText = result.cleanedText
        noteKind = result.kind
    }

    fun startVoiceCapture() {
        val recognizer = speechRecognizer
        if (!recognizerAvailable || recognizer == null) {
            voiceError = "Speech recognition is not available on this device. Type the note instead."
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        liveVoiceText = ""
        voiceError = null
        listening = true
        recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your ${voiceType.label.lowercase()}")
        })
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceError = null
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                listening = false
            }

            override fun onError(error: Int) {
                listening = false
                if (liveVoiceText.isNotBlank() && error in setOf(SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_CLIENT)) {
                    applyVoiceTranscript(liveVoiceText)
                    voiceError = null
                    return
                }
                voiceError = speechErrorMessage(error)
            }

            override fun onResults(results: Bundle?) {
                listening = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    .ifBlank { liveVoiceText }
                liveVoiceText = text
                applyVoiceTranscript(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                liveVoiceText = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { speechRecognizer?.destroy() }
    }

    LaunchedEffect(startVoiceAfterPermission) {
        if (startVoiceAfterPermission) {
            startVoiceAfterPermission = false
            startVoiceCapture()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Work notes", "Shift handoffs, orders, training, reminders, and pay/timecard notes.")
            Text(
                "General voice notes and personal notes belong in NotePilot. Keep this area focused on work records.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                workNoteKinds.forEach { kind ->
                    FilterChip(selected = noteKind == kind, onClick = { noteKind = kind }, label = { Text(kind.label) })
                }
            }
            Text("Work voice capture", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Use this for orders, shift notes, handoffs, tasks, truck, inventory, and reminders only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkVoiceCaptureType.entries.forEach { type ->
                    FilterChip(
                        selected = voiceType == type,
                        onClick = {
                            voiceType = type
                            noteKind = type.noteKind
                            if (rawVoiceTranscript.isNotBlank()) {
                                val result = WorkVoiceCaptureParser.format(rawVoiceTranscript, type)
                                noteTitle = result.title
                                noteText = result.cleanedText
                                noteKind = result.kind
                            }
                        },
                        label = { Text(type.label) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        noteKind = WorkNoteKind.ManagerHandoff
                        noteTitle = "Manager handoff"
                        noteText = WorkNoteTemplates.managerHandoff
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Handoff template") }
                OutlinedButton(
                    onClick = {
                        noteKind = WorkNoteKind.OrderNote
                        noteTitle = "Order note"
                        noteText = WorkNoteTemplates.orderNote
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Order template") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (listening) {
                    Button(
                        onClick = {
                            speechRecognizer?.stopListening()
                            listening = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop listening")
                    }
                } else {
                    Button(onClick = { startVoiceCapture() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Capture work note")
                    }
                }
                OutlinedButton(
                    onClick = {
                        liveVoiceText = ""
                        rawVoiceTranscript = ""
                        voiceError = null
                    },
                    enabled = liveVoiceText.isNotBlank() || rawVoiceTranscript.isNotBlank() || voiceError != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear voice")
                }
            }
            if (listening || liveVoiceText.isNotBlank() || voiceError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (voiceError == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.dangerContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (listening) "Listening..." else "Voice capture", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            liveVoiceText.ifBlank { voiceError.orEmpty() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (voiceError == null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onDangerContainer
                        )
                    }
                }
            }
            if (rawVoiceTranscript.isNotBlank()) {
                AssistChip(
                    onClick = {},
                    label = { Text("Captured as ${voiceType.label}; review before saving") },
                    leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) }
                )
                OutlinedTextField(
                    value = rawVoiceTranscript,
                    onValueChange = { rawVoiceTranscript = it },
                    label = { Text("Raw transcript") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val result = WorkVoiceCaptureParser.format(rawVoiceTranscript, voiceType)
                        noteTitle = result.title
                        noteText = result.cleanedText
                        noteKind = result.kind
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Format transcript")
                }
            }
            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Body") },
                placeholder = { Text("Example: Frozen order short 2 cases, follow up tomorrow") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            if (shifts.isNotEmpty()) {
                Text("Linked shift", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = linkedShiftId == null, onClick = { linkedShiftId = null }, label = { Text("None") })
                    shifts.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }).take(8).forEach { shift ->
                        FilterChip(
                            selected = linkedShiftId == shift.id,
                            onClick = { linkedShiftId = shift.id },
                            label = { Text("${shift.date.format(shortDateFormatter)} ${shift.start.format(timeFormatter)}") }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Pinned", style = MaterialTheme.typography.bodyMedium)
                    Text("Keep this work note at the top.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = pinned, onCheckedChange = { pinned = it })
            }
            Button(
                enabled = noteText.isNotBlank() || noteTitle.isNotBlank(),
                onClick = {
                    onSaveNote(
                        WorkNoteOrganizer.create(
                            text = noteText,
                            title = noteTitle,
                            kindOverride = noteKind,
                            linkedShiftId = linkedShiftId,
                            pinned = pinned,
                            rawTranscript = rawVoiceTranscript
                        )
                    )
                    noteTitle = ""
                    noteText = ""
                    noteKind = WorkNoteKind.ShiftNote
                    voiceType = WorkVoiceCaptureType.ShiftNote
                    rawVoiceTranscript = ""
                    linkedShiftId = null
                    pinned = false
                    liveVoiceText = ""
                    voiceError = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save work note")
            }
            val notesToShow = todayNotes + recentNotes
            val issueCount = notesToShow.count { it.kind in setOf(WorkNoteKind.Issue, WorkNoteKind.PayTimecardNote) }
            val followUpCount = notesToShow.count { it.kind in setOf(WorkNoteKind.FollowUp, WorkNoteKind.ReminderNote, WorkNoteKind.ManagerHandoff) }
            val orderCount = notesToShow.count { it.kind in setOf(WorkNoteKind.Order, WorkNoteKind.OrderNote, WorkNoteKind.TruckNote, WorkNoteKind.InventoryNote) }
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
                    note.title.contains(searchText, ignoreCase = true) ||
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
                        onMakeChecklist = { onCreateChecklistFromNote(note.id) },
                        onTogglePinned = { onTogglePinned(note.id) },
                        onArchive = { onToggleArchived(note.id) },
                        onDelete = { onDeleteNote(note.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkNoteCard(
    note: WorkNote,
    onMakeTask: () -> Unit,
    onMakeChecklist: () -> Unit,
    onTogglePinned: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(note.title.ifBlank { note.kind.label }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("${note.kind.label} | ${note.date.format(dateFormatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (note.pinned) AssistChip(onClick = {}, label = { Text("Pinned") })
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
                    Text("Task")
                }
                OutlinedButton(onClick = onMakeChecklist, modifier = Modifier.weight(1f)) {
                    Text("Checklist")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onTogglePinned, modifier = Modifier.weight(1f)) {
                    Text(if (note.pinned) "Unpin" else "Pin")
                }
                OutlinedButton(onClick = onArchive, modifier = Modifier.weight(1f)) {
                    Text("Archive")
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
private fun EventCard(event: WorkEvent, modifier: Modifier = Modifier, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.animateContentSize(animationSpec = tween(220))
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = task.priority.priorityColor()
    val cardAlpha by animateFloatAsState(
        targetValue = if (task.completed) 0.68f else 1f,
        animationSpec = tween(180),
        label = "taskCompletedAlpha"
    )
    val containerColor by animateColorAsState(
        targetValue = if (task.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(180),
        label = "taskContainer"
    )
    val priorityWidth by animateDpAsState(
        targetValue = if (task.completed) 3.dp else 5.dp,
        animationSpec = tween(180),
        label = "priorityRail"
    )
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            1.dp,
            if (task.priority == TaskPriority.Normal) MaterialTheme.colorScheme.surfaceVariant else priorityColor.copy(alpha = 0.65f)
        ),
        modifier = modifier
            .alpha(cardAlpha)
            .animateContentSize(animationSpec = tween(220))
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(priorityWidth)
                    .fillMaxHeight()
                    .background(priorityColor)
            )
            Column(Modifier.padding(14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleX = if (task.completed) 1.06f else 1f
                            scaleY = if (task.completed) 1.06f else 1f
                        }
                    ) {
                        Checkbox(checked = task.completed, onCheckedChange = { onToggleComplete() })
                    }
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
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.warning, fontWeight = FontWeight.SemiBold)
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
    var carryOverBehavior by remember(task?.id) { mutableStateOf(task?.carryOverBehavior ?: CarryOverBehavior.KeepOverdue) }
    var alarmOffsetMinutes by remember(task?.id) { mutableStateOf((task?.alarmOffsetMinutes ?: 30).toString()) }
    var completed by remember(task?.id) { mutableStateOf(task?.completed ?: false) }
    var showAdvancedRepeat by remember(task?.id) { mutableStateOf(task?.repeatRule == RepeatRule.CustomDays) }
    var showTaskDetails by remember(task?.id) {
        mutableStateOf(task != null && (task.category != TaskCategory.General || task.priority != TaskPriority.Normal || !task.workRelated))
    }
    var showTemplates by remember(task?.id) { mutableStateOf(false) }
    var showMoreRules by remember(task?.id) { mutableStateOf(false) }
    val advancedRulesUnlocked = PremiumAccess.canUse(state, PremiumFeature.AdvancedTaskRules)
    val templatesUnlocked = PremiumAccess.canUse(state, PremiumFeature.TaskTemplates)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var voiceListening by remember { mutableStateOf(false) }
    var voiceTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var pendingVoicePermissionStart by remember { mutableStateOf(false) }
    var ambiguousEditorVoiceTask by remember { mutableStateOf<VoiceTaskParseResult?>(null) }
    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember(recognizerAvailable) {
        if (recognizerAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingVoicePermissionStart = true
        } else {
            voiceError = "Microphone permission denied. You can still add tasks by typing."
        }
    }
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
        carryOverBehavior = CarryOverBehavior.KeepOverdue
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
    fun saveVoiceTranscript(text: String) {
        val transcript = text.trim()
        if (transcript.isBlank()) {
            voiceError = "Couldn't hear that. Tap to try again."
            return
        }
        val parsed = parseVoiceTaskResults(transcript, state).firstOrNull() ?: VoiceTaskParser.parse(transcript)
        if ("AM/PM unclear" in parsed.ambiguityReasons && parsed.dueAt != null) {
            ambiguousEditorVoiceTask = parsed
            voiceTranscript = transcript
            voiceError = null
            return
        }
        val taskFromVoice = parsed.toTask(alarmDelivery = state.alarmSettings.defaultAlarmDelivery)
        if (
            taskFromVoice.alarmAt != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            onNotificationPermissionNeeded()
        }
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onSave(taskFromVoice)
    }
    fun startVoiceTaskFromEditor() {
        val recognizer = speechRecognizer
        if (!recognizerAvailable || recognizer == null) {
            voiceError = "Speech recognition is not available on this device."
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            voiceError = "Workday Planner needs microphone access to turn speech into a task."
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        voiceTranscript = ""
        voiceError = null
        ambiguousEditorVoiceTask = null
        voiceListening = true
        runCatching {
            recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak one work task")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            })
        }.onFailure {
            voiceListening = false
            voiceError = "Couldn't hear that. Tap to try again."
        }
    }
    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                voiceError = null
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                voiceListening = false
            }

            override fun onError(error: Int) {
                voiceListening = false
                voiceError = voiceTaskErrorMessage(error, voiceTranscript)
                if (voiceTranscript.isNotBlank() && error == SpeechRecognizer.ERROR_NO_MATCH) {
                    saveVoiceTranscript(voiceTranscript)
                }
            }

            override fun onResults(results: Bundle?) {
                voiceListening = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                voiceTranscript = text
                saveVoiceTranscript(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                voiceTranscript = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose { speechRecognizer?.destroy() }
    }
    LaunchedEffect(pendingVoicePermissionStart) {
        if (pendingVoicePermissionStart) {
            pendingVoicePermissionStart = false
            startVoiceTaskFromEditor()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(task?.let { "Edit task" } ?: "Add task", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = ::startVoiceTaskFromEditor, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Mic, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Speak task")
        }
        WorkdayAnimatedVisibility(visible = voiceListening) {
            VoiceTaskListeningCard(
                transcript = voiceTranscript,
                error = voiceError,
                onCancel = {
                    speechRecognizer?.cancel()
                    voiceListening = false
                    voiceTranscript = ""
                    voiceError = null
                },
                onStop = {
                    speechRecognizer?.stopListening()
                    voiceListening = false
                }
            )
        }
        WorkdayAnimatedVisibility(visible = !voiceListening && voiceError != null) {
            VoiceTaskErrorCard(message = voiceError.orEmpty(), onRetry = ::startVoiceTaskFromEditor)
        }
        ambiguousEditorVoiceTask?.let { result ->
            VoiceTaskAmbiguityCard(
                result = result,
                onChoose = { adjusted ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(adjusted.toTask(alarmDelivery = state.alarmSettings.defaultAlarmDelivery))
                },
                onCancel = { ambiguousEditorVoiceTask = null }
            )
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AssistChip(onClick = {}, label = { Text(category.label) })
                        AssistChip(onClick = {}, label = { Text(priority.label) })
                        if (workRelated) AssistChip(onClick = {}, label = { Text("Work") })
                    }
                    TextButton(onClick = { showTaskDetails = !showTaskDetails }) {
                        Text(if (showTaskDetails) "Hide details" else "Details")
                    }
                }
                WorkdayAnimatedVisibility(visible = showTaskDetails) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            }
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Templates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Optional shortcuts for repeated work tasks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showTemplates = !showTemplates }) {
                        Text(if (showTemplates) "Hide" else "Use")
                    }
                }
                WorkdayAnimatedVisibility(visible = showTemplates) {
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
                            showTaskDetails = template.category != TaskCategory.General || template.priority != TaskPriority.Normal || !template.workRelated
                            showTemplates = false
                        },
                        onSaveTemplate = onSaveTaskTemplate,
                        onDeleteTemplate = onDeleteTaskTemplate,
                        premiumUnlocked = templatesUnlocked,
                        onOpenPremium = onOpenPremium
                    )
                }
            }
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))) {
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
                        TaskTimingRule.AfterShiftStarts -> nextShift?.let {
                            deadline = it.plusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                            alarmAt = deadline
                        }
                        TaskTimingRule.BeforeShiftEnds -> {
                            val shift = state.shifts.filter { it.date.atTime(it.start).isAfter(LocalDateTime.now()) }.minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
                            shift?.let {
                                val end = it.date.atTime(it.end).let { endTime -> if (it.end.isBefore(it.start)) endTime.plusDays(1) else endTime }
                                deadline = end.minusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                                alarmAt = deadline
                            }
                        }
                        TaskTimingRule.AfterShiftEnds -> {
                            val shift = state.shifts.filter { it.date.atTime(it.start).isAfter(LocalDateTime.now()) }.minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
                            shift?.let {
                                val end = it.date.atTime(it.end).let { endTime -> if (it.end.isBefore(it.start)) endTime.plusDays(1) else endTime }
                                deadline = end.plusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                                alarmAt = deadline
                            }
                        }
                        TaskTimingRule.MorningOfWorkday -> {
                            skipDaysOff = true
                            deadline = deadline.toLocalDate().atTime(8, 0)
                            alarmAt = deadline.minusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                        }
                        TaskTimingRule.NightBeforeShift -> nextShift?.let {
                            deadline = it.minusDays(1).withHour(20).withMinute(0)
                            alarmAt = deadline.minusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                        }
                        TaskTimingRule.DaysOffOnly -> {
                            skipDaysOff = false
                            deadline = deadline.toLocalDate().atTime(10, 0)
                            alarmAt = deadline.minusMinutes(alarmOffsetMinutes.toLongOrNull() ?: 30)
                        }
                        TaskTimingRule.WorkdaysOnly -> skipDaysOff = true
                    }
                })
                if (timingRule in setOf(TaskTimingRule.AtTime, TaskTimingRule.WorkdaysOnly, TaskTimingRule.MorningOfWorkday, TaskTimingRule.DaysOffOnly)) {
                    DateTimeRow("Deadline", deadline, onChanged = { deadline = it })
                } else {
                    Text("The exact reminder time will follow your saved shift schedule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Reminder", style = MaterialTheme.typography.bodyLarge)
                        Text("Default: ${state.alarmSettings.defaultAlarmDelivery.label}, ${reminderOffsetLabel(state.alarmSettings.defaultReminderOffsetMinutes)}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Workday Planner only alarms for tasks you create.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("You can turn reminders off anytime.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                WorkdayAnimatedVisibility(visible = reminderEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (timingRule in setOf(TaskTimingRule.AtTime, TaskTimingRule.WorkdaysOnly, TaskTimingRule.MorningOfWorkday, TaskTimingRule.DaysOffOnly)) {
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
        }
        Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("More rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showMoreRules = !showMoreRules }) { Text(if (showMoreRules) "Hide" else "Show") }
                }
                if (!showMoreRules) {
                    Text("Repeat, shift type, day-off, and carry-over rules.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                WorkdayAnimatedVisibility(visible = showMoreRules) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Repeat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                RepeatRuleChips(selected = repeatRule, onSelected = {
                    val premiumRepeat = it in setOf(
                        RepeatRule.OpeningShifts,
                        RepeatRule.ClosingShifts,
                        RepeatRule.TruckDays,
                        RepeatRule.InventoryDays
                    )
                    if (premiumRepeat && !advancedRulesUnlocked) {
                        onOpenPremium()
                    } else {
                        repeatRule = it
                        if (it != RepeatRule.CustomDays) repeatDays = emptySet()
                        linkedShiftType = when (it) {
                            RepeatRule.OpeningShifts -> LinkedShiftType.Opening
                            RepeatRule.ClosingShifts -> LinkedShiftType.Closing
                            RepeatRule.TruckDays -> LinkedShiftType.Truck
                            RepeatRule.InventoryDays -> LinkedShiftType.Inventory
                            else -> linkedShiftType
                        }
                    }
                })
                WorkdayAnimatedVisibility(visible = showAdvancedRepeat || repeatRule == RepeatRule.CustomDays) {
                    CustomRepeatDays(
                        selectedDays = repeatDays,
                        onToggle = { day ->
                            repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                        }
                    )
                }
                if (!showAdvancedRepeat && repeatRule != RepeatRule.CustomDays) {
                    TextButton(onClick = {
                        showAdvancedRepeat = true
                        repeatRule = RepeatRule.CustomDays
                    }) { Text("Choose specific days") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = skipDaysOff, onCheckedChange = { skipDaysOff = it })
                    Text("Skip days off")
                }
                if (!advancedRulesUnlocked) {
                    PremiumLockedInline(PremiumFeature.AdvancedTaskRules, "Free includes basic repeats and skip-days-off. Premium adds shift-type rules and carry-over behavior.", onOpenPremium)
                } else {
                    LinkedShiftTypeChips(selected = linkedShiftType, onSelected = { linkedShiftType = it })
                    CarryOverChips(selected = carryOverBehavior, onSelected = { carryOverBehavior = it })
                }
                    }
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
    TaskTemplate(id = "builtin-closing-checklist", name = "Closing shift checklist", title = "Closing shift checklist", notes = "Clean area, finish closing tasks, and leave notes for next shift.", category = TaskCategory.Cleaning, priority = TaskPriority.High, repeatRule = RepeatRule.ClosingShifts, linkedShiftType = LinkedShiftType.Closing, timingRule = TaskTimingRule.BeforeShiftEnds, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-truck-checklist", name = "Truck/order day checklist", title = "Truck/order day checklist", notes = "Check order, truck notes, inventory gaps, and follow-up items.", category = TaskCategory.Orders, priority = TaskPriority.High, repeatRule = RepeatRule.TruckDays, linkedShiftType = LinkedShiftType.Truck, timingRule = TaskTimingRule.BeforeNextShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-inventory-checklist", name = "Inventory day checklist", title = "Inventory day checklist", notes = "Counts, outs, order review, and shrink notes.", category = TaskCategory.Orders, priority = TaskPriority.High, repeatRule = RepeatRule.InventoryDays, linkedShiftType = LinkedShiftType.Inventory, timingRule = TaskTimingRule.BeforeNextShift, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
    TaskTemplate(id = "builtin-manager-handoff", name = "Manager handoff checklist", title = "Manager handoff checklist", notes = "Open issues, unfinished tasks, associate follow-up, and schedule notes.", category = TaskCategory.Admin, priority = TaskPriority.High, timingRule = TaskTimingRule.AfterShiftEnds, carryOverBehavior = CarryOverBehavior.NextWorkday, builtIn = true),
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            templates.forEach { template ->
                OutlinedButton(onClick = { onApply(template) }) {
                    Text(template.name)
                }
            }
        }
        if (!premiumUnlocked) {
            PremiumLockedInline(PremiumFeature.TaskTemplates, "Built-in task templates are free. Premium unlocks custom template creation and editing.", onOpenPremium)
        }
        if (!premiumUnlocked && templates.any { !it.builtIn }) {
            Text(
                "Saved custom templates stay visible. Premium is needed to edit or create more.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        templates.filterNot { it.builtIn }.forEach { template ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(template.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = {
                    if (premiumUnlocked) {
                        editing = template
                        editorOpen = true
                    } else {
                        onOpenPremium()
                    }
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
        RepeatRule.TruckDays,
        RepeatRule.InventoryDays
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
    TaskPriority.Low -> MaterialTheme.colorScheme.onSurfaceVariant
    TaskPriority.Normal -> MaterialTheme.colorScheme.secondary
    TaskPriority.High -> MaterialTheme.colorScheme.warning
    TaskPriority.Critical -> MaterialTheme.colorScheme.error
}

@Composable
private fun TaskCategory.categoryColor(): Color = when (this) {
    TaskCategory.General -> MaterialTheme.colorScheme.primary
    TaskCategory.Orders -> MaterialTheme.colorScheme.tertiary
    TaskCategory.Cleaning -> MaterialTheme.colorScheme.success
    TaskCategory.Prep -> MaterialTheme.colorScheme.warning
    TaskCategory.Admin -> MaterialTheme.colorScheme.secondary
    TaskCategory.Personal -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun TaskScheduleLabel.scheduleLabelColor(): Color = when (this) {
    TaskScheduleLabel.BeforeWork -> MaterialTheme.colorScheme.primary
    TaskScheduleLabel.AfterWork -> MaterialTheme.colorScheme.secondary
    TaskScheduleLabel.DayOffTask -> MaterialTheme.colorScheme.onSurfaceVariant
    TaskScheduleLabel.DueDuringShift -> MaterialTheme.colorScheme.warning
    TaskScheduleLabel.QuickTask -> MaterialTheme.colorScheme.outline
    TaskScheduleLabel.Overdue -> MaterialTheme.colorScheme.error
    TaskScheduleLabel.RepeatsNextWorkday -> MaterialTheme.colorScheme.success
    TaskScheduleLabel.SkippedDayOff -> MaterialTheme.colorScheme.warning
}

@Composable
private fun LocalDateTime.deadlineColor(): Color {
    val now = LocalDateTime.now()
    return when {
        isBefore(now) -> MaterialTheme.colorScheme.error
        isBefore(now.plusHours(12)) -> MaterialTheme.colorScheme.warning
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Work calendar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                nextShift?.let {
                    "Next shift: ${it.date.format(dateFormatter)} at ${it.start.format(timeFormatter)}"
                } ?: "No upcoming shift imported",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    onAppearanceModeChanged: (AppearanceMode) -> Unit,
    onAccentStyleChanged: (AccentStyle) -> Unit,
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit,
    onOpenPremium: () -> Unit
) {
    val themeUnlocked = PremiumAccess.canUse(state, PremiumFeature.ThemeCustomization)
    val widgetUnlocked = PremiumAccess.canUse(state, PremiumFeature.Widgets)
    val systemDark = isSystemInDarkTheme()
    val previewDarkMode = when (state.appearanceMode) {
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
        AppearanceMode.System -> systemDark
    }
    var previewStyle by remember(state.accentStyle) { mutableStateOf(state.accentStyle) }
    var lockedStyle by remember { mutableStateOf<AppThemeStyle?>(null) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Style Packs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Choose your Workday look. Default light and dark stay free.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Appearance mode", style = MaterialTheme.typography.bodyLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppearanceMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.appearanceMode == mode,
                            onClick = { onAppearanceModeChanged(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }
                Text(
                    if (state.appearanceMode == AppearanceMode.System) {
                        "Using Android's current ${if (systemDark) "dark" else "light"} setting."
                    } else {
                        "${state.appearanceMode.label} mode is applied."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ThemePreviewCard(style = previewStyle, darkMode = previewDarkMode)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccentStyle.entries.forEach { style ->
                    val locked = style.premium && !themeUnlocked
                    FilterChip(
                        selected = state.accentStyle == style,
                        onClick = {
                            previewStyle = style
                            if (locked) {
                                lockedStyle = style
                            } else {
                                onAccentStyleChanged(style)
                            }
                        },
                        label = { Text(if (locked) "${style.label} - Premium" else style.label) }
                    )
                }
            }
            lockedStyle?.let { style ->
                PremiumThemeUpsell(
                    style = style,
                    onPreview = { previewStyle = style },
                    onUnlock = onOpenPremium,
                    onDismiss = { lockedStyle = null }
                )
            }
            if (!themeUnlocked) PremiumLockedInline(PremiumFeature.ThemeCustomization, "Premium unlocks extra style packs, widgets, advanced planning tools, and personalization.", onOpenPremium)
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
private fun ThemePreviewCard(style: AppThemeStyle, darkMode: Boolean) {
    MaterialTheme(colorScheme = workdayColorScheme(style, darkMode), typography = MaterialTheme.typography) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(style.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(if (darkMode) "Dark preview" else "Light preview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("Workday") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Next shift", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Opening shift", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("6:00 AM - 3:00 PM", style = MaterialTheme.typography.bodySmall)
                        Text("Starts in 1h 20m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Clock in") }
                    AssistChip(
                        onClick = {},
                        label = { Text("Reminder") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.warningContainer,
                            labelColor = MaterialTheme.colorScheme.onWarningContainer
                        )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)
                ) {
                    Text("Today", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Text("Schedule", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    Text("Settings", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PremiumThemeUpsell(
    style: AppThemeStyle,
    onPreview: () -> Unit,
    onUnlock: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${style.label} is a premium style pack", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Premium unlocks extra style packs, widgets, advanced planning tools, and personalization.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onPreview, modifier = Modifier.weight(1f)) { Text("Preview theme") }
                Button(onClick = onUnlock, modifier = Modifier.weight(1f)) { Text("Unlock Premium") }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Not now") }
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
    var deductUnpaidBreaks by remember(settings.deductUnpaidBreaks) { mutableStateOf(settings.deductUnpaidBreaks) }
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
            OutlinedTextField(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                label = { Text("Hourly rate") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Account for unpaid breaks?", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (deductUnpaidBreaks) {
                                    "Pay and hours estimates subtract ${lunchMinutes.toIntOrNull()?.coerceAtLeast(0) ?: 0} minutes from each shift."
                                } else {
                                    "Estimates use full shift length without subtracting break time."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = deductUnpaidBreaks, onCheckedChange = { deductUnpaidBreaks = it })
                    }
                    if (deductUnpaidBreaks) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0, 15, 30, 45, 60).forEach { minutes ->
                                FilterChip(
                                    selected = lunchMinutes.toIntOrNull() == minutes,
                                    onClick = { lunchMinutes = minutes.toString() },
                                    label = { Text(if (minutes == 0) "No break" else "${minutes} min") }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = lunchMinutes,
                            onValueChange = { lunchMinutes = it },
                            label = { Text("Break minutes per shift") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
                            deductUnpaidBreaks = deductUnpaidBreaks,
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
            PayBreakdownLine("Overtime pay", estimate.overtimePay, MaterialTheme.colorScheme.warning)
            PayBreakdownLine("Shift differential", estimate.differentialPay, MaterialTheme.colorScheme.success)
            Divider()
            PayBreakdownLine("Estimated gross pay", estimate.grossPay, MaterialTheme.colorScheme.secondary, emphasized = true)
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
    onAddTask: (TaskItem) -> Unit,
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
                onSave = { shift, template ->
                    onAddShift(shift)
                    template?.let { selectedTemplate ->
                        tasksForShiftTemplate(shift, selectedTemplate).forEach(onAddTask)
                    }
                    scheduleMessage = template?.let { selectedTemplate ->
                        if (selectedTemplate.defaultTasks.isNotEmpty() || selectedTemplate.defaultReminders.isNotEmpty()) {
                            "${selectedTemplate.name} shift added with template tasks."
                        } else {
                            "${selectedTemplate.name} shift added."
                        }
                    } ?: "Shift added."
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
    val weekShifts = state.shifts
        .filter { !it.date.isBefore(currentWeekStart) && !it.date.isAfter(currentWeekEnd) }
    val weekEstimate = PayEstimator.estimate(weekShifts, state.paySettings)
    val weekHours = if (state.paySettings.deductUnpaidBreaks) weekEstimate.paidHours else weekEstimate.scheduledHours
    val weekHoursLabel = if (state.paySettings.deductUnpaidBreaks) "paid hrs this week" else "scheduled hrs this week"
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                when {
                    today in state.daysOff -> "Today is marked as a day off."
                    todayShifts.isNotEmpty() -> "Today: ${todayShifts.joinToString { it.shiftTimeLabel() }}"
                    else -> "No shift scheduled today."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                nextShift?.let { "Next shift ${it.timeUntilShift(now)}: ${it.date.format(dateFormatter)} at ${it.start.format(timeFormatter)}" }
                    ?: "No upcoming shift saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${state.shifts.count { !it.date.isBefore(today) }} upcoming shifts") })
                AssistChip(onClick = {}, label = { Text("${weekHours.toSimpleString()} $weekHoursLabel") })
                AssistChip(onClick = {}, label = { Text("${state.daysOff.count { !it.isBefore(today) }} upcoming days off") })
            }
        }
    }
}

@Composable
private fun AddShiftCard(
    templates: List<ShiftTemplate>,
    onAddDayOff: (LocalDate, ShiftTemplateKind) -> Unit,
    onSave: (WorkShift, ShiftTemplate?) -> Unit,
    onSaveTemplate: (ShiftTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("Work") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<ShiftTemplate?>(null) }
    var startsAt by remember { mutableStateOf(LocalDate.now().atTime(9, 0)) }
    var endsAt by remember { mutableStateOf(startsAt.plusHours(8)) }
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add shift", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            ShiftTemplateChips(
                templates = templates,
                onApply = { template ->
                    selectedTemplate = template
                    if (template.kind != ShiftTemplateKind.Work) {
                        onAddDayOff(startsAt.toLocalDate(), template.kind)
                        return@ShiftTemplateChips
                    }
                    title = template.label
                    location = template.location
                    notes = buildList {
                        if (template.notes.isNotBlank()) add(template.notes)
                        if (template.unpaidBreakMinutes > 0) add("Default unpaid break: ${template.unpaidBreakMinutes} min")
                    }.joinToString("\n")
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
                        val shift = WorkShift(
                            date = startsAt.toLocalDate(),
                            start = startsAt.toLocalTime(),
                            end = endsAt.toLocalTime(),
                            label = title.trim(),
                            location = location.trim(),
                            notes = notes.trim()
                        )
                        onSave(shift, selectedTemplate)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save shift") }
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            }
        }
    }
}

private val builtInShiftTemplates = listOf(
    ShiftTemplate(
        id = "builtin-opening-shift",
        name = "Opening",
        label = "Opening shift",
        start = LocalTime.of(6, 0),
        end = LocalTime.of(14, 0),
        notes = "Opening setup and first shift tasks.",
        status = "Workday",
        colorHex = "#C05621",
        defaultTasks = listOf("Check dates", "Fill case", "Review orders", "Prep department"),
        defaultReminders = listOf("Opening shift prep"),
        linkedShiftType = LinkedShiftType.Opening,
        builtIn = true
    ),
    ShiftTemplate(
        id = "builtin-closing-shift",
        name = "Closing",
        label = "Closing shift",
        start = LocalTime.of(14, 0),
        end = LocalTime.of(22, 0),
        notes = "Closing cleanup and manager handoff.",
        status = "Workday",
        colorHex = "#6B5A45",
        defaultTasks = listOf("Clean slicer", "Pull dates", "Fill grab-and-go", "Sweep/mop", "Manager handoff note"),
        defaultReminders = listOf("Start closing tasks"),
        linkedShiftType = LinkedShiftType.Closing,
        builtIn = true
    ),
    ShiftTemplate(id = "builtin-mid-shift", name = "Mid shift", label = "Mid shift", start = LocalTime.of(10, 0), end = LocalTime.of(18, 0), status = "Workday", colorHex = "#3F4E46", defaultTasks = listOf("Check schedule", "Restock station"), linkedShiftType = LinkedShiftType.Mid, builtIn = true),
    ShiftTemplate(id = "builtin-truck-order", name = "Truck day", label = "Truck day", start = LocalTime.of(7, 0), end = LocalTime.of(15, 0), notes = "Truck, ordering, or delivery follow-up.", status = "Truck", colorHex = "#D89A22", defaultTasks = listOf("Check order", "Note shorts", "Update inventory", "Follow up on vendor issues"), defaultReminders = listOf("Truck/order follow-up"), linkedShiftType = LinkedShiftType.Truck, builtIn = true),
    ShiftTemplate(id = "builtin-inventory", name = "Inventory", label = "Inventory", start = LocalTime.of(8, 0), end = LocalTime.of(16, 0), notes = "Inventory count and order checks.", status = "Inventory", colorHex = "#2F6F4E", defaultTasks = listOf("Count priority items", "Check outs", "Review order needs"), linkedShiftType = LinkedShiftType.Inventory, builtIn = true),
    ShiftTemplate(id = "builtin-training", name = "Training day", label = "Training day", start = LocalTime.of(9, 0), end = LocalTime.of(17, 0), notes = "Associate training and CBT follow-up.", status = "Training", colorHex = "#465A64", defaultTasks = listOf("Check training list", "Follow up with associates", "Update completion notes"), linkedShiftType = LinkedShiftType.Training, builtIn = true),
    ShiftTemplate(id = "builtin-manager-shift", name = "Manager shift", label = "Manager shift", start = LocalTime.of(8, 0), end = LocalTime.of(17, 0), notes = "Manager walk, handoff, schedule, and team follow-up.", status = "Manager", colorHex = "#6D3A7A", defaultTasks = listOf("Review staffing", "Check training follow-ups", "Manager handoff note"), linkedShiftType = LinkedShiftType.Manager, builtIn = true),
    ShiftTemplate(id = "builtin-day-off", name = "Day off", label = "Day off", status = "Day off", colorHex = "#3F4E46", kind = ShiftTemplateKind.DayOff, unpaidBreakMinutes = 0, builtIn = true),
    ShiftTemplate(id = "builtin-vacation", name = "Vacation", label = "Vacation", status = "Vacation", colorHex = "#3F4E46", kind = ShiftTemplateKind.Vacation, unpaidBreakMinutes = 0, builtIn = true),
    ShiftTemplate(id = "builtin-sick-day", name = "Sick day", label = "Sick day", status = "Sick", colorHex = "#C2413A", kind = ShiftTemplateKind.Sick, unpaidBreakMinutes = 0, builtIn = true),
    ShiftTemplate(id = "builtin-custom-shift", name = "Custom", label = "Custom shift", start = LocalTime.of(9, 0), end = LocalTime.of(17, 0), notes = "Custom shift template.", status = "Workday", colorHex = "#C05621", linkedShiftType = LinkedShiftType.Any, builtIn = true)
)

private fun shiftTemplatesFor(state: AppState): List<ShiftTemplate> = builtInShiftTemplates + state.shiftTemplates

private fun tasksForShiftTemplate(shift: WorkShift, template: ShiftTemplate): List<TaskItem> {
    val start = shift.date.atTime(shift.start)
    val end = shift.date.atTime(shift.end).let { if (shift.end.isBefore(shift.start)) it.plusDays(1) else it }
    val defaultTasks = template.defaultTasks.mapIndexed { index, title ->
        TaskItem(
            title = title,
            notes = "From ${template.name} shift template.",
            category = if (template.linkedShiftType == LinkedShiftType.Truck || template.linkedShiftType == LinkedShiftType.Inventory) TaskCategory.Orders else TaskCategory.General,
            priority = if (index == 0) TaskPriority.High else TaskPriority.Normal,
            deadline = if (template.linkedShiftType == LinkedShiftType.Closing) end.minusMinutes(30) else start.plusMinutes((index * 15L).coerceAtMost(90)),
            alarmAt = null,
            workRelated = true,
            linkedShiftId = shift.id,
            linkedShiftType = template.linkedShiftType,
            timingRule = if (template.linkedShiftType == LinkedShiftType.Closing) TaskTimingRule.BeforeShiftEnds else TaskTimingRule.AfterShiftStarts,
            skipDaysOff = true
        )
    }
    val reminders = template.defaultReminders.map { title ->
        TaskItem(
            title = title,
            notes = "Reminder from ${template.name} shift template.",
            category = TaskCategory.Admin,
            priority = TaskPriority.High,
            deadline = start.minusMinutes(30),
            alarmAt = start.minusMinutes(30),
            workRelated = true,
            linkedShiftId = shift.id,
            linkedShiftType = template.linkedShiftType,
            timingRule = TaskTimingRule.BeforeNextShift,
            skipDaysOff = true
        )
    }
    return defaultTasks + reminders
}

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
            templates.filter { it.enabled }.forEach { template ->
                OutlinedButton(onClick = { onApply(template) }) { Text(template.name) }
            }
        }
        Text("Manage templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        templates.forEach { template ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(template.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        buildList {
                            add(template.status)
                            if (template.defaultTasks.isNotEmpty()) add("${template.defaultTasks.size} tasks")
                            if (template.defaultReminders.isNotEmpty()) add("${template.defaultReminders.size} reminders")
                            if (!template.enabled) add("Disabled")
                        }.joinToString(" | "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!template.builtIn) {
                    TextButton(onClick = {
                        editing = template
                        editorOpen = true
                    }) { Text("Edit") }
                }
                TextButton(onClick = {
                    onSaveTemplate(
                        template.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            name = "${template.name} copy",
                            builtIn = false,
                            enabled = true
                        )
                    )
                }) { Text("Duplicate") }
                if (!template.builtIn) {
                    TextButton(onClick = { onSaveTemplate(template.copy(enabled = !template.enabled)) }) {
                        Text(if (template.enabled) "Disable" else "Enable")
                    }
                    TextButton(onClick = { onDeleteTemplate(template.id) }) { Text("Delete") }
                }
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
    var status by remember(template?.id) { mutableStateOf(template?.status ?: "Workday") }
    var colorHex by remember(template?.id) { mutableStateOf(template?.colorHex ?: "#C05621") }
    var defaultTasks by remember(template?.id) { mutableStateOf(template?.defaultTasks?.joinToString("\n").orEmpty()) }
    var defaultReminders by remember(template?.id) { mutableStateOf(template?.defaultReminders?.joinToString("\n").orEmpty()) }
    var breakMinutes by remember(template?.id) { mutableStateOf((template?.unpaidBreakMinutes ?: 30).toString()) }
    var linkedShiftType by remember(template?.id) { mutableStateOf(template?.linkedShiftType ?: LinkedShiftType.Any) }
    var enabled by remember(template?.id) { mutableStateOf(template?.enabled ?: true) }
    var marksDayOff by remember(template?.id) { mutableStateOf(template?.kind != null && template.kind != ShiftTemplateKind.Work) }
    var startsAt by remember(template?.id) { mutableStateOf(LocalDate.now().atTime(template?.start ?: LocalTime.of(9, 0))) }
    var endsAt by remember(template?.id) { mutableStateOf(LocalDate.now().atTime(template?.end ?: LocalTime.of(17, 0))) }
    Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template?.let { "Edit shift template" } ?: "Create shift template", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Template name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Role/title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = colorHex, onValueChange = { colorHex = it.take(7) }, label = { Text("Color hex") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = marksDayOff, onCheckedChange = { marksDayOff = it })
                Text("Marks a day off")
            }
            if (!marksDayOff) {
                DateTimeRow("Starts", startsAt, onChanged = { startsAt = it })
                DateTimeRow("Ends", endsAt, onChanged = { endsAt = it })
                OutlinedTextField(
                    value = breakMinutes,
                    onValueChange = { breakMinutes = it.filter(Char::isDigit).take(3) },
                    label = { Text("Default unpaid break minutes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Linked task type", style = MaterialTheme.typography.labelLarge)
                LinkedShiftTypeChips(selected = linkedShiftType, onSelected = { linkedShiftType = it })
            }
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = defaultTasks,
                onValueChange = { defaultTasks = it },
                label = { Text("Default tasks, one per line") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = defaultReminders,
                onValueChange = { defaultReminders = it },
                label = { Text("Default reminders, one per line") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                Text("Template enabled")
            }
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
                            status = status.trim().ifBlank { if (marksDayOff) "Day off" else "Workday" },
                            colorHex = colorHex.trim().ifBlank { "#C05621" },
                            defaultTasks = defaultTasks.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList(),
                            defaultReminders = defaultReminders.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList(),
                            unpaidBreakMinutes = breakMinutes.toIntOrNull()?.coerceAtLeast(0) ?: 30,
                            linkedShiftType = if (marksDayOff) LinkedShiftType.Any else linkedShiftType,
                            kind = if (marksDayOff) ShiftTemplateKind.DayOff else ShiftTemplateKind.Work,
                            enabled = enabled
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
    onAppearanceModeChanged: (AppearanceMode) -> Unit,
    onAccentStyleChanged: (AccentStyle) -> Unit,
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit,
    onPaySettingsChanged: (PaySettings) -> Unit,
    onShiftAlarmSettingsChanged: (ShiftAlarmSettings) -> Unit,
    onAlarmSettingsChanged: (AlarmSettings) -> Unit,
    calendars: List<DeviceCalendar>,
    calendarMessage: String?,
    onLoadCalendars: () -> Unit,
    onSelectCalendar: (Long?) -> Unit,
    onSyncCalendar: () -> Unit,
    onNotificationPermissionNeeded: () -> Unit,
    onTesterModeChanged: (Boolean) -> Unit,
    onOpenPremium: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        StyleSection(
            state = state,
            onAppearanceModeChanged = onAppearanceModeChanged,
            onAccentStyleChanged = onAccentStyleChanged,
            onWidgetLayoutModeChanged = onWidgetLayoutModeChanged,
            onOpenPremium = onOpenPremium
        )
        PayEstimateSection(
            state = state,
            onPaySettingsChanged = onPaySettingsChanged,
            onOpenPremium = onOpenPremium
        )
        ShiftAlarmSettingsSection(
            state = state,
            onSettingsChanged = onShiftAlarmSettingsChanged,
            onNotificationPermissionNeeded = onNotificationPermissionNeeded
        )
        AlarmDeliverySettingsSection(
            state = state,
            onSettingsChanged = onAlarmSettingsChanged,
            onNotificationPermissionNeeded = onNotificationPermissionNeeded
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
        PremiumSettingsSection(
            state = state,
            onTesterModeChanged = onTesterModeChanged,
            onOpenPremium = onOpenPremium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftAlarmSettingsSection(
    state: AppState,
    onSettingsChanged: (ShiftAlarmSettings) -> Unit,
    onNotificationPermissionNeeded: () -> Unit
) {
    val context = LocalContext.current
    val settings = state.shiftAlarmSettings
    val nextShift = state.shifts
        .filter { LocalDateTime.of(it.date, it.start).isAfter(LocalDateTime.now()) }
        .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
        .firstOrNull()
    val nextAlarm = nextShift?.let { LocalDateTime.of(it.date, it.start).minusMinutes(settings.offsetMinutes.toLong()) }
    fun askPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            onNotificationPermissionNeeded()
        }
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Shift alarms", "Ring before saved or imported shifts.")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Wake-up alarm before shifts", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Uses the same full alarm screen as task reminders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = {
                        if (it) askPermissionIfNeeded()
                        onSettingsChanged(settings.copy(enabled = it))
                    }
                )
            }
            Text("Alarm offset", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 80, 90, 120).forEach { minutes ->
                    FilterChip(
                        selected = settings.offsetMinutes == minutes,
                        onClick = { onSettingsChanged(settings.copy(offsetMinutes = minutes)) },
                        label = { Text(shiftAlarmOffsetLabel(minutes)) }
                    )
                }
            }
            OutlinedTextField(
                value = settings.offsetMinutes.toString(),
                onValueChange = { value ->
                    value.filter(Char::isDigit).take(4).toIntOrNull()?.let { minutes ->
                        onSettingsChanged(settings.copy(offsetMinutes = minutes.coerceIn(0, 24 * 60)))
                    }
                },
                label = { Text("Custom minutes before shift") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Only early shifts", style = MaterialTheme.typography.bodyLarge)
                    Text("Skip alarms for shifts starting at 9 AM or later.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.onlyEarlyShifts,
                    onCheckedChange = { onSettingsChanged(settings.copy(onlyEarlyShifts = it)) }
                )
            }
            val summary = when {
                !settings.enabled -> "Shift alarms are off."
                nextShift == null -> "Add or import shifts and Workday Planner will schedule alarms before them."
                nextAlarm != null -> "Next alarm: ${nextAlarm.format(dateTimeFormatter)} for ${nextShift.start.format(timeFormatter)} shift."
                else -> "Next shift alarm will follow your saved schedule."
            }
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun shiftAlarmOffsetLabel(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60
    return when {
        hours == 0 -> "$remaining min"
        remaining == 0 -> "${hours} hr"
        else -> "${hours} hr ${remaining} min"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlarmDeliverySettingsSection(
    state: AppState,
    onSettingsChanged: (AlarmSettings) -> Unit,
    onNotificationPermissionNeeded: () -> Unit
) {
    val context = LocalContext.current
    val settings = state.alarmSettings
    var testMessage by remember { mutableStateOf<String?>(null) }
    val alarmManager = remember { context.getSystemService(AlarmManager::class.java) }
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun openAlarmAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun openSystemAlarms() {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            testMessage = "No system alarms screen was available on this device."
        }
    }

    fun testAlarm() {
        val alarmAt = LocalDateTime.now().plusMinutes(1).withSecond(0).withNano(0)
        val task = TaskItem(
            title = "Test alarm",
            deadline = alarmAt.plusMinutes(settings.defaultReminderOffsetMinutes.toLong()),
            alarmAt = alarmAt,
            alarmDelivery = settings.defaultAlarmDelivery,
            alarmLabel = "Workday Planner - Test alarm",
            reminderType = ReminderType.FullAlarm,
            alarmOffsetMinutes = settings.defaultReminderOffsetMinutes.toLong()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            settings.defaultAlarmDelivery != AlarmDelivery.SystemClockAlarm &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            onNotificationPermissionNeeded()
        }
        val status = AlarmScheduler(context).schedule(task)
        testMessage = when (status) {
            com.example.workdayplanner.data.AlarmDispatchStatus.SentToSystemClock -> "Test sent to Clock for ${alarmAt.format(timeFormatter)}."
            com.example.workdayplanner.data.AlarmDispatchStatus.ScheduledInApp -> "Workday Planner test alarm scheduled for ${alarmAt.format(timeFormatter)}."
            com.example.workdayplanner.data.AlarmDispatchStatus.ScheduledNotification -> "Notification test scheduled for ${alarmAt.format(timeFormatter)}."
            com.example.workdayplanner.data.AlarmDispatchStatus.SystemClockFallbackScheduled -> "No Clock app accepted the alarm. Workday Planner test alarm scheduled for ${alarmAt.format(timeFormatter)}."
            com.example.workdayplanner.data.AlarmDispatchStatus.ExactAlarmAccessNeeded -> "Workday Planner needs alarm access to ring at the exact time."
            com.example.workdayplanner.data.AlarmDispatchStatus.NoClockAppAvailable -> "No compatible Clock app was found. Workday Planner will use its own alarm if access is allowed."
            else -> "Test alarm could not be scheduled. Check alarm and notification access."
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Task alarms", "Voice-created timed tasks use System Clock by default.")
            Text("Default reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 5, 15, 30, 60).forEach { minutes ->
                    FilterChip(
                        selected = settings.defaultReminderOffsetMinutes == minutes,
                        onClick = { onSettingsChanged(settings.copy(defaultReminderOffsetMinutes = minutes)) },
                        label = { Text(reminderOffsetLabel(minutes)) }
                    )
                }
            }
            Text("Alarm delivery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            AlarmDelivery.values().forEach { delivery ->
                FilterChip(
                    selected = settings.defaultAlarmDelivery == delivery,
                    onClick = { onSettingsChanged(settings.copy(defaultAlarmDelivery = delivery)) },
                    label = {
                        Text(if (delivery == AlarmDelivery.SystemClockAlarm) "${delivery.label} (recommended)" else delivery.label)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                if (canExact) "Alarm access: allowed or not required on this Android version."
                else "Alarm access: needed for Workday Planner-managed exact alarms.",
                style = MaterialTheme.typography.bodySmall,
                color = if (canExact) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = ::openSystemAlarms, modifier = Modifier.weight(1f)) {
                    Text("Open alarms")
                }
                OutlinedButton(onClick = ::openAlarmAccess, enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S, modifier = Modifier.weight(1f)) {
                    Text("Alarm access")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Vibration", style = MaterialTheme.typography.bodyLarge)
                    Text("Use vibration for app-managed alarms.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.vibration, onCheckedChange = { onSettingsChanged(settings.copy(vibration = it)) })
            }
            Text("Snooze default", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { minutes ->
                    FilterChip(
                        selected = settings.defaultSnoozeMinutes == minutes,
                        onClick = { onSettingsChanged(settings.copy(defaultSnoozeMinutes = minutes)) },
                        label = { Text("$minutes min") }
                    )
                }
            }
            Button(onClick = ::testAlarm, modifier = Modifier.fillMaxWidth()) {
                Text("Test alarm in 1 minute")
            }
            testMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { testMessage = "Good. Keep this delivery mode selected." }, modifier = Modifier.weight(1f)) { Text("I heard it") }
                    OutlinedButton(onClick = { testMessage = "Check alarm volume, battery restrictions, and selected delivery mode." }, modifier = Modifier.weight(1f)) { Text("Too quiet") }
                    OutlinedButton(onClick = { testMessage = "Check alarm access, notification access, Clock alarm volume, battery restrictions, and Do Not Disturb." }, modifier = Modifier.weight(1f)) { Text("No ring") }
                }
            }
        }
    }
}

private fun reminderOffsetLabel(minutes: Int): String = when (minutes) {
    0 -> "At due time"
    60 -> "1 hr before"
    else -> "$minutes min before"
}

@Composable
private fun PremiumSettingsSection(
    state: AppState,
    onTesterModeChanged: (Boolean) -> Unit,
    onOpenPremium: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Premium", "Free stays useful. Premium adds convenience for power users.")
            Text(if (state.premium.has(PremiumFeature.UnlimitedImports)) "Premium active" else "Free plan", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("Premium options are shown for planning. Purchases are not available in this build.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Tester mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Closed-test access for this device: unlimited imports, style packs, and premium planning tools. No purchase or subscription starts here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.premium.mockPremiumEnabled,
                    onCheckedChange = onTesterModeChanged
                )
            }
            if (state.premium.mockPremiumEnabled) {
                AssistChip(
                    onClick = {},
                    label = { Text("Beta testing unlocked") },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
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
    onTesterModeChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Workday Planner Premium", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("The free app covers basic shifts, tasks, reminders, days off, a few imports, and the Today dashboard.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Premium unlocks convenience and power-user tools without hiding existing data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Tester mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Use this for closed testing. It unlocks premium features locally and does not charge the tester.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.premium.mockPremiumEnabled,
                            onCheckedChange = onTesterModeChanged
                        )
                    }
                }
            }
        }
        item {
            Card(border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Premium preview", "Purchases are not available in this build.")
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
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to settings") }
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
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))
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
                isDayOff -> WorkdayAnimatedVisibility(visible = true) { DayOffCard() }
                shifts.isEmpty() -> WorkdayAnimatedVisibility(visible = true) {
                    Text("No shift saved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> shifts.forEach { shift ->
                    WorkdayAnimatedVisibility(visible = true) {
                        ShiftSummaryCard(shift = shift, linkedTaskCount = linkedTaskCount(shift), onDelete = { onDeleteShift(shift.id) })
                    }
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
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(220))
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
        ImportStepHeader("1", "Pick a schedule screenshot", "Take a photo or choose an image. Schedule images stay on your phone.")
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
        ImportProgressCard(
            isReading = isReading,
            hasPreview = parsed != null,
            completeMessage = message
        )
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
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        guidance?.let { ScheduleImportGuidanceCard(it) }
        parsed?.let {
            if (rawText.isNotBlank() && it.shifts.isEmpty() && it.daysOff.isEmpty()) {
                Text(
                    "OCR found schedule text, but we could not confidently build shifts. Try cropping the screenshot to only the schedule list, or review the detected lines below.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.animateContentSize(animationSpec = tween(220))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImportStepHeader("4", "Review detected shifts", "High confidence rows are pre-selected. Needs review rows must be confirmed before saving.")
                    val corrected = reviewRows.toParsedSchedule()
                    Text("${corrected.shifts.size} shifts, ${corrected.daysOff.size} days off confirmed")
                    Button(
                        onClick = { onApply(corrected) },
                        enabled = corrected.shifts.isNotEmpty() || corrected.daysOff.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Accept and proceed")
                    }
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
                        },
                        onAddDayOff = {
                            val date = reviewRows.firstOrNull { row -> row.kind != ScheduleReviewKind.Ignored }?.date ?: LocalDate.now()
                            reviewRows = reviewRows + ScheduleReviewRow(
                                kind = ScheduleReviewKind.DayOff,
                                date = date,
                                title = "Day off",
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
                        Text("Accept")
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

private enum class ScheduleReviewKind(val label: String) {
    Shift("Shift"),
    DayOff("Day off"),
    Vacation("Vacation"),
    Sick("Sick day"),
    Ignored("Ignored")
}

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
    val shiftType: String = "Custom",
    val role: String = "",
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
            shiftType = shift.label.toScheduleShiftType(),
            role = shift.notes,
            location = shift.location,
            notes = shift.notes,
            sourceText = shift.displayLine(),
            confidence = shift.importConfidence(),
            selected = shift.importConfidence() == ScheduleImportConfidence.High
        )
    }
    val dayOffRows = daysOff.map { date ->
        val kind = when (dayOffTypes[date]) {
            ShiftTemplateKind.Vacation -> ScheduleReviewKind.Vacation
            ShiftTemplateKind.Sick -> ScheduleReviewKind.Sick
            else -> ScheduleReviewKind.DayOff
        }
        ScheduleReviewRow(kind = kind, date = date, title = kind.label, confidence = ScheduleImportConfidence.High)
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
                    label = it.shiftType.takeIf { type -> type != "Custom" } ?: it.title.ifBlank { "Work" },
                    location = it.location,
                    notes = listOf(it.role, it.notes).filter { value -> value.isNotBlank() }.distinct().joinToString("\n")
                )
            },
        daysOff = activeRows.filter { it.kind in dayOffReviewKinds }.map { it.date }.toSet(),
        dayOffTypes = activeRows.filter { it.kind in dayOffReviewKinds }.associate { row ->
            row.date to when (row.kind) {
                ScheduleReviewKind.Vacation -> ShiftTemplateKind.Vacation
                ScheduleReviewKind.Sick -> ShiftTemplateKind.Sick
                else -> ShiftTemplateKind.DayOff
            }
        },
        unparsedLines = filter { it.kind == ScheduleReviewKind.Ignored && it.sourceText.isNotBlank() }.map { it.sourceText }
    )
}

private val dayOffReviewKinds = setOf(ScheduleReviewKind.DayOff, ScheduleReviewKind.Vacation, ScheduleReviewKind.Sick)

private val scheduleShiftTypes = listOf("Open", "Close", "Mid", "Truck", "Inventory", "Custom")

private fun String.toScheduleShiftType(): String {
    val lower = lowercase()
    return when {
        "open" in lower -> "Open"
        "close" in lower -> "Close"
        "mid" in lower -> "Mid"
        "truck" in lower -> "Truck"
        "inventory" in lower -> "Inventory"
        else -> "Custom"
    }
}

private fun WorkShift.importConfidence(): ScheduleImportConfidence {
    return when {
        label.isBlank() || label == "Work" -> ScheduleImportConfidence.NeedsReview
        location.isBlank() && notes.isBlank() -> ScheduleImportConfidence.NeedsReview
        else -> ScheduleImportConfidence.High
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleReviewRows(
    rows: List<ScheduleReviewRow>,
    onRowsChanged: (List<ScheduleReviewRow>) -> Unit,
    onAddShift: () -> Unit,
    onAddDayOff: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Review rows", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onAddShift) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add shift")
            }
            OutlinedButton(onClick = onAddDayOff) {
                Text("Add day off")
            }
        }
        rows.groupBy { if (it.kind == ScheduleReviewKind.Ignored) null else it.date }.toSortedMap(compareBy<LocalDate?> { it ?: LocalDate.MAX }).forEach { (date, dateRows) ->
            Text(date?.format(dateFormatter) ?: "Unclear OCR lines", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            dateRows.forEach { row ->
                WorkdayAnimatedVisibility(visible = true) {
                    ScheduleReviewRowCard(
                        row = row,
                        onChange = { changed -> onRowsChanged(rows.map { if (it.id == row.id) changed else it }) },
                        onDelete = { onRowsChanged(rows.filterNot { it.id == row.id }) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportProgressCard(
    isReading: Boolean,
    hasPreview: Boolean,
    completeMessage: String?
) {
    val importSaved = completeMessage?.startsWith("Added or updated") == true
    val stage = when {
        isReading -> "Reading image"
        importSaved -> "Import complete"
        hasPreview -> "Reviewing results"
        else -> null
    } ?: return
    val motionEnabled = rememberMotionEnabled()
    val pulse = if (motionEnabled && isReading) {
        val transition = rememberInfiniteTransition(label = "importPulse")
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "importAlpha"
        ).value
    } else {
        1f
    }
    WorkdayAnimatedVisibility(visible = true) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stage, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf("Reading image", "Detecting shifts", "Reviewing results", "Import complete").forEach { label ->
                        val active = label == stage || (hasPreview && label == "Detecting shifts")
                        val color by animateColorAsState(
                            targetValue = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            animationSpec = tween(160),
                            label = "importStageColor"
                        )
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .weight(1f)
                                .alpha(if (active && isReading) pulse else 1f)
                                .background(color)
                        )
                    }
                }
                Text(
                    when {
                        isReading -> "Reading the screenshot on this device."
                        completeMessage != null -> completeMessage
                        else -> "Detected entries are ready to review before saving."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                if (row.kind == ScheduleReviewKind.Vacation) AssistChip(onClick = {}, label = { Text("Vacation") })
                if (row.kind == ScheduleReviewKind.Sick) AssistChip(onClick = {}, label = { Text("Sick day") })
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
                Text("Shift type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    scheduleShiftTypes.forEach { type ->
                        FilterChip(
                            selected = row.shiftType == type,
                            onClick = {
                                onChange(
                                    row.copy(
                                        shiftType = type,
                                        title = if (type == "Custom") row.title else type,
                                        selected = true,
                                        confidence = ScheduleImportConfidence.NeedsReview
                                    )
                                )
                            },
                            label = { Text(type) }
                        )
                    }
                }
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
                    label = { Text("Custom title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = row.role,
                    onValueChange = { onChange(row.copy(role = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) },
                    label = { Text("Role / department") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = row.location,
                    onValueChange = { onChange(row.copy(location = it, selected = true, confidence = ScheduleImportConfidence.NeedsReview)) },
                    label = { Text("Store / location") },
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
                if (row.kind != ScheduleReviewKind.Vacation) {
                    OutlinedButton(onClick = { onChange(row.copy(kind = ScheduleReviewKind.Vacation, title = "Vacation", selected = true, confidence = ScheduleImportConfidence.NeedsReview)) }, modifier = Modifier.weight(1f)) {
                        Text("Vacation")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (row.kind != ScheduleReviewKind.Sick) {
                    OutlinedButton(onClick = { onChange(row.copy(kind = ScheduleReviewKind.Sick, title = "Sick day", selected = true, confidence = ScheduleImportConfidence.NeedsReview)) }, modifier = Modifier.weight(1f)) {
                        Text("Sick")
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
        ScheduleImportConfidence.High -> MaterialTheme.colorScheme.success
        ScheduleImportConfidence.NeedsReview -> MaterialTheme.colorScheme.warning
        ScheduleImportConfidence.Unclear -> MaterialTheme.colorScheme.error
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
    val motionEnabled = rememberMotionEnabled()
    var appeared by remember { mutableStateOf(!motionEnabled) }
    LaunchedEffect(Unit) { appeared = true }
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(if (motionEnabled) 220 else 0),
        label = "emptyStateAlpha"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .alpha(alpha),
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
    data object WeeklyReview : Screen("weeklyReview", "Weekly", Icons.Default.Event)
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
    RepeatRule.InventoryDays -> "Inventory days"
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
