package com.example.workdayplanner.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.workdayplanner.calendar.DeviceCalendar
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.RepeatRule
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.TaskCategory
import com.example.workdayplanner.data.TaskRecurrence
import com.example.workdayplanner.data.WorkNote
import com.example.workdayplanner.data.WorkNoteKind
import com.example.workdayplanner.data.WidgetLayoutMode
import com.example.workdayplanner.data.ParsedSchedule
import com.example.workdayplanner.data.WorkShift
import com.example.workdayplanner.data.WorkEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val screenPadding = 18.dp
private val sectionGap = 14.dp

@Composable
fun PlannerApp(
    viewModel: PlannerViewModel,
    requestedTaskId: String? = null,
    onTaskRequestHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val calendarMessage by viewModel.calendarMessage.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
    val topLevel = listOf(Screen.Tasks, Screen.Schedule, Screen.Import)

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
                    currentRoute.startsWith(Screen.TaskDetail.route) -> "Task"
                    currentRoute.startsWith(Screen.EventDetail.route) -> "Event"
                    currentRoute == Screen.Schedule.route -> "Schedule"
                    currentRoute == Screen.Import.route -> "Import"
                    else -> "Workday Planner"
                }
            )
        },
        bottomBar = {
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
                        label = { Text(screen.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Tasks.route) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("${Screen.TaskDetail.route}/new") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New task") }
                )
            }
        }
    ) { padding ->
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
                    onAddEvent = { navController.navigate("${Screen.EventDetail.route}/new") },
                    onToggleComplete = viewModel::toggleComplete,
                    onDelete = viewModel::deleteTask,
                    onAddNote = viewModel::addWorkNote,
                    onDeleteNote = viewModel::deleteWorkNote,
                    onCreateTaskFromNote = viewModel::createTaskFromNote,
                    onDeleteEvent = viewModel::deleteEvent
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(
                    state = state,
                    onAddDayOff = viewModel::addDayOff,
                    onRemoveDayOff = viewModel::removeDayOff,
                    onClearSchedule = viewModel::clearSchedule,
                    onDarkModeChanged = viewModel::setDarkMode,
                    onAccentStyleChanged = viewModel::setAccentStyle,
                    onWidgetLayoutModeChanged = viewModel::setWidgetLayoutMode,
                    calendars = calendars,
                    calendarMessage = calendarMessage,
                    onLoadCalendars = viewModel::loadCalendars,
                    onSelectCalendar = viewModel::setSelectedCalendar,
                    onSyncCalendar = viewModel::syncShiftsToCalendar
                )
            }
            composable(Screen.Import.route) {
                ImportScreen(
                    rawText = importState.rawText,
                    parsed = importState.parsed,
                    isReading = importState.isReadingImage,
                    message = importState.appliedMessage,
                    error = importState.error,
                    onTextChange = viewModel::setImportText,
                    onImagePicked = viewModel::recognizeScheduleImage,
                    onPreview = { viewModel.previewImport() },
                    onApply = viewModel::applyImport
                )
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
    onAddEvent: () -> Unit,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onCreateTaskFromNote: (String) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val today = LocalDate.now()
    val tasks = state.tasks.sortedWith(compareBy<TaskItem> { it.completed }.thenBy { it.deadline })
    val todayTasks = tasks.filter { task ->
        !task.completed && task.deadline?.toLocalDate() == today
    }
    val todayTaskIds = todayTasks.map { it.id }.toSet()
    val otherTasks = tasks.filterNot { task -> task.id in todayTaskIds }
    val events = state.events.sortedBy { it.startsAt }
    val todayNotes = state.notes.filter { it.date == today }.sortedByDescending { it.createdAt }
    val recentNotes = state.notes.filterNot { it.date == today }.sortedByDescending { it.createdAt }.take(4)
    if (tasks.isEmpty() && events.isEmpty() && state.notes.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(screenPadding), verticalArrangement = Arrangement.spacedBy(sectionGap)) {
            CommandCenterCard(state = state)
            DailyNotesSection(
                todayNotes = emptyList(),
                recentNotes = emptyList(),
                onAddNote = onAddNote,
                onDeleteNote = onDeleteNote,
                onCreateTaskFromNote = onCreateTaskFromNote
            )
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
        item { CommandCenterCard(state = state) }
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
        if (todayTasks.isNotEmpty()) {
            item { SectionHeader("Today tasks", "Work items due today") }
            items(todayTasks, key = { "today-${it.id}" }) { task ->
                TaskCard(
                    task = task,
                    isDayOff = task.deadline?.toLocalDate()?.let { TaskRecurrence.isDayOff(it, state) } == true,
                    onClick = { onTaskClick(task) },
                    onToggleComplete = { onToggleComplete(task.id) },
                    onDelete = { onDelete(task.id) }
                )
            }
        }
        if (otherTasks.isNotEmpty()) {
            item {
                SectionHeader(
                    title = if (todayTasks.isEmpty()) "Daily to-dos" else "Upcoming and completed",
                    subtitle = if (todayTasks.isEmpty()) "Tasks for your workday" else "Everything not due today"
                )
            }
        }
        items(otherTasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                isDayOff = task.deadline?.toLocalDate()?.let { TaskRecurrence.isDayOff(it, state) } == true,
                onClick = { onTaskClick(task) },
                onToggleComplete = { onToggleComplete(task.id) },
                onDelete = { onDelete(task.id) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommandCenterCard(state: AppState) {
    val today = LocalDate.now()
    val todayShifts = state.shifts.filter { it.date == today }.sortedBy { it.start }
    val nextShift = state.shifts
        .filter { !it.date.isBefore(today) }
        .minWithOrNull(compareBy<WorkShift> { it.date }.thenBy { it.start })
    val openTasks = state.tasks.count { !it.completed }
    val upcomingEvents = state.events.count { !it.startsAt.toLocalDate().isBefore(today) }
    val todayNotes = state.notes.count { it.date == today }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Today at work",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = when {
                    today in state.daysOff -> "You are marked not scheduled today."
                    todayShifts.isNotEmpty() -> todayShifts.joinToString { "${it.start.format(timeFormatter)} - ${it.end.format(timeFormatter)}" }
                    nextShift != null -> "Next shift: ${nextShift.date.format(dateFormatter)} at ${nextShift.start.format(timeFormatter)}"
                    else -> "No upcoming shift imported."
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("$openTasks open tasks") })
                AssistChip(onClick = {}, label = { Text("$upcomingEvents upcoming events") })
                AssistChip(onClick = {}, label = { Text("$todayNotes notes today") })
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
    isDayOff: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                if (task.category != TaskCategory.General) AssistChip(onClick = {}, label = { Text(task.category.label) })
                task.deadline?.let { AssistChip(onClick = {}, label = { Text(it.format(dateTimeFormatter)) }) }
                if (task.repeatRule != RepeatRule.None) AssistChip(onClick = {}, label = { Text(task.repeatLabel()) })
                if (isDayOff) AssistChip(onClick = {}, label = { Text("Day off") })
            }
        }
    }
}

@Composable
private fun TaskDetailScreen(state: AppState, task: TaskItem?, onSave: (TaskItem) -> Unit, onCancel: () -> Unit) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var notes by remember(task?.id) { mutableStateOf(task?.notes.orEmpty()) }
    var category by remember(task?.id) { mutableStateOf(task?.category ?: TaskCategory.General) }
    var deadline by remember(task?.id) { mutableStateOf(task?.deadline ?: LocalDateTime.now().plusHours(4)) }
    var alarmAt by remember(task?.id) { mutableStateOf(task?.alarmAt ?: deadline.minusMinutes(30)) }
    var repeatRule by remember(task?.id) { mutableStateOf(task?.repeatRule ?: RepeatRule.None) }
    var repeatDays by remember(task?.id) { mutableStateOf(task?.repeatDays ?: emptySet()) }
    var skipDaysOff by remember(task?.id) { mutableStateOf(task?.skipDaysOff ?: true) }
    val nextShift = remember(state.shifts) {
        val now = LocalDateTime.now()
        state.shifts
            .map { shift -> shift.date.atTime(shift.start) }
            .filter { it.isAfter(now) }
            .minOrNull()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        TaskCategorySelector(selected = category, onSelected = { category = it })
        DateTimeRow("Deadline", deadline, onChanged = { deadline = it })
        DateTimeRow("Alarm", alarmAt, onChanged = { alarmAt = it })
        nextShift?.let { shiftStart ->
            OutlinedButton(
                onClick = {
                    deadline = shiftStart.minusMinutes(30)
                    alarmAt = deadline.minusMinutes(30)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set due before next shift (${shiftStart.format(dateTimeFormatter)})")
            }
        }
        RepeatRuleDropdown(selected = repeatRule, onSelected = { repeatRule = it })
        if (repeatRule == RepeatRule.CustomDays) {
            CustomRepeatDays(
                selectedDays = repeatDays,
                onToggle = { day ->
                    repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = skipDaysOff, onCheckedChange = { skipDaysOff = it })
            Text("Skip days off when repeating")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = title.isNotBlank() && (repeatRule != RepeatRule.CustomDays || repeatDays.isNotEmpty()),
                onClick = {
                    onSave(
                        TaskItem(
                            id = task?.id ?: java.util.UUID.randomUUID().toString(),
                            title = title.trim(),
                            notes = notes.trim(),
                            category = category,
                            deadline = deadline,
                            alarmAt = alarmAt,
                            repeatRule = repeatRule,
                            repeatDays = if (repeatRule == RepeatRule.CustomDays) repeatDays else emptySet(),
                            skipDaysOff = skipDaysOff,
                            completed = task?.completed ?: false
                        )
                    )
                }
            ) { Text("Save task") }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
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
                if (category == selected) {
                    Button(onClick = { onSelected(category) }) { Text(category.label) }
                } else {
                    OutlinedButton(onClick = { onSelected(category) }) { Text(category.label) }
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
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Dark mode", style = MaterialTheme.typography.bodyLarge)
                    Text("App and widgets use the same mood.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.darkMode, onCheckedChange = onDarkModeChanged)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AccentStyle.entries.forEach { style ->
                    if (state.accentStyle == style) {
                        Button(onClick = { onAccentStyleChanged(style) }, modifier = Modifier.weight(1f)) {
                            Text(style.label)
                        }
                    } else {
                        OutlinedButton(onClick = { onAccentStyleChanged(style) }, modifier = Modifier.weight(1f)) {
                            Text(style.label)
                        }
                    }
                }
            }
            Text("Planner widget", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WidgetLayoutMode.entries.forEach { mode ->
                    if (state.widgetLayoutMode == mode) {
                        Button(onClick = { onWidgetLayoutModeChanged(mode) }, modifier = Modifier.weight(1f)) {
                            Text(mode.label)
                        }
                    } else {
                        OutlinedButton(onClick = { onWidgetLayoutModeChanged(mode) }, modifier = Modifier.weight(1f)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarSyncSection(
    state: AppState,
    calendars: List<DeviceCalendar>,
    message: String?,
    onLoadCalendars: () -> Unit,
    onSelectCalendar: (Long?) -> Unit,
    onSyncCalendar: () -> Unit
) {
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
    onAddDayOff: (LocalDate) -> Unit,
    onRemoveDayOff: (LocalDate) -> Unit,
    onClearSchedule: () -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onAccentStyleChanged: (AccentStyle) -> Unit,
    onWidgetLayoutModeChanged: (WidgetLayoutMode) -> Unit,
    calendars: List<DeviceCalendar>,
    calendarMessage: String?,
    onLoadCalendars: () -> Unit,
    onSelectCalendar: (Long?) -> Unit,
    onSyncCalendar: () -> Unit
) {
    var dayOffText by remember { mutableStateOf(LocalDate.now().toString()) }
    var invalidDate by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        ScheduleHero(state = state)
        StyleSection(
            state = state,
            onDarkModeChanged = onDarkModeChanged,
            onAccentStyleChanged = onAccentStyleChanged,
            onWidgetLayoutModeChanged = onWidgetLayoutModeChanged
        )
        CalendarSyncSection(
            state = state,
            calendars = calendars,
            message = calendarMessage,
            onLoadCalendars = onLoadCalendars,
            onSelectCalendar = onSelectCalendar,
            onSyncCalendar = onSyncCalendar
        )
        OutlinedButton(
            onClick = onClearSchedule,
            enabled = state.shifts.isNotEmpty() || state.daysOff.isNotEmpty()
        ) {
            Text("Clear imported schedule data")
        }
        WeekCalendar(state = state)
        Text("Days off", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = dayOffText,
                onValueChange = {
                    dayOffText = it
                    invalidDate = false
                },
                label = { Text("Add day off (YYYY-MM-DD)") },
                isError = invalidDate,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                runCatching { LocalDate.parse(dayOffText) }
                    .onSuccess(onAddDayOff)
                    .onFailure { invalidDate = true }
            }) { Text("Add day off") }
        }
        state.daysOff.sorted().forEach { date ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(date.format(dateFormatter), modifier = Modifier.weight(1f))
                TextButton(onClick = { onRemoveDayOff(date) }) { Text("Remove") }
            }
        }
        Divider()
        Text("Default days off: ${state.defaultDaysOff.sortedBy(DayOfWeek::getValue).joinToString { it.name.lowercase().replaceFirstChar(Char::uppercase) }}")
    }
}

@Composable
private fun WeekCalendar(state: AppState) {
    val visibleDates = state.shifts.map { it.date } + state.daysOff
    val weekStarts = visibleDates
        .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)) }
        .distinct()
        .sorted()
        .ifEmpty { listOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Work weeks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        weekStarts.forEach { weekStart ->
            ScheduleWeekCard(weekStart = weekStart, state = state)
        }
    }
}

@Composable
private fun ScheduleWeekCard(weekStart: LocalDate, state: AppState) {
    val days = (0L..6L).map { weekStart.plusDays(it) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "${weekStart.format(dateFormatter)} - ${days.last().format(dateFormatter)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Divider()
            days.forEach { date ->
                val shifts = state.shifts.filter { it.date == date }.sortedBy { it.start }
                val isOff = date in state.daysOff
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.width(92.dp)) {
                        Text(
                            date.dayOfWeek.name.take(3).lowercase().replaceFirstChar(Char::uppercase),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            "${date.monthValue}/${date.dayOfMonth}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        when {
                            isOff -> Text(
                                "Not scheduled",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            shifts.isEmpty() -> Text("No shift", style = MaterialTheme.typography.bodyLarge)
                            else -> shifts.forEach { shift ->
                                Text(
                                    "${shift.start.format(timeFormatter)} - ${shift.end.format(timeFormatter)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportScreen(
    rawText: String,
    parsed: ParsedSchedule?,
    isReading: Boolean,
    message: String?,
    error: String?,
    onTextChange: (String) -> Unit,
    onImagePicked: (android.net.Uri) -> Unit,
    onPreview: () -> Unit,
    onApply: () -> Unit
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImagePicked(uri)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        ImportStepHeader("1", "Pick a schedule screenshot", "Crop to the schedule list when possible for the cleanest import.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { imagePicker.launch("image/*") }, enabled = !isReading) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (isReading) "Reading screenshot..." else "Choose screenshot")
            }
            OutlinedButton(onClick = onPreview, enabled = rawText.isNotBlank()) { Text("Build preview") }
        }
        ImportStepHeader("2", "Review recognized text", "You can edit the OCR text before building the preview.")
        OutlinedTextField(
            value = rawText,
            onValueChange = onTextChange,
            label = { Text("Recognized schedule text") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
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
                    ImportStepHeader("3", "Preview shifts", "Confirm the app found the right shifts and days off.")
                    Text("${it.shifts.size} shifts, ${it.daysOff.size} days off")
                    ParsedScheduleRows(it)
                    if (it.unparsedLines.isNotEmpty()) Text("${it.unparsedLines.size} lines need review")
                    ManualCorrectionRows(it.unparsedLines)
                    Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) { Text("Apply schedule import") }
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
    data object Tasks : Screen("tasks", "Tasks", Icons.Default.CheckCircle)
    data object Schedule : Screen("schedule", "Schedule", Icons.Default.CalendarMonth)
    data object Import : Screen("import", "Import", Icons.Default.FileUpload)
    data object TaskDetail : Screen("task", "Task", Icons.Default.CheckCircle)
    data object EventDetail : Screen("event", "Event", Icons.Default.Event)
}

private fun RepeatRule.displayName(): String = when (this) {
    RepeatRule.None -> "None"
    RepeatRule.Daily -> "Daily"
    RepeatRule.Weekdays -> "Weekdays"
    RepeatRule.Weekly -> "Weekly"
    RepeatRule.CustomDays -> "Custom days"
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
