package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DeliStandardsTest {
    @Test
    fun newDayStartsWithTheElevenObjectivesUnchecked() {
        val book = DeliStandardsBook.seed()
        val sheet = book.sheet(LocalDate.of(2026, 10, 5))

        assertEquals(11, sheet.lines.size)
        assertEquals("Orders placed", sheet.lines.first().objective)
        assertEquals("Safe close-out", sheet.lines.last().objective)
        assertTrue(sheet.lines.all { !it.done && it.owner.isBlank() && it.time.isBlank() && it.initials.isBlank() })
        assertEquals("", sheet.managerOnOpen)
        assertEquals("", sheet.handoff)
        assertEquals("", sheet.stockerScheduled)
    }

    @Test
    fun workingAgreementsStayBlank() {
        val book = DeliStandardsBook.seed()

        assertEquals(6, book.agreements.size)
        assertTrue(book.agreements.all { it.answer.isBlank() })
        assertEquals(8, book.agreements.first().choices.size)
        assertEquals("", book.departmentManager)
        assertEquals("", book.assistantManagerDate)
    }

    @Test
    fun trackerStartsTheWorkWeekAfterSundayOctober4() {
        val book = DeliStandardsBook.seed()

        assertEquals(14, book.tracker.size)
        assertEquals(LocalDate.of(2026, 10, 5), book.tracker.first().date)
        assertEquals(DayOfWeek.MONDAY, book.tracker.first().date.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 18), book.tracker.last().date)
        assertEquals(DayOfWeek.SUNDAY, book.tracker.last().date.dayOfWeek)
        assertTrue(book.tracker.all { it.marks.isEmpty() && it.notes.isBlank() })
        assertEquals(0, book.totalN("orders"))
    }

    @Test
    fun toDoCanCheckALineWithoutAddingObjectives() {
        val state = AppState()
        val result = ToDoPush.apply(
            state,
            ToDoCommands.fromFields(mapOf("kind" to "standards", "page" to "daily")),
            today = LocalDate.of(2026, 10, 5),
            fields = mapOf(
                "page" to "daily",
                "date" to "2026-10-05",
                "line" to "11",
                "done" to "yes",
                "initials" to "KH",
                "why" to ""
            )
        )
        val sheet = result.state.deliStandards.sheet(LocalDate.of(2026, 10, 5))

        assertEquals(11, sheet.lines.size)
        assertTrue(sheet.lines.last().done)
        assertEquals("KH", sheet.lines.last().initials)
        assertFalse(sheet.lines.first().done)
        assertTrue(result.state.deliStandards.agreements.all { it.answer.isBlank() })
    }

    @Test
    fun toDoRecordsATrackerMarkAndCountsN() {
        val updated = DeliStandardsPush.apply(
            DeliStandardsBook.seed(),
            mapOf("page" to "tracker", "date" to "2026-10-06", "column" to "counts", "mark" to "N")
        )

        assertEquals("N", updated.tracker.first { it.date == LocalDate.of(2026, 10, 6) }.marks["counts"])
        assertEquals(1, updated.totalN("counts"))
        assertEquals(0, updated.totalN("orders"))
    }
}
