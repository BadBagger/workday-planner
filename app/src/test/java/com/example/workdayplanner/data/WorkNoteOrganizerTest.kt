package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WorkNoteOrganizerTest {
    @Test
    fun detectsOrderNotesAndUsefulTags() {
        val note = WorkNoteOrganizer.create(
            text = "Frozen order short 2 cases from truck",
            date = LocalDate.of(2026, 7, 6)
        )

        assertEquals(WorkNoteKind.TruckNote, note.kind)
        assertTrue(note.tags.contains("Frozen"))
        assertTrue(note.tags.contains("Truck"))
    }

    @Test
    fun detectsFollowUpBeforeGenericIssueWhenExplicit() {
        val note = WorkNoteOrganizer.create("Remember to follow up next shift about missing labels")

        assertEquals(WorkNoteKind.ReminderNote, note.kind)
        assertTrue(note.tags.contains("Follow-up"))
    }

    @Test
    fun detectsCleaningNotes() {
        val note = WorkNoteOrganizer.create("Sanitize slicer and wipe case before close")

        assertEquals(WorkNoteKind.Cleaning, note.kind)
        assertTrue(note.tags.contains("Cleaning"))
    }

    @Test
    fun detectsManagerCustomerNotes() {
        val note = WorkNoteOrganizer.create("Manager said customer request needs follow up")

        assertEquals(WorkNoteKind.Customer, note.kind)
        assertTrue(note.tags.contains("Customer"))
        assertTrue(note.tags.contains("Manager"))
    }
}
