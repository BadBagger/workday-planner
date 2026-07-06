package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

enum class RepeatRule {
    None,
    Daily,
    Weekdays,
    Weekly,
    CustomDays
}

enum class AccentStyle(val label: String) {
    Classic("Blue"),
    Emerald("Teal"),
    Sunrise("Amber")
}

enum class TaskCategory(val label: String) {
    General("General"),
    Orders("Orders"),
    Cleaning("Cleaning"),
    Prep("Prep"),
    Admin("Admin"),
    Personal("Personal")
}

enum class TaskPriority(val label: String, val sortWeight: Int) {
    Low("Low", 0),
    Normal("Normal", 1),
    High("High", 2),
    Critical("Critical", 3)
}

enum class WidgetLayoutMode(val label: String) {
    Compact("Compact"),
    Standard("Standard"),
    Detailed("Detailed")
}

enum class WorkNoteKind(val label: String) {
    General("General"),
    Order("Order"),
    Cleaning("Cleaning"),
    Customer("Customer"),
    Manager("Manager"),
    Issue("Issue"),
    FollowUp("Follow-up"),
    Meeting("Meeting")
}

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val category: TaskCategory = TaskCategory.General,
    val priority: TaskPriority = TaskPriority.Normal,
    val deadline: LocalDateTime? = null,
    val alarmAt: LocalDateTime? = null,
    val repeatRule: RepeatRule = RepeatRule.None,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val skipDaysOff: Boolean = true,
    val completed: Boolean = false
)

data class WorkNote(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val text: String,
    val kind: WorkNoteKind = WorkNoteKind.General,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class WorkImage(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val title: String,
    val imagePath: String,
    val detectedText: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class TrainingItem(
    val id: String = UUID.randomUUID().toString(),
    val associateName: String,
    val trainingTitle: String,
    val dueDate: LocalDate? = null,
    val sourceText: String = "",
    val importedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class WorkShift(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val label: String = "Work"
)

data class WorkEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val location: String = ""
)

data class PaySettings(
    val hourlyRate: Double = 0.0,
    val unpaidLunchMinutes: Int = 30,
    val overtimeThresholdHours: Double = 40.0,
    val overtimeMultiplier: Double = 1.5
)

data class TimecardEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val clockIn: LocalDateTime? = null,
    val lunchStart: LocalDateTime? = null,
    val lunchEnd: LocalDateTime? = null,
    val clockOut: LocalDateTime? = null,
    val note: String = ""
)

data class AppState(
    val tasks: List<TaskItem> = emptyList(),
    val notes: List<WorkNote> = emptyList(),
    val images: List<WorkImage> = emptyList(),
    val events: List<WorkEvent> = emptyList(),
    val shifts: List<WorkShift> = emptyList(),
    val daysOff: Set<LocalDate> = emptySet(),
    val defaultDaysOff: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    val darkMode: Boolean = false,
    val accentStyle: AccentStyle = AccentStyle.Classic,
    val widgetLayoutMode: WidgetLayoutMode = WidgetLayoutMode.Standard,
    val selectedCalendarId: Long? = null,
    val paySettings: PaySettings = PaySettings(),
    val timecards: List<TimecardEntry> = emptyList(),
    val trainingItems: List<TrainingItem> = emptyList()
)
