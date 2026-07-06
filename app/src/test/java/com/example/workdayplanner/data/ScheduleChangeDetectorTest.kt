package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ScheduleChangeDetectorTest {
    @Test
    fun detectsChangedShiftAndNewDayOff() {
        val friday = LocalDate.of(2026, 7, 10)
        val current = AppState(
            shifts = listOf(WorkShift(date = friday, start = LocalTime.of(9, 0), end = LocalTime.of(17, 0)))
        )
        val parsed = ParsedSchedule(
            shifts = listOf(WorkShift(date = friday, start = LocalTime.of(12, 0), end = LocalTime.of(20, 0))),
            daysOff = setOf(LocalDate.of(2026, 7, 11)),
            unparsedLines = emptyList()
        )

        val changes = ScheduleChangeDetector.compare(current, parsed)

        assertEquals(1, changes.changedShifts.size)
        assertEquals(LocalTime.of(9, 0), changes.changedShifts.first().oldShift.start)
        assertEquals(LocalTime.of(12, 0), changes.changedShifts.first().newShift.start)
        assertTrue(LocalDate.of(2026, 7, 11) in changes.newDaysOff)
    }

    @Test
    fun detectsAddedAndRemovedShifts() {
        val monday = LocalDate.of(2026, 7, 6)
        val tuesday = LocalDate.of(2026, 7, 7)
        val current = AppState(
            shifts = listOf(WorkShift(date = monday, start = LocalTime.of(9, 0), end = LocalTime.of(17, 0)))
        )
        val parsed = ParsedSchedule(
            shifts = listOf(WorkShift(date = tuesday, start = LocalTime.of(10, 0), end = LocalTime.of(18, 0))),
            daysOff = setOf(monday),
            unparsedLines = emptyList()
        )

        val changes = ScheduleChangeDetector.compare(current, parsed)

        assertEquals(1, changes.removedShifts.size)
        assertEquals(monday, changes.removedShifts.first().date)
        assertEquals(1, changes.addedShifts.size)
        assertEquals(tuesday, changes.addedShifts.first().date)
    }

    @Test
    fun warnsWhenImportPushesWeekOvertime() {
        val monday = LocalDate.of(2026, 7, 6)
        val existing = (0L..3L).map { day ->
            WorkShift(date = monday.plusDays(day), start = LocalTime.of(8, 0), end = LocalTime.of(18, 0))
        }
        val parsed = ParsedSchedule(
            shifts = listOf(WorkShift(date = monday.plusDays(4), start = LocalTime.of(8, 0), end = LocalTime.of(14, 0))),
            daysOff = emptySet(),
            unparsedLines = emptyList()
        )

        val changes = ScheduleChangeDetector.compare(AppState(shifts = existing), parsed)

        assertTrue(changes.overtimeWarnings.any { it.contains("Overtime risk") })
    }
}
