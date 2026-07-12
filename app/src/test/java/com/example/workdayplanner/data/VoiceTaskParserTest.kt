package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class VoiceTaskParserTest {
    private val now = LocalDateTime.of(2026, 7, 10, 10, 0)
    private val zone = ZoneId.of("America/New_York")

    @Test
    fun parsesBoxMeatTruckOrderAt1130Am() {
        val result = VoiceTaskParser.parse("Box meat truck order at 11:30 a.m.", now, zone)

        assertEquals("Box meat truck order", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 11, 30), result.dueAt)
        assertEquals(LocalDateTime.of(2026, 7, 10, 11, 0), result.alarmAt)
        assertEquals(30, result.reminderOffsetMinutes)
        assertEquals(AlarmDelivery.SystemClockAlarm, result.toTask().alarmDelivery)
    }

    @Test
    fun parsesBakeryOrderTomorrowAtEight() {
        val result = VoiceTaskParser.parse("Bakery order tomorrow at 8", now, zone)

        assertEquals("Bakery order", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 11, 8, 0), result.dueAt)
        assertEquals(LocalDateTime.of(2026, 7, 11, 7, 30), result.alarmAt)
        assertTrue(result.ambiguityReasons.contains("AM/PM unclear"))
    }

    @Test
    fun parsesPullDatesFridayAtTwoPm() {
        val result = VoiceTaskParser.parse("Pull dates Friday at 2 PM", now, zone)

        assertEquals("Pull dates", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 14, 0), result.dueAt)
        assertEquals(LocalDateTime.of(2026, 7, 10, 13, 30), result.alarmAt)
    }

    @Test
    fun parsesCountInventoryIn45Minutes() {
        val result = VoiceTaskParser.parse("Count inventory in 45 minutes", now, zone)

        assertEquals("Count inventory", result.title)
        assertEquals(now.plusMinutes(45), result.dueAt)
        assertEquals(now.plusMinutes(15), result.alarmAt)
    }

    @Test
    fun parsesCleanSlicerAtNoon() {
        val result = VoiceTaskParser.parse("Clean slicer at noon", now, zone)

        assertEquals("Clean slicer", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 12, 0), result.dueAt)
    }

    @Test
    fun parsesFreezerCountAtMidnight() {
        val result = VoiceTaskParser.parse("Freezer count at midnight", now, zone)

        assertEquals("Freezer count", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 11, 0, 0), result.dueAt)
    }

    @Test
    fun parsesChickenOrderEveryMondayAtTen() {
        val result = VoiceTaskParser.parse("Chicken order every Monday at 10 AM", now, zone)

        assertEquals("Chicken order", result.title)
        assertEquals(RepeatRule.Weekly, result.repeatRule)
        assertEquals(setOf(DayOfWeek.MONDAY), result.repeatDays)
        assertEquals(LocalDate.of(2026, 7, 13), result.dueAt?.toLocalDate())
        assertEquals(LocalTime.of(10, 0), result.dueAt?.toLocalTime())
    }

    @Test
    fun parsesOneHourReminderBefore() {
        val result = VoiceTaskParser.parse("Truck order at 4, remind me one hour before", now, zone)

        assertEquals("Truck order", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 16, 0), result.dueAt)
        assertEquals(LocalDateTime.of(2026, 7, 10, 15, 0), result.alarmAt)
        assertEquals(60, result.reminderOffsetMinutes)
    }

    @Test
    fun parsesAlarmAtTheTime() {
        val result = VoiceTaskParser.parse("Prep list at 7, alarm at the time", now, zone)

        assertEquals("Prep list", result.title)
        assertEquals(result.dueAt, result.alarmAt)
        assertEquals(0, result.reminderOffsetMinutes)
    }

    @Test
    fun parsesNoAlarm() {
        val result = VoiceTaskParser.parse("Clean cooler, no alarm", now, zone)

        assertEquals("Clean cooler", result.title)
        assertEquals(ReminderType.None, result.reminderType)
        assertEquals(null, result.alarmAt)
    }

    @Test
    fun movesPassedExplicitTimeToTomorrow() {
        val result = VoiceTaskParser.parse("Check freezer at 8 AM", now, zone)

        assertEquals(LocalDateTime.of(2026, 7, 11, 8, 0), result.dueAt)
    }

    @Test
    fun handlesDaylightSavingTransition() {
        val dstNow = LocalDateTime.of(2026, 3, 8, 1, 30)
        val result = VoiceTaskParser.parse("Check case in two hours", dstNow, zone)

        assertEquals(LocalDateTime.of(2026, 3, 8, 3, 30), result.dueAt)
        assertEquals(zone.id, result.timeZoneId)
    }

    @Test
    fun parses12Am() {
        val result = VoiceTaskParser.parse("Freezer count at 12 AM", now, zone)

        assertEquals(LocalTime.MIDNIGHT, result.dueAt?.toLocalTime())
    }

    @Test
    fun parses12Pm() {
        val result = VoiceTaskParser.parse("Clean slicer at 12 PM", now, zone)

        assertEquals(LocalTime.NOON, result.dueAt?.toLocalTime())
    }

    @Test
    fun stripsFillerWords() {
        val result = VoiceTaskParser.parse("Um remind me to like complete the chicken order at 2 PM please", now, zone)

        assertEquals("Complete the chicken order", result.title)
    }

    @Test
    fun handlesRecognitionPunctuation() {
        val result = VoiceTaskParser.parse("Remind me to pull dates, Friday at 2 p.m.", now, zone)

        assertEquals("Pull dates", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 14, 0), result.dueAt)
    }

    @Test
    fun handlesEmptyRecognition() {
        val result = VoiceTaskParser.parse("", now, zone)

        assertEquals(0.0, result.confidence, 0.0)
        assertEquals(ReminderType.None, result.reminderType)
    }

    @Test
    fun savesInboxTaskWhenNoDateOrTime() {
        val result = VoiceTaskParser.parse("Clean cooler", now, zone)

        assertEquals("Clean cooler", result.title)
        assertEquals(null, result.dueAt)
        assertEquals(null, result.alarmAt)
        assertEquals(ReminderType.None, result.reminderType)
    }

    @Test
    fun flagsAmbiguousAmPm() {
        val result = VoiceTaskParser.parse("Prep list at 7", now, zone)

        assertTrue(result.ambiguityReasons.contains("AM/PM unclear"))
        assertEquals(LocalTime.of(19, 0), result.dueAt?.toLocalTime())
    }

    @Test
    fun preservesWorkplaceLanguageForTemperatureLog() {
        val result = VoiceTaskParser.parse("Temperature log in 20 minutes", now, zone)

        assertEquals("Temperature log", result.title)
        assertEquals(now.plusMinutes(20), result.dueAt)
    }

    @Test
    fun preservesCbtAndCoworkerTrainingLanguage() {
        val result = VoiceTaskParser.parse("CBT for Jason due Monday at 9 AM", now, zone)

        assertEquals("CBT for Jason", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 13, 9, 0), result.dueAt)
    }

    @Test
    fun parsesExplicitAlarmTime() {
        val result = VoiceTaskParser.parse("Do inventory at 4 PM, alarm at 3", now, zone)

        assertEquals("Do inventory", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 10, 16, 0), result.dueAt)
        assertEquals(LocalDateTime.of(2026, 7, 10, 15, 0), result.alarmAt)
        assertEquals(60, result.reminderOffsetMinutes)
    }

    @Test
    fun keepsCommaListAsOneTask() {
        val result = VoiceTaskParser.parse("Order ham, turkey, and roast beef at 10 AM", now, zone)

        assertEquals("Order ham turkey and roast beef", result.title)
        assertEquals(LocalDateTime.of(2026, 7, 11, 10, 0), result.dueAt)
    }
}
