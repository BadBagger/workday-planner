package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PlannerDayPlanTest {
    @Test
    fun datedTasksAndEventsShareTheScheduleDay() {
        val day = LocalDate.of(2026, 7, 19)
        val state = AppState(
            tasks = listOf(
                TaskItem(
                    id = "all-day",
                    title = "Renew passport",
                    deadline = day.atTime(23, 59),
                    todoistRecurring = true,
                    todoistDueString = "every 2 weeks",
                    repeatRule = RepeatRule.None
                ),
                TaskItem(
                    id = "timed",
                    title = "Call hotel",
                    deadline = day.atTime(10, 0),
                    durationMinutes = 30,
                    repeatRule = RepeatRule.Weekly,
                    repeatDays = setOf(java.time.DayOfWeek.SUNDAY)
                ),
                TaskItem(id = "undated", title = "Someday", deadline = null),
                TaskItem(id = "other-day", title = "Later", deadline = day.plusDays(1).atTime(9, 0))
            ),
            events = listOf(
                WorkEvent(
                    id = "meeting",
                    title = "Train tickets",
                    startsAt = day.atTime(15, 0),
                    endsAt = day.atTime(16, 30)
                )
            )
        )

        val items = PlannerDayPlan.itemsFor(state, day)

        assertEquals(listOf("timed", "meeting", "all-day"), items.map { it.id })
        assertEquals(LocalTime.of(10, 0), items[0].start)
        assertEquals(LocalTime.of(10, 30), items[0].end)
        assertEquals("Every Sunday", items[0].repeats)
        assertEquals(LocalTime.of(15, 0), items[1].start)
        assertEquals(LocalTime.of(16, 30), items[1].end)
        assertEquals(PlannerDayKind.Event, items[1].kind)
        assertNull(items[2].start)
        assertEquals("every 2 weeks", items[2].repeats)
    }
}
