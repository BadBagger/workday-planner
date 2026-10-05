package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TodoistSyncTest {
    private val zone = ZoneId.of("America/Chicago")

    @Test
    fun allDayRecurringMondayMapsOntoPlannerTask() {
        val task = TodoistSync.applyRemote(mondayTask(), null, zone)

        assertEquals("todoist-monday", task.id)
        assertEquals("Pack charger", task.title)
        assertEquals(LocalDateTime.of(2026, 7, 20, 23, 59), task.deadline)
        assertEquals(RepeatRule.Weekly, task.repeatRule)
        assertEquals(setOf(DayOfWeek.MONDAY), task.repeatDays)
        assertEquals(TaskPriority.Critical, task.priority)
        assertTrue(task.todoistRecurring)
        assertFalse(task.completed)
        assertEquals(ReminderType.None, task.reminderType)
        assertNull(task.alarmAt)
        assertFalse(task.skipDaysOff)
    }

    @Test
    fun timedDueDateUsesThePhoneZone() {
        val task = TodoistSync.applyRemote(
            TodoistTask(
                id = "timed",
                content = "Call hotel",
                priority = 3,
                due = TodoistDue(
                    date = LocalDate.of(2026, 7, 19),
                    dateTimeUtc = LocalDateTime.of(2026, 7, 19, 15, 0),
                    timezone = "America/Chicago",
                    isRecurring = false,
                    string = "Jul 19 10:00 AM"
                ),
                durationMinutes = 30
            ),
            null,
            zone
        )

        assertEquals(LocalDateTime.of(2026, 7, 19, 10, 0), task.deadline)
        assertEquals(30, task.durationMinutes)
        assertEquals(RepeatRule.None, task.repeatRule)
        assertEquals(TaskPriority.High, task.priority)
    }

    @Test
    fun unmappedRecurrenceStaysOnTheDueDateWithoutAFakeRule() {
        val task = TodoistSync.applyRemote(
            TodoistTask(
                id = "biweekly",
                content = "Water plants",
                due = TodoistDue(
                    date = LocalDate.of(2026, 7, 21),
                    dateTimeUtc = null,
                    timezone = null,
                    isRecurring = true,
                    string = "every 2 weeks"
                )
            ),
            null,
            zone
        )

        assertEquals(RepeatRule.None, task.repeatRule)
        assertTrue(task.todoistRecurring)
        assertFalse(TodoistSync.mapRepeat(taskDue("every 2 weeks", recurring = true)).clean)
        assertEquals(LocalDate.of(2026, 7, 21), task.deadline?.toLocalDate())
    }

    @Test
    fun weekdayAndCustomRepeatsMapCleanly() {
        assertEquals(RepeatRule.Weekdays, TodoistSync.mapRepeat(taskDue("every weekday", recurring = true)).rule)
        assertEquals(RepeatRule.Daily, TodoistSync.mapRepeat(taskDue("every day at 9:00", recurring = true)).rule)
        val custom = TodoistSync.mapRepeat(taskDue("every monday and friday", recurring = true))
        assertEquals(RepeatRule.CustomDays, custom.rule)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), custom.days)
    }

    @Test
    fun recurringDraftKeepsTheRuleInsteadOfASingleDate() {
        val draft = TodoistSync.draftFor(
            TaskItem(
                title = "Pack charger",
                deadline = LocalDateTime.of(2026, 7, 20, 9, 0),
                repeatRule = RepeatRule.Weekly,
                repeatDays = setOf(DayOfWeek.MONDAY)
            ),
            zone
        )

        assertEquals("every monday at 9:00 AM", draft.due.dueString)
        assertNull(draft.due.dueDate)
        assertNull(draft.due.dueDatetime)
    }

    @Test
    fun mergeKeepsLocalTasksAndAdvancesRemoteChanges() {
        val localOnly = TaskItem(id = "local", title = "Check slicer", deadline = LocalDateTime.of(2026, 7, 19, 8, 0))
        val linked = TaskItem(
            id = "todoist-monday",
            title = "Old title",
            todoistId = "monday",
            deadline = LocalDateTime.of(2026, 7, 13, 23, 59),
            completed = true
        )
        val pending = TaskItem(
            id = "new-local",
            title = "Buy tickets",
            todoistPending = TodoistPendingAction.Create
        )
        val finished = TaskItem(id = "gone", title = "Done in Todoist", todoistId = "gone", completed = false)
        val event = WorkEvent(
            id = "event-1",
            title = "Old meeting",
            startsAt = LocalDateTime.of(2026, 7, 19, 9, 0),
            endsAt = LocalDateTime.of(2026, 7, 19, 10, 0),
            todoistId = "meeting"
        )

        val merged = TodoistSync.merge(
            tasks = listOf(localOnly, linked, pending, finished),
            events = listOf(event),
            remote = listOf(
                mondayTask(),
                TodoistTask(
                    id = "meeting",
                    content = "Train tickets",
                    due = TodoistDue(
                        date = LocalDate.of(2026, 7, 19),
                        dateTimeUtc = LocalDateTime.of(2026, 7, 19, 15, 0),
                        timezone = "America/Chicago",
                        isRecurring = false,
                        string = "Jul 19 10:00 AM"
                    ),
                    durationMinutes = 90
                ),
                TodoistTask(id = "inbox", content = "Renew passport")
            ),
            zone = zone
        )

        assertEquals("Check slicer", merged.tasks.first { it.id == "local" }.title)
        assertEquals("Buy tickets", merged.tasks.first { it.id == "new-local" }.title)
        assertEquals(TodoistPendingAction.Create, merged.tasks.first { it.id == "new-local" }.todoistPending)
        val monday = merged.tasks.first { it.todoistId == "monday" }
        assertEquals("Pack charger", monday.title)
        assertFalse(monday.completed)
        assertTrue(merged.tasks.first { it.id == "gone" }.completed)
        assertEquals("Renew passport", merged.tasks.first { it.todoistId == "inbox" }.title)
        assertTrue(merged.tasks.none { it.todoistId == "meeting" })
        assertEquals("Train tickets", merged.events.single().title)
        assertEquals(LocalDateTime.of(2026, 7, 19, 10, 0), merged.events.single().startsAt)
        assertEquals(LocalDateTime.of(2026, 7, 19, 11, 30), merged.events.single().endsAt)
    }

    @Test
    fun summaryNamesFlorenceTripWithoutLimitingSync() {
        val message = TodoistSync.projectSummary(
            listOf(TodoistProject("1", "Inbox"), TodoistProject("2", "Florence trip")),
            4
        )
        assertEquals("Synced 4 open tasks from every Todoist project, including Florence trip.", message)
    }

    @Test
    fun shiftRepeatsStayLocal() {
        assertFalse(TodoistSync.canMirror(TaskItem(title = "Open case", repeatRule = RepeatRule.OpeningShifts)))
        assertTrue(TodoistSync.canMirror(TaskItem(title = "Call hotel", repeatRule = RepeatRule.Weekly)))
    }

    private fun mondayTask() = TodoistTask(
        id = "monday",
        content = "Pack charger",
        description = "Florence",
        projectId = "florence",
        priority = 4,
        due = TodoistDue(
            date = LocalDate.of(2026, 7, 20),
            dateTimeUtc = null,
            timezone = null,
            isRecurring = true,
            string = "every Monday"
        )
    )

    private fun taskDue(text: String, recurring: Boolean) = TodoistDue(
        date = LocalDate.of(2026, 7, 20),
        dateTimeUtc = null,
        timezone = null,
        isRecurring = recurring,
        string = text
    )
}
