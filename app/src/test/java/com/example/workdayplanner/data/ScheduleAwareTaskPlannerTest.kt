package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleAwareTaskPlannerTest {
    @Test
    fun workdayRepeatSkipsMarkedDayOff() {
        val task = TaskItem(
            title = "Check dates",
            deadline = LocalDateTime.of(2026, 7, 7, 8, 0),
            repeatRule = RepeatRule.EveryWorkday,
            skipDaysOff = true
        )
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 8)), defaultDaysOff = emptySet())

        val next = TaskRecurrence.nextOccurrence(task, state)!!

        assertEquals(LocalDateTime.of(2026, 7, 9, 8, 0), next.deadline)
    }

    @Test
    fun unfinishedWorkTaskRollsToNextWorkday() {
        val task = TaskItem(
            title = "Handoff",
            deadline = LocalDateTime.of(2026, 7, 10, 18, 0),
            alarmAt = LocalDateTime.of(2026, 7, 10, 17, 30),
            workRelated = true,
            carryOverBehavior = CarryOverBehavior.NextWorkday
        )
        val state = AppState(
            tasks = listOf(task),
            defaultDaysOff = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )

        val rolled = ScheduleAwareTaskPlanner.rollUnfinishedWorkTasks(state, LocalDateTime.of(2026, 7, 10, 20, 0)).first()

        assertEquals(LocalDateTime.of(2026, 7, 13, 18, 0), rolled.deadline)
        assertEquals(LocalDateTime.of(2026, 7, 13, 17, 30), rolled.alarmAt)
    }

    @Test
    fun shiftLinkedReminderUpdatesWhenShiftTimeChanges() {
        val shift = WorkShift(
            id = "shift-1",
            date = LocalDate.of(2026, 7, 9),
            start = LocalTime.of(8, 0),
            end = LocalTime.of(16, 0),
            label = "Opening shift"
        )
        val task = TaskItem(
            title = "Opening checklist",
            linkedShiftId = shift.id,
            linkedShiftType = LinkedShiftType.Opening,
            timingRule = TaskTimingRule.BeforeNextShift,
            alarmOffsetMinutes = 30,
            deadline = LocalDateTime.of(2026, 7, 9, 7, 30),
            alarmAt = LocalDateTime.of(2026, 7, 9, 7, 30)
        )
        val changedShift = shift.copy(start = LocalTime.of(9, 0), end = LocalTime.of(17, 0))
        val resolved = ScheduleAwareTaskPlanner.resolve(task, AppState(shifts = listOf(changedShift)))

        assertEquals(LocalDateTime.of(2026, 7, 9, 8, 30), resolved.deadline)
        assertEquals(LocalDateTime.of(2026, 7, 9, 8, 30), resolved.alarmAt)
        assertEquals("shift-1", resolved.linkedShiftId)
    }

    @Test
    fun beforeShiftEndsTracksChangedShiftEnd() {
        val shift = WorkShift(
            id = "shift-2",
            date = LocalDate.of(2026, 7, 9),
            start = LocalTime.of(12, 0),
            end = LocalTime.of(20, 0),
            label = "Closing shift"
        )
        val task = TaskItem(
            title = "Closing sweep",
            linkedShiftId = shift.id,
            timingRule = TaskTimingRule.BeforeShiftEnds,
            alarmOffsetMinutes = 45,
            alarmAt = LocalDateTime.of(2026, 7, 9, 19, 15)
        )

        val resolved = ScheduleAwareTaskPlanner.resolve(task, AppState(shifts = listOf(shift.copy(end = LocalTime.of(21, 0)))))

        assertEquals(LocalDateTime.of(2026, 7, 9, 20, 15), resolved.deadline)
        assertEquals(LocalDateTime.of(2026, 7, 9, 20, 15), resolved.alarmAt)
    }

    @Test
    fun daysOffOnlyMovesToNextMarkedDayOff() {
        val task = TaskItem(
            title = "Wash uniforms",
            deadline = LocalDateTime.of(2026, 7, 6, 10, 0),
            timingRule = TaskTimingRule.DaysOffOnly,
            alarmAt = LocalDateTime.of(2026, 7, 6, 9, 30),
            alarmOffsetMinutes = 30
        )
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 8)), defaultDaysOff = emptySet())

        val resolved = ScheduleAwareTaskPlanner.resolve(task, state, LocalDateTime.of(2026, 7, 6, 8, 0))

        assertEquals(LocalDateTime.of(2026, 7, 8, 10, 0), resolved.deadline)
        assertEquals(LocalDateTime.of(2026, 7, 8, 9, 30), resolved.alarmAt)
    }

    @Test
    fun skipDaysOffMovesSpecificTimeTaskAndAlarmTogether() {
        val task = TaskItem(
            title = "Submit order",
            deadline = LocalDateTime.of(2026, 7, 8, 15, 0),
            alarmAt = LocalDateTime.of(2026, 7, 8, 14, 30),
            timingRule = TaskTimingRule.AtTime,
            skipDaysOff = true,
            workRelated = true
        )
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 8)), defaultDaysOff = emptySet())

        val resolved = ScheduleAwareTaskPlanner.resolve(task, state, LocalDateTime.of(2026, 7, 8, 8, 0))

        assertEquals(LocalDateTime.of(2026, 7, 9, 15, 0), resolved.deadline)
        assertEquals(LocalDateTime.of(2026, 7, 9, 14, 30), resolved.alarmAt)
    }

    @Test
    fun dismissIfMissedCompletesOverdueTask() {
        val task = TaskItem(
            title = "Quick reminder",
            deadline = LocalDateTime.of(2026, 7, 6, 10, 0),
            carryOverBehavior = CarryOverBehavior.DismissIfMissed
        )
        val state = AppState(tasks = listOf(task))

        val resolved = ScheduleAwareTaskPlanner.rollUnfinishedWorkTasks(state, LocalDateTime.of(2026, 7, 6, 12, 0)).first()

        assertTrue(resolved.completed)
        assertEquals(1, resolved.completionHistory.size)
    }

    @Test
    fun hidesNonUrgentWorkTasksOnDaysOff() {
        val task = TaskItem(
            title = "Routine work note",
            workRelated = true,
            priority = TaskPriority.Normal,
            deadline = LocalDateTime.of(2026, 7, 10, 12, 0)
        )
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 9)), defaultDaysOff = emptySet())

        assertTrue(ScheduleAwareTaskPlanner.shouldHideOnDayOff(task, state, LocalDate.of(2026, 7, 9)))
    }
}
