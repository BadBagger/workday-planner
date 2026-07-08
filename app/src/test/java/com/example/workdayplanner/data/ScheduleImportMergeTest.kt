package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleImportMergeTest {
    @Test
    fun importOnlyReplacesDatesPresentInNewScreenshot() {
        val oldSaturday = WorkShift(
            date = LocalDate.of(2026, 7, 4),
            start = LocalTime.of(11, 0),
            end = LocalTime.of(19, 30)
        )
        val oldMonday = WorkShift(
            date = LocalDate.of(2026, 7, 6),
            start = LocalTime.of(6, 0),
            end = LocalTime.of(15, 0)
        )
        val updatedMonday = WorkShift(
            date = LocalDate.of(2026, 7, 6),
            start = LocalTime.of(7, 0),
            end = LocalTime.of(16, 0)
        )
        val nextSaturday = WorkShift(
            date = LocalDate.of(2026, 7, 11),
            start = LocalTime.of(6, 0),
            end = LocalTime.of(17, 0)
        )

        val state = AppState(
            shifts = listOf(oldSaturday, oldMonday),
            daysOff = setOf(LocalDate.of(2026, 7, 5))
        )
        val parsed = ParsedSchedule(
            shifts = listOf(updatedMonday, nextSaturday),
            daysOff = setOf(LocalDate.of(2026, 7, 12)),
            unparsedLines = emptyList()
        )

        val merged = mergeImportedSchedule(state, parsed)

        assertEquals(listOf(oldSaturday, updatedMonday, nextSaturday), merged.shifts)
        assertTrue(LocalDate.of(2026, 7, 5) in merged.daysOff)
        assertTrue(LocalDate.of(2026, 7, 12) in merged.daysOff)
    }

    @Test
    fun deleteShiftUnlinksTasksAndClearsRelatedReminderData() {
        val shift = WorkShift(
            id = "shift-1",
            date = LocalDate.of(2026, 7, 8),
            start = LocalTime.of(9, 0),
            end = LocalTime.of(17, 0)
        )
        val linkedTask = TaskItem(
            id = "task-1",
            title = "Bring uniform",
            linkedShiftId = shift.id,
            deadline = LocalDateTime.of(2026, 7, 8, 8, 30),
            alarmAt = LocalDateTime.of(2026, 7, 8, 8, 0)
        )
        val state = AppState(shifts = listOf(shift), tasks = listOf(linkedTask))

        val updated = removeShiftAndUnlinkTasks(state, shift.id)

        assertTrue(updated.shifts.isEmpty())
        assertEquals(null, updated.tasks.first().linkedShiftId)
        assertEquals(null, updated.tasks.first().alarmAt)
        assertEquals(LocalDateTime.of(2026, 7, 8, 8, 30), updated.tasks.first().deadline)
    }
}
