package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class TaskRecurrenceTest {
    @Test
    fun customDaysRepeatsOnSelectedWeekdaysOnly() {
        val task = TaskItem(
            title = "Frozen Order",
            deadline = LocalDateTime.of(2026, 7, 6, 18, 30),
            alarmAt = LocalDateTime.of(2026, 7, 6, 17, 50),
            repeatRule = RepeatRule.CustomDays,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            skipDaysOff = false
        )

        val thursday = TaskRecurrence.nextOccurrence(task, AppState())!!
        val nextMonday = TaskRecurrence.nextOccurrence(thursday, AppState())!!

        assertEquals(DayOfWeek.THURSDAY, thursday.deadline!!.dayOfWeek)
        assertEquals("2026-07-09T18:30", thursday.deadline.toString())
        assertEquals("2026-07-09T17:50", thursday.alarmAt.toString())
        assertEquals(DayOfWeek.MONDAY, nextMonday.deadline!!.dayOfWeek)
        assertEquals("2026-07-13T18:30", nextMonday.deadline.toString())
    }

    @Test
    fun dailyRepeatSkipsExplicitDayOff() {
        val task = TaskItem(
            title = "Date check",
            deadline = LocalDateTime.of(2026, 7, 7, 8, 0),
            repeatRule = RepeatRule.Daily,
            skipDaysOff = true
        )
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 8)), defaultDaysOff = emptySet())

        val next = TaskRecurrence.nextOccurrence(task, state)!!

        assertEquals("2026-07-09T08:00", next.deadline.toString())
    }

    @Test
    fun dailyRepeatSkipsDefaultDayOff() {
        val task = TaskItem(
            title = "Check schedule",
            deadline = LocalDateTime.of(2026, 7, 10, 8, 0),
            repeatRule = RepeatRule.Daily,
            skipDaysOff = true
        )
        val state = AppState(defaultDaysOff = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))

        val next = TaskRecurrence.nextOccurrence(task, state)!!

        assertEquals("2026-07-13T08:00", next.deadline.toString())
    }
}
