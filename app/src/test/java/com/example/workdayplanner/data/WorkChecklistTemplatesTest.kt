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
}
