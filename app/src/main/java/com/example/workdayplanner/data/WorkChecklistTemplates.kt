package com.example.workdayplanner.data

import java.time.LocalDate

data class WorkChecklistTemplate(
    val id: String,
    val title: String,
    val category: TaskCategory,
    val items: List<String>
)

object WorkChecklistTemplates {
    val all = listOf(
        WorkChecklistTemplate(
            id = "opening",
            title = "Opening",
            category = TaskCategory.Prep,
            items = listOf(
                "Check schedule and priorities",
                "Walk department for urgent issues",
                "Set up workstation",
                "Check temps and equipment",
                "Review orders and low stock"
            )
        ),
        WorkChecklistTemplate(
            id = "closing",
            title = "Closing",
            category = TaskCategory.Cleaning,
            items = listOf(
                "Finish customer-facing tasks",
                "Clean and sanitize station",
                "Pull out-of-date product",
                "Take trash and cardboard",
                "Leave handoff note for next shift"
            )
        ),
        WorkChecklistTemplate(
            id = "truck",
            title = "Truck / order day",
            category = TaskCategory.Orders,
            items = listOf(
                "Check delivery against order",
                "Note shorts or damages",
                "Rotate older product forward",
                "Put away cold items first",
                "Update anything that needs follow-up"
            )
        ),
        WorkChecklistTemplate(
            id = "cleaning",
            title = "Cleaning",
            category = TaskCategory.Cleaning,
            items = listOf(
                "Wipe high-touch surfaces",
                "Sweep and spot mop",
                "Clean cases and handles",
                "Restock supplies",
                "Check final appearance"
            )
        ),
        WorkChecklistTemplate(
            id = "before_leave",
            title = "Before I leave",
            category = TaskCategory.General,
            items = listOf(
                "Check unfinished tasks",
                "Save any work notes",
                "Add proof photos if needed",
                "Confirm schedule for next shift",
                "Clock out"
            )
        )
    )

    fun tasksFor(templateId: String, date: LocalDate = LocalDate.now()): List<TaskItem> {
        val template = all.firstOrNull { it.id == templateId } ?: return emptyList()
        return template.items.map { title ->
            TaskItem(
                title = title,
                category = template.category,
                deadline = date.atTime(17, 0)
            )
        }
    }
}
