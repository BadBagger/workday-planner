package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkChecklistTemplatesTest {
    @Test
    fun createsTodayTasksForTemplate() {
        val date = LocalDate.of(2026, 7, 6)
        val tasks = WorkChecklistTemplates.tasksFor("closing", date)

        assertEquals(5, tasks.size)
        assertTrue(tasks.all { it.category == TaskCategory.Cleaning })
        assertTrue(tasks.all { it.deadline?.toLocalDate() == date })
    }

    @Test
    fun unknownTemplateCreatesNoTasks() {
        assertTrue(WorkChecklistTemplates.tasksFor("missing").isEmpty())
    }

    @Test
    fun deliDailyStandardsCreatesDocumentBackedTasks() {
        val date = LocalDate.of(2026, 10, 4)
        val tasks = WorkChecklistTemplates.tasksFor("deli_daily_standards", date)

        assertEquals(12, tasks.size)
        assertEquals("Orders placed", tasks.first().title)
        assertEquals(TaskCategory.Orders, tasks.first().category)
        assertEquals(TaskPriority.Critical, tasks.first().priority)
        assertEquals(date.atTime(10, 0), tasks.first().deadline)
        assertTrue(tasks.any { it.title == "Safe close-out" && it.notes.contains("cut gloves on both hands") })
        assertTrue(tasks.any { it.title == "Stocking coverage handoff" && it.notes.contains("stocker scheduled") })
    }

    @Test
    fun deliWorkingAgreementsCaptureRecurringJudgmentCalls() {
        val tasks = WorkChecklistTemplates.tasksFor("deli_working_agreements", LocalDate.of(2026, 10, 4))

        assertEquals(6, tasks.size)
        assertTrue(tasks.any { it.title == "Rank short-staffed priorities" && it.notes.contains("Rank 1-8") })
        assertTrue(tasks.any { it.title == "Define break flex rule" && it.notes.contains("within policy") })
        assertTrue(tasks.all { it.category == TaskCategory.Admin })
    }

    @Test
    fun deliTwoWeekTrackerIncludesYnMeasurementColumns() {
        val tasks = WorkChecklistTemplates.tasksFor("deli_two_week_tracker", LocalDate.of(2026, 10, 4))

        assertEquals(1, tasks.size)
        assertEquals("Start two-week standards tracker", tasks.single().title)
        assertTrue(tasks.single().notes.contains("14 days"))
        assertTrue(tasks.single().notes.contains("total N"))
        assertTrue(tasks.single().notes.contains("stocker kept in role"))
    }
}
