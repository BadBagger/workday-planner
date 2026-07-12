package com.example.workdayplanner.ui

import com.example.workdayplanner.data.AppState
import com.example.workdayplanner.data.WorkShift
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class VoiceTaskShiftRelativeTest {
    @Test
    fun resolvesMondayAfterWorkToThatShiftEnd() {
        val monday = spokenDateForShiftRelativePhrase("meeting with bill byrd on monday after work")
        val state = AppState(
            shifts = listOf(
                WorkShift(
                    date = monday!!,
                    start = LocalTime.of(6, 0),
                    end = LocalTime.of(15, 0),
                    label = "Deli"
                )
            )
        )

        val parsed = parseVoiceTaskResults("Meeting with Bill Byrd on Monday after work", state).single()

        assertEquals("Meeting with Bill Byrd", parsed.title)
        assertEquals(LocalDateTime.of(monday, LocalTime.of(15, 0)), parsed.dueAt)
        assertEquals(LocalDateTime.of(monday, LocalTime.of(14, 30)), parsed.alarmAt)
    }

    @Test
    fun findsNextMondayFromSunday() {
        val date = spokenDateForShiftRelativePhrase("meeting on monday after work", LocalDate.of(2026, 7, 12))

        assertEquals(DayOfWeek.MONDAY, date?.dayOfWeek)
        assertEquals(LocalDate.of(2026, 7, 13), date)
    }
}
