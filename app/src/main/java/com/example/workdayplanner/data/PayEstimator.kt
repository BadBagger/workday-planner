package com.example.workdayplanner.data

import java.time.Duration
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class PayEstimate(
    val scheduledHours: Double,
    val paidHours: Double,
    val regularHours: Double,
    val overtimeHours: Double,
    val grossPay: Double,
    val hoursUntilOvertime: Double
)

object PayEstimator {
    fun estimateWeek(state: AppState, dateInWeek: LocalDate = LocalDate.now()): PayEstimate {
        val weekStart = dateInWeek.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)
        val shifts = state.shifts.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        return estimate(shifts, state.paySettings)
    }

    fun estimateDay(state: AppState, date: LocalDate = LocalDate.now()): PayEstimate {
        return estimate(state.shifts.filter { it.date == date }, state.paySettings)
    }

    fun estimate(shifts: List<WorkShift>, settings: PaySettings): PayEstimate {
        val scheduledHours = shifts.sumOf(::shiftHours)
        val lunchHours = shifts.count { shiftHours(it) > settings.unpaidLunchMinutes / 60.0 } *
            (settings.unpaidLunchMinutes / 60.0)
        val paidHours = (scheduledHours - lunchHours).coerceAtLeast(0.0)
        val regularHours = paidHours.coerceAtMost(settings.overtimeThresholdHours)
        val overtimeHours = (paidHours - settings.overtimeThresholdHours).coerceAtLeast(0.0)
        val grossPay = regularHours * settings.hourlyRate +
            overtimeHours * settings.hourlyRate * settings.overtimeMultiplier
        return PayEstimate(
            scheduledHours = scheduledHours,
            paidHours = paidHours,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            grossPay = grossPay,
            hoursUntilOvertime = (settings.overtimeThresholdHours - paidHours).coerceAtLeast(0.0)
        )
    }

    private fun shiftHours(shift: WorkShift): Double {
        val minutes = Duration.between(shift.start, shift.end).toMinutes()
        val adjusted = if (minutes < 0) minutes + 24 * 60 else minutes
        return adjusted / 60.0
    }
}
