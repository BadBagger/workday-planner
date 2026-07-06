package com.example.workdayplanner.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

data class ScheduleRisk(
    val title: String,
    val detail: String
)

object ScheduleRiskAnalyzer {
    fun risks(state: AppState, today: LocalDate = LocalDate.now()): List<ScheduleRisk> {
        val shifts = state.shifts.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start })
        val weekPay = PayEstimator.estimateWeek(state, today)
        val risks = mutableListOf<ScheduleRisk>()

        if (weekPay.overtimeHours > 0.0) {
            risks += ScheduleRisk("Overtime week", "${weekPay.paidHours.formatHours()} paid hours scheduled this week.")
        } else if (weekPay.hoursUntilOvertime in 0.01..4.0) {
            risks += ScheduleRisk("Close to overtime", "${weekPay.hoursUntilOvertime.formatHours()} hours until overtime.")
        }

        tightTurnarounds(shifts, today).forEach { (previous, next, restHours) ->
            risks += ScheduleRisk(
                "Tight turnaround",
                "${previous.date.formatShort()} close into ${next.date.formatShort()} open: ${restHours.formatHours()} hours between shifts."
            )
        }

        val streak = longestUpcomingStreak(shifts, today)
        if (streak >= 6) {
            risks += ScheduleRisk("Long stretch", "$streak scheduled days in a row.")
        }

        return risks.distinctBy { "${it.title}-${it.detail}" }.take(4)
    }

    private fun tightTurnarounds(shifts: List<WorkShift>, today: LocalDate): List<Triple<WorkShift, WorkShift, Double>> {
        return shifts
            .zipWithNext()
            .mapNotNull { (previous, next) ->
                if (next.date.isBefore(today)) return@mapNotNull null
                val rest = Duration.between(previous.endDateTime(), next.startDateTime()).toMinutes() / 60.0
                if (rest < 10.0) Triple(previous, next, rest.coerceAtLeast(0.0)) else null
            }
    }

    private fun longestUpcomingStreak(shifts: List<WorkShift>, today: LocalDate): Int {
        val dates = shifts.map { it.date }.filter { !it.isBefore(today) }.toSortedSet()
        var longest = 0
        var current = 0
        var previous: LocalDate? = null
        dates.forEach { date ->
            current = if (previous?.plusDays(1) == date) current + 1 else 1
            longest = maxOf(longest, current)
            previous = date
        }
        return longest
    }

    private fun WorkShift.startDateTime() = LocalDateTime.of(date, start)

    private fun WorkShift.endDateTime(): LocalDateTime {
        val endDateTime = LocalDateTime.of(date, end)
        return if (end.isBefore(start)) endDateTime.plusDays(1) else endDateTime
    }

    private fun LocalDate.formatShort(): String {
        val weekStart = this.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        return if (this == weekStart) "Mon" else dayOfWeek.name.take(3).lowercase().replaceFirstChar(Char::uppercase)
    }

    private fun Double.formatHours(): String {
        return if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
    }
}
