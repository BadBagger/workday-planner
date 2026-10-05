package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalTime

data class WorkChecklistTemplate(
    val id: String,
    val title: String,
    val category: TaskCategory,
    val items: List<WorkChecklistTemplateItem>
)

data class WorkChecklistTemplateItem(
    val title: String,
    val notes: String = "",
    val category: TaskCategory? = null,
    val priority: TaskPriority = TaskPriority.Normal,
    val deadlineTime: LocalTime = LocalTime.of(17, 0)
)

object WorkChecklistTemplates {
    val all = listOf(
        WorkChecklistTemplate(
            id = "opening",
            title = "Opening",
            category = TaskCategory.Prep,
            items = listOf(
                item("Check schedule and priorities"),
                item("Walk department for urgent issues"),
                item("Set up workstation"),
                item("Check temps and equipment"),
                item("Review orders and low stock")
            )
        ),
        WorkChecklistTemplate(
            id = "closing",
            title = "Closing",
            category = TaskCategory.Cleaning,
            items = listOf(
                item("Finish customer-facing tasks"),
                item("Clean and sanitize station"),
                item("Pull out-of-date product"),
                item("Take trash and cardboard"),
                item("Leave handoff note for next shift")
            )
        ),
        WorkChecklistTemplate(
            id = "truck",
            title = "Truck / order day",
            category = TaskCategory.Orders,
            items = listOf(
                item("Check delivery against order"),
                item("Note shorts or damages"),
                item("Rotate older product forward"),
                item("Put away cold items first"),
                item("Update anything that needs follow-up")
            )
        ),
        WorkChecklistTemplate(
            id = "cleaning",
            title = "Cleaning",
            category = TaskCategory.Cleaning,
            items = listOf(
                item("Wipe high-touch surfaces"),
                item("Sweep and spot mop"),
                item("Clean cases and handles"),
                item("Restock supplies"),
                item("Check final appearance")
            )
        ),
        WorkChecklistTemplate(
            id = "before_leave",
            title = "Before I leave",
            category = TaskCategory.General,
            items = listOf(
                item("Check unfinished tasks"),
                item("Save any work notes"),
                item("Add proof photos if needed"),
                item("Confirm schedule for next shift"),
                item("Clock out")
            )
        ),
        WorkChecklistTemplate(
            id = "deli_daily_standards",
            title = "Deli daily standards",
            category = TaskCategory.General,
            items = listOf(
                item(
                    "Orders placed",
                    "Daily standard: all orders in on time and reviewed. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Orders,
                    TaskPriority.Critical,
                    LocalTime.of(10, 0)
                ),
                item(
                    "Counts complete",
                    "Daily standard: required counts entered. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Admin,
                    TaskPriority.Critical,
                    LocalTime.of(11, 0)
                ),
                item(
                    "Truck received",
                    "Daily standard: checked in and cold chain maintained. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Orders,
                    TaskPriority.High,
                    LocalTime.of(12, 0)
                ),
                item(
                    "Back stock worked",
                    "Daily standard: back stock to the floor before truck. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Orders,
                    TaskPriority.High,
                    LocalTime.of(13, 0)
                ),
                item(
                    "Truck worked",
                    "Daily standard: truck product stocked and faced. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Orders,
                    TaskPriority.High,
                    LocalTime.of(15, 0)
                ),
                item(
                    "Back room clear",
                    "Daily standard: nothing left that belongs on the floor. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Cleaning,
                    TaskPriority.High,
                    LocalTime.of(16, 0)
                ),
                item(
                    "Floor full and faced",
                    "Daily standard: customer-facing shelves and cases stocked. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.General,
                    TaskPriority.High,
                    LocalTime.of(16, 30)
                ),
                item(
                    "Production complete",
                    "Daily standard: production plan hit for the day. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Prep,
                    TaskPriority.High,
                    LocalTime.of(16, 30)
                ),
                item(
                    "Department clean and organized",
                    "Daily standard: cleaning tasks done and workstations reset. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Cleaning,
                    TaskPriority.High,
                    LocalTime.of(17, 0)
                ),
                item(
                    "Breaks and meals taken",
                    "Daily standard: everyone takes breaks/meals per policy, timed to the flow of business. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Admin,
                    TaskPriority.Critical,
                    LocalTime.of(17, 0)
                ),
                item(
                    "Safe close-out",
                    "Daily standard: slicer breakdown with correct PPE, including cut gloves on both hands. Capture owner, done time, initials, and reason/next owner if not done.",
                    TaskCategory.Cleaning,
                    TaskPriority.Critical,
                    LocalTime.of(17, 0)
                ),
                item(
                    "Stocking coverage handoff",
                    "Record: stocker scheduled, pulled from stocking, pulled to do what, who covered stocking, stocking finished by close, product left in back room, and notes for next shift.",
                    TaskCategory.Admin,
                    TaskPriority.High,
                    LocalTime.of(17, 0)
                )
            )
        ),
        WorkChecklistTemplate(
            id = "deli_working_agreements",
            title = "Deli working agreements",
            category = TaskCategory.Admin,
            items = listOf(
                item(
                    "Rank short-staffed priorities",
                    "Rank 1-8: orders/counts, truck worked, back stock worked, floor full and faced, production, cleaning/organization, resets/reorganizing projects, cross-training. Lowest rank is the agreed drop item.",
                    TaskCategory.Admin,
                    TaskPriority.High
                ),
                item(
                    "Define stocking pull rules",
                    "Agree when it is OK to pull the stocker and who covers stocking when that happens.",
                    TaskCategory.Admin,
                    TaskPriority.High
                ),
                item(
                    "Define break flex rule",
                    "Agree how much flex is allowed to finish with a customer while staying within policy.",
                    TaskCategory.Admin,
                    TaskPriority.High
                ),
                item(
                    "Define extra-hours approval",
                    "Agree when a willing part-timer can stay longer without overtime and who approves it.",
                    TaskCategory.Admin,
                    TaskPriority.High
                ),
                item(
                    "Plan cross-training changes",
                    "Agree who learns which role next and how the change gets introduced.",
                    TaskCategory.Admin,
                    TaskPriority.Normal
                ),
                item(
                    "Schedule weekly check-in",
                    "Pick the day and time for checking the standards together each week.",
                    TaskCategory.Admin,
                    TaskPriority.Normal
                )
            )
        ),
        WorkChecklistTemplate(
            id = "deli_two_week_tracker",
            title = "Deli two-week tracker",
            category = TaskCategory.Admin,
            items = listOf(
                item(
                    "Start two-week standards tracker",
                    "For 14 days, mark Y or N for: orders, counts, back stock worked, truck worked, back room clear, floor stocked, production, clean, breaks on time, stocker kept in role. Count total N per column and bring facts to the weekly check-in.",
                    TaskCategory.Admin,
                    TaskPriority.High
                )
            )
        )
    )

    fun tasksFor(templateId: String, date: LocalDate = LocalDate.now()): List<TaskItem> {
        val template = all.firstOrNull { it.id == templateId } ?: return emptyList()
        return template.items.map { checklistItem ->
            TaskItem(
                title = checklistItem.title,
                notes = checklistItem.notes,
                category = checklistItem.category ?: template.category,
                priority = checklistItem.priority,
                deadline = date.atTime(checklistItem.deadlineTime)
            )
        }
    }

    private fun item(
        title: String,
        notes: String = "",
        category: TaskCategory? = null,
        priority: TaskPriority = TaskPriority.Normal,
        deadlineTime: LocalTime = LocalTime.of(17, 0)
    ) = WorkChecklistTemplateItem(
        title = title,
        notes = notes,
        category = category,
        priority = priority,
        deadlineTime = deadlineTime
    )
}
