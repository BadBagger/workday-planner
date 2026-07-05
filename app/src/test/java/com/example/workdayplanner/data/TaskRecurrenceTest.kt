package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
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
}
