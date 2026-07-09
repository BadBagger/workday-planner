package com.example.workdayplanner

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workdayplanner.alarm.AlarmScheduler
import com.example.workdayplanner.calendar.CalendarSyncManager
import com.example.workdayplanner.calendar.DeviceCalendar
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AppearanceMode
import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.ParsedSchedule
import com.example.workdayplanner.data.PaySettings
import com.example.workdayplanner.data.PremiumAccess
import com.example.workdayplanner.data.PlannerRepository
import com.example.workdayplanner.data.ScheduleTextParser
import com.example.workdayplanner.data.ScheduleChangeDetector
import com.example.workdayplanner.data.ScheduleChangeSet
import com.example.workdayplanner.data.ScheduleImportGuidance
import com.example.workdayplanner.data.ScheduleImportGuidanceClassifier
import com.example.workdayplanner.data.ScheduleImportIssue
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.TaskTemplate
import com.example.workdayplanner.data.TaskRecurrence
import com.example.workdayplanner.data.TaskCategory
import com.example.workdayplanner.data.TaskPriority
import com.example.workdayplanner.data.TimecardEntry
import com.example.workdayplanner.data.TrainingItem
import com.example.workdayplanner.data.TrainingTextParser
import com.example.workdayplanner.data.WorkChecklistTemplates
import com.example.workdayplanner.data.WorkNoteOrganizer
import com.example.workdayplanner.data.WorkEvent
import com.example.workdayplanner.data.WidgetLayoutMode
import com.example.workdayplanner.data.WorkImage
import com.example.workdayplanner.data.WorkShift
import com.example.workdayplanner.data.ShiftTemplate
import com.example.workdayplanner.data.ShiftTemplateKind
import com.example.workdayplanner.data.ShiftPattern
import com.example.workdayplanner.data.ScheduleAwareTaskPlanner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

class PlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlannerRepository(application)
    private val alarmScheduler = AlarmScheduler(application)
    private val calendarSyncManager = CalendarSyncManager(application)
    val state: StateFlow<AppState> = repository.state

    private val mutableImportState = MutableStateFlow(ImportUiState())
    val importState: StateFlow<ImportUiState> = mutableImportState.asStateFlow()
    private val mutableCalendars = MutableStateFlow<List<DeviceCalendar>>(emptyList())
    val calendars: StateFlow<List<DeviceCalendar>> = mutableCalendars.asStateFlow()
    private val mutableCalendarMessage = MutableStateFlow<String?>(null)
    val calendarMessage: StateFlow<String?> = mutableCalendarMessage.asStateFlow()
    private val mutableImageMessage = MutableStateFlow<String?>(null)
    val imageMessage: StateFlow<String?> = mutableImageMessage.asStateFlow()
    private val mutableTrainingImportState = MutableStateFlow(TrainingImportUiState())
    val trainingImportState: StateFlow<TrainingImportUiState> = mutableTrainingImportState.asStateFlow()

    init {
        alarmScheduler.rescheduleOpenTasks(state.value.tasks)
    }

    fun saveTask(task: TaskItem) {
        val resolved = ScheduleAwareTaskPlanner.resolve(task, state.value)
        repository.upsertTask(resolved)
        alarmScheduler.cancel(resolved.id)
        alarmScheduler.schedule(resolved)
    }

    fun addChecklistTemplate(templateId: String) {
        WorkChecklistTemplates.tasksFor(templateId).forEach(::saveTask)
    }

    fun deleteTask(taskId: String) {
        repository.deleteTask(taskId)
        alarmScheduler.cancel(taskId)
    }

    fun addWorkNote(text: String) {
        if (text.isBlank()) return
        repository.addNote(WorkNoteOrganizer.create(text))
    }

    fun deleteWorkNote(noteId: String) {
        repository.deleteNote(noteId)
    }

    fun createTaskFromNote(noteId: String) {
        val note = state.value.notes.firstOrNull { it.id == noteId } ?: return
        val task = TaskItem(
            title = note.text.lineSequence().firstOrNull()?.take(60)?.ifBlank { "Follow up note" } ?: "Follow up note",
            notes = note.text,
            category = TaskCategory.Admin,
            deadline = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
            alarmAt = LocalDateTime.now().plusDays(1).withHour(8).withMinute(30)
        )
        saveTask(task)
    }

    fun addWorkImage(title: String, sourceUri: Uri) {
        viewModelScope.launch {
            mutableImageMessage.value = "Reading work image..."
            val result = runCatching {
                val savedFile = copyWorkImage(sourceUri)
                val image = InputImage.fromFilePath(getApplication(), Uri.fromFile(savedFile))
                val recognized = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .await()
                val detectedText = recognized.toReadingOrderText()
                val cleanTitle = title.trim().ifBlank {
                    detectedText.lineSequence().firstOrNull { it.isNotBlank() }?.take(48) ?: "Work image"
                }
                WorkImage(
                    title = cleanTitle,
                    imagePath = savedFile.absolutePath,
                    detectedText = detectedText,
                    tags = detectImageTags("$cleanTitle\n$detectedText")
                )
            }
            result.fold(
                onSuccess = {
                    repository.addImage(it)
                    mutableImageMessage.value = if (it.detectedText.isBlank()) {
                        "Saved image. Add a clear title so it is easy to search."
                    } else {
                        "Saved image and indexed detected text."
                    }
                },
                onFailure = {
                    mutableImageMessage.value = it.message ?: "Could not save image."
                }
            )
        }
    }

    fun deleteWorkImage(imageId: String) {
        repository.deleteImage(imageId)
    }

    fun setTrainingImportText(text: String) {
        val parsed = TrainingTextParser.parse(text)
        mutableTrainingImportState.value = mutableTrainingImportState.value.copy(
            rawText = text,
            parsedItems = parsed,
            message = null,
            error = null
        )
    }

    fun recognizeTrainingImage(uri: Uri) {
        viewModelScope.launch {
            mutableTrainingImportState.value = mutableTrainingImportState.value.copy(isReadingImage = true, error = null, message = null)
            val result = runCatching {
                val image = InputImage.fromFilePath(getApplication(), uri)
                val recognized = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .await()
                recognized.toReadingOrderText()
            }
            result.fold(
                onSuccess = { text ->
                    mutableTrainingImportState.value = TrainingImportUiState(
                        rawText = text,
                        parsedItems = TrainingTextParser.parse(text),
                        isReadingImage = false
                    )
                },
                onFailure = {
                    mutableTrainingImportState.value = mutableTrainingImportState.value.copy(
                        isReadingImage = false,
                        error = it.message ?: "Could not read training photo."
                    )
                }
            )
        }
    }

    fun importTrainingItems() {
        val current = mutableTrainingImportState.value
        val items = current.parsedItems.ifEmpty { TrainingTextParser.parse(current.rawText) }
        if (items.isEmpty()) {
            mutableTrainingImportState.value = current.copy(error = "No training rows found yet.")
            return
        }
        repository.addTrainingItems(items)
        mutableTrainingImportState.value = current.copy(
            parsedItems = items,
            message = "Imported ${items.size} training items.",
            error = null
        )
    }

    fun addManualTrainingItem(associateName: String, trainingTitle: String, dueDate: LocalDate?) {
        if (associateName.isBlank() || trainingTitle.isBlank()) {
            mutableTrainingImportState.value = mutableTrainingImportState.value.copy(error = "Associate and training title are required.")
            return
        }
        repository.addTrainingItems(
            listOf(
                TrainingItem(
                    associateName = associateName.trim(),
                    trainingTitle = trainingTitle.trim(),
                    dueDate = dueDate,
                    sourceText = "Manual entry"
                )
            )
        )
        mutableTrainingImportState.value = mutableTrainingImportState.value.copy(
            message = "Added training for ${associateName.trim()}.",
            error = null
        )
    }

    fun toggleTrainingComplete(trainingId: String) {
        repository.toggleTrainingComplete(trainingId)
    }

    fun deleteTrainingItem(trainingId: String) {
        repository.deleteTrainingItem(trainingId)
    }

    fun createTaskFromTrainingItem(trainingId: String) {
        val item = state.value.trainingItems.firstOrNull { it.id == trainingId } ?: return
        saveTask(item.toTrainingTask())
    }

    fun createTrainingFollowUpTasks() {
        val today = LocalDate.now()
        val existingTitles = state.value.tasks.map { it.title.lowercase() }.toSet()
        state.value.trainingItems
            .filter { it.completedAt == null }
            .filter { item -> item.dueDate == null || !item.dueDate.isAfter(today.plusDays(7)) }
            .sortedWith(compareBy<TrainingItem> { it.dueDate ?: LocalDate.MAX }.thenBy { it.associateName })
            .take(10)
            .map { it.toTrainingTask() }
            .filterNot { it.title.lowercase() in existingTitles }
            .forEach(::saveTask)
    }

    fun saveEvent(event: WorkEvent) {
        repository.upsertEvent(event)
    }

    fun deleteEvent(eventId: String) {
        repository.deleteEvent(eventId)
    }

    fun toggleComplete(taskId: String) {
        val task = state.value.tasks.firstOrNull { it.id == taskId } ?: return
        repository.toggleComplete(taskId)
        if (!task.completed) {
            alarmScheduler.cancel(taskId)
            TaskRecurrence.nextOccurrence(task, state.value)?.let(::saveTaskIfMissing)
        } else {
            alarmScheduler.schedule(task.copy(completed = false))
        }
    }

    private fun saveTaskIfMissing(task: TaskItem) {
        val exists = state.value.tasks.any {
            it.id != task.id && it.title == task.title && it.deadline == task.deadline
        }
        if (!exists) saveTask(task)
    }

    fun addDayOff(date: LocalDate) {
        repository.addDayOff(date)
    }

    fun addTypedDayOff(date: LocalDate, kind: ShiftTemplateKind) {
        repository.addDayOff(date, kind)
    }

    fun saveShift(shift: WorkShift) {
        repository.upsertShift(shift)
        rescheduleTasksForShift(shift.id)
    }

    fun deleteShift(shiftId: String) {
        state.value.tasks.filter { it.linkedShiftId == shiftId }.forEach { task ->
            alarmScheduler.cancel(task.id)
        }
        repository.deleteShift(shiftId)
    }

    fun saveShiftPattern(pattern: ShiftPattern) {
        repository.upsertShiftPattern(pattern)
    }

    fun applyShiftPattern(pattern: ShiftPattern, allowDuplicates: Boolean) {
        repository.applyShiftPattern(pattern, allowDuplicates)
        alarmScheduler.rescheduleOpenTasks(state.value.tasks)
    }

    fun setShiftPatternEnabled(patternId: String, enabled: Boolean) {
        repository.setShiftPatternEnabled(patternId, enabled)
    }

    fun deleteShiftPattern(patternId: String) {
        repository.deleteShiftPattern(patternId)
    }

    fun saveShiftTemplate(template: ShiftTemplate) {
        repository.upsertShiftTemplate(template)
    }

    fun deleteShiftTemplate(templateId: String) {
        repository.deleteShiftTemplate(templateId)
    }

    fun saveTaskTemplate(template: TaskTemplate) {
        repository.upsertTaskTemplate(template)
    }

    fun deleteTaskTemplate(templateId: String) {
        repository.deleteTaskTemplate(templateId)
    }

    fun removeDayOff(date: LocalDate) {
        repository.removeDayOff(date)
    }

    fun clearSchedule() {
        repository.clearSchedule()
        alarmScheduler.rescheduleOpenTasks(state.value.tasks)
    }

    private fun rescheduleTasksForShift(shiftId: String) {
        val linked = state.value.tasks.filter { it.linkedShiftId == shiftId }
        linked.forEach { task ->
            val resolved = ScheduleAwareTaskPlanner.resolve(task, state.value)
            repository.upsertTask(resolved)
            alarmScheduler.cancel(resolved.id)
            alarmScheduler.schedule(resolved)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        repository.setAppearanceMode(mode)
    }

    fun setAccentStyle(style: AccentStyle) {
        repository.setAccentStyle(style)
    }

    fun setWidgetLayoutMode(mode: WidgetLayoutMode) {
        repository.setWidgetLayoutMode(mode)
    }

    fun loadCalendars() {
        mutableCalendars.value = runCatching { calendarSyncManager.loadWritableCalendars() }
            .getOrDefault(emptyList())
    }

    fun setSelectedCalendar(calendarId: Long?) {
        repository.setSelectedCalendar(calendarId)
    }

    fun setPaySettings(settings: PaySettings) {
        repository.setPaySettings(settings)
    }

    fun setMockPremium(enabled: Boolean) {
        repository.setMockPremium(enabled)
    }

    fun completeOnboarding() {
        repository.completeOnboarding()
    }

    fun clockIn() {
        updateTodayTimecard { entry, now -> entry.copy(clockIn = entry.clockIn ?: now) }
    }

    fun startLunch() {
        updateTodayTimecard { entry, now -> entry.copy(lunchStart = entry.lunchStart ?: now) }
    }

    fun endLunch() {
        updateTodayTimecard { entry, now -> entry.copy(lunchEnd = entry.lunchEnd ?: now) }
    }

    fun clockOut() {
        updateTodayTimecard { entry, now -> entry.copy(clockOut = entry.clockOut ?: now) }
    }

    fun saveTimecardNote(note: String) {
        updateTodayTimecard { entry, _ -> entry.copy(note = note.trim()) }
    }

    fun syncShiftsToCalendar() {
        val calendarId = state.value.selectedCalendarId
        if (calendarId == null) {
            mutableCalendarMessage.value = "Choose a calendar first."
            return
        }
        val count = runCatching {
            calendarSyncManager.syncWorkShifts(calendarId, state.value.shifts)
        }.getOrElse {
            mutableCalendarMessage.value = it.message ?: "Calendar sync failed."
            return
        }
        mutableCalendarMessage.value = "Synced $count work shifts."
    }

    fun setImportText(text: String) {
        mutableImportState.value = mutableImportState.value.copy(rawText = text, error = null, guidance = null)
    }

    fun recognizeScheduleImage(uri: Uri) {
        if (!PremiumAccess.canImportScreenshot(state.value)) {
            mutableImportState.value = currentImportState().copy(
                error = "Free screenshot imports are used for this month.",
                guidance = null,
                isReadingImage = false
            )
            return
        }
        viewModelScope.launch {
            mutableImportState.value = mutableImportState.value.copy(isReadingImage = true, error = null, guidance = null)
            val result = runCatching {
                val image = InputImage.fromFilePath(getApplication(), uri)
                val recognized = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .await()
                recognized.toReadingOrderText()
            }
            mutableImportState.value = result.fold(
                onSuccess = {
                    Log.d(TAG, "Schedule OCR completed. textLength=${it.length}")
                    ImportUiState(
                        rawText = it,
                        isReadingImage = false,
                        imageBased = true,
                        guidance = ScheduleImportGuidanceClassifier.fromRecognizedText(it)
                    )
                },
                onFailure = {
                    Log.d(TAG, "Schedule OCR failed. type=${it::class.java.simpleName}")
                    currentImportState().copy(
                        isReadingImage = false,
                        error = null,
                        guidance = scheduleImportFailureGuidance(it)
                    )
                }
            )
        }
    }

    fun cancelScheduleImport() {
        mutableImportState.value = currentImportState().copy(
            isReadingImage = false,
            error = null,
            guidance = ScheduleImportGuidance(ScheduleImportIssue.ImportCancelled)
        )
    }

    fun resetScheduleImport() {
        mutableImportState.value = ImportUiState()
    }

    fun previewImport(): ParsedSchedule {
        val rawText = currentImportState().rawText
        val parsed = ScheduleTextParser.parse(rawText)
        val changes = ScheduleChangeDetector.compare(state.value, parsed)
        mutableImportState.value = currentImportState().copy(
            parsed = parsed,
            changes = changes,
            error = null,
            guidance = ScheduleImportGuidanceClassifier.fromParsedText(rawText, parsed)
        )
        return parsed
    }

    fun applyImport(parsedOverride: ParsedSchedule? = null) {
        val parsed = parsedOverride ?: previewImport()
        val changes = ScheduleChangeDetector.compare(state.value, parsed)
        repository.importSchedule(parsed)
        if (currentImportState().imageBased) repository.recordScreenshotImport()
        mutableImportState.value = currentImportState().copy(
            parsed = parsed,
            changes = changes,
            appliedMessage = "Added or updated ${parsed.shifts.size} shifts and ${parsed.daysOff.size} days off.",
            error = null,
            guidance = null
        )
    }

    private fun currentImportState() = mutableImportState.value

    private fun scheduleImportFailureGuidance(error: Throwable): ScheduleImportGuidance {
        val message = error.message.orEmpty()
        val denied = error is SecurityException ||
            message.contains("permission", ignoreCase = true) ||
            message.contains("denied", ignoreCase = true)
        return if (denied) {
            ScheduleImportGuidance(ScheduleImportIssue.PermissionDenied)
        } else {
            ScheduleImportGuidance(ScheduleImportIssue.ImageTooBlurry, message.takeIf { it.isNotBlank() })
        }
    }

    private fun updateTodayTimecard(block: (TimecardEntry, LocalDateTime) -> TimecardEntry) {
        val today = LocalDate.now()
        val current = state.value.timecards.firstOrNull { it.date == today } ?: TimecardEntry(date = today)
        repository.upsertTimecard(block(current, LocalDateTime.now()))
    }

    private fun copyWorkImage(sourceUri: Uri): File {
        val imageDir = File(getApplication<Application>().filesDir, "work_images")
        if (!imageDir.exists()) imageDir.mkdirs()
        val output = File(imageDir, "${UUID.randomUUID()}.jpg")
        getApplication<Application>().contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Could not open selected image." }
            output.outputStream().use { input.copyTo(it) }
        }
        return output
    }

    private fun detectImageTags(text: String): List<String> {
        val lower = text.lowercase()
        val tags = linkedSetOf<String>()
        if (lower.contains("plannogram") || lower.contains("planogram")) tags += "Plannogram"
        if (lower.contains("fresh slice") || lower.contains("freshslice")) tags += "Fresh Slice"
        if (lower.contains("deli") || lower.contains("sub") || lower.contains("slicer")) tags += "Deli"
        if (lower.contains("bakery")) tags += "Bakery"
        if (lower.contains("produce")) tags += "Produce"
        if (lower.contains("meat") || lower.contains("seafood")) tags += "Meat"
        if (lower.contains("order") || lower.contains("truck")) tags += "Orders"
        return tags.take(6)
    }

    private fun Text.toReadingOrderText(): String {
        val lines = textBlocks
            .flatMap { it.lines }
            .mapNotNull { line ->
                val box = line.boundingBox ?: return@mapNotNull null
                OcrLine(
                    text = line.text.trim(),
                    left = box.left,
                    centerY = (box.top + box.bottom) / 2,
                    height = max(1, box.bottom - box.top)
                )
            }
            .filter { it.text.isNotBlank() }

        if (lines.isEmpty()) return text

        val medianHeight = lines.map { it.height }.sorted()[lines.size / 2]
        val rowThreshold = max(10, (medianHeight * 0.8).toInt())
        val rows = mutableListOf<MutableList<OcrLine>>()

        lines.sortedWith(compareBy<OcrLine> { it.centerY }.thenBy { it.left }).forEach { line ->
            val row = rows.firstOrNull { existing ->
                abs(existing.map { it.centerY }.average() - line.centerY) <= rowThreshold
            }
            if (row == null) {
                rows += mutableListOf(line)
            } else {
                row += line
            }
        }

        return rows
            .sortedBy { row -> row.minOf { it.centerY } }
            .joinToString("\n") { row ->
                row.sortedBy { it.left }.joinToString(" ") { it.text }
            }
    }

    private data class OcrLine(
        val text: String,
        val left: Int,
        val centerY: Int,
        val height: Int
    )

    private companion object {
        const val TAG = "WorkdayPlannerVM"
    }
}

private fun TrainingItem.toTrainingTask(): TaskItem {
    val today = LocalDate.now()
    val due = dueDate ?: today.plusDays(1)
    val priority = when {
        dueDate?.isBefore(today) == true -> TaskPriority.Critical
        dueDate?.let { !it.isAfter(today.plusDays(3)) } == true -> TaskPriority.High
        else -> TaskPriority.Normal
    }
    return TaskItem(
        title = "Training: $associateName - $trainingTitle",
        notes = buildString {
            append("Associate training follow-up.")
            dueDate?.let { append("\nDue: $it") }
            if (sourceText.isNotBlank()) append("\nSource: $sourceText")
        },
        category = TaskCategory.Admin,
        priority = priority,
        deadline = due.atTime(9, 0),
        alarmAt = due.atTime(8, 30)
    )
}

data class ImportUiState(
    val rawText: String = "",
    val parsed: ParsedSchedule? = null,
    val changes: ScheduleChangeSet? = null,
    val isReadingImage: Boolean = false,
    val error: String? = null,
    val guidance: ScheduleImportGuidance? = null,
    val appliedMessage: String? = null,
    val imageBased: Boolean = false
)

data class TrainingImportUiState(
    val rawText: String = "",
    val parsedItems: List<TrainingItem> = emptyList(),
    val isReadingImage: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
