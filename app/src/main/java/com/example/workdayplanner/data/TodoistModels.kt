package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalDateTime

data class TodoistDue(
    val date: LocalDate?,
    val dateTimeUtc: LocalDateTime?,
    val timezone: String?,
    val isRecurring: Boolean,
    val string: String
)

data class TodoistTask(
    val id: String,
    val content: String,
    val description: String = "",
    val projectId: String? = null,
    val priority: Int = 1,
    val due: TodoistDue? = null,
    val durationMinutes: Int? = null,
    val checked: Boolean = false,
    val isDeleted: Boolean = false,
    val addedAt: String? = null,
    val updatedAt: String? = null
)

data class TodoistProject(
    val id: String,
    val name: String
)

data class TodoistPage<T>(
    val items: List<T>,
    val nextCursor: String?
)

data class TodoistDueDraft(
    val dueString: String? = null,
    val dueDate: String? = null,
    val dueDatetime: String? = null
)

data class TodoistTaskDraft(
    val content: String,
    val description: String,
    val priority: Int,
    val due: TodoistDueDraft = TodoistDueDraft(),
    val duration: Int? = null,
    val durationUnit: String? = null
)

data class TodoistMergeResult(
    val tasks: List<TaskItem>,
    val events: List<WorkEvent>
)

data class MappedRepeat(
    val rule: RepeatRule,
    val days: Set<java.time.DayOfWeek>,
    val clean: Boolean
)

class TodoistHttpException(
    val code: Int,
    message: String
) : Exception(message)

class TodoistNotConnected : Exception("Todoist is not connected.")
