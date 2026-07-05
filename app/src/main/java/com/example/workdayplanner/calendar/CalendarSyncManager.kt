package com.example.workdayplanner.calendar

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.workdayplanner.data.WorkShift
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone

data class DeviceCalendar(
    val id: Long,
    val name: String,
    val accountName: String,
    val accountType: String
) {
    val displayName: String
        get() = if (accountName.isBlank()) name else "$name ($accountName)"
}

class CalendarSyncManager(private val context: Context) {
    fun loadWritableCalendars(): List<DeviceCalendar> {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE
        )
        val selection = "${CalendarContract.Calendars.VISIBLE}=1 AND ${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL}>=?"
        val args = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

        return context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            args,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC"
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        DeviceCalendar(
                            id = cursor.getLong(0),
                            name = cursor.getString(1).orEmpty(),
                            accountName = cursor.getString(2).orEmpty(),
                            accountType = cursor.getString(3).orEmpty()
                        )
                    )
                }
            }
        }.orEmpty()
    }

    fun syncWorkShifts(calendarId: Long, shifts: List<WorkShift>): Int {
        deleteExistingWorkdayEvents(calendarId)
        shifts.forEach { shift -> insertShift(calendarId, shift) }
        return shifts.size
    }

    private fun deleteExistingWorkdayEvents(calendarId: Long) {
        context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events.CALENDAR_ID}=? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf(calendarId.toString(), "%$EVENT_MARKER%")
        )
    }

    private fun insertShift(calendarId: Long, shift: WorkShift) {
        val zone = ZoneId.systemDefault()
        val startsAt = LocalDateTime.of(shift.date, shift.start)
        val endsAt = LocalDateTime.of(
            if (shift.end.isAfter(shift.start)) shift.date else shift.date.plusDays(1),
            shift.end
        )
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, "Work shift")
            put(CalendarContract.Events.DESCRIPTION, "Synced from Workday Planner\n$EVENT_MARKER${shift.id}")
            put(CalendarContract.Events.EVENT_LOCATION, shift.label.takeIf { it != "Work" }.orEmpty())
            put(CalendarContract.Events.DTSTART, startsAt.atZone(zone).toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, endsAt.atZone(zone).toInstant().toEpochMilli())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    companion object {
        private const val EVENT_MARKER = "WorkdayPlannerShift:"
    }
}
