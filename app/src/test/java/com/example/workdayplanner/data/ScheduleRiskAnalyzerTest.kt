package com.example.workdayplanner.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ScheduleRiskAnalyzerTest {
    @Test
    fun detectsTightTurnaround() {
        val today = LocalDate.of(2026, 7, 6)
        val state = AppState(
            shifts = listOf(
                WorkShift(date = today, start = LocalTime.of(14, 0), end = LocalTime.of(22, 30)),
                WorkShift(date = today.plusDays(1), start = LocalTime.of(6, 0), end = LocalTime.of(14, 0))
            )
        )

        val risks = ScheduleRiskAnalyzer.risks(state, today)

        assertTrue(risks.any { it.title == "Tight turnaround" })
    }

    @Test
    fun detectsLongStretch() {
        val today = LocalDate.of(2026, 7, 6)
        val shifts = (0L..5L).map { day ->
            WorkShift(date = today.plusDays(day), start = LocalTime.of(9, 0), end = LocalTime.of(17, 0))
        }

        val risks = ScheduleRiskAnalyzer.risks(AppState(shifts = shifts), today)

        assertTrue(risks.any { it.title == "Long stretch" })
    }

    @Test
    fun detectsCloseToOvertime() {
        val today = LocalDate.of(2026, 7, 6)
        val shifts = listOf(
            WorkShift(date = today, start = LocalTime.of(8, 0), end = LocalTime.of(17, 0)),
            WorkShift(date = today.plusDays(1), start = LocalTime.of(8, 0), end = LocalTime.of(17, 0)),
            WorkShift(date = today.plusDays(2), start = LocalTime.of(8, 0), end = LocalTime.of(17, 0)),
            WorkShift(date = today.plusDays(3), start = LocalTime.of(8, 0), end = LocalTime.of(17, 0)),
            WorkShift(date = today.plusDays(4), start = LocalTime.of(9, 0), end = LocalTime.of(12, 0))
        )
        val state = AppState(
            shifts = shifts,
            paySettings = PaySettings(hourlyRate = 20.0, unpaidLunchMinutes = 0, overtimeThresholdHours = 40.0)
        )

        val risks = ScheduleRiskAnalyzer.risks(state, today)

        assertTrue(risks.any { it.title == "Close to overtime" })
    }
}
