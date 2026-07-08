package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalTime

object ShiftPatternGenerator {
    fun preview(pattern: ShiftPattern, daysToPreview: Int = 30): ShiftPatternPreview {
        if (pattern.cycleLength <= 0 || pattern.days.isEmpty()) {
            return ShiftPatternPreview(emptyList(), emptySet())
        }
        val shifts = mutableListOf<WorkShift>()
        val daysOff = mutableSetOf<LocalDate>()
        val dayMap = pattern.days.associateBy { it.index }
        repeat(daysToPreview.coerceAtLeast(0)) { offset ->
            val date = pattern.startDate.plusDays(offset.toLong())
            if (pattern.endDate != null && date.isAfter(pattern.endDate)) return@repeat
            val cycleIndex = offset % pattern.cycleLength
            val day = dayMap[cycleIndex] ?: ShiftPatternDay(index = cycleIndex)
            if (day.kind == ShiftPatternDayKind.Off) {
                daysOff += date
            } else {
                shifts += WorkShift(
                    date = date,
                    start = day.start,
                    end = day.end,
                    label = day.label.ifBlank { "Work" },
                    location = day.location,
                    notes = day.notes,
                    patternId = pattern.id
                )
            }
        }
        return ShiftPatternPreview(
            shifts = shifts.sortedWith(compareBy<WorkShift> { it.date }.thenBy { it.start }),
            daysOff = daysOff
        )
    }

    fun preset(name: String, startDate: LocalDate): ShiftPattern {
        return when (name) {
            "Standard weekdays" -> weeklyPreset(name, startDate, setOf(0, 1, 2, 3, 4), "Work", LocalTime.of(9, 0), LocalTime.of(17, 0))
            "Weekends only" -> weeklyPreset(name, startDate, setOf(5, 6), "Weekend shift", LocalTime.of(9, 0), LocalTime.of(17, 0))
            "4 on / 4 off" -> onOffPreset(name, startDate, workDays = 4, offDays = 4, label = "Work", start = LocalTime.of(7, 0), end = LocalTime.of(19, 0))
            "5 on / 2 off" -> onOffPreset(name, startDate, workDays = 5, offDays = 2, label = "Work", start = LocalTime.of(9, 0), end = LocalTime.of(17, 0))
            "5 day / 5 off / 5 night" -> {
                val days = (0 until 15).map { index ->
                    when (index) {
                        in 0..4 -> ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Work, label = "Day shift", start = LocalTime.of(7, 0), end = LocalTime.of(15, 0))
                        in 5..9 -> ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Off)
                        else -> ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Work, label = "Night shift", start = LocalTime.of(22, 0), end = LocalTime.of(6, 0))
                    }
                }
                ShiftPattern(name = name, startDate = startDate, cycleLength = 15, days = days)
            }
            else -> onOffPreset("Custom", startDate, workDays = 1, offDays = 1, label = "Work", start = LocalTime.of(9, 0), end = LocalTime.of(17, 0))
        }
    }

    private fun weeklyPreset(
        name: String,
        startDate: LocalDate,
        workIndexes: Set<Int>,
        label: String,
        start: LocalTime,
        end: LocalTime
    ): ShiftPattern {
        return ShiftPattern(
            name = name,
            startDate = startDate,
            cycleLength = 7,
            days = (0 until 7).map { index ->
                if (index in workIndexes) {
                    ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Work, label = label, start = start, end = end)
                } else {
                    ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Off)
                }
            }
        )
    }

    private fun onOffPreset(
        name: String,
        startDate: LocalDate,
        workDays: Int,
        offDays: Int,
        label: String,
        start: LocalTime,
        end: LocalTime
    ): ShiftPattern {
        val cycleLength = (workDays + offDays).coerceAtLeast(1)
        return ShiftPattern(
            name = name,
            startDate = startDate,
            cycleLength = cycleLength,
            days = (0 until cycleLength).map { index ->
                if (index < workDays) {
                    ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Work, label = label, start = start, end = end)
                } else {
                    ShiftPatternDay(index = index, kind = ShiftPatternDayKind.Off)
                }
            }
        )
    }
}
