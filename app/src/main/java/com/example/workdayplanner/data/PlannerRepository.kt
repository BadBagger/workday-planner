package com.example.workdayplanner.data

import android.content.Context
import com.example.workdayplanner.widget.PlannerWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PlannerRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("planner", Context.MODE_PRIVATE)
    private val mutableState = MutableStateFlow(loadState())
    val state: StateFlow<AppState> = mutableState

    fun upsertTask(task: TaskItem) = update { state ->
        state.copy(tasks = state.tasks.filterNot { it.id == task.id } + task)
    }

    fun deleteTask(taskId: String) = update { state ->
        state.copy(tasks = state.tasks.filterNot { it.id == taskId })
    }

    fun addNote(note: WorkNote) = update { state ->
        state.copy(notes = (state.notes + note).sortedWith(compareByDescending<WorkNote> { it.date }.thenByDescending { it.createdAt }))
    }

    fun deleteNote(noteId: String) = update { state ->
        state.copy(notes = state.notes.filterNot { it.id == noteId })
    }

    fun addImage(image: WorkImage) = update { state ->
        state.copy(images = (state.images + image).sortedWith(compareByDescending<WorkImage> { it.date }.thenByDescending { it.createdAt }))
    }

    fun deleteImage(imageId: String) = update { state ->
        state.images.firstOrNull { it.id == imageId }?.imagePath?.let { path ->
            runCatching { File(path).delete() }
        }
        state.copy(images = state.images.filterNot { it.id == imageId })
    }

    fun upsertEvent(event: WorkEvent) = update { state ->
        state.copy(events = state.events.filterNot { it.id == event.id } + event)
    }

    fun deleteEvent(eventId: String) = update { state ->
        state.copy(events = state.events.filterNot { it.id == eventId })
    }

    fun toggleComplete(taskId: String) = update { state ->
        state.copy(tasks = state.tasks.map { task ->
            if (task.id == taskId) task.copy(completed = !task.completed) else task
        })
    }

    fun mergeSchedule(parsed: ParsedSchedule) = update { state ->
        state.copy(
            shifts = (state.shifts + parsed.shifts).distinctBy { "${it.date}-${it.start}-${it.end}" }
                .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
            daysOff = state.daysOff + parsed.daysOff
        )
    }

    fun importSchedule(parsed: ParsedSchedule) = update { state ->
        mergeImportedSchedule(state, parsed)
    }

    fun replaceSchedule(parsed: ParsedSchedule) = update { state ->
        state.copy(
            shifts = parsed.shifts
                .distinctBy { "${it.date}-${it.start}-${it.end}" }
                .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
            daysOff = parsed.daysOff
        )
    }

    fun addDayOff(date: LocalDate) = update { state ->
        state.copy(daysOff = state.daysOff + date)
    }

    fun removeDayOff(date: LocalDate) = update { state ->
        state.copy(daysOff = state.daysOff - date)
    }

    fun clearSchedule() = update { state ->
        state.copy(shifts = emptyList(), daysOff = emptySet())
    }

    fun setDefaultDayOff(day: DayOfWeek, isOff: Boolean) = update { state ->
        state.copy(defaultDaysOff = if (isOff) state.defaultDaysOff + day else state.defaultDaysOff - day)
    }

    fun setDarkMode(enabled: Boolean) = update { state ->
        state.copy(darkMode = enabled)
    }

    fun setAccentStyle(style: AccentStyle) = update { state ->
        state.copy(accentStyle = style)
    }

    fun setWidgetLayoutMode(mode: WidgetLayoutMode) = update { state ->
        state.copy(widgetLayoutMode = mode)
    }

    fun setSelectedCalendar(calendarId: Long?) = update { state ->
        state.copy(selectedCalendarId = calendarId)
    }

    fun setPaySettings(settings: PaySettings) = update { state ->
        state.copy(paySettings = settings)
    }

    fun upsertTimecard(entry: TimecardEntry) = update { state ->
        state.copy(timecards = (state.timecards.filterNot { it.id == entry.id || it.date == entry.date } + entry).sortedByDescending { it.date })
    }

    fun addTrainingItems(items: List<TrainingItem>) = update { state ->
        state.copy(
            trainingItems = (state.trainingItems + items)
                .distinctBy { "${it.associateName.lowercase()}|${it.trainingTitle.lowercase()}|${it.dueDate}" }
                .sortedWith(compareBy<TrainingItem> { it.completedAt != null }.thenBy { it.dueDate ?: LocalDate.MAX }.thenBy { it.associateName })
        )
    }

    fun toggleTrainingComplete(trainingId: String) = update { state ->
        state.copy(trainingItems = state.trainingItems.map { item ->
            if (item.id == trainingId) {
                item.copy(completedAt = if (item.completedAt == null) LocalDateTime.now() else null)
            } else {
                item
            }
        })
    }

    fun deleteTrainingItem(trainingId: String) = update { state ->
        state.copy(trainingItems = state.trainingItems.filterNot { it.id == trainingId })
    }

    private fun update(block: (AppState) -> AppState) {
        val next = block(mutableState.value)
        mutableState.value = next
        saveState(next)
        PlannerWidgetUpdater.updateAll(appContext)
    }

    private fun loadState(): AppState {
        val root = prefs.getString("state", null)?.let(::JSONObject) ?: return AppState()
        return AppState(
            tasks = root.optJSONArray("tasks").toObjects(::taskFromJson),
            notes = root.optJSONArray("notes").toObjects(::noteFromJson),
            images = root.optJSONArray("images").toObjects(::imageFromJson),
            events = root.optJSONArray("events").toObjects(::eventFromJson),
            shifts = root.optJSONArray("shifts").toObjects(::shiftFromJson),
            daysOff = root.optJSONArray("daysOff").toStrings().map(LocalDate::parse).toSet(),
            defaultDaysOff = root.optJSONArray("defaultDaysOff").toStrings().map(DayOfWeek::valueOf).toSet(),
            darkMode = root.optBoolean("darkMode", false),
            accentStyle = runCatching {
                AccentStyle.valueOf(root.optString("accentStyle", AccentStyle.Classic.name))
            }.getOrDefault(AccentStyle.Classic),
            widgetLayoutMode = runCatching {
                WidgetLayoutMode.valueOf(root.optString("widgetLayoutMode", WidgetLayoutMode.Standard.name))
            }.getOrDefault(WidgetLayoutMode.Standard),
            selectedCalendarId = if (root.has("selectedCalendarId") && !root.isNull("selectedCalendarId")) {
                root.optLong("selectedCalendarId")
            } else {
                null
            },
            paySettings = root.optJSONObject("paySettings")?.let(::paySettingsFromJson) ?: PaySettings(),
            timecards = root.optJSONArray("timecards").toObjects(::timecardFromJson),
            trainingItems = root.optJSONArray("trainingItems").toObjects(::trainingItemFromJson)
        )
    }

    private fun saveState(state: AppState) {
        val root = JSONObject()
            .put("tasks", JSONArray(state.tasks.map(::taskToJson)))
            .put("notes", JSONArray(state.notes.map(::noteToJson)))
            .put("images", JSONArray(state.images.map(::imageToJson)))
            .put("events", JSONArray(state.events.map(::eventToJson)))
            .put("shifts", JSONArray(state.shifts.map(::shiftToJson)))
            .put("daysOff", JSONArray(state.daysOff.map(LocalDate::toString)))
            .put("defaultDaysOff", JSONArray(state.defaultDaysOff.map(DayOfWeek::name)))
            .put("darkMode", state.darkMode)
            .put("accentStyle", state.accentStyle.name)
            .put("widgetLayoutMode", state.widgetLayoutMode.name)
            .put("selectedCalendarId", state.selectedCalendarId)
            .put("paySettings", paySettingsToJson(state.paySettings))
            .put("timecards", JSONArray(state.timecards.map(::timecardToJson)))
            .put("trainingItems", JSONArray(state.trainingItems.map(::trainingItemToJson)))
        prefs.edit().putString("state", root.toString()).apply()
    }

    private fun taskToJson(task: TaskItem) = JSONObject()
        .put("id", task.id)
        .put("title", task.title)
        .put("notes", task.notes)
        .put("category", task.category.name)
        .put("priority", task.priority.name)
        .put("deadline", task.deadline?.toString())
        .put("alarmAt", task.alarmAt?.toString())
        .put("repeatRule", task.repeatRule.name)
        .put("repeatDays", JSONArray(task.repeatDays.map(DayOfWeek::name)))
        .put("skipDaysOff", task.skipDaysOff)
        .put("completed", task.completed)

    private fun taskFromJson(json: JSONObject) = TaskItem(
        id = json.getString("id"),
        title = json.getString("title"),
        notes = json.optString("notes"),
        category = runCatching {
            TaskCategory.valueOf(json.optString("category", TaskCategory.General.name))
        }.getOrDefault(TaskCategory.General),
        priority = runCatching {
            TaskPriority.valueOf(json.optString("priority", TaskPriority.Normal.name))
        }.getOrDefault(TaskPriority.Normal),
        deadline = json.optString("deadline").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        alarmAt = json.optString("alarmAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        repeatRule = runCatching { RepeatRule.valueOf(json.optString("repeatRule")) }.getOrDefault(RepeatRule.None),
        repeatDays = json.optJSONArray("repeatDays").toStrings().mapNotNull { value ->
            runCatching { DayOfWeek.valueOf(value) }.getOrNull()
        }.toSet(),
        skipDaysOff = json.optBoolean("skipDaysOff", true),
        completed = json.optBoolean("completed")
    )

    private fun noteToJson(note: WorkNote) = JSONObject()
        .put("id", note.id)
        .put("date", note.date.toString())
        .put("text", note.text)
        .put("kind", note.kind.name)
        .put("tags", JSONArray(note.tags))
        .put("createdAt", note.createdAt.toString())

    private fun noteFromJson(json: JSONObject) = WorkNote(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        text = json.getString("text"),
        kind = runCatching {
            WorkNoteKind.valueOf(json.optString("kind", WorkNoteKind.General.name))
        }.getOrDefault(WorkNoteKind.General),
        tags = json.optJSONArray("tags").toStrings(),
        createdAt = json.optString("createdAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse)
            ?: LocalDateTime.now()
    )

    private fun imageToJson(image: WorkImage) = JSONObject()
        .put("id", image.id)
        .put("date", image.date.toString())
        .put("title", image.title)
        .put("imagePath", image.imagePath)
        .put("detectedText", image.detectedText)
        .put("tags", JSONArray(image.tags))
        .put("createdAt", image.createdAt.toString())

    private fun imageFromJson(json: JSONObject) = WorkImage(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        title = json.optString("title", "Work image"),
        imagePath = json.getString("imagePath"),
        detectedText = json.optString("detectedText"),
        tags = json.optJSONArray("tags").toStrings(),
        createdAt = json.optString("createdAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse)
            ?: LocalDateTime.now()
    )

    private fun trainingItemToJson(item: TrainingItem) = JSONObject()
        .put("id", item.id)
        .put("associateName", item.associateName)
        .put("trainingTitle", item.trainingTitle)
        .put("dueDate", item.dueDate?.toString())
        .put("sourceText", item.sourceText)
        .put("importedAt", item.importedAt.toString())
        .put("completedAt", item.completedAt?.toString())

    private fun trainingItemFromJson(json: JSONObject) = TrainingItem(
        id = json.getString("id"),
        associateName = json.optString("associateName", "Unknown associate"),
        trainingTitle = json.optString("trainingTitle", "Training"),
        dueDate = json.optString("dueDate").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDate::parse),
        sourceText = json.optString("sourceText"),
        importedAt = json.optString("importedAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse)
            ?: LocalDateTime.now(),
        completedAt = json.optString("completedAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse)
    )

    private fun paySettingsToJson(settings: PaySettings) = JSONObject()
        .put("hourlyRate", settings.hourlyRate)
        .put("unpaidLunchMinutes", settings.unpaidLunchMinutes)
        .put("overtimeThresholdHours", settings.overtimeThresholdHours)
        .put("overtimeMultiplier", settings.overtimeMultiplier)

    private fun paySettingsFromJson(json: JSONObject) = PaySettings(
        hourlyRate = json.optDouble("hourlyRate", 0.0),
        unpaidLunchMinutes = json.optInt("unpaidLunchMinutes", 30).coerceAtLeast(0),
        overtimeThresholdHours = json.optDouble("overtimeThresholdHours", 40.0).coerceAtLeast(0.0),
        overtimeMultiplier = json.optDouble("overtimeMultiplier", 1.5).coerceAtLeast(1.0)
    )

    private fun timecardToJson(entry: TimecardEntry) = JSONObject()
        .put("id", entry.id)
        .put("date", entry.date.toString())
        .put("clockIn", entry.clockIn?.toString())
        .put("lunchStart", entry.lunchStart?.toString())
        .put("lunchEnd", entry.lunchEnd?.toString())
        .put("clockOut", entry.clockOut?.toString())
        .put("note", entry.note)

    private fun timecardFromJson(json: JSONObject) = TimecardEntry(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        clockIn = json.optString("clockIn").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        lunchStart = json.optString("lunchStart").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        lunchEnd = json.optString("lunchEnd").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        clockOut = json.optString("clockOut").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse),
        note = json.optString("note")
    )

    private fun eventToJson(event: WorkEvent) = JSONObject()
        .put("id", event.id)
        .put("title", event.title)
        .put("notes", event.notes)
        .put("startsAt", event.startsAt.toString())
        .put("endsAt", event.endsAt.toString())
        .put("location", event.location)

    private fun eventFromJson(json: JSONObject) = WorkEvent(
        id = json.getString("id"),
        title = json.getString("title"),
        notes = json.optString("notes"),
        startsAt = LocalDateTime.parse(json.getString("startsAt")),
        endsAt = LocalDateTime.parse(json.getString("endsAt")),
        location = json.optString("location")
    )

    private fun shiftToJson(shift: WorkShift) = JSONObject()
        .put("id", shift.id)
        .put("date", shift.date.toString())
        .put("start", shift.start.toString())
        .put("end", shift.end.toString())
        .put("label", shift.label)

    private fun shiftFromJson(json: JSONObject) = WorkShift(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        start = LocalTime.parse(json.getString("start")),
        end = LocalTime.parse(json.getString("end")),
        label = json.optString("label", "Work")
    )
}

private fun JSONArray?.toStrings(): List<String> {
    if (this == null) return emptyList()
    return List(length()) { index -> getString(index) }
}

private fun <T> JSONArray?.toObjects(mapper: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return List(length()) { index -> mapper(getJSONObject(index)) }
}

fun mergeImportedSchedule(state: AppState, parsed: ParsedSchedule): AppState {
    val importedDates = parsed.shifts.map { it.date }.toSet() + parsed.daysOff
    if (importedDates.isEmpty()) return state

    return state.copy(
        shifts = (state.shifts.filterNot { it.date in importedDates } + parsed.shifts)
            .distinctBy { "${it.date}-${it.start}-${it.end}" }
            .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
        daysOff = (state.daysOff - importedDates) + parsed.daysOff
    )
}
