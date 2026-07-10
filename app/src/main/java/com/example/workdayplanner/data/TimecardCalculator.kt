package com.example.workdayplanner.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

data class TimecardSummary(
    val workedHours: Double,
    val lunchHours: Double,
    val paidHours: Double,
    val grossPay: Double,
    val overtimeHours: Double = 0.0
)

object TimecardCalculator {
    fun summarize(entry: TimecardEntry, settings: PaySettings, now: LocalDateTime = LocalDateTime.now()): TimecardSummary {
        val clockIn = entry.clockIn ?: return TimecardSummary(0.0, 0.0, 0.0, 0.0)
        val clockOut = entry.clockOut ?: now
        val worked = minutesBetween(clockIn, clockOut) / 60.0
        val lunch = if (entry.lunchStart != null) {
            minutesBetween(entry.lunchStart, entry.lunchEnd ?: now) / 60.0
        } else {
            0.0
        }
        val paid = (worked - lunch).coerceAtLeast(0.0)
        return TimecardSummary(
            workedHours = worked,
            lunchHours = lunch,
            paidHours = paid,
            grossPay = paid * settings.hourlyRate
        )
    }

    fun summarizeWeek(state: AppState, dateInWeek: LocalDate = LocalDate.now()): TimecardSummary {
        val weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val entries = state.timecards.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        val summaries = entries.map { summarize(it, state.paySettings) }
        val paid = summaries.sumOf { it.paidHours }
        val worked = summaries.sumOf { it.workedHours }
        val lunch = summaries.sumOf { it.lunchHours }
        val regularHours = paid.coerceAtMost(state.paySettings.overtimeThresholdHours)
        val overtimeHours = (paid - state.paySettings.overtimeThresholdHours).coerceAtLeast(0.0)
        val gross = regularHours * state.paySettings.hourlyRate +
            overtimeHours * state.paySettings.hourlyRate * state.paySettings.overtimeMultiplier
        return TimecardSummary(workedHours = worked, lunchHours = lunch, paidHours = paid, grossPay = gross, overtimeHours = overtimeHours)
    }

    private fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Long {
        return Duration.between(start, end).toMinutes().coerceAtLeast(0)
    }
}
