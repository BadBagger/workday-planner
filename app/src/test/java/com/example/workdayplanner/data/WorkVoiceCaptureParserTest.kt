package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkVoiceCaptureParserTest {
    @Test
    fun orderListFormatsSpokenGallonsAndInventoryChecks() {
        val result = WorkVoiceCaptureParser.format(
            rawTranscript = "Diet Coke five gallon, Root Beer two and a half, Coke Zero five gallon, check lemonade.",
            type = WorkVoiceCaptureType.OrderList
        )

        assertEquals("Order List", result.title)
        assertEquals(WorkNoteKind.OrderNote, result.kind)
        assertTrue(result.cleanedText.contains("Diet Coke - 5 gal"))
        assertTrue(result.cleanedText.contains("Root Beer - 2.5"))
        assertTrue(result.cleanedText.contains("Coke Zero - 5 gal"))
        assertTrue(result.cleanedText.contains("Check Lemonade inventory"))
    }

    @Test
    fun taskListSeparatesClosingTasksAndReminder() {
        val result = WorkVoiceCaptureParser.format(
            rawTranscript = "Closing tasks clean slicer, check dates, fill grab and go, sweep cooler, remind me to order chicken tomorrow.",
            type = WorkVoiceCaptureType.TaskList
        )

        assertEquals("Closing Tasks", result.title)
        assertEquals(WorkNoteKind.ShiftNote, result.kind)
        assertTrue(result.cleanedText.contains("- Clean slicer"))
        assertTrue(result.cleanedText.contains("- Check dates"))
        assertTrue(result.cleanedText.contains("- Fill grab-and-go"))
        assertTrue(result.cleanedText.contains("Reminder:"))
        assertTrue(result.cleanedText.contains("- Order chicken tomorrow"))
    }

    @Test
    fun taskListSplitsActionPhrasesJoinedByAnd() {
        val result = WorkVoiceCaptureParser.format(
            rawTranscript = "Opening tasks clean slicer and check dates and fill grab n go and follow up with night crew",
            type = WorkVoiceCaptureType.TaskList
        )

        assertEquals("Opening Tasks", result.title)
        assertTrue(result.cleanedText.contains("- Clean slicer"))
        assertTrue(result.cleanedText.contains("- Check dates"))
        assertTrue(result.cleanedText.contains("- Fill grab-and-go"))
        assertTrue(result.cleanedText.contains("Reminder:"))
        assertTrue(result.cleanedText.contains("- Follow up with night crew"))
    }

    @Test
    fun managerHandoffBuildsStructuredSections() {
        val result = WorkVoiceCaptureParser.format(
            rawTranscript = "Got done fresh slice case and truck shorts. Still needs done chicken order. Issues label printer jammed. People notes Jason needs CBT. Follow up tomorrow with bakery.",
            type = WorkVoiceCaptureType.ManagerHandoff
        )

        assertEquals("Manager Handoff", result.title)
        assertEquals(WorkNoteKind.ManagerHandoff, result.kind)
        assertTrue(result.cleanedText.contains("What got done:"))
        assertTrue(result.cleanedText.contains("- Fresh slice case"))
        assertTrue(result.cleanedText.contains("What still needs done:"))
        assertTrue(result.cleanedText.contains("- Chicken order"))
        assertTrue(result.cleanedText.contains("Issues:"))
        assertTrue(result.cleanedText.contains("- Label printer jammed"))
        assertTrue(result.cleanedText.contains("Follow-up tasks:"))
        assertTrue(result.cleanedText.contains("- Tomorrow with bakery"))
    }

    @Test
    fun shiftNoteNormalizesWorkplaceVocabulary() {
        val result = WorkVoiceCaptureParser.format(
            rawTranscript = "make a note check g g m and c b t list before filling grab n go",
            type = WorkVoiceCaptureType.ShiftNote
        )

        assertEquals("Shift Note", result.title)
        assertTrue(result.cleanedText.contains("GGM"))
        assertTrue(result.cleanedText.contains("CBT"))
        assertTrue(result.cleanedText.contains("grab-and-go"))
    }

    @Test
    fun futureAiFormatterIsPlaceholderOnly() {
        assertFalse(FutureAiFormatter.isAvailable())
        assertNull(FutureAiFormatter.format("test", WorkVoiceCaptureType.ShiftNote))
    }
}
