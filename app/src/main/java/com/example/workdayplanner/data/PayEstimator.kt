package com.example.workdayplanner.data

import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class PayEstimate(
    val scheduledHours: Double,
    val paidHours: Double,
    val regularHours: Double,
    val overtimeHours: Double,
    val regularPay: Double,
    val overtimePay: Double,
    val differentialPay: Double,
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

    fun estimatePayPeriod(state: AppState, dateInPeriod: LocalDate = LocalDate.now()): PayEstimate {
        val (start, end) = payPeriodRange(state.paySettings, dateInPeriod)
        val shifts = state.shifts.filter { !it.date.isBefore(start) && !it.date.isAfter(end) }
        return shifts
            .groupBy { it.date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)) }
            .values
            .map { estimate(it, state.paySettings) }
            .combine(state.paySettings)
    }

    fun estimate(shifts: List<WorkShift>, settings: PaySettings): PayEstimate {
        val shiftRows = shifts.map { shift ->
            val scheduled = shiftHours(shift)
            val breakHours = if (settings.deductUnpaidBreaks) settings.unpaidLunchMinutes / 60.0 else 0.0
            val paid = (scheduled - breakHours).coerceAtLeast(0.0)
            val dailyOvertime = if (settings.dailyOvertimeThresholdHours > 0.0) {
                (paid - settings.dailyOvertimeThresholdHours).coerceAtLeast(0.0)
            } else {
                0.0
            }
            ShiftPayRow(shift, scheduled, paid, dailyOvertime)
        }
        val scheduledHours = shiftRows.sumOf { it.scheduledHours }
        val paidHours = shiftRows.sumOf { it.paidHours }
        val weeklyOvertime = (paidHours - settings.overtimeThresholdHours).coerceAtLeast(0.0)
        val overtimeHours = maxOf(weeklyOvertime, shiftRows.sumOf { it.dailyOvertimeHours })
        val regularHours = (paidHours - overtimeHours).coerceAtLeast(0.0)
        val regularPay = regularHours * settings.hourlyRate
        val overtimePay = overtimeHours * settings.hourlyRate * settings.overtimeMultiplier
        val differentialPay = shiftRows.sumOf { row -> row.paidHours * differentialFor(row.shift, settings) }
        val grossPay = regularPay + overtimePay + differentialPay
        return PayEstimate(
            scheduledHours = scheduledHours,
            paidHours = paidHours,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            regularPay = regularPay,
            overtimePay = overtimePay,
            differentialPay = differentialPay,
            grossPay = grossPay,
            hoursUntilOvertime = (settings.overtimeThresholdHours - paidHours).coerceAtLeast(0.0)
        )
    }

    private fun shiftHours(shift: WorkShift): Double {
        val minutes = Duration.between(shift.start, shift.end).toMinutes()
        val adjusted = if (minutes < 0) minutes + 24 * 60 else minutes
        return adjusted / 60.0
    }

    private fun differentialFor(shift: WorkShift, settings: PaySettings): Double {
        val text = shift.label.lowercase()
        val night = shift.end.isBefore(shift.start) || shift.start.hour >= 18 || shift.end.hour <= 6
        val weekend = shift.date.dayOfWeek.value >= 6
        val custom = settings.customShiftTypeLabel.isNotBlank() && text.contains(settings.customShiftTypeLabel.lowercase())
        return listOf(
            settings.nightShiftExtraAmount.takeIf { night },
            settings.weekendExtraAmount.takeIf { weekend },
            settings.customShiftTypeExtraAmount.takeIf { custom }
        ).filterNotNull().sum()
    }

    private fun payPeriodRange(settings: PaySettings, date: LocalDate): Pair<LocalDate, LocalDate> {
        return when (settings.payPeriodType) {
            PayPeriodType.Weekly -> {
                val start = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                start to start.plusDays(6)
            }
            PayPeriodType.Biweekly -> {
                val anchor = settings.customPayPeriodStart
                val days = ChronoUnit.DAYS.between(anchor, date)
                val periods = Math.floorDiv(days, 14)
                val start = anchor.plusDays(periods * 14)
                start to start.plusDays(13)
            }
            PayPeriodType.SemiMonthly -> {
                val start = if (date.dayOfMonth <= 15) date.withDayOfMonth(1) else date.withDayOfMonth(16)
                val end = if (date.dayOfMonth <= 15) date.withDayOfMonth(15) else date.withDayOfMonth(date.lengthOfMonth())
                start to end
            }
            PayPeriodType.Custom -> {
                val start = settings.customPayPeriodStart
                start to start.plusDays(13)
            }
        }
    }

    private data class ShiftPayRow(
        val shift: WorkShift,
        val scheduledHours: Double,
        val paidHours: Double,
        val dailyOvertimeHours: Double
    )

    private fun List<PayEstimate>.combine(settings: PaySettings): PayEstimate {
        val scheduled = sumOf { it.scheduledHours }
        val paid = sumOf { it.paidHours }
        val regular = sumOf { it.regularHours }
        val overtime = sumOf { it.overtimeHours }
        val regularPay = sumOf { it.regularPay }
        val overtimePay = sumOf { it.overtimePay }
        val differential = sumOf { it.differentialPay }
        return PayEstimate(
            scheduledHours = scheduled,
            paidHours = paid,
            regularHours = regular,
            overtimeHours = overtime,
            regularPay = regularPay,
            overtimePay = overtimePay,
            differentialPay = differential,
            grossPay = regularPay + overtimePay + differential,
            hoursUntilOvertime = (settings.overtimeThresholdHours - paid).coerceAtLeast(0.0)
        )
    }
}
