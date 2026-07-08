package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalDateTime

object ScheduleAwareTaskPlanner {
    fun resolve(task: TaskItem, state: AppState, now: LocalDateTime = LocalDateTime.now()): TaskItem {
        val shift = when {
            task.linkedShiftId != null -> state.shifts.firstOrNull { it.id == task.linkedShiftId }
            task.timingRule != TaskTimingRule.AtTime || task.repeatRule in shiftRepeatRules -> nextMatchingShift(state, now.toLocalDate(), task.linkedShiftType)
            else -> null
        }
        val deadline = when (task.timingRule) {
            TaskTimingRule.AtTime -> task.deadline
            TaskTimingRule.BeforeNextShift -> shift?.startDateTime()?.minusMinutes(task.alarmOffsetMinutes)
            TaskTimingRule.DuringShift -> shift?.startDateTime()?.plusMinutes(30)
            TaskTimingRule.AfterShift -> shift?.endDateTime()?.plusMinutes(task.alarmOffsetMinutes)
            TaskTimingRule.WorkdaysOnly -> task.deadline?.let { moveToNextWorkday(it, state) } ?: shift?.startDateTime()
        }
        val alarm = when {
            task.alarmAt == null -> null
            shift != null && task.timingRule == TaskTimingRule.BeforeNextShift -> shift.startDateTime().minusMinutes(task.alarmOffsetMinutes)
            shift != null && task.timingRule == TaskTimingRule.AfterShift -> shift.endDateTime().plusMinutes(task.alarmOffsetMinutes)
            shift != null && task.timingRule == TaskTimingRule.DuringShift -> shift.startDateTime().plusMinutes(task.alarmOffsetMinutes)
            deadline != null && task.timingRule == TaskTimingRule.WorkdaysOnly -> deadline.minusMinutes(task.alarmOffsetMinutes)
            else -> task.alarmAt
        }
        return task.copy(deadline = deadline, alarmAt = alarm, linkedShiftId = task.linkedShiftId ?: shift?.id)
    }

    fun rollUnfinishedWorkTasks(state: AppState, now: LocalDateTime = LocalDateTime.now()): List<TaskItem> {
        return state.tasks.map { task ->
            if (
                !task.completed &&
                task.workRelated &&
                task.carryOverBehavior == CarryOverBehavior.NextWorkday &&
                task.deadline?.isBefore(now) == true
            ) {
                val nextWorkday = nextWorkdayAfter(task.deadline.toLocalDate(), state)
                val deadline = task.deadline.withYear(nextWorkday.year).withMonth(nextWorkday.monthValue).withDayOfMonth(nextWorkday.dayOfMonth)
                val offset = task.alarmAt?.let { java.time.Duration.between(task.deadline, it) }
                task.copy(deadline = deadline, alarmAt = offset?.let { deadline.plus(it) })
            } else {
                task
            }
        }
    }

    fun shouldHideOnDayOff(task: TaskItem, state: AppState, date: LocalDate = LocalDate.now()): Boolean {
        return task.workRelated &&
            task.priority.sortWeight < TaskPriority.High.sortWeight &&
            task.deadline?.toLocalDate() != date &&
            TaskRecurrence.isDayOff(date, state)
    }

    fun nextMatchingShift(state: AppState, afterDate: LocalDate, type: LinkedShiftType): WorkShift? {
        return state.shifts
            .filter { !it.date.isBefore(afterDate) && matchesShiftType(it, type) }
            .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
            .firstOrNull()
    }

    fun nextMatchingShiftAfter(state: AppState, afterDate: LocalDate, type: LinkedShiftType): WorkShift? {
        return state.shifts
            .filter { it.date.isAfter(afterDate) && matchesShiftType(it, type) }
            .sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
            .firstOrNull()
    }

    fun matchesShiftType(shift: WorkShift, type: LinkedShiftType): Boolean {
        val text = "${shift.label} ${shift.notes}".lowercase()
        return when (type) {
            LinkedShiftType.Any -> true
            LinkedShiftType.Opening -> "open" in text || shift.start.hour < 9
            LinkedShiftType.Closing -> "close" in text || shift.end.hour >= 20
            LinkedShiftType.Truck -> "truck" in text || "order" in text
            LinkedShiftType.Inventory -> "inventory" in text
        }
    }

    private fun moveToNextWorkday(dateTime: LocalDateTime, state: AppState): LocalDateTime {
        return if (TaskRecurrence.isDayOff(dateTime.toLocalDate(), state)) {
            val next = nextWorkdayAfter(dateTime.toLocalDate(), state)
            dateTime.withYear(next.year).withMonth(next.monthValue).withDayOfMonth(next.dayOfMonth)
        } else {
            dateTime
        }
    }

    private fun nextWorkdayAfter(date: LocalDate, state: AppState): LocalDate {
        var candidate = date.plusDays(1)
        var attempts = 0
        while (TaskRecurrence.isDayOff(candidate, state)) {
            candidate = candidate.plusDays(1)
            attempts += 1
            if (attempts > 370) return date.plusDays(1)
        }
        return candidate
    }

    private val shiftRepeatRules = setOf(RepeatRule.OpeningShifts, RepeatRule.ClosingShifts, RepeatRule.TruckDays)

    private fun WorkShift.startDateTime(): LocalDateTime = LocalDateTime.of(date, start)

    private fun WorkShift.endDateTime(): LocalDateTime {
        val endDateTime = LocalDateTime.of(date, end)
        return if (end.isBefore(start)) endDateTime.plusDays(1) else endDateTime
    }
}
