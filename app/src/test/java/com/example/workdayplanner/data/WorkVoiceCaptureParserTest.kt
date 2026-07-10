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
    fun futureAiFormatterIsPlaceholderOnly() {
        assertFalse(FutureAiFormatter.isAvailable())
        assertNull(FutureAiFormatter.format("test", WorkVoiceCaptureType.ShiftNote))
    }
}
