package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

enum class PlannerDayKind {
    Task,
    Event
}

data class PlannerDayItem(
    val id: String,
    val title: String,
    val kind: PlannerDayKind,
    val start: LocalTime?,
    val end: LocalTime?,
    val repeats: String?
)

object PlannerDayPlan {
    fun itemsFor(state: AppState, date: LocalDate): List<PlannerDayItem> {
        val tasks = state.tasks.mapNotNull { task -> task.toDayItem(date) }
        val events = state.events.mapNotNull { event -> event.toDayItem(date) }
        return (events + tasks).sortedWith(
            compareBy<PlannerDayItem> { it.start == null }
                .thenBy { it.start ?: LocalTime.MAX }
                .thenBy { it.title.lowercase() }
        )
    }

    private fun TaskItem.toDayItem(date: LocalDate): PlannerDayItem? {
        if (completed) return null
        val deadline = deadline ?: return null
        if (deadline.toLocalDate() != date) return null
        val allDay = deadline.hour == 23 && deadline.minute >= 59
        val end = if (!allDay && durationMinutes != null && durationMinutes > 0) {
            deadline.toLocalTime().plusMinutes(durationMinutes.toLong())
        } else {
            null
        }
        return PlannerDayItem(
            id = id,
            title = title,
            kind = PlannerDayKind.Task,
            start = if (allDay) null else deadline.toLocalTime(),
            end = end,
            repeats = repeatText()
        )
    }

    private fun WorkEvent.toDayItem(date: LocalDate): PlannerDayItem? {
        if (!occursOn(date)) return null
        return PlannerDayItem(
            id = id,
            title = title,
            kind = PlannerDayKind.Event,
            start = startsAt.toLocalTime(),
            end = endsAt.toLocalTime(),
            repeats = repeatText()
        )
    }

    private fun WorkEvent.occursOn(date: LocalDate): Boolean {
        val startDate = startsAt.toLocalDate()
        if (date.isBefore(startDate)) return false
        return when (repeatRule) {
            RepeatRule.None -> {
                val dayStart = date.atStartOfDay()
                val dayEnd = date.plusDays(1).atStartOfDay()
                startsAt.isBefore(dayEnd) && endsAt.isAfter(dayStart)
            }
            RepeatRule.Daily -> true
            RepeatRule.Weekdays, RepeatRule.EveryWorkday -> date.dayOfWeek.value <= 5
            RepeatRule.Weekly -> date.dayOfWeek == startDate.dayOfWeek
            RepeatRule.CustomDays -> date.dayOfWeek in repeatDays
            else -> date == startDate
        }
    }

    private fun WorkEvent.repeatText(): String? = when (repeatRule) {
        RepeatRule.None -> null
        RepeatRule.Daily -> "Daily"
        RepeatRule.Weekdays -> "Weekdays"
        RepeatRule.EveryWorkday -> "Every workday"
        RepeatRule.Weekly -> "Weekly"
        RepeatRule.CustomDays -> repeatDays.sortedBy(DayOfWeek::getValue).joinToString(", ") { it.display() }
        else -> null
    }

    private fun TaskItem.repeatText(): String? {
        return when (repeatRule) {
            RepeatRule.Daily -> "Daily"
            RepeatRule.Weekdays -> "Weekdays"
            RepeatRule.EveryWorkday -> "Every workday"
            RepeatRule.Weekly -> repeatDays.singleOrNull()?.let { "Every ${it.display()}" } ?: "Weekly"
            RepeatRule.CustomDays -> repeatDays.sortedBy(DayOfWeek::getValue).joinToString(", ") { it.display() }
            RepeatRule.OpeningShifts -> "Opening shifts"
            RepeatRule.ClosingShifts -> "Closing shifts"
            RepeatRule.TruckDays -> "Truck days"
            RepeatRule.InventoryDays -> "Inventory days"
            RepeatRule.None -> null
        }
    }

    private fun DayOfWeek.display(): String {
        return name.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
    }
}
