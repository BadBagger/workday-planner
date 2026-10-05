package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PostedPassportScheduleTest {
    @Test
    fun importsTheTwoPassportWeeksAsPostedShifts() {
        val parsed = PostedPassportSchedule.parsed()
        val byDate = parsed.shifts.associateBy { it.date }

        assertEquals(10, parsed.shifts.size)
        assertTrue(parsed.shifts.all { it.label == "Asst. Deli Manager" })
        assertTrue(parsed.shifts.none { it.patternId != null })
        assertEquals(setOf(LocalDate.of(2026, 9, 26), LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 4)), parsed.daysOff)

        val monday = byDate.getValue(LocalDate.of(2026, 10, 5))
        assertEquals(LocalTime.of(6, 0), monday.start)
        assertEquals(LocalTime.of(18, 0), monday.end)
        assertEquals("Store #2058", monday.location)
        assertTrue(monday.notes.contains("11 hours"))
        assertTrue("Monday stays the posted 6 a.m. shift" , !monday.notes.contains("Meal"))

        val thursdayFlorence = byDate.getValue(LocalDate.of(2026, 10, 8))
        assertEquals(LocalTime.of(8, 0), thursdayFlorence.start)
        assertEquals(LocalTime.of(18, 0), thursdayFlorence.end)
        assertTrue(thursdayFlorence.notes.contains("Meal 1:00 p.m.–2:00 p.m."))

        val thursdayPrior = byDate.getValue(LocalDate.of(2026, 10, 1))
        assertEquals(LocalTime.of(19, 0), thursdayPrior.end)
        assertEquals("Store #1640", thursdayPrior.location)
        assertTrue(thursdayPrior.notes.contains("Meal 1:00 p.m.–2:00 p.m."))

        val warned = byDate.getValue(LocalDate.of(2026, 9, 30))
        assertTrue(warned.notes.contains("warning"))
        assertEquals(LocalTime.of(22, 30), byDate.getValue(LocalDate.of(2026, 9, 28)).end)
        assertEquals(LocalTime.of(15, 0), byDate.getValue(LocalDate.of(2026, 10, 2)).end)
    }
}
