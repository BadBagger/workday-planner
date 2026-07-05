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
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.TaskRecurrence
import com.example.workdayplanner.data.WorkEvent
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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

    fun saveTask(task: TaskItem) {
        repository.upsertTask(task)
        alarmScheduler.cancel(task.id)
        alarmScheduler.schedule(task)
    }

    fun deleteTask(taskId: String) {
        repository.deleteTask(taskId)
        alarmScheduler.cancel(taskId)
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
            TaskRecurrence.nextOccurrence(task, state.value)?.let(::saveTask)
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
        mutableImportState.value = currentImportState().copy(parsed = parsed, error = null)
        return parsed
    }

    fun applyImport() {
        val parsed = previewImport()
        repository.importSchedule(parsed)
        mutableImportState.value = currentImportState().copy(appliedMessage = "Added or updated ${parsed.shifts.size} shifts and ${parsed.daysOff.size} days off.")
    }

    private fun currentImportState() = mutableImportState.value

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
    val isReadingImage: Boolean = false,
    val error: String? = null,
    val appliedMessage: String? = null
)
