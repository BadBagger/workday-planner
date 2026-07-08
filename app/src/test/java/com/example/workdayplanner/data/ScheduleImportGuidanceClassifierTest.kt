package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleImportGuidanceClassifierTest {
    @Test
    fun blankOcrTextShowsNoTextDetected() {
        val guidance = ScheduleImportGuidanceClassifier.fromRecognizedText("")

        assertEquals(ScheduleImportIssue.NoTextDetected, guidance?.issue)
    }

    @Test
    fun datesWithoutTimesShowsSpecificGuidance() {
        val parsed = ParsedSchedule(shifts = emptyList(), daysOff = emptySet(), unparsedLines = listOf("Mon Jul 8 Fresh Kitchen"))

        val guidance = ScheduleImportGuidanceClassifier.fromParsedText("Mon Jul 8 Fresh Kitchen", parsed)

        assertEquals(ScheduleImportIssue.DatesDetectedTimesMissing, guidance?.issue)
    }

    @Test
    fun timesWithoutDatesShowsSpecificGuidance() {
        val parsed = ParsedSchedule(shifts = emptyList(), daysOff = emptySet(), unparsedLines = listOf("9:00 AM - 5:00 PM"))

        val guidance = ScheduleImportGuidanceClassifier.fromParsedText("9:00 AM - 5:00 PM", parsed)

        assertEquals(ScheduleImportIssue.TimesDetectedDatesMissing, guidance?.issue)
    }

    @Test
    fun parsedScheduleDoesNotShowGuidance() {
        val parsed = ParsedSchedule(
            shifts = listOf(WorkShift(date = java.time.LocalDate.of(2026, 7, 8), start = java.time.LocalTime.of(9, 0), end = java.time.LocalTime.of(17, 0))),
            daysOff = emptySet(),
            unparsedLines = emptyList()
        )

        val guidance = ScheduleImportGuidanceClassifier.fromParsedText("7/8 9:00 AM - 5:00 PM", parsed)

        assertNull(guidance)
    }
}
