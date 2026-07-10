package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleTextParserTest {
    @Test
    fun parsesShiftsAndDaysOff() {
        val parsed = ScheduleTextParser.parse(
            """
            7/6 9:00 AM - 5:30 PM
            7/7 OFF
            hello
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(1, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 6), parsed.shifts.first().date)
        assertEquals(LocalTime.of(9, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(17, 30), parsed.shifts.first().end)
        assertTrue(LocalDate.of(2026, 7, 7) in parsed.daysOff)
        assertEquals(listOf("hello"), parsed.unparsedLines)
    }

    @Test
    fun parsesPublixScheduleCardOcr() {
        val parsed = ScheduleTextParser.parse(
            """
            Schedule
            Net hours: 45.5
            7/11/2026
            Sat
            11
            6 a.m. - 5 p.m.
            Asst. Deli Manager
            Store #1640
            10.25 hours
            Sun
            12
            Not Scheduled
            Mon
            13
            6 a.m. - 2 p.m.
            Tue
            14
            Not Scheduled
            Wed
            15
            6a.m. -5 p.m.
            Thu
            16
            1 p.m. - 10:30 p.m.
            Fri
            17
            10 a.m. - 7 p.m.
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(5, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 11), parsed.shifts[0].date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts[0].start)
        assertEquals(LocalTime.of(17, 0), parsed.shifts[0].end)
        assertEquals(LocalDate.of(2026, 7, 13), parsed.shifts[1].date)
        assertEquals(LocalTime.of(14, 0), parsed.shifts[1].end)
        assertEquals(LocalDate.of(2026, 7, 16), parsed.shifts[3].date)
        assertEquals(LocalTime.of(22, 30), parsed.shifts[3].end)
        assertTrue(LocalDate.of(2026, 7, 12) in parsed.daysOff)
        assertTrue(LocalDate.of(2026, 7, 14) in parsed.daysOff)
    }

    @Test
    fun fallsBackToScheduleOrderWhenOcrDropsDayLabels() {
        val parsed = ScheduleTextParser.parse(
            """
            Asst. Deli Manager
            Store #1640
            8.5 hours
            Not Scheduled
            6a.m. -5 p.m.
            Asst. Deli Manager
            Store #1640
            10.25 hours
            1 p.m. - 10:30 p.m.
            Asst. Deli Manager
            Store #1640
            9 hours
            9 a.m. -7 p.m.
            Asst. Deli Manager
            Store #1640
            9.5 hours
            268
            64
            MENU
            7/4/2026 4
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(3, parsed.shifts.size)
        assertTrue(LocalDate.of(2026, 7, 4) in parsed.daysOff)
        assertEquals(LocalDate.of(2026, 7, 5), parsed.shifts[0].date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts[0].start)
        assertEquals(LocalTime.of(17, 0), parsed.shifts[0].end)
        assertEquals(LocalDate.of(2026, 7, 6), parsed.shifts[1].date)
        assertEquals(LocalTime.of(22, 30), parsed.shifts[1].end)
        assertEquals(LocalDate.of(2026, 7, 7), parsed.shifts[2].date)
    }

    @Test
    fun pairsColumnOrderedDayCardsWithScheduleRows() {
        val parsed = ScheduleTextParser.parse(
            """
            Schedule
            Net hours: 45.25
            7/4/2026
            Sat
            4
            Sun
            5
            Mon
            6
            Tue
            7
            Wed
            8
            Thu
            9
            Fri
            10
            11 a.m. -7:30 p.m.
            Asst. Deli Manager
            Store #1640
            8 hours
            Not Scheduled
            6 a.m. - 3 p.m.
            Asst. Deli Manager
            Store #1640
            8.5 hours
            Not Scheduled
            6 a.m. - 5 p.m.
            1 p.m. - 10:30 p.m.
            9 a.m. -7 p.m.
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(5, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 4), parsed.shifts[0].date)
        assertEquals(LocalDate.of(2026, 7, 6), parsed.shifts[1].date)
        assertEquals(LocalDate.of(2026, 7, 8), parsed.shifts[2].date)
        assertEquals(LocalDate.of(2026, 7, 9), parsed.shifts[3].date)
        assertEquals(LocalDate.of(2026, 7, 10), parsed.shifts[4].date)
        assertTrue(LocalDate.of(2026, 7, 5) in parsed.daysOff)
        assertTrue(LocalDate.of(2026, 7, 7) in parsed.daysOff)
    }

    @Test
    fun parsesDayLineWhenDateAndStartHourAreSameNumber() {
        val parsed = ScheduleTextParser.parse(
            """
            7/4/2026
            Mon 6 a.m. - 3 p.m.
            6 Asst. Deli Manager
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(1, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 6), parsed.shifts.first().date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(15, 0), parsed.shifts.first().end)
    }

    @Test
    fun pairsDateFromFollowingJobDetailLineWhenHourFollowsWeekday() {
        val parsed = ScheduleTextParser.parse(
            """
            7/4/2026
            Wed 6a.m. -5 p.m.
            8 Asst. Deli Manager
            Store #1640
            Thu 1 p.m. - 10:30 p.m.
            9 Asst. Deli Manager
            Store #1640
            Fri 9 a.m. -7 p.m.
            10 Asst. Deli Manager
            Store #1640
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(3, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 8), parsed.shifts[0].date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts[0].start)
        assertEquals(LocalTime.of(17, 0), parsed.shifts[0].end)
        assertEquals(LocalDate.of(2026, 7, 9), parsed.shifts[1].date)
        assertEquals(LocalTime.of(13, 0), parsed.shifts[1].start)
        assertEquals(LocalTime.of(22, 30), parsed.shifts[1].end)
        assertEquals(LocalDate.of(2026, 7, 10), parsed.shifts[2].date)
        assertEquals(LocalTime.of(9, 0), parsed.shifts[2].start)
        assertEquals(LocalTime.of(19, 0), parsed.shifts[2].end)
    }

    @Test
    fun infersDateFromWeekdayWhenOcrDropsDateNumber() {
        val parsed = ScheduleTextParser.parse(
            """
            7/4/2026
            Tue
            7 Not Scheduled
            Wed 6a.m. -5 p.m.
            Asst. Deli Manager
            Store #1640
            Thu 1 p.m. - 10:30 p.m.
            9 Asst. Deli Manager
            Fri 9 a.m. -7 p.m.
            10 Asst. Deli Manager
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(3, parsed.shifts.size)
        assertTrue(LocalDate.of(2026, 7, 7) in parsed.daysOff)
        assertEquals(LocalDate.of(2026, 7, 8), parsed.shifts[0].date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts[0].start)
        assertEquals(LocalTime.of(17, 0), parsed.shifts[0].end)
        assertEquals(LocalDate.of(2026, 7, 9), parsed.shifts[1].date)
        assertEquals(LocalDate.of(2026, 7, 10), parsed.shifts[2].date)
    }

    @Test
    fun repairsSplitElevenAmWithoutTreatingFirstDigitAsDate() {
        val parsed = ScheduleTextParser.parse(
            """
            7/4/2026
            Sat 1 1 a.m. -7:30 p.m.
            Asst. Deli Manager
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(1, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 4), parsed.shifts.first().date)
        assertEquals(LocalTime.of(11, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(19, 30), parsed.shifts.first().end)
    }

    @Test
    fun parsesPublixGreatPeopleScheduleWithoutFullDateHeader() {
        val parsed = ScheduleTextParser.parse(
            """
            Sat
            11
            Not Scheduled

            Sun
            12
            Not Scheduled

            Mon 6 a.m. - 2:30 p.m.
            13 Deli Clerk
            Store #1640
            8 hours

            Tue 6 a.m. - 2 p.m.
            14 Deli Clerk
            Store #1640
            7.5 hours

            Wed 7 a.m. - 3 p.m.
            15 Deli Clerk
            Store #1640
            75 hours

            Thu 2 p.m. - 10:30 p.m.
            16 Deli Clerk
            Store #1640
            8 hours

            Fri 2 p.m. - 10:30 p.m.
            17 Deli Clerk
            Store #1640
            8 hours
            great
            """.trimIndent(),
            currentYear = 2026
        )

        assertEquals(5, parsed.shifts.size)
        assertEquals(2, parsed.daysOff.size)
        assertTrue(LocalDate.of(2026, 7, 11) in parsed.daysOff)
        assertTrue(LocalDate.of(2026, 7, 12) in parsed.daysOff)
        assertEquals(LocalDate.of(2026, 7, 13), parsed.shifts[0].date)
        assertEquals(LocalTime.of(6, 0), parsed.shifts[0].start)
        assertEquals(LocalTime.of(14, 30), parsed.shifts[0].end)
        assertEquals("Deli Clerk", parsed.shifts[0].label)
        assertEquals("Store #1640", parsed.shifts[0].location)
        assertEquals(LocalDate.of(2026, 7, 14), parsed.shifts[1].date)
        assertEquals(LocalTime.of(14, 0), parsed.shifts[1].end)
        assertEquals(LocalDate.of(2026, 7, 15), parsed.shifts[2].date)
        assertEquals(LocalTime.of(7, 0), parsed.shifts[2].start)
        assertEquals(LocalTime.of(15, 0), parsed.shifts[2].end)
        assertEquals(LocalDate.of(2026, 7, 16), parsed.shifts[3].date)
        assertEquals(LocalTime.of(22, 30), parsed.shifts[3].end)
        assertEquals(LocalDate.of(2026, 7, 17), parsed.shifts[4].date)
    }

    @Test
    fun parsesCompactWeekdayShift() {
        val parsed = ScheduleTextParser.parse("Mon 8-4", currentYear = 2026)

        assertEquals(1, parsed.shifts.size)
        assertEquals(DayOfWeek.MONDAY, parsed.shifts.first().date.dayOfWeek)
        assertEquals(LocalTime.of(8, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(16, 0), parsed.shifts.first().end)
    }

    @Test
    fun parsesFullWeekdayShiftWithMinutes() {
        val parsed = ScheduleTextParser.parse("Tuesday 2:00 PM - 10:30 PM", currentYear = 2026)

        assertEquals(1, parsed.shifts.size)
        assertEquals(DayOfWeek.TUESDAY, parsed.shifts.first().date.dayOfWeek)
        assertEquals(LocalTime.of(14, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(22, 30), parsed.shifts.first().end)
    }

    @Test
    fun parsesWeekdayOff() {
        val parsed = ScheduleTextParser.parse("Wed OFF", currentYear = 2026)

        assertEquals(1, parsed.daysOff.size)
        assertEquals(DayOfWeek.WEDNESDAY, parsed.daysOff.first().dayOfWeek)
    }

    @Test
    fun parsesDatedOpenShiftWithCompactPeriods() {
        val parsed = ScheduleTextParser.parse("7/12 Open 6a-2p", currentYear = 2026)

        assertEquals(1, parsed.shifts.size)
        assertEquals(LocalDate.of(2026, 7, 12), parsed.shifts.first().date)
        assertEquals("Open", parsed.shifts.first().label)
        assertEquals(LocalTime.of(6, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(14, 0), parsed.shifts.first().end)
    }

    @Test
    fun parsesCloseShiftWithCompactPmPeriods() {
        val parsed = ScheduleTextParser.parse("Fri Close 1pm-9pm", currentYear = 2026)

        assertEquals(1, parsed.shifts.size)
        assertEquals(DayOfWeek.FRIDAY, parsed.shifts.first().date.dayOfWeek)
        assertEquals("Close", parsed.shifts.first().label)
        assertEquals(LocalTime.of(13, 0), parsed.shifts.first().start)
        assertEquals(LocalTime.of(21, 0), parsed.shifts.first().end)
    }

    @Test
    fun preservesVacationAndSickDayTypes() {
        val parsed = ScheduleTextParser.parse(
            """
            Mon Vacation
            Tue Sick
            """.trimIndent(),
            currentYear = 2026
        )

        val vacationDate = parsed.daysOff.first { it.dayOfWeek == DayOfWeek.MONDAY }
        val sickDate = parsed.daysOff.first { it.dayOfWeek == DayOfWeek.TUESDAY }

        assertEquals(ShiftTemplateKind.Vacation, parsed.dayOffTypes[vacationDate])
        assertEquals(ShiftTemplateKind.Sick, parsed.dayOffTypes[sickDate])
    }

    @Test
    fun separatesShiftTypeRoleAndStore() {
        val parsed = ScheduleTextParser.parse(
            """
            7/12 Inventory 6a-2p
            12 Assistant Deli Manager
            Store #1640
            """.trimIndent(),
            currentYear = 2026
        )

        val shift = parsed.shifts.first()

        assertEquals("Inventory", shift.label)
        assertEquals("Assistant Deli Manager", shift.notes)
        assertEquals("Store #1640", shift.location)
    }
}
