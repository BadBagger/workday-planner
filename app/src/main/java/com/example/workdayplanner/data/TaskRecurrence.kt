package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalDateTime

object TaskRecurrence {
    fun nextOccurrence(task: TaskItem, state: AppState): TaskItem? {
        val deadline = task.deadline ?: return null
        val nextDeadline = nextDateTime(deadline, task, state) ?: return null
        val adjustedDeadline = if (task.skipDaysOff) skipDaysOff(nextDeadline, state, task) else nextDeadline
        val alarmOffset = if (task.alarmAt != null) java.time.Duration.between(task.deadline, task.alarmAt) else null
        return task.copy(
            id = java.util.UUID.randomUUID().toString(),
            completed = false,
            deadline = adjustedDeadline,
            alarmAt = alarmOffset?.let { adjustedDeadline.plus(it) }
        )
    }

    private fun nextDateTime(deadline: LocalDateTime, task: TaskItem, state: AppState): LocalDateTime? = when (task.repeatRule) {
        RepeatRule.None -> null
        RepeatRule.Daily -> deadline.plusDays(1)
        RepeatRule.Weekdays -> {
            var next = deadline.plusDays(1)
            while (next.dayOfWeek.value > 5) next = next.plusDays(1)
            next
        }
        RepeatRule.Weekly -> deadline.plusWeeks(1)
        RepeatRule.EveryWorkday -> nextWorkdayDateTime(deadline, state)
        RepeatRule.OpeningShifts -> nextShiftDateTime(deadline, state, LinkedShiftType.Opening, task.timingRule, task.alarmOffsetMinutes)
        RepeatRule.ClosingShifts -> nextShiftDateTime(deadline, state, LinkedShiftType.Closing, task.timingRule, task.alarmOffsetMinutes)
        RepeatRule.TruckDays -> nextShiftDateTime(deadline, state, LinkedShiftType.Truck, task.timingRule, task.alarmOffsetMinutes)
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

    private fun nextWorkdayDateTime(deadline: LocalDateTime, state: AppState): LocalDateTime {
        var next = deadline.plusDays(1)
        var attempts = 0
        while (isDayOff(next.toLocalDate(), state)) {
            next = next.plusDays(1)
            attempts += 1
            if (attempts > 370) return deadline.plusDays(1)
        }
        return next
    }

    private fun nextShiftDateTime(
        deadline: LocalDateTime,
        state: AppState,
        type: LinkedShiftType,
        timingRule: TaskTimingRule,
        offsetMinutes: Long
    ): LocalDateTime? {
        val shift = ScheduleAwareTaskPlanner.nextMatchingShiftAfter(state, deadline.toLocalDate(), type) ?: return null
        val start = LocalDateTime.of(shift.date, shift.start)
        val end = LocalDateTime.of(shift.date, shift.end).let { if (shift.end.isBefore(shift.start)) it.plusDays(1) else it }
        return when (timingRule) {
            TaskTimingRule.BeforeNextShift, TaskTimingRule.AtTime, TaskTimingRule.WorkdaysOnly -> start.minusMinutes(offsetMinutes)
            TaskTimingRule.DuringShift -> start.plusMinutes(30)
            TaskTimingRule.AfterShift -> end.plusMinutes(offsetMinutes)
        }
    }

    private fun skipDaysOff(dateTime: LocalDateTime, state: AppState, task: TaskItem): LocalDateTime {
        var candidate = dateTime
        var attempts = 0
        // Keep repeating tasks on valid workdays only. Custom-day repeats still honor the
        // user's selected weekdays after skipping explicit or default days off.
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
