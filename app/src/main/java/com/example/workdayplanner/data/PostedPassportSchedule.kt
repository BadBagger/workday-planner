package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalTime

/**
 * One-off Publix Passport shifts read from the two schedule screenshots.
 * Times are the posted America/New_York wall times. These are not a repeating pattern.
 */
object PostedPassportSchedule {
    const val ROLE = "Asst. Deli Manager"
    const val ZONE = "America/New_York"

    fun parsed(): ParsedSchedule {
        val shifts = listOf(
            shift(LocalDate.of(2026, 9, 27), 6, 0, 17, 0, "1640", "10 hours"),
            shift(LocalDate.of(2026, 9, 28), 12, 0, 22, 30, "1640", "9.5 hours"),
            shift(LocalDate.of(2026, 9, 30), 7, 0, 17, 0, "1640", "9 hours", warning = true),
            shift(LocalDate.of(2026, 10, 1), 8, 0, 19, 0, "1640", "10 hours", meal = "1:00 p.m.–2:00 p.m."),
            shift(LocalDate.of(2026, 10, 2), 7, 0, 15, 0, "1640", "7 hours"),
            shift(LocalDate.of(2026, 10, 5), 6, 0, 18, 0, "2058", "11 hours"),
            shift(LocalDate.of(2026, 10, 6), 7, 0, 17, 0, "2058", "9 hours"),
            shift(LocalDate.of(2026, 10, 7), 7, 0, 17, 0, "2058", "9 hours"),
            shift(LocalDate.of(2026, 10, 8), 8, 0, 18, 0, "2058", "9 hours", meal = "1:00 p.m.–2:00 p.m."),
            shift(LocalDate.of(2026, 10, 9), 7, 0, 17, 0, "2058", "9 hours")
        )
        val daysOff = setOf(
            LocalDate.of(2026, 9, 26),
            LocalDate.of(2026, 9, 29),
            LocalDate.of(2026, 10, 3),
            LocalDate.of(2026, 10, 4)
        )
        return ParsedSchedule(
            shifts = shifts,
            daysOff = daysOff,
            unparsedLines = emptyList(),
            dayOffTypes = daysOff.associateWith { ShiftTemplateKind.DayOff }
        )
    }

    private fun shift(
        date: LocalDate,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        store: String,
        hours: String,
        meal: String? = null,
        warning: Boolean = false
    ): WorkShift {
        val notes = buildList {
            add("$hours. $ZONE.")
            if (meal != null) add("Meal $meal.")
            if (warning) add("Passport showed a warning on this shift.")
            add("Posted shift, not a repeating pattern.")
        }.joinToString(" ")
        return WorkShift(
            id = "passport-$date",
            date = date,
            start = LocalTime.of(startHour, startMinute),
            end = LocalTime.of(endHour, endMinute),
            label = ROLE,
            location = "Store #$store",
            notes = notes
        )
    }
}
