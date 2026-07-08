package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalDateTime

enum class TaskScheduleLabel(val label: String) {
    BeforeWork("Before work"),
    AfterWork("After work"),
    DayOffTask("Day off task"),
    DueDuringShift("Due during shift"),
    QuickTask("Quick task"),
    Overdue("Overdue"),
    RepeatsNextWorkday("Repeats next workday"),
    SkippedDayOff("Skipped because this is a day off.")
}

data class TaskScheduleInsight(
    val labels: List<TaskScheduleLabel>,
    val warning: String? = null,
    val suggestion: String? = null
)

object TaskScheduleClassifier {
    fun classify(task: TaskItem, state: AppState, now: LocalDateTime = LocalDateTime.now()): TaskScheduleInsight {
        val labels = linkedSetOf<TaskScheduleLabel>()
        val deadline = task.deadline
        val dueDate = deadline?.toLocalDate()
        val shifts = dueDate?.let { date -> state.shifts.filter { it.date == date }.sortedBy { it.start } }.orEmpty()
        val isDayOff = dueDate?.let { TaskRecurrence.isDayOff(it, state) } == true

        if (!task.completed && deadline != null && deadline.isBefore(now)) {
            labels += TaskScheduleLabel.Overdue
        }

        if (isDayOff) {
            labels += if (task.repeatRule != RepeatRule.None && task.skipDaysOff) {
                TaskScheduleLabel.SkippedDayOff
            } else {
                TaskScheduleLabel.DayOffTask
            }
        }

        val dueDuringShift = deadline != null && shifts.any { shift -> deadline in shift.startDateTime()..shift.endDateTime() }
        if (dueDuringShift) {
            labels += TaskScheduleLabel.DueDuringShift
        } else if (deadline != null && shifts.isNotEmpty()) {
            val firstShift = shifts.first()
            val lastShift = shifts.maxBy { it.endDateTime() }
            if (deadline.isBefore(firstShift.startDateTime())) {
                labels += TaskScheduleLabel.BeforeWork
            } else if (deadline.isAfter(lastShift.endDateTime())) {
                labels += TaskScheduleLabel.AfterWork
            }
        }

        if (task.repeatRule != RepeatRule.None && task.skipDaysOff) {
            val next = TaskRecurrence.nextOccurrence(task, state)
            if (next?.deadline != null && dueDate != null && (isDayOff || next.deadline.toLocalDate() != nextCalendarDate(task))) {
                labels += TaskScheduleLabel.RepeatsNextWorkday
            }
        }

        if (labels.none { it in setOf(TaskScheduleLabel.Overdue, TaskScheduleLabel.DueDuringShift, TaskScheduleLabel.BeforeWork, TaskScheduleLabel.AfterWork) }) {
            labels += TaskScheduleLabel.QuickTask
        }

        val alarmDuringShift = task.alarmAt != null && shifts.any { shift -> task.alarmAt in shift.startDateTime()..shift.endDateTime() }
        val warning = if (alarmDuringShift) "Reminder falls during a shift" else null
        val movedRepeat = if (task.repeatRule != RepeatRule.None && task.skipDaysOff) TaskRecurrence.nextOccurrence(task, state) else null
        val suggestion = when {
            isDayOff && movedRepeat?.deadline != null -> "Moved to your next workday: ${movedRepeat.deadline.toLocalDate()}."
            alarmDuringShift && deadline != null && shifts.isNotEmpty() -> {
                val firstShift = shifts.first()
                if (deadline.isAfter(firstShift.startDateTime())) "Consider reminding before ${firstShift.start.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}."
                else "Consider reminding after the shift."
            }
            isDayOff && task.priority.sortWeight >= TaskPriority.High.sortWeight -> "Good flexible day-off task if you choose to handle it."
            else -> null
        }

        return TaskScheduleInsight(labels.toList(), warning, suggestion)
    }

    private fun nextCalendarDate(task: TaskItem): LocalDate? {
        val deadline = task.deadline ?: return null
        return when (task.repeatRule) {
            RepeatRule.None -> null
            RepeatRule.Daily -> deadline.plusDays(1).toLocalDate()
            RepeatRule.Weekdays -> {
                var next = deadline.plusDays(1)
                while (next.dayOfWeek.value > 5) next = next.plusDays(1)
                next.toLocalDate()
            }
            RepeatRule.Weekly -> deadline.plusWeeks(1).toLocalDate()
            RepeatRule.EveryWorkday -> TaskRecurrence.nextOccurrence(task, AppState(defaultDaysOff = emptySet()))?.deadline?.toLocalDate()
            RepeatRule.OpeningShifts,
            RepeatRule.ClosingShifts,
            RepeatRule.TruckDays -> null
            RepeatRule.CustomDays -> {
                if (task.repeatDays.isEmpty()) null else {
                    var next = deadline.plusDays(1)
                    while (next.dayOfWeek !in task.repeatDays) next = next.plusDays(1)
                    next.toLocalDate()
                }
            }
        }
    }

    private fun WorkShift.startDateTime(): LocalDateTime = LocalDateTime.of(date, start)

    // Overnight shifts end on the next calendar day; otherwise a due time after midnight
    // would be incorrectly treated as outside the shift.
    private fun WorkShift.endDateTime(): LocalDateTime {
        val endDateTime = LocalDateTime.of(date, end)
        return if (end.isBefore(start)) endDateTime.plusDays(1) else endDateTime
    }
}
