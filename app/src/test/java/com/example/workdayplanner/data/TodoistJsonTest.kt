package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoistJsonTest {
    @Test
    fun parsesPaginatedTasksAndProjects() {
        val tasks = TodoistJson.parseTaskPage(
            """
            {
              "results": [
                {
                  "id": "task-1",
                  "content": "Book train",
                  "description": "Window seat",
                  "project_id": "proj-1",
                  "priority": 1,
                  "checked": false,
                  "due": {
                    "date": "2026-07-19",
                    "is_recurring": false,
                    "string": "Jul 19"
                  }
                }
              ],
              "next_cursor": "cursor-2"
            }
            """.trimIndent()
        )
        val projects = TodoistJson.parseProjectPage(
            """
            {"results":[{"id":"proj-1","name":"Florence trip"}],"next_cursor":null}
            """.trimIndent()
        )

        assertEquals("cursor-2", tasks.nextCursor)
        assertEquals("Book train", tasks.items.single().content)
        assertEquals("Window seat", tasks.items.single().description)
        assertEquals("Florence trip", projects.items.single().name)
        assertNull(projects.nextCursor)
    }

    @Test
    fun parsesTimedRecurringTask() {
        val task = TodoistJson.parseTask(
            """
            {
              "id": "task-2",
              "content": "Call hotel",
              "priority": 4,
              "duration": {"amount": 30, "unit": "minute"},
              "due": {
                "date": "2026-07-19",
                "datetime": "2026-07-19T15:00:00Z",
                "timezone": "America/Chicago",
                "is_recurring": true,
                "string": "every day at 10:00"
              }
            }
            """.trimIndent()
        )

        requireNotNull(task)
        assertEquals(java.time.LocalDateTime.of(2026, 7, 19, 15, 0), task.due?.dateTimeUtc)
        assertTrue(task.due?.isRecurring == true)
        assertEquals(30, task.durationMinutes)
        assertEquals(4, task.priority)
    }

    @Test
    fun taskBodyUsesDueStringForRepeats() {
        val body = TodoistJson.taskBody(
            TodoistTaskDraft(
                content = "Pack charger",
                description = "",
                priority = 1,
                due = TodoistDueDraft(dueString = "every monday")
            )
        )
        assertTrue(body.contains("\"due_string\":\"every monday\""))
        assertTrue(body.contains("\"due_lang\":\"en\""))
        assertTrue(!body.contains("due_date"))
    }
}
