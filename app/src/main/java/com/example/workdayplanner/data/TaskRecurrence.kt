package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalDateTime

object TaskRecurrence {
    fun nextOccurrence(task: TaskItem, state: AppState): TaskItem? {
        val deadline = task.deadline ?: return null
        val nextDeadline = nextDateTime(deadline, task) ?: return null
        val adjustedDeadline = if (task.skipDaysOff) skipDaysOff(nextDeadline, state, task) else nextDeadline
        val alarmOffset = if (task.alarmAt != null) java.time.Duration.between(task.deadline, task.alarmAt) else null
        return task.copy(
            id = java.util.UUID.randomUUID().toString(),
            completed = false,
            deadline = adjustedDeadline,
            alarmAt = alarmOffset?.let { adjustedDeadline.plus(it) }
        )
    }

    private fun nextDateTime(deadline: LocalDateTime, task: TaskItem): LocalDateTime? = when (task.repeatRule) {
        RepeatRule.None -> null
        RepeatRule.Daily -> deadline.plusDays(1)
        RepeatRule.Weekdays -> {
            var next = deadline.plusDays(1)
            while (next.dayOfWeek.value > 5) next = next.plusDays(1)
            next
        }
        RepeatRule.Weekly -> deadline.plusWeeks(1)
        RepeatRule.CustomDays -> {
            if (task.repeatDays.isEmpty()) {
                null
            } else {
                var next = deadline.plusDays(1)
                while (next.dayOfWeek !in task.repeatDays) next = next.plusDays(1)
                next
            }
        }
    }

    private fun skipDaysOff(dateTime: LocalDateTime, state: AppState, task: TaskItem): LocalDateTime {
        var candidate = dateTime
        var attempts = 0
        while (
            isDayOff(candidate.toLocalDate(), state) ||
            (task.repeatRule == RepeatRule.CustomDays && candidate.dayOfWeek !in task.repeatDays)
        ) {
            candidate = candidate.plusDays(1)
            attempts += 1
            if (attempts > 370) return dateTime
        }
        return candidate
    }

    fun isDayOff(date: LocalDate, state: AppState): Boolean {
        return date in state.daysOff || date.dayOfWeek in state.defaultDaysOff
    }
}
