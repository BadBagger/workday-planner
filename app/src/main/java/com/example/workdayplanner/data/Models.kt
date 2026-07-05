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

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val deadline: LocalDateTime? = null,
    val alarmAt: LocalDateTime? = null,
    val repeatRule: RepeatRule = RepeatRule.None,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val skipDaysOff: Boolean = true,
    val completed: Boolean = false
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

data class AppState(
    val tasks: List<TaskItem> = emptyList(),
    val events: List<WorkEvent> = emptyList(),
    val shifts: List<WorkShift> = emptyList(),
    val daysOff: Set<LocalDate> = emptySet(),
    val defaultDaysOff: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    val darkMode: Boolean = false,
    val accentStyle: AccentStyle = AccentStyle.Classic,
    val selectedCalendarId: Long? = null
)
