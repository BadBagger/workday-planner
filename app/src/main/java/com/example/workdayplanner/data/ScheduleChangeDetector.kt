package com.example.workdayplanner.data

import java.time.Duration
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ScheduleChangeSet(
    val addedShifts: List<WorkShift> = emptyList(),
    val removedShifts: List<WorkShift> = emptyList(),
    val changedShifts: List<ShiftChange> = emptyList(),
    val newDaysOff: Set<LocalDate> = emptySet(),
    val removedDaysOff: Set<LocalDate> = emptySet(),
    val overtimeWarnings: List<String> = emptyList()
) {
    val hasChanges: Boolean
        get() = addedShifts.isNotEmpty() ||
            removedShifts.isNotEmpty() ||
            changedShifts.isNotEmpty() ||
            newDaysOff.isNotEmpty() ||
            removedDaysOff.isNotEmpty() ||
            overtimeWarnings.isNotEmpty()
}

data class ShiftChange(
    val date: LocalDate,
    val oldShift: WorkShift,
    val newShift: WorkShift
)

object ScheduleChangeDetector {
    fun compare(current: AppState, parsed: ParsedSchedule): ScheduleChangeSet {
        val importedDates = (parsed.shifts.map { it.date } + parsed.daysOff).toSet()
        if (importedDates.isEmpty()) return ScheduleChangeSet()

        val oldShiftsByDate = current.shifts.filter { it.date in importedDates }.groupBy { it.date }
        val newShiftsByDate = parsed.shifts.groupBy { it.date }

        val changed = mutableListOf<ShiftChange>()
        val added = mutableListOf<WorkShift>()
        val removed = mutableListOf<WorkShift>()

        importedDates.sorted().forEach { date ->
            val oldShifts = oldShiftsByDate[date].orEmpty().sortedBy { it.start }
            val newShifts = newShiftsByDate[date].orEmpty().sortedBy { it.start }
            when {
                oldShifts.size == 1 && newShifts.size == 1 && !oldShifts.first().sameScheduleAs(newShifts.first()) -> {
                    changed += ShiftChange(date, oldShifts.first(), newShifts.first())
                }
                else -> {
                    val unmatchedOld = oldShifts.toMutableList()
                    newShifts.forEach { newShift ->
                        val match = unmatchedOld.firstOrNull { it.sameScheduleAs(newShift) }
                        if (match == null) {
                            added += newShift
                        } else {
                            unmatchedOld -= match
                        }
                    }
                    removed += unmatchedOld
                }
            }
        }

        val merged = mergeImportedSchedule(current, parsed)
        return ScheduleChangeSet(
            addedShifts = added,
            removedShifts = removed,
            changedShifts = changed,
            newDaysOff = parsed.daysOff - current.daysOff,
            removedDaysOff = current.daysOff.intersect(importedDates) - parsed.daysOff,
            overtimeWarnings = overtimeWarnings(current, merged, importedDates)
        )
    }

    private fun WorkShift.sameScheduleAs(other: WorkShift): Boolean {
        return date == other.date && start == other.start && end == other.end && label == other.label
    }

    private fun overtimeWarnings(oldState: AppState, newState: AppState, importedDates: Set<LocalDate>): List<String> {
        return importedDates
            .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .distinct()
            .sorted()
            .mapNotNull { weekStart ->
                val oldHours = scheduledHours(oldState.shifts, weekStart)
                val newHours = scheduledHours(newState.shifts, weekStart)
                when {
                    oldHours <= 40.0 && newHours > 40.0 -> "Week of ${weekStart}: ${newHours.formatHours()} scheduled hours. Overtime risk."
                    newHours in 38.0..40.0 -> "Week of ${weekStart}: ${newHours.formatHours()} scheduled hours. Close to overtime."
                    else -> null
                }
            }
    }

    private fun scheduledHours(shifts: List<WorkShift>, weekStart: LocalDate): Double {
        val weekEnd = weekStart.plusDays(6)
        return shifts
            .filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
            .sumOf { shift ->
                val minutes = Duration.between(shift.start, shift.end).toMinutes()
                if (minutes < 0) minutes + 24 * 60 else minutes
            } / 60.0
    }

    private fun Double.formatHours(): String {
        return if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
    }
}
