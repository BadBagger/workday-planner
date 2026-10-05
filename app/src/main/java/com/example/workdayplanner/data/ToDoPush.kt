package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.util.UUID

enum class ToDoKind {
    Task,
    Todo,
    Work,
    Event,
    Goal,
    Schedule,
    File,
    Standards
}

enum class ToDoAction {
    Create,
    Update,
    Complete,
    Delete
}

data class ToDoCommand(
    val action: ToDoAction = ToDoAction.Create,
    val kind: ToDoKind,
    val id: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val due: LocalDateTime? = null,
    val end: LocalDateTime? = null,
    val repeat: RepeatRule? = null,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val completed: Boolean? = null,
    val priority: TaskPriority? = null,
    val category: TaskCategory? = null,
    val location: String? = null,
    val workRelated: Boolean? = null,
    val focus: WorkGoalFocus? = null,
    val target: String? = null,
    val dailyRequirements: List<String> = emptyList(),
    val text: String? = null,
    val tags: List<String> = emptyList(),
    val mimeType: String? = null,
    val storedPath: String? = null,
    val detectedText: String? = null,
    val scheduleYear: Int = Year.now().value
)

data class ToDoResult(
    val state: AppState,
    val id: String,
    val message: String
)

object ToDoCommands {
    fun fromFields(fields: Map<String, String?>): ToDoCommand {
        val kind = kindOf(fields.value("kind"))
        val action = actionOf(fields.value("action"))
        return ToDoCommand(
            action = action,
            kind = kind,
            id = fields.value("id"),
            title = fields.value("title"),
            notes = fields.value("notes"),
            due = fields.value("due")?.let(::parseDateTime),
            end = fields.value("end")?.let(::parseDateTime),
            repeat = fields.value("repeat")?.let(::repeatOf),
            repeatDays = fields.value("repeat_days").orEmpty()
                .split(',', ' ')
                .mapNotNull { token -> token.takeIf { it.isNotBlank() }?.let(::dayOf) }
                .toSet(),
            completed = fields.value("completed")?.let { it.equals("true", true) || it == "1" },
            priority = fields.value("priority")?.let { raw ->
                TaskPriority.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            },
            category = fields.value("category")?.let { raw ->
                TaskCategory.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            },
            location = fields.value("location"),
            workRelated = fields.value("work_related")?.let { it.equals("true", true) || it == "1" },
            focus = fields.value("focus")?.let(WorkGoalFocus::fromStored),
            target = fields.value("target"),
            dailyRequirements = fields.value("daily_requirements").orEmpty()
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList(),
            text = fields.value("text"),
            tags = fields.value("tags").orEmpty().split(',').map { it.trim() }.filter { it.isNotEmpty() },
            mimeType = fields.value("mime"),
            storedPath = fields.value("path"),
            detectedText = fields.value("detected_text")
        )
    }

    private fun Map<String, String?>.value(key: String): String? {
        return this[key]?.trim()?.takeIf { it.isNotEmpty() && it != "null" }
    }

    private fun kindOf(raw: String?): ToDoKind {
        return when (raw?.lowercase()?.replace('-', '_')?.replace(' ', '_')) {
            "todo", "to_do" -> ToDoKind.Todo
            "work", "work_item" -> ToDoKind.Work
            "event", "recurring_event" -> ToDoKind.Event
            "goal" -> ToDoKind.Goal
            "schedule" -> ToDoKind.Schedule
            "file", "photo", "document", "image" -> ToDoKind.File
            "standards", "deli", "deli_standards" -> ToDoKind.Standards
            "task", null -> ToDoKind.Task
            else -> throw IllegalArgumentException("Unknown kind \"$raw\". Use task, todo, work, event, goal, schedule, file, or standards.")
        }
    }

    private fun actionOf(raw: String?): ToDoAction {
        return when (raw?.lowercase()) {
            "update", "edit" -> ToDoAction.Update
            "complete", "done" -> ToDoAction.Complete
            "delete", "remove" -> ToDoAction.Delete
            "create", "add", "import", null -> ToDoAction.Create
            else -> throw IllegalArgumentException("Unknown action \"$raw\". Use create, update, complete, or delete.")
        }
    }

    private fun repeatOf(raw: String): RepeatRule {
        return when (raw.lowercase().replace('-', '_').replace(' ', '_')) {
            "none", "once" -> RepeatRule.None
            "daily", "every_day" -> RepeatRule.Daily
            "weekdays", "every_weekday" -> RepeatRule.Weekdays
            "weekly", "every_week" -> RepeatRule.Weekly
            "custom", "custom_days" -> RepeatRule.CustomDays
            "every_workday", "workday" -> RepeatRule.EveryWorkday
            "opening" -> RepeatRule.OpeningShifts
            "closing" -> RepeatRule.ClosingShifts
            "truck" -> RepeatRule.TruckDays
            "inventory" -> RepeatRule.InventoryDays
            else -> runCatching { RepeatRule.valueOf(raw) }.getOrElse {
                throw IllegalArgumentException("Unknown repeat \"$raw\".")
            }
        }
    }

    private fun dayOf(raw: String): DayOfWeek {
        return when (raw.trim().lowercase().take(3)) {
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            "sun" -> DayOfWeek.SUNDAY
            else -> throw IllegalArgumentException("Unknown day \"$raw\".")
        }
    }

    private fun parseDateTime(raw: String): LocalDateTime {
        val text = raw.trim()
        runCatching { return LocalDateTime.parse(text) }
        runCatching { return LocalDate.parse(text).atTime(9, 0) }
        throw IllegalArgumentException("Could not read the date \"$raw\". Use a local date or date-time such as 2026-10-05T06:00.")
    }
}

object ToDoPush {
    fun apply(
        state: AppState,
        command: ToDoCommand,
        today: LocalDate = LocalDate.now(),
        fields: Map<String, String?> = emptyMap()
    ): ToDoResult {
        return when (command.kind) {
            ToDoKind.Task, ToDoKind.Todo, ToDoKind.Work -> applyTask(state, command, today)
            ToDoKind.Event -> applyEvent(state, command)
            ToDoKind.Goal -> applyGoal(state, command, today)
            ToDoKind.Schedule -> applySchedule(state, command)
            ToDoKind.File -> applyFile(state, command, today)
            ToDoKind.Standards -> {
                val book = DeliStandardsPush.apply(state.deliStandards, fields)
                ToDoResult(state.copy(deliStandards = book), command.id ?: "deli-standards", "Updated Deli Daily Standards.")
            }
        }
    }

    private fun applyTask(state: AppState, command: ToDoCommand, today: LocalDate): ToDoResult {
        val id = command.id ?: if (command.action == ToDoAction.Create) UUID.randomUUID().toString() else missingId()
        return when (command.action) {
            ToDoAction.Delete -> ToDoResult(
                state.copy(tasks = state.tasks.filterNot { it.id == id }),
                id,
                "Deleted the task."
            )
            ToDoAction.Complete -> completeTask(state, id)
            ToDoAction.Create, ToDoAction.Update -> {
                val existing = state.tasks.firstOrNull { it.id == id }
                if (command.action == ToDoAction.Update && existing == null) {
                    throw IllegalArgumentException("No task with id $id.")
                }
                val title = command.title ?: existing?.title ?: throw IllegalArgumentException("A task needs a title.")
                val workRelated = command.workRelated ?: when (command.kind) {
                    ToDoKind.Todo -> false
                    ToDoKind.Work -> true
                    else -> existing?.workRelated ?: true
                }
                val base = existing ?: TaskItem(
                    id = id,
                    title = title,
                    reminderType = ReminderType.None,
                    deadline = command.due ?: today.atTime(LocalTime.of(9, 0)).takeIf { command.repeat == RepeatRule.Daily }
                )
                val task = base.copy(
                    title = title,
                    notes = command.notes ?: base.notes,
                    category = command.category ?: base.category,
                    priority = command.priority ?: base.priority,
                    deadline = command.due ?: base.deadline,
                    repeatRule = command.repeat ?: base.repeatRule,
                    repeatDays = command.repeatDays.ifEmpty { base.repeatDays },
                    workRelated = workRelated,
                    completed = command.completed ?: base.completed
                )
                ToDoResult(
                    state.copy(tasks = state.tasks.filterNot { it.id == id } + task),
                    id,
                    "Saved ${task.title}."
                )
            }
        }
    }

    private fun completeTask(state: AppState, id: String): ToDoResult {
        val task = state.tasks.firstOrNull { it.id == id } ?: throw IllegalArgumentException("No task with id $id.")
        if (task.completed) return ToDoResult(state, id, "${task.title} is already done.")
        val done = task.copy(
            completed = true,
            completionHistory = task.completionHistory + LocalDateTime.now()
        )
        val next = TaskRecurrence.nextOccurrence(done, state)
        val tasks = state.tasks.filterNot { it.id == id } + done + listOfNotNull(next).filter { candidate ->
            state.tasks.none { it.title == candidate.title && it.deadline == candidate.deadline }
        }
        return ToDoResult(state.copy(tasks = tasks), id, "Completed ${task.title}.")
    }

    private fun applyEvent(state: AppState, command: ToDoCommand): ToDoResult {
        val id = command.id ?: if (command.action == ToDoAction.Create) UUID.randomUUID().toString() else missingId()
        return when (command.action) {
            ToDoAction.Delete -> ToDoResult(
                state.copy(events = state.events.filterNot { it.id == id }),
                id,
                "Deleted the event."
            )
            ToDoAction.Complete -> throw IllegalArgumentException("Events are removed with delete, not completed.")
            ToDoAction.Create, ToDoAction.Update -> {
                val existing = state.events.firstOrNull { it.id == id }
                if (command.action == ToDoAction.Update && existing == null) {
                    throw IllegalArgumentException("No event with id $id.")
                }
                val title = command.title ?: existing?.title ?: throw IllegalArgumentException("An event needs a title.")
                val start = command.due ?: existing?.startsAt ?: throw IllegalArgumentException("An event needs a start in due.")
                val end = command.end ?: existing?.endsAt ?: start.plusHours(1)
                val event = WorkEvent(
                    id = id,
                    title = title,
                    notes = command.notes ?: existing?.notes.orEmpty(),
                    startsAt = start,
                    endsAt = end,
                    location = command.location ?: existing?.location.orEmpty(),
                    repeatRule = command.repeat ?: existing?.repeatRule ?: RepeatRule.None,
                    repeatDays = if (command.repeatDays.isNotEmpty()) command.repeatDays else existing?.repeatDays.orEmpty()
                )
                ToDoResult(
                    state.copy(events = state.events.filterNot { it.id == id } + event),
                    id,
                    "Saved ${event.title}."
                )
            }
        }
    }

    private fun applyGoal(state: AppState, command: ToDoCommand, today: LocalDate): ToDoResult {
        val id = command.id ?: if (command.action == ToDoAction.Create) UUID.randomUUID().toString() else missingId()
        if (command.action == ToDoAction.Delete) {
            return ToDoResult(
                state.copy(
                    goals = state.goals.filterNot { it.id == id },
                    tasks = state.tasks.filterNot { it.goalId == id }
                ),
                id,
                "Deleted the goal."
            )
        }
        val existing = state.goals.firstOrNull { it.id == id }
        if (command.action == ToDoAction.Update && existing == null) {
            throw IllegalArgumentException("No goal with id $id.")
        }
        val title = command.title ?: existing?.title ?: throw IllegalArgumentException("A goal needs a title.")
        val requirements = if (command.dailyRequirements.isNotEmpty() || command.action == ToDoAction.Create) {
            command.dailyRequirements
        } else {
            existing?.dailyRequirements.orEmpty()
        }
        val goal = WorkGoal(
            id = id,
            title = title,
            focus = command.focus ?: existing?.focus ?: WorkGoalFocus.General,
            notes = command.notes ?: existing?.notes.orEmpty(),
            target = command.target ?: existing?.target.orEmpty(),
            dailyRequirements = requirements
        )
        val withoutOld = state.tasks.filterNot { it.goalId == id }
        val dailyTasks = requirements.map { requirement ->
            val taskId = "goal-$id-${requirement.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')}"
            val previous = state.tasks.firstOrNull { it.id == taskId }
            TaskItem(
                id = taskId,
                title = requirement,
                notes = "Daily requirement for ${goal.title}.",
                goalId = id,
                workRelated = true,
                repeatRule = RepeatRule.Daily,
                deadline = previous?.deadline ?: today.atTime(9, 0),
                completed = previous?.completed ?: false,
                completionHistory = previous?.completionHistory.orEmpty(),
                reminderType = ReminderType.None,
                skipDaysOff = false,
                carryOverBehavior = CarryOverBehavior.KeepOverdue
            )
        }
        return ToDoResult(
            state.copy(goals = state.goals.filterNot { it.id == id } + goal, tasks = withoutOld + dailyTasks),
            id,
            "Saved the goal ${goal.title}."
        )
    }

    private fun applySchedule(state: AppState, command: ToDoCommand): ToDoResult {
        val text = command.text?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("A schedule push needs the schedule text. A photo can be written to the item after it is created.")
        val parsed = ScheduleTextParser.parse(text, command.scheduleYear)
        if (parsed.shifts.isEmpty() && parsed.daysOff.isEmpty()) {
            throw IllegalArgumentException("No shifts or days off were found in that schedule.")
        }
        val id = command.id ?: "schedule-${UUID.randomUUID()}"
        return ToDoResult(
            mergeImportedSchedule(state, parsed),
            id,
            "Imported ${parsed.shifts.size} shifts and ${parsed.daysOff.size} days off."
        )
    }

    private fun applyFile(state: AppState, command: ToDoCommand, today: LocalDate): ToDoResult {
        val id = command.id ?: if (command.action == ToDoAction.Create) UUID.randomUUID().toString() else missingId()
        if (command.action == ToDoAction.Delete) {
            return ToDoResult(
                state.copy(
                    files = state.files.filterNot { it.id == id },
                    images = state.images.filterNot { it.id == id }
                ),
                id,
                "Deleted the file."
            )
        }
        val title = command.title ?: state.files.firstOrNull { it.id == id }?.title
            ?: state.images.firstOrNull { it.id == id }?.title
            ?: "Work file"
        val path = command.storedPath.orEmpty()
        val mime = command.mimeType.orEmpty()
        val image = mime.startsWith("image", ignoreCase = true) || command.tags.any { it.equals("photo", true) }
        return if (image && path.isNotBlank()) {
            val photo = WorkImage(
                id = id,
                date = today,
                title = title,
                imagePath = path,
                detectedText = command.detectedText ?: command.text.orEmpty(),
                tags = command.tags
            )
            ToDoResult(
                state.copy(images = state.images.filterNot { it.id == id } + photo),
                id,
                "Saved the photo ${photo.title}."
            )
        } else {
            val file = WorkFile(
                id = id,
                title = title,
                filePath = path,
                mimeType = mime,
                notes = command.notes ?: command.text.orEmpty(),
                tags = command.tags
            )
            ToDoResult(
                state.copy(files = state.files.filterNot { it.id == id } + file),
                id,
                "Saved the file ${file.title}."
            )
        }
    }

    private fun missingId(): String {
        throw IllegalArgumentException("Update, complete, and delete need an id.")
    }
}

object WorkLibrary {
    data class Hit(val id: String, val title: String, val kind: String, val detail: String)

    fun search(state: AppState, query: String): List<Hit> {
        val needle = query.trim()
        val images = state.images.map { image ->
            Hit(image.id, image.title, "Photo", listOf(image.detectedText, image.tags.joinToString(" ")).filter { it.isNotBlank() }.joinToString(" "))
        }
        val files = state.files.map { file ->
            Hit(file.id, file.title, "File", listOf(file.notes, file.tags.joinToString(" "), file.mimeType).filter { it.isNotBlank() }.joinToString(" "))
        }
        return (images + files).filter { hit ->
            needle.isEmpty() ||
                hit.title.contains(needle, ignoreCase = true) ||
                hit.detail.contains(needle, ignoreCase = true) ||
                hit.kind.contains(needle, ignoreCase = true)
        }
    }
}
