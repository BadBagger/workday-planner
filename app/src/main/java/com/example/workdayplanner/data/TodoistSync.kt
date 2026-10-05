package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object TodoistSync {
    private val weekdaySet = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )
    private val shiftRepeats = setOf(
        RepeatRule.OpeningShifts,
        RepeatRule.ClosingShifts,
        RepeatRule.TruckDays,
        RepeatRule.InventoryDays
    )
    private val dayNames = mapOf(
        "monday" to DayOfWeek.MONDAY,
        "mon" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY,
        "tues" to DayOfWeek.TUESDAY,
        "tue" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY,
        "wed" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY,
        "thurs" to DayOfWeek.THURSDAY,
        "thur" to DayOfWeek.THURSDAY,
        "thu" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY,
        "fri" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY,
        "sat" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY,
        "sun" to DayOfWeek.SUNDAY
    )
    private val spokenTime: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    private val utcStamp: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun localId(todoistId: String): String = "todoist-$todoistId"

    fun canMirror(task: TaskItem): Boolean = task.repeatRule !in shiftRepeats

    fun userFieldsChanged(before: TaskItem, after: TaskItem): Boolean {
        return before.title != after.title ||
            before.notes != after.notes ||
            before.deadline != after.deadline ||
            before.priority != after.priority ||
            before.repeatRule != after.repeatRule ||
            before.repeatDays != after.repeatDays ||
            before.durationMinutes != after.durationMinutes ||
            before.completed != after.completed
    }

    fun apiPriority(priority: TaskPriority): Int = when (priority) {
        TaskPriority.Critical -> 4
        TaskPriority.High -> 3
        TaskPriority.Normal, TaskPriority.Low -> 1
    }

    fun fromApiPriority(priority: Int): TaskPriority = when (priority) {
        4 -> TaskPriority.Critical
        3 -> TaskPriority.High
        else -> TaskPriority.Normal
    }

    fun isAllDay(time: LocalTime): Boolean = time.hour == 23 && time.minute >= 59

    fun mapRepeat(due: TodoistDue?): MappedRepeat {
        if (due == null || !due.isRecurring) return MappedRepeat(RepeatRule.None, emptySet(), clean = true)
        val text = normalizeRecurrence(due.string)
        if (
            Regex("""every\s+\d+""").containsMatchIn(text) ||
            text.contains("every other") ||
            text.contains("every last") ||
            text.contains("every!")
        ) {
            return MappedRepeat(RepeatRule.None, emptySet(), clean = false)
        }
        return when (text) {
            "every day", "daily", "every single day" -> MappedRepeat(RepeatRule.Daily, emptySet(), true)
            "every weekday", "every week day", "weekdays", "every workday", "every work day" ->
                MappedRepeat(RepeatRule.Weekdays, weekdaySet, true)
            "every week", "weekly" -> {
                val day = due.date?.dayOfWeek
                MappedRepeat(RepeatRule.Weekly, setOfNotNull(day), true)
            }
            else -> daysFrom(text, due.date)
        }
    }

    fun draftFor(task: TaskItem, zone: ZoneId = ZoneId.systemDefault()): TodoistTaskDraft {
        return TodoistTaskDraft(
            content = task.title.ifBlank { "Task" },
            description = task.notes,
            priority = apiPriority(task.priority),
            due = dueDraft(task, zone),
            duration = task.durationMinutes?.takeIf { it > 0 },
            durationUnit = task.durationMinutes?.takeIf { it > 0 }?.let { "minute" }
        )
    }

    fun draftFor(event: WorkEvent, zone: ZoneId = ZoneId.systemDefault()): TodoistTaskDraft {
        val minutes = Duration.between(event.startsAt, event.endsAt).toMinutes().coerceAtLeast(1)
        val wholeDays = minutes >= 24 * 60 && minutes % (24 * 60) == 0L
        return TodoistTaskDraft(
            content = event.title.ifBlank { "Event" },
            description = event.notes,
            priority = 1,
            due = TodoistDueDraft(dueDatetime = formatUtc(event.startsAt, zone)),
            duration = if (wholeDays) (minutes / (24 * 60)).toInt() else minutes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            durationUnit = if (wholeDays) "day" else "minute"
        )
    }

    fun applyRemote(remote: TodoistTask, existing: TaskItem?, zone: ZoneId = ZoneId.systemDefault()): TaskItem {
        val deadline = remote.due?.toDeadline(zone)
        val mapped = mapRepeat(remote.due)
        val repeatRule = when {
            existing?.repeatRule == RepeatRule.EveryWorkday && mapped.rule == RepeatRule.Weekdays -> RepeatRule.EveryWorkday
            else -> mapped.rule
        }
        val repeatDays = when (repeatRule) {
            RepeatRule.EveryWorkday -> existing?.repeatDays.orEmpty()
            RepeatRule.Weekly, RepeatRule.CustomDays, RepeatRule.Weekdays -> mapped.days
            else -> emptySet()
        }
        val base = existing ?: TaskItem(
            id = localId(remote.id),
            title = remote.content.ifBlank { "Todoist task" },
            reminderType = ReminderType.None,
            workRelated = false,
            skipDaysOff = false,
            timingRule = TaskTimingRule.AtTime,
            carryOverBehavior = CarryOverBehavior.KeepOverdue,
            alarmSchedulingStatus = AlarmSchedulingStatus.NoAlarmRequested
        )
        return base.copy(
            title = remote.content.ifBlank { base.title },
            notes = remote.description,
            priority = fromApiPriority(remote.priority),
            deadline = deadline,
            alarmAt = preservedAlarm(base, deadline),
            repeatRule = repeatRule,
            repeatDays = repeatDays,
            skipDaysOff = false,
            workRelated = existing?.workRelated ?: false,
            timingRule = TaskTimingRule.AtTime,
            carryOverBehavior = CarryOverBehavior.KeepOverdue,
            reminderType = existing?.reminderType ?: ReminderType.None,
            todoistId = remote.id,
            todoistProjectId = remote.projectId,
            todoistDueString = remote.due?.string,
            todoistRecurring = remote.due?.isRecurring == true,
            todoistUpdatedAt = remote.updatedAt,
            durationMinutes = remote.durationMinutes,
            completed = false,
            todoistPending = TodoistPendingAction.None,
            createdAt = existing?.createdAt ?: parseStamp(remote.addedAt, zone) ?: base.createdAt
        )
    }

    fun applyRemoteEvent(remote: TodoistTask, event: WorkEvent, zone: ZoneId = ZoneId.systemDefault()): WorkEvent {
        val start = remote.due?.toEventStart(zone) ?: event.startsAt
        val end = remote.durationMinutes
            ?.takeIf { it > 0 }
            ?.let { start.plusMinutes(it.toLong()) }
            ?.takeIf { it.isAfter(start) }
            ?: event.endsAt.takeIf { it.isAfter(start) }
            ?: start.plusHours(1)
        return event.copy(
            title = remote.content.ifBlank { event.title },
            notes = remote.description,
            startsAt = start,
            endsAt = end,
            todoistId = remote.id,
            todoistPending = TodoistPendingAction.None
        )
    }

    fun merge(
        tasks: List<TaskItem>,
        events: List<WorkEvent>,
        remote: List<TodoistTask>,
        zone: ZoneId = ZoneId.systemDefault()
    ): TodoistMergeResult {
        val remoteById = remote.filterNot { it.isDeleted || it.checked }.associateBy { it.id }
        val eventLinked = events.mapNotNull { it.todoistId }.toSet()
        val seen = mutableSetOf<String>()
        val nextTasks = mutableListOf<TaskItem>()
        tasks.forEach { task ->
            val remoteId = task.todoistId
            when {
                remoteId == null -> nextTasks += task
                task.todoistPending != TodoistPendingAction.None -> {
                    nextTasks += task
                    seen += remoteId
                }
                remoteId in eventLinked -> Unit
                remoteById[remoteId] == null -> nextTasks += task.copy(
                    completed = true,
                    todoistPending = TodoistPendingAction.None
                )
                else -> {
                    nextTasks += applyRemote(remoteById.getValue(remoteId), task, zone)
                    seen += remoteId
                }
            }
        }
        remoteById.values.forEach { item ->
            if (item.id !in eventLinked && item.id !in seen) {
                nextTasks += applyRemote(item, null, zone)
            }
        }
        val nextEvents = events.map { event ->
            val remoteItem = event.todoistId?.let { remoteById[it] }
            when {
                event.todoistPending != TodoistPendingAction.None -> event
                remoteItem != null -> applyRemoteEvent(remoteItem, event, zone)
                else -> event
            }
        }
        return TodoistMergeResult(nextTasks, nextEvents)
    }

    fun projectSummary(projects: List<TodoistProject>, openTaskCount: Int): String {
        val includesFlorence = projects.any { it.name.equals("Florence trip", ignoreCase = true) }
        val scope = if (includesFlorence) {
            "every Todoist project, including Florence trip"
        } else {
            "every Todoist project"
        }
        val taskLabel = if (openTaskCount == 1) "open task" else "open tasks"
        return "Synced $openTaskCount $taskLabel from $scope."
    }

    private fun dueDraft(task: TaskItem, zone: ZoneId): TodoistDueDraft {
        val time = task.deadline?.toLocalTime()
        val allDay = time == null || isAllDay(time)
        val timeSuffix = if (!allDay && time != null) " at ${time.format(spokenTime)}" else ""
        when (task.repeatRule) {
            RepeatRule.Daily -> return TodoistDueDraft(dueString = "every day$timeSuffix")
            RepeatRule.Weekdays, RepeatRule.EveryWorkday -> return TodoistDueDraft(dueString = "every weekday$timeSuffix")
            RepeatRule.Weekly -> {
                val day = task.repeatDays.firstOrNull() ?: task.deadline?.dayOfWeek
                val base = if (day != null) "every ${day.name.lowercase(Locale.US)}" else "every week"
                return TodoistDueDraft(dueString = base + timeSuffix)
            }
            RepeatRule.CustomDays -> {
                if (task.repeatDays.isNotEmpty()) {
                    val names = task.repeatDays.sortedBy { it.value }.joinToString(", ") { it.name.lowercase(Locale.US) }
                    return TodoistDueDraft(dueString = "every $names$timeSuffix")
                }
            }
            else -> Unit
        }
        if (task.todoistRecurring && !task.todoistDueString.isNullOrBlank() && task.repeatRule == RepeatRule.None) {
            return TodoistDueDraft(dueString = task.todoistDueString)
        }
        val deadline = task.deadline ?: return TodoistDueDraft()
        if (allDay) return TodoistDueDraft(dueDate = deadline.toLocalDate().toString())
        return TodoistDueDraft(dueDatetime = formatUtc(deadline, zone))
    }

    private fun preservedAlarm(existing: TaskItem, newDeadline: LocalDateTime?): LocalDateTime? {
        if (existing.reminderType == ReminderType.None) return null
        val alarmAt = existing.alarmAt ?: return null
        val oldDeadline = existing.deadline ?: return alarmAt
        if (newDeadline == null) return alarmAt
        return newDeadline.minus(Duration.between(alarmAt, oldDeadline))
    }

    private fun normalizeRecurrence(raw: String): String {
        return raw.lowercase(Locale.US)
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s+at\\s+\\d.*$"), "")
            .replace("&", " and ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun daysFrom(text: String, dueDate: LocalDate?): MappedRepeat {
        if (!text.startsWith("every ")) return MappedRepeat(RepeatRule.None, emptySet(), clean = false)
        val tokens = text.removePrefix("every ").split(Regex("[^a-z]+")).filter { it.isNotBlank() }
        val days = tokens.mapNotNull { dayNames[it] }.toSet()
        return when (days.size) {
            0 -> MappedRepeat(RepeatRule.None, emptySet(), clean = false)
            1 -> MappedRepeat(RepeatRule.Weekly, days, true)
            else -> MappedRepeat(RepeatRule.CustomDays, days, true)
        }.let { mapped ->
            if (mapped.rule == RepeatRule.Weekly && mapped.days.isEmpty() && dueDate != null) {
                mapped.copy(days = setOf(dueDate.dayOfWeek))
            } else {
                mapped
            }
        }
    }

    private fun formatUtc(time: LocalDateTime, zone: ZoneId): String {
        return time.atZone(zone).withZoneSameInstant(ZoneOffset.UTC).format(utcStamp)
    }

    private fun parseStamp(value: String?, zone: ZoneId): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(zone).toLocalDateTime() }
            .getOrElse { runCatching { LocalDateTime.parse(value) }.getOrNull() }
    }
}

fun TodoistDue.toDeadline(zone: ZoneId): LocalDateTime? {
    dateTimeUtc?.let { utc ->
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime()
    }
    return date?.atTime(23, 59)
}

fun TodoistDue.toEventStart(zone: ZoneId): LocalDateTime? {
    dateTimeUtc?.let { utc ->
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(zone).toLocalDateTime()
    }
    return date?.atTime(9, 0)
}
