package com.example.workdayplanner

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.workdayplanner.data.PlannerRepository
import java.time.LocalDate
import java.time.LocalDateTime

class WorkdayPlannerSummaryProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uri.authority != AUTHORITY || uri.lastPathSegment != PATH_SUMMARY) return null
        val appContext = context?.applicationContext ?: return emptyCursor()
        val state = PlannerRepository(appContext).state.value
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val incompleteTasks = state.tasks.filterNot { it.completed }
        val dueTasks = incompleteTasks.filter { task -> task.deadline?.let { it <= now } == true }
        val upcomingTasks = incompleteTasks
            .sortedWith(compareBy(nullsLast()) { it.deadline })
            .take(3)
            .map { it.title.ifBlank { "Untitled task" } }
        val upcomingShifts = state.shifts
            .filter { it.date >= today }
            .sortedWith(compareBy({ it.date }, { it.start }))
            .take(2)
        val pinnedNotes = state.notes.count { it.pinned && !it.archived }
        val status = when {
            dueTasks.isEmpty() && upcomingShifts.isEmpty() -> "No urgent work items right now"
            dueTasks.isEmpty() -> "Work schedule is ready"
            dueTasks.size <= 3 -> "Small work task queue ready"
            else -> "Work task queue needs attention"
        }
        val alert = when {
            dueTasks.size > 5 -> "${dueTasks.size} work tasks are due. Pick the next important one first."
            upcomingShifts.firstOrNull()?.date == today -> "A work shift is on today's schedule."
            else -> ""
        }
        val keyInfo = listOf(
            "${incompleteTasks.size} open tasks",
            "${dueTasks.size} due",
            "${upcomingShifts.size} upcoming shifts"
        ).joinToString(", ")
        val counts = listOf(
            "${incompleteTasks.size} open",
            "${dueTasks.size} due",
            "${state.shifts.size} shifts",
            "$pinnedNotes pinned notes"
        ).joinToString("|")
        val dueSoon = when {
            upcomingTasks.isNotEmpty() -> upcomingTasks.joinToString("|")
            upcomingShifts.isNotEmpty() -> upcomingShifts.joinToString("|") { "${it.date} ${it.label}" }
            else -> "No work items due"
        }
        return MatrixCursor(COLUMNS).apply {
            addRow(
                arrayOf(
                    APP_ID,
                    status,
                    keyInfo,
                    alert,
                    counts,
                    dueSoon,
                    "just now",
                    "Workday Planner summary provider"
                )
            )
        }
    }

    override fun getType(uri: Uri): String? = if (uri.authority == AUTHORITY && uri.lastPathSegment == PATH_SUMMARY) {
        "vnd.android.cursor.item/vnd.smithware.workdayplanner.summary"
    } else {
        null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    private fun emptyCursor(): Cursor = MatrixCursor(COLUMNS)

    companion object {
        private const val AUTHORITY = "com.smithware.workdayplanner.summary"
        private const val PATH_SUMMARY = "summary"
        private const val APP_ID = "workday_planner"
        private val COLUMNS = arrayOf(
            "app_id",
            "status",
            "key_info",
            "alert",
            "counts",
            "due_soon",
            "last_updated",
            "source"
        )
    }
}
