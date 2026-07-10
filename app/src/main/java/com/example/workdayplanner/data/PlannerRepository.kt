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

    fun addDayOff(date: LocalDate, kind: ShiftTemplateKind = ShiftTemplateKind.DayOff) = update { state ->
        state.copy(
            daysOff = state.daysOff + date,
            dayOffTypes = state.dayOffTypes + (date to kind)
        )
    }

    fun upsertShift(shift: WorkShift) = update { state ->
        state.copy(
            shifts = (state.shifts.filterNot { it.id == shift.id } + shift)
                .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
            daysOff = state.daysOff - shift.date,
            dayOffTypes = state.dayOffTypes - shift.date
        )
    }

    fun deleteShift(shiftId: String) = update { state ->
        removeShiftAndUnlinkTasks(state, shiftId)
    }

    fun upsertShiftPattern(pattern: ShiftPattern) = update { state ->
        state.copy(shiftPatterns = (state.shiftPatterns.filterNot { it.id == pattern.id } + pattern).sortedBy { it.startDate })
    }

    fun setShiftPatternEnabled(patternId: String, enabled: Boolean) = update { state ->
        state.copy(shiftPatterns = state.shiftPatterns.map { if (it.id == patternId) it.copy(enabled = enabled) else it })
    }

    fun deleteShiftPattern(patternId: String) = update { state ->
        state.copy(shiftPatterns = state.shiftPatterns.filterNot { it.id == patternId })
    }

    fun applyShiftPattern(pattern: ShiftPattern, allowDuplicates: Boolean) = update { state ->
        val preview = ShiftPatternGenerator.preview(pattern)
        val existingKeys = state.shifts.map { it.shiftCollisionKey() }.toSet()
        val generated = if (allowDuplicates) preview.shifts else preview.shifts.filterNot { it.shiftCollisionKey() in existingKeys }
        state.copy(
            shiftPatterns = (state.shiftPatterns.filterNot { it.id == pattern.id } + pattern).sortedBy { it.startDate },
            shifts = (state.shifts + generated).sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
            daysOff = state.daysOff + preview.daysOff,
            dayOffTypes = state.dayOffTypes + preview.daysOff.associateWith { ShiftTemplateKind.DayOff }
        )
    }

    fun upsertShiftTemplate(template: ShiftTemplate) = update { state ->
        state.copy(shiftTemplates = (state.shiftTemplates.filterNot { it.id == template.id } + template.copy(builtIn = false)).sortedBy { it.name.lowercase() })
    }

    fun deleteShiftTemplate(templateId: String) = update { state ->
        state.copy(shiftTemplates = state.shiftTemplates.filterNot { it.id == templateId })
    }

    fun upsertTaskTemplate(template: TaskTemplate) = update { state ->
        state.copy(taskTemplates = (state.taskTemplates.filterNot { it.id == template.id } + template.copy(builtIn = false)).sortedBy { it.name.lowercase() })
    }

    fun deleteTaskTemplate(templateId: String) = update { state ->
        state.copy(taskTemplates = state.taskTemplates.filterNot { it.id == templateId })
    }

    fun removeDayOff(date: LocalDate) = update { state ->
        state.copy(daysOff = state.daysOff - date, dayOffTypes = state.dayOffTypes - date)
    }

    fun clearSchedule() = update { state ->
        state.copy(shifts = emptyList(), daysOff = emptySet(), dayOffTypes = emptyMap())
    }

    fun setDefaultDayOff(day: DayOfWeek, isOff: Boolean) = update { state ->
        state.copy(defaultDaysOff = if (isOff) state.defaultDaysOff + day else state.defaultDaysOff - day)
    }

    fun setDarkMode(enabled: Boolean) = update { state ->
        state.copy(appearanceMode = if (enabled) AppearanceMode.Dark else AppearanceMode.Light, darkMode = enabled)
    }

    fun setAppearanceMode(mode: AppearanceMode) = update { state ->
        state.copy(appearanceMode = mode, darkMode = mode == AppearanceMode.Dark)
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

    fun setShiftAlarmSettings(settings: ShiftAlarmSettings) = update { state ->
        state.copy(shiftAlarmSettings = settings)
    }

    fun setMockPremium(enabled: Boolean) = update { state ->
        state.copy(premium = state.premium.copy(mockPremiumEnabled = enabled, isPremium = false))
    }

    fun completeOnboarding() = update { state ->
        state.copy(onboardingCompleted = true)
    }

    fun recordScreenshotImport(month: String = java.time.YearMonth.now().toString()) = update { state ->
        val currentCount = if (state.premium.importMonth == month) state.premium.screenshotImportsThisMonth else 0
        state.copy(
            premium = state.premium.copy(
                importMonth = month,
                screenshotImportsThisMonth = currentCount + 1
            )
        )
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
        val legacyDarkMode = root.optBoolean("darkMode", false)
        return AppState(
            tasks = root.optJSONArray("tasks").toObjects(::taskFromJson),
            notes = root.optJSONArray("notes").toObjects(::noteFromJson),
            images = root.optJSONArray("images").toObjects(::imageFromJson),
            events = root.optJSONArray("events").toObjects(::eventFromJson),
            shifts = root.optJSONArray("shifts").toObjects(::shiftFromJson),
            daysOff = root.optJSONArray("daysOff").toStrings().map(LocalDate::parse).toSet(),
            dayOffTypes = root.optJSONObject("dayOffTypes").toDayOffTypes(),
            defaultDaysOff = root.optJSONArray("defaultDaysOff").toStrings().map(DayOfWeek::valueOf).toSet(),
            appearanceMode = AppearanceMode.fromStored(
                if (root.has("appearanceMode") && !root.isNull("appearanceMode")) root.optString("appearanceMode") else null,
                legacyDarkMode
            ),
            darkMode = legacyDarkMode,
            accentStyle = AccentStyle.fromStored(root.optString("accentStyle", AccentStyle.Default.name)),
            widgetLayoutMode = runCatching {
                WidgetLayoutMode.valueOf(root.optString("widgetLayoutMode", WidgetLayoutMode.Standard.name))
            }.getOrDefault(WidgetLayoutMode.Standard),
            selectedCalendarId = if (root.has("selectedCalendarId") && !root.isNull("selectedCalendarId")) {
                root.optLong("selectedCalendarId")
            } else {
                null
            },
            paySettings = root.optJSONObject("paySettings")?.let(::paySettingsFromJson) ?: PaySettings(),
            shiftAlarmSettings = root.optJSONObject("shiftAlarmSettings")?.let(::shiftAlarmSettingsFromJson) ?: ShiftAlarmSettings(),
            timecards = root.optJSONArray("timecards").toObjects(::timecardFromJson),
            trainingItems = root.optJSONArray("trainingItems").toObjects(::trainingItemFromJson),
            shiftTemplates = root.optJSONArray("shiftTemplates").toObjects(::shiftTemplateFromJson),
            taskTemplates = root.optJSONArray("taskTemplates").toObjects(::taskTemplateFromJson),
            shiftPatterns = root.optJSONArray("shiftPatterns").toObjects(::shiftPatternFromJson),
            premium = root.optJSONObject("premium")?.let(::premiumFromJson) ?: PremiumEntitlement(),
            onboardingCompleted = root.optBoolean("onboardingCompleted", false)
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
            .put("dayOffTypes", JSONObject().apply {
                state.dayOffTypes.forEach { (date, kind) -> put(date.toString(), kind.name) }
            })
            .put("defaultDaysOff", JSONArray(state.defaultDaysOff.map(DayOfWeek::name)))
            .put("appearanceMode", state.appearanceMode.name)
            .put("darkMode", state.darkMode)
            .put("accentStyle", state.accentStyle.name)
            .put("widgetLayoutMode", state.widgetLayoutMode.name)
            .put("selectedCalendarId", state.selectedCalendarId)
            .put("paySettings", paySettingsToJson(state.paySettings))
            .put("shiftAlarmSettings", shiftAlarmSettingsToJson(state.shiftAlarmSettings))
            .put("timecards", JSONArray(state.timecards.map(::timecardToJson)))
            .put("trainingItems", JSONArray(state.trainingItems.map(::trainingItemToJson)))
            .put("shiftTemplates", JSONArray(state.shiftTemplates.map(::shiftTemplateToJson)))
            .put("taskTemplates", JSONArray(state.taskTemplates.map(::taskTemplateToJson)))
            .put("shiftPatterns", JSONArray(state.shiftPatterns.map(::shiftPatternToJson)))
            .put("premium", premiumToJson(state.premium))
            .put("onboardingCompleted", state.onboardingCompleted)
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
        .put("workRelated", task.workRelated)
        .put("linkedShiftId", task.linkedShiftId)
        .put("linkedShiftType", task.linkedShiftType.name)
        .put("timingRule", task.timingRule.name)
        .put("carryOverBehavior", task.carryOverBehavior.name)
        .put("alarmOffsetMinutes", task.alarmOffsetMinutes)
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
        workRelated = json.optBoolean("workRelated", true),
        linkedShiftId = json.optString("linkedShiftId").takeIf { it.isNotBlank() && it != "null" },
        linkedShiftType = runCatching { LinkedShiftType.valueOf(json.optString("linkedShiftType", LinkedShiftType.Any.name)) }.getOrDefault(LinkedShiftType.Any),
        timingRule = runCatching { TaskTimingRule.valueOf(json.optString("timingRule", TaskTimingRule.AtTime.name)) }.getOrDefault(TaskTimingRule.AtTime),
        carryOverBehavior = runCatching { CarryOverBehavior.valueOf(json.optString("carryOverBehavior", CarryOverBehavior.None.name)) }.getOrDefault(CarryOverBehavior.None),
        alarmOffsetMinutes = json.optLong("alarmOffsetMinutes", 30).coerceAtLeast(0),
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
        .put("deductUnpaidBreaks", settings.deductUnpaidBreaks)
        .put("unpaidLunchMinutes", settings.unpaidLunchMinutes)
        .put("overtimeThresholdHours", settings.overtimeThresholdHours)
        .put("overtimeMultiplier", settings.overtimeMultiplier)
        .put("dailyOvertimeThresholdHours", settings.dailyOvertimeThresholdHours)
        .put("nightShiftExtraAmount", settings.nightShiftExtraAmount)
        .put("weekendExtraAmount", settings.weekendExtraAmount)
        .put("customShiftTypeLabel", settings.customShiftTypeLabel)
        .put("customShiftTypeExtraAmount", settings.customShiftTypeExtraAmount)
        .put("payPeriodType", settings.payPeriodType.name)
        .put("customPayPeriodStart", settings.customPayPeriodStart.toString())
        .put("showPayOnDashboard", settings.showPayOnDashboard)
        .put("estimatedTaxRate", settings.estimatedTaxRate)
        .put("estimatedDeductionRate", settings.estimatedDeductionRate)

    private fun paySettingsFromJson(json: JSONObject) = PaySettings(
        hourlyRate = json.optDouble("hourlyRate", 0.0),
        deductUnpaidBreaks = json.optBoolean("deductUnpaidBreaks", true),
        unpaidLunchMinutes = json.optInt("unpaidLunchMinutes", 30).coerceAtLeast(0),
        overtimeThresholdHours = json.optDouble("overtimeThresholdHours", 40.0).coerceAtLeast(0.0),
        overtimeMultiplier = json.optDouble("overtimeMultiplier", 1.5).coerceAtLeast(1.0),
        dailyOvertimeThresholdHours = json.optDouble("dailyOvertimeThresholdHours", 0.0).coerceAtLeast(0.0),
        nightShiftExtraAmount = json.optDouble("nightShiftExtraAmount", 0.0).coerceAtLeast(0.0),
        weekendExtraAmount = json.optDouble("weekendExtraAmount", 0.0).coerceAtLeast(0.0),
        customShiftTypeLabel = json.optString("customShiftTypeLabel"),
        customShiftTypeExtraAmount = json.optDouble("customShiftTypeExtraAmount", 0.0).coerceAtLeast(0.0),
        payPeriodType = runCatching { PayPeriodType.valueOf(json.optString("payPeriodType", PayPeriodType.Weekly.name)) }.getOrDefault(PayPeriodType.Weekly),
        customPayPeriodStart = json.optString("customPayPeriodStart").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDate::parse) ?: LocalDate.now(),
        showPayOnDashboard = json.optBoolean("showPayOnDashboard", false),
        estimatedTaxRate = json.optDouble("estimatedTaxRate", 18.0).coerceIn(0.0, 100.0),
        estimatedDeductionRate = json.optDouble("estimatedDeductionRate", 5.0).coerceIn(0.0, 100.0)
    )

    private fun shiftAlarmSettingsToJson(settings: ShiftAlarmSettings) = JSONObject()
        .put("enabled", settings.enabled)
        .put("offsetMinutes", settings.offsetMinutes)
        .put("onlyEarlyShifts", settings.onlyEarlyShifts)
        .put("earlyShiftCutoffHour", settings.earlyShiftCutoffHour)

    private fun shiftAlarmSettingsFromJson(json: JSONObject) = ShiftAlarmSettings(
        enabled = json.optBoolean("enabled", false),
        offsetMinutes = json.optInt("offsetMinutes", 80).coerceIn(0, 24 * 60),
        onlyEarlyShifts = json.optBoolean("onlyEarlyShifts", false),
        earlyShiftCutoffHour = json.optInt("earlyShiftCutoffHour", 9).coerceIn(0, 23)
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
        .put("location", shift.location)
        .put("notes", shift.notes)
        .put("patternId", shift.patternId)

    private fun shiftFromJson(json: JSONObject) = WorkShift(
        id = json.getString("id"),
        date = LocalDate.parse(json.getString("date")),
        start = LocalTime.parse(json.getString("start")),
        end = LocalTime.parse(json.getString("end")),
        label = json.optString("label", "Work"),
        location = json.optString("location"),
        notes = json.optString("notes"),
        patternId = json.optString("patternId").takeIf { it.isNotBlank() && it != "null" }
    )

    private fun shiftTemplateToJson(template: ShiftTemplate) = JSONObject()
        .put("id", template.id)
        .put("name", template.name)
        .put("label", template.label)
        .put("start", template.start.toString())
        .put("end", template.end.toString())
        .put("location", template.location)
        .put("notes", template.notes)
        .put("kind", template.kind.name)

    private fun shiftTemplateFromJson(json: JSONObject) = ShiftTemplate(
        id = json.getString("id"),
        name = json.optString("name", "Custom shift template"),
        label = json.optString("label", "Work"),
        start = json.optString("start").takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime.of(9, 0),
        end = json.optString("end").takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime.of(17, 0),
        location = json.optString("location"),
        notes = json.optString("notes"),
        kind = runCatching { ShiftTemplateKind.valueOf(json.optString("kind", ShiftTemplateKind.Work.name)) }.getOrDefault(ShiftTemplateKind.Work)
    )

    private fun taskTemplateToJson(template: TaskTemplate) = JSONObject()
        .put("id", template.id)
        .put("name", template.name)
        .put("title", template.title)
        .put("notes", template.notes)
        .put("category", template.category.name)
        .put("priority", template.priority.name)
        .put("repeatRule", template.repeatRule.name)
        .put("reminderEnabled", template.reminderEnabled)
        .put("workRelated", template.workRelated)
        .put("linkedShiftType", template.linkedShiftType.name)
        .put("timingRule", template.timingRule.name)
        .put("carryOverBehavior", template.carryOverBehavior.name)
        .put("alarmOffsetMinutes", template.alarmOffsetMinutes)

    private fun taskTemplateFromJson(json: JSONObject) = TaskTemplate(
        id = json.getString("id"),
        name = json.optString("name", "Custom task template"),
        title = json.optString("title", json.optString("name", "Task")),
        notes = json.optString("notes"),
        category = runCatching { TaskCategory.valueOf(json.optString("category", TaskCategory.General.name)) }.getOrDefault(TaskCategory.General),
        priority = runCatching { TaskPriority.valueOf(json.optString("priority", TaskPriority.Normal.name)) }.getOrDefault(TaskPriority.Normal),
        repeatRule = runCatching { RepeatRule.valueOf(json.optString("repeatRule", RepeatRule.None.name)) }.getOrDefault(RepeatRule.None),
        reminderEnabled = json.optBoolean("reminderEnabled", false),
        workRelated = json.optBoolean("workRelated", true),
        linkedShiftType = runCatching { LinkedShiftType.valueOf(json.optString("linkedShiftType", LinkedShiftType.Any.name)) }.getOrDefault(LinkedShiftType.Any),
        timingRule = runCatching { TaskTimingRule.valueOf(json.optString("timingRule", TaskTimingRule.AtTime.name)) }.getOrDefault(TaskTimingRule.AtTime),
        carryOverBehavior = runCatching { CarryOverBehavior.valueOf(json.optString("carryOverBehavior", CarryOverBehavior.None.name)) }.getOrDefault(CarryOverBehavior.None),
        alarmOffsetMinutes = json.optLong("alarmOffsetMinutes", 30).coerceAtLeast(0)
    )

    private fun shiftPatternToJson(pattern: ShiftPattern) = JSONObject()
        .put("id", pattern.id)
        .put("name", pattern.name)
        .put("startDate", pattern.startDate.toString())
        .put("cycleLength", pattern.cycleLength)
        .put("days", JSONArray(pattern.days.map(::shiftPatternDayToJson)))
        .put("endDate", pattern.endDate?.toString())
        .put("enabled", pattern.enabled)
        .put("createdAt", pattern.createdAt.toString())

    private fun shiftPatternFromJson(json: JSONObject) = ShiftPattern(
        id = json.getString("id"),
        name = json.optString("name", "Shift pattern"),
        startDate = json.optString("startDate").takeIf { it.isNotBlank() }?.let(LocalDate::parse) ?: LocalDate.now(),
        cycleLength = json.optInt("cycleLength", 7).coerceIn(1, 60),
        days = json.optJSONArray("days").toObjects(::shiftPatternDayFromJson),
        endDate = json.optString("endDate").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDate::parse),
        enabled = json.optBoolean("enabled", true),
        createdAt = json.optString("createdAt").takeIf { it.isNotBlank() && it != "null" }?.let(LocalDateTime::parse)
            ?: LocalDateTime.now()
    )

    private fun shiftPatternDayToJson(day: ShiftPatternDay) = JSONObject()
        .put("index", day.index)
        .put("kind", day.kind.name)
        .put("label", day.label)
        .put("start", day.start.toString())
        .put("end", day.end.toString())
        .put("location", day.location)
        .put("notes", day.notes)

    private fun shiftPatternDayFromJson(json: JSONObject) = ShiftPatternDay(
        index = json.optInt("index"),
        kind = runCatching { ShiftPatternDayKind.valueOf(json.optString("kind", ShiftPatternDayKind.Off.name)) }.getOrDefault(ShiftPatternDayKind.Off),
        label = json.optString("label", "Work"),
        start = json.optString("start").takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime.of(9, 0),
        end = json.optString("end").takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime.of(17, 0),
        location = json.optString("location"),
        notes = json.optString("notes")
    )

    private fun premiumToJson(entitlement: PremiumEntitlement) = JSONObject()
        .put("isPremium", entitlement.isPremium)
        .put("mockPremiumEnabled", entitlement.mockPremiumEnabled)
        .put("importMonth", entitlement.importMonth)
        .put("screenshotImportsThisMonth", entitlement.screenshotImportsThisMonth)

    private fun premiumFromJson(json: JSONObject) = PremiumEntitlement(
        isPremium = json.optBoolean("isPremium", false),
        mockPremiumEnabled = json.optBoolean("mockPremiumEnabled", false),
        importMonth = json.optString("importMonth"),
        screenshotImportsThisMonth = json.optInt("screenshotImportsThisMonth", 0).coerceAtLeast(0)
    )
}

private fun WorkShift.shiftCollisionKey(): String = "${date}|${start}|${end}"

private fun JSONArray?.toStrings(): List<String> {
    if (this == null) return emptyList()
    return List(length()) { index -> getString(index) }
}

private fun <T> JSONArray?.toObjects(mapper: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return List(length()) { index -> mapper(getJSONObject(index)) }
}

private fun JSONObject?.toDayOffTypes(): Map<LocalDate, ShiftTemplateKind> {
    if (this == null) return emptyMap()
    return keys().asSequence().mapNotNull { key ->
        val date = runCatching { LocalDate.parse(key) }.getOrNull() ?: return@mapNotNull null
        val kind = runCatching { ShiftTemplateKind.valueOf(optString(key, ShiftTemplateKind.DayOff.name)) }
            .getOrDefault(ShiftTemplateKind.DayOff)
        date to kind
    }.toMap()
}

fun mergeImportedSchedule(state: AppState, parsed: ParsedSchedule): AppState {
    val importedDates = parsed.shifts.map { it.date }.toSet() + parsed.daysOff
    if (importedDates.isEmpty()) return state

    return state.copy(
        shifts = (state.shifts.filterNot { it.date in importedDates } + parsed.shifts)
            .distinctBy { "${it.date}-${it.start}-${it.end}" }
            .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
        daysOff = (state.daysOff - importedDates) + parsed.daysOff,
        dayOffTypes = (state.dayOffTypes - importedDates) + parsed.daysOff.associateWith { ShiftTemplateKind.DayOff }
    )
}

fun removeShiftAndUnlinkTasks(state: AppState, shiftId: String): AppState {
    return state.copy(
        shifts = state.shifts.filterNot { it.id == shiftId },
        tasks = state.tasks.map { task ->
            if (task.linkedShiftId == shiftId) {
                task.copy(linkedShiftId = null, alarmAt = null)
            } else {
                task
            }
        }
    )
}
