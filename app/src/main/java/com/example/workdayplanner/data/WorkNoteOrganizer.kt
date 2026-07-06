package com.example.workdayplanner.data

import java.time.LocalDate

object WorkNoteOrganizer {
    fun create(text: String, date: LocalDate = LocalDate.now()): WorkNote {
        val cleaned = text.trim()
        val lower = cleaned.lowercase()
        val kind = detectKind(lower)
        return WorkNote(
            date = date,
            text = cleaned,
            kind = kind,
            tags = detectTags(lower, kind)
        )
    }

    private fun detectKind(text: String): WorkNoteKind = when {
        text.hasAny("order", "boxmeat", "fresh kitchen", "frozen", "truck", "delivery") -> WorkNoteKind.Order
        text.hasAny("clean", "sanitize", "sweep", "mop", "trash", "wipe") -> WorkNoteKind.Cleaning
        text.hasAny("customer", "complaint", "refund", "special request") -> WorkNoteKind.Customer
        text.hasAny("manager", "lead", "supervisor", "mic", "store manager") -> WorkNoteKind.Manager
        text.hasAny("follow up", "tomorrow", "next shift", "remember", "check back") -> WorkNoteKind.FollowUp
        text.hasAny("broken", "out of stock", "short", "missing", "late", "issue", "problem") -> WorkNoteKind.Issue
        text.hasAny("meeting", "huddle", "training", "walkthrough") -> WorkNoteKind.Meeting
        else -> WorkNoteKind.General
    }

    private fun detectTags(text: String, kind: WorkNoteKind): List<String> {
        val tags = linkedSetOf(kind.label)
        keywordTags.forEach { (tag, keywords) ->
            if (keywords.any { text.contains(it) }) tags += tag
        }
        return tags.filterNot { it == WorkNoteKind.General.label }.take(5)
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
        "Cleaning" to listOf("clean", "sanitize", "trash", "mop", "sweep", "wipe")
    )
}
