package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ShiftPatternGeneratorTest {
    @Test
    fun generatesFourOnFourOffPreview() {
        val pattern = ShiftPatternGenerator.preset("4 on / 4 off", LocalDate.of(2026, 7, 1))
        val preview = ShiftPatternGenerator.preview(pattern, daysToPreview = 8)

        assertEquals(4, preview.shifts.size)
        assertEquals(4, preview.daysOff.size)
        assertEquals(LocalDate.of(2026, 7, 1), preview.shifts.first().date)
        assertTrue(LocalDate.of(2026, 7, 5) in preview.daysOff)
    }

    @Test
    fun generatesFiveDayFiveOffFiveNightPreset() {
        val pattern = ShiftPatternGenerator.preset("5 day / 5 off / 5 night", LocalDate.of(2026, 7, 1))
        val preview = ShiftPatternGenerator.preview(pattern, daysToPreview = 15)

        assertEquals(10, preview.shifts.size)
        assertEquals(5, preview.daysOff.size)
        assertEquals("Day shift", preview.shifts.first().label)
        assertEquals("Night shift", preview.shifts.last().label)
        assertEquals(LocalTime.of(22, 0), preview.shifts.last().start)
        assertEquals(LocalTime.of(6, 0), preview.shifts.last().end)
    }

    @Test
    fun respectsEndDate() {
        val pattern = ShiftPatternGenerator.preset("5 on / 2 off", LocalDate.of(2026, 7, 1))
            .copy(endDate = LocalDate.of(2026, 7, 3))
        val preview = ShiftPatternGenerator.preview(pattern, daysToPreview = 30)

        assertEquals(3, preview.shifts.size)
        assertTrue(preview.shifts.all { !it.date.isAfter(LocalDate.of(2026, 7, 3)) })
    }
}
