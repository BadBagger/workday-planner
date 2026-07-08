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
