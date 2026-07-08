package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskScheduleClassifierTest {
    private val shift = WorkShift(
        date = LocalDate.of(2026, 7, 7),
        start = LocalTime.of(9, 0),
        end = LocalTime.of(17, 0)
    )

    @Test
    fun labelsTasksAroundShift() {
        val state = AppState(shifts = listOf(shift), defaultDaysOff = emptySet())
        val now = LocalDateTime.of(2026, 7, 7, 8, 0)

        assertTrue(labelsAt(8, state, now).contains(TaskScheduleLabel.BeforeWork))
        assertTrue(labelsAt(12, state, now).contains(TaskScheduleLabel.DueDuringShift))
        assertTrue(labelsAt(18, state, now).contains(TaskScheduleLabel.AfterWork))
    }

    @Test
    fun warnsWhenAlarmFallsDuringShift() {
        val state = AppState(shifts = listOf(shift), defaultDaysOff = emptySet())
        val task = TaskItem(
            title = "Scan outs",
            deadline = LocalDateTime.of(2026, 7, 7, 18, 0),
            alarmAt = LocalDateTime.of(2026, 7, 7, 10, 0)
        )

        val insight = TaskScheduleClassifier.classify(task, state, LocalDateTime.of(2026, 7, 7, 8, 0))

        assertEquals("Reminder falls during a shift", insight.warning)
        assertTrue(insight.labels.contains(TaskScheduleLabel.AfterWork))
    }

    @Test
    fun labelsSkippedRepeatsOnDaysOff() {
        val state = AppState(daysOff = setOf(LocalDate.of(2026, 7, 8)), defaultDaysOff = emptySet())
        val task = TaskItem(
            title = "Cooler check",
            deadline = LocalDateTime.of(2026, 7, 8, 8, 0),
            repeatRule = RepeatRule.Daily,
            skipDaysOff = true
        )

        val insight = TaskScheduleClassifier.classify(task, state, LocalDateTime.of(2026, 7, 7, 8, 0))

        assertTrue(insight.labels.contains(TaskScheduleLabel.SkippedDayOff))
        assertTrue(insight.labels.contains(TaskScheduleLabel.RepeatsNextWorkday))
        assertEquals("Skipped because this is a day off.", TaskScheduleLabel.SkippedDayOff.label)
        assertEquals("Moved to your next workday: 2026-07-09.", insight.suggestion)
    }

    private fun labelsAt(hour: Int, state: AppState, now: LocalDateTime): List<TaskScheduleLabel> {
        return TaskScheduleClassifier.classify(
            TaskItem(title = "Task", deadline = LocalDateTime.of(2026, 7, 7, hour, 0)),
            state,
            now
        ).labels
    }
}
