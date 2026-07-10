package com.example.workdayplanner.data

import java.time.LocalDate

object WorkNoteOrganizer {
    fun create(
        text: String,
        date: LocalDate = LocalDate.now(),
        title: String = "",
        kindOverride: WorkNoteKind? = null,
        linkedShiftId: String? = null,
        pinned: Boolean = false,
        rawTranscript: String = ""
    ): WorkNote {
        val cleaned = text.trim()
        val lower = cleaned.lowercase()
        val kind = kindOverride ?: detectKind(lower)
        return WorkNote(
            date = date,
            text = cleaned,
            rawTranscript = rawTranscript.trim(),
            title = title.trim().ifBlank { defaultTitle(cleaned, kind) },
            kind = kind,
            linkedShiftId = linkedShiftId,
            tags = detectTags(lower, kind)
                .plus(kind.label)
                .distinct()
                .filterNot { it == WorkNoteKind.General.label }
                .take(6),
            pinned = pinned
        )
    }

    private fun detectKind(text: String): WorkNoteKind = when {
        text.hasAny("customer", "complaint", "refund", "special request") -> WorkNoteKind.Customer
        text.hasAny("clean", "sanitize", "sweep", "mop", "trash", "wipe") -> WorkNoteKind.Cleaning
        text.hasAny("training", "cbt", "associate", "employee") -> WorkNoteKind.EmployeeTrainingNote
        text.hasAny("pay", "timecard", "punch", "lunch", "clock") -> WorkNoteKind.PayTimecardNote
        text.hasAny("remind", "remember", "follow up", "tomorrow", "check back") -> WorkNoteKind.ReminderNote
        text.hasAny("truck", "delivery", "load") -> WorkNoteKind.TruckNote
        text.hasAny("inventory", "count", "out of stock", "short", "missing") -> WorkNoteKind.InventoryNote
        text.hasAny("order", "boxmeat", "fresh kitchen", "frozen", "vendor") -> WorkNoteKind.OrderNote
        text.hasAny("handoff", "next shift", "got done", "still needs", "people notes") -> WorkNoteKind.ManagerHandoff
        text.hasAny("manager", "lead", "supervisor", "mic", "store manager") -> WorkNoteKind.Manager
        text.hasAny("follow up", "tomorrow", "next shift", "remember", "check back") -> WorkNoteKind.FollowUp
        text.hasAny("broken", "out of stock", "short", "missing", "late", "issue", "problem") -> WorkNoteKind.Issue
        text.hasAny("meeting", "huddle", "training", "walkthrough") -> WorkNoteKind.Meeting
        text.hasAny("shift", "open", "close", "mid") -> WorkNoteKind.ShiftNote
        else -> WorkNoteKind.General
    }

    private fun detectTags(text: String, kind: WorkNoteKind): List<String> {
        val tags = linkedSetOf(kind.label)
        keywordTags.forEach { (tag, keywords) ->
            if (keywords.any { text.contains(it) }) tags += tag
        }
        return tags.filterNot { it == WorkNoteKind.General.label }.take(5)
    }

    private fun defaultTitle(text: String, kind: WorkNoteKind): String {
        return text.lineSequence().firstOrNull()
            ?.take(48)
            ?.ifBlank { null }
            ?: kind.label
    }

    private fun String.hasAny(vararg keywords: String): Boolean {
        return keywords.any(::contains)
    }

    private val keywordTags = mapOf(
        "Deli" to listOf("deli", "sub", "slicer", "case", "fresh kitchen"),
        "Meat" to listOf("meat", "boxmeat", "chicken", "turkey", "ham"),
        "Frozen" to listOf("frozen", "freezer"),
        "Inventory" to listOf("inventory", "count", "out of stock", "short", "missing"),
        "Truck" to listOf("truck", "delivery", "load"),
        "Follow-up" to listOf("follow up", "tomorrow", "next shift", "check back", "remember"),
        "Customer" to listOf("customer", "complaint", "refund", "request"),
        "Manager" to listOf("manager", "lead", "supervisor", "mic"),
        "Training" to listOf("training", "cbt", "associate", "employee"),
        "Pay/timecard" to listOf("pay", "timecard", "punch", "clock", "lunch"),
        "Cleaning" to listOf("clean", "sanitize", "trash", "mop", "sweep", "wipe")
    )
}

object WorkNoteTemplates {
    val managerHandoff = """
        What got done:

        What still needs done:

        Issues:

        People notes:

        Product/order notes:

        Follow-up tasks:
    """.trimIndent()

    val orderNote = """
        Product:
        Quantity:
        Unit:
        Vendor/source:
        Needed by:
        Ordered status:
    """.trimIndent()
}
