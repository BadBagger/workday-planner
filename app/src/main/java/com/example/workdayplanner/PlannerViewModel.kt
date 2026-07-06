package com.example.workdayplanner

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workdayplanner.alarm.AlarmScheduler
import com.example.workdayplanner.calendar.CalendarSyncManager
import com.example.workdayplanner.calendar.DeviceCalendar
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.ParsedSchedule
import com.example.workdayplanner.data.PlannerRepository
import com.example.workdayplanner.data.ScheduleTextParser
import com.example.workdayplanner.data.ScheduleChangeDetector
import com.example.workdayplanner.data.ScheduleChangeSet
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.TaskRecurrence
import com.example.workdayplanner.data.TaskCategory
import com.example.workdayplanner.data.WorkNoteOrganizer
import com.example.workdayplanner.data.WorkEvent
import com.example.workdayplanner.data.WidgetLayoutMode
import com.example.workdayplanner.data.WorkImage
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

    fun saveTask(task: TaskItem) {
        repository.upsertTask(task)
        alarmScheduler.cancel(task.id)
        alarmScheduler.schedule(task)
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
            TaskRecurrence.nextOccurrence(task, state.value)?.let(::saveTask)
        } else {
            alarmScheduler.schedule(task.copy(completed = false))
        }
    }

    fun addDayOff(date: LocalDate) {
        repository.addDayOff(date)
    }

    fun removeDayOff(date: LocalDate) {
        repository.removeDayOff(date)
    }

    fun clearSchedule() {
        repository.clearSchedule()
    }

    fun setDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
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
        mutableImportState.value = mutableImportState.value.copy(rawText = text, error = null)
    }

    fun recognizeScheduleImage(uri: Uri) {
        viewModelScope.launch {
            mutableImportState.value = mutableImportState.value.copy(isReadingImage = true, error = null)
            val result = runCatching {
                val image = InputImage.fromFilePath(getApplication(), uri)
                val recognized = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    .process(image)
                    .await()
                recognized.toReadingOrderText()
            }
            mutableImportState.value = result.fold(
                onSuccess = { ImportUiState(rawText = it, isReadingImage = false) },
                onFailure = { currentImportState().copy(isReadingImage = false, error = it.message ?: "Could not read image.") }
            )
        }
    }

    fun previewImport(): ParsedSchedule {
        val parsed = ScheduleTextParser.parse(currentImportState().rawText)
        val changes = ScheduleChangeDetector.compare(state.value, parsed)
        mutableImportState.value = currentImportState().copy(parsed = parsed, changes = changes, error = null)
        return parsed
    }

    fun applyImport() {
        val parsed = previewImport()
        repository.importSchedule(parsed)
        mutableImportState.value = currentImportState().copy(appliedMessage = "Added or updated ${parsed.shifts.size} shifts and ${parsed.daysOff.size} days off.")
    }

    private fun currentImportState() = mutableImportState.value

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
}

data class ImportUiState(
    val rawText: String = "",
    val parsed: ParsedSchedule? = null,
    val changes: ScheduleChangeSet? = null,
    val isReadingImage: Boolean = false,
    val error: String? = null,
    val appliedMessage: String? = null
)
