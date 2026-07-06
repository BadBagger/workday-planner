package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TimecardCalculatorTest {
    @Test
    fun subtractsLunchFromPaidHours() {
        val day = LocalDate.of(2026, 7, 6)
        val entry = TimecardEntry(
            date = day,
            clockIn = LocalDateTime.of(2026, 7, 6, 9, 0),
            lunchStart = LocalDateTime.of(2026, 7, 6, 13, 0),
            lunchEnd = LocalDateTime.of(2026, 7, 6, 13, 30),
            clockOut = LocalDateTime.of(2026, 7, 6, 17, 0)
        )

        val summary = TimecardCalculator.summarize(entry, PaySettings(hourlyRate = 20.0))

        assertEquals(8.0, summary.workedHours, 0.01)
        assertEquals(0.5, summary.lunchHours, 0.01)
        assertEquals(7.5, summary.paidHours, 0.01)
        assertEquals(150.0, summary.grossPay, 0.01)
    }

    @Test
    fun openTimecardUsesCurrentTime() {
        val entry = TimecardEntry(
            date = LocalDate.of(2026, 7, 6),
            clockIn = LocalDateTime.of(2026, 7, 6, 9, 0)
        )

        val summary = TimecardCalculator.summarize(
            entry,
            PaySettings(hourlyRate = 10.0),
            now = LocalDateTime.of(2026, 7, 6, 12, 0)
        )

        assertEquals(3.0, summary.paidHours, 0.01)
        assertEquals(30.0, summary.grossPay, 0.01)
    }
}
