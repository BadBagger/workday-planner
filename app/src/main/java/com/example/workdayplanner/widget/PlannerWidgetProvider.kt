package com.example.workdayplanner.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.workdayplanner.MainActivity
import com.example.workdayplanner.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PlannerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildViews(context))
        }
        PlannerWidgetUpdater.scheduleNextDateRefresh(context)
    }

    override fun onEnabled(context: Context) {
        PlannerWidgetUpdater.scheduleNextDateRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_TASK) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            WidgetStore.toggleTask(context, taskId)
            PlannerWidgetUpdater.updateAll(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.example.workdayplanner.widget.TOGGLE_TASK"
        const val EXTRA_TASK_ID = "task_id"
    }
}

class WorkScheduleWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildScheduleViews(context, widgetId, appWidgetManager))
        }
        PlannerWidgetUpdater.scheduleNextDateRefresh(context)
    }

    override fun onEnabled(context: Context) {
        PlannerWidgetUpdater.scheduleNextDateRefresh(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        appWidgetManager.updateAppWidget(appWidgetId, buildScheduleViews(context, appWidgetId, appWidgetManager))
    }
}

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PlannerWidgetUpdater.updateAll(context)
    }
}

object PlannerWidgetUpdater {
    private const val ACTION_REFRESH_WIDGETS = "com.example.workdayplanner.widget.REFRESH_WIDGETS"
    private const val MIDNIGHT_REFRESH_REQUEST = 7301

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val plannerIds = manager.getAppWidgetIds(ComponentName(context, PlannerWidgetProvider::class.java))
        plannerIds.forEach { manager.updateAppWidget(it, buildViews(context)) }
        val scheduleIds = manager.getAppWidgetIds(ComponentName(context, WorkScheduleWidgetProvider::class.java))
        scheduleIds.forEach { manager.updateAppWidget(it, buildScheduleViews(context, it, manager)) }
        if (plannerIds.isNotEmpty() || scheduleIds.isNotEmpty()) {
            scheduleNextDateRefresh(context)
        }
    }

    fun scheduleNextDateRefresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val hasWidgets =
            manager.getAppWidgetIds(ComponentName(context, PlannerWidgetProvider::class.java)).isNotEmpty() ||
                manager.getAppWidgetIds(ComponentName(context, WorkScheduleWidgetProvider::class.java)).isNotEmpty()
        if (!hasWidgets) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = refreshPendingIntent(context)
        val triggerAtMillis = LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .plusMinutes(1)
            .toInstant()
            .toEpochMilli()
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC, triggerAtMillis, pendingIntent)
        }
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetRefreshReceiver::class.java).setAction(ACTION_REFRESH_WIDGETS)
        return PendingIntent.getBroadcast(
            context,
            MIDNIGHT_REFRESH_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

private fun buildViews(context: Context): RemoteViews {
    val state = WidgetStore.load(context)
    val views = RemoteViews(context.packageName, R.layout.widget_planner)
    val palette = WidgetPalette.from(state)
    val launchIntent = Intent(context, MainActivity::class.java)
    val launchPendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_header, launchPendingIntent)
    views.setOnClickPendingIntent(R.id.widget_logo, launchPendingIntent)
    views.setOnClickPendingIntent(R.id.widget_title, launchPendingIntent)
    views.setOnClickPendingIntent(R.id.widget_shift, launchPendingIntent)
    views.setInt(R.id.widget_root, "setBackgroundColor", palette.background)
    views.setTextColor(R.id.widget_title, palette.title)
    views.setTextColor(R.id.widget_shift, palette.accent)
    views.setTextColor(R.id.widget_task_1, palette.body)
    views.setTextColor(R.id.widget_task_2, palette.body)
    views.setTextColor(R.id.widget_task_3, palette.body)
    views.setTextColor(R.id.widget_task_box_1, palette.accent)
    views.setTextColor(R.id.widget_task_box_2, palette.accent)
    views.setTextColor(R.id.widget_task_box_3, palette.accent)

    val nextShift = state.shifts
        .filter { !it.date.isBefore(LocalDate.now()) }
        .minWithOrNull(compareBy<WidgetShift> { it.date }.thenBy { it.start })
    views.setTextViewText(
        R.id.widget_shift,
        nextShift?.let { "${it.date.format(widgetDate)} ${it.start.format(widgetTime)} - ${it.end.format(widgetTime)}" }
            ?: "No upcoming shift"
    )

    val taskLimit = when (state.widgetLayoutMode) {
        WidgetLayoutMode.Compact -> 1
        WidgetLayoutMode.Standard -> 3
        WidgetLayoutMode.Detailed -> 2
    }
    val tasks = state.tasks.filterNot { it.completed }.take(taskLimit)
    val latestNote = state.notes.maxByOrNull { it.createdAt }
    val rows = listOf(
        TaskRow(R.id.widget_task_row_1, R.id.widget_task_box_1, R.id.widget_task_1),
        TaskRow(R.id.widget_task_row_2, R.id.widget_task_box_2, R.id.widget_task_2),
        TaskRow(R.id.widget_task_row_3, R.id.widget_task_box_3, R.id.widget_task_3)
    )
    if (tasks.isEmpty() && (state.widgetLayoutMode != WidgetLayoutMode.Detailed || latestNote == null)) {
        val firstRow = rows.first()
        views.setViewVisibility(firstRow.containerId, View.VISIBLE)
        views.setTextViewText(firstRow.boxId, "\u2713")
        views.setTextViewText(firstRow.titleId, "All tasks clear")
        views.setTextColor(firstRow.boxId, palette.success)
        views.setOnClickPendingIntent(firstRow.containerId, launchPendingIntent)
        views.setOnClickPendingIntent(firstRow.boxId, launchPendingIntent)
        views.setOnClickPendingIntent(firstRow.titleId, launchPendingIntent)
        rows.drop(1).forEach { row ->
            views.setViewVisibility(row.containerId, View.GONE)
        }
    } else {
        rows.forEachIndexed { index, row ->
            val task = tasks.getOrNull(index)
            if (task == null && state.widgetLayoutMode == WidgetLayoutMode.Detailed && index == 2 && latestNote != null) {
                views.setViewVisibility(row.containerId, View.VISIBLE)
                views.setTextViewText(row.boxId, "\u2022")
                views.setTextColor(row.boxId, palette.accent)
                views.setTextViewText(row.titleId, latestNote.toWidgetText())
                views.setOnClickPendingIntent(row.containerId, launchPendingIntent)
                views.setOnClickPendingIntent(row.boxId, launchPendingIntent)
                views.setOnClickPendingIntent(row.titleId, launchPendingIntent)
            } else if (task == null) {
                views.setViewVisibility(row.containerId, View.GONE)
            } else {
                views.setViewVisibility(row.containerId, View.VISIBLE)
                views.setTextViewText(row.boxId, "\u2610")
                views.setTextColor(row.boxId, palette.accent)
                views.setTextViewText(row.titleId, task.title)
            }
        }
    }
    rows.forEachIndexed { index, row ->
        tasks.getOrNull(index)?.let { task ->
            val intent = Intent(context, PlannerWidgetProvider::class.java)
                .setAction(PlannerWidgetProvider.ACTION_TOGGLE_TASK)
                .putExtra(PlannerWidgetProvider.EXTRA_TASK_ID, task.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(row.containerId, pendingIntent)
            views.setOnClickPendingIntent(row.boxId, pendingIntent)
            views.setOnClickPendingIntent(row.titleId, pendingIntent)
        }
    }
    return views
}

private fun buildScheduleViews(
    context: Context,
    widgetId: Int,
    appWidgetManager: AppWidgetManager
): RemoteViews {
    val state = WidgetStore.load(context)
    val views = RemoteViews(context.packageName, R.layout.widget_schedule)
    val palette = WidgetPalette.from(state)
    val launchIntent = Intent(context, MainActivity::class.java)
    val launchPendingIntent = PendingIntent.getActivity(
        context,
        1,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.schedule_widget_root, launchPendingIntent)
    views.setOnClickPendingIntent(R.id.schedule_widget_header, launchPendingIntent)
    views.setInt(R.id.schedule_widget_root, "setBackgroundColor", palette.background)
    views.setTextColor(R.id.schedule_widget_header, palette.title)

    val rowCount = scheduleRowCount(appWidgetManager.getAppWidgetOptions(widgetId))
    val firstDate = firstScheduleDate(state)
    val rows = listOf(
        R.id.schedule_row_1,
        R.id.schedule_row_2,
        R.id.schedule_row_3,
        R.id.schedule_row_4,
        R.id.schedule_row_5,
        R.id.schedule_row_6,
        R.id.schedule_row_7,
        R.id.schedule_row_8,
        R.id.schedule_row_9,
        R.id.schedule_row_10,
        R.id.schedule_row_11,
        R.id.schedule_row_12,
        R.id.schedule_row_13,
        R.id.schedule_row_14
    )
    views.setTextViewText(R.id.schedule_widget_header, "Work Schedule - $rowCount days")
    rows.forEachIndexed { index, viewId ->
        if (index >= rowCount) {
            views.setViewVisibility(viewId, View.GONE)
            return@forEachIndexed
        }
        views.setViewVisibility(viewId, View.VISIBLE)
        val date = firstDate.plusDays(index.toLong())
        val isToday = date == LocalDate.now()
        val shiftText = state.shifts
            .filter { it.date == date }
            .sortedBy { it.start }
            .joinToString { "${it.start.format(widgetTime)}-${it.end.format(widgetTime)}" }
            .ifBlank { if (date in state.daysOff) "Not scheduled" else "No shift" }
        views.setTextViewText(viewId, "${if (isToday) "Today" else date.format(widgetShortDate)}  $shiftText")
        views.setTextColor(viewId, if (isToday) palette.accent else palette.body)
    }
    return views
}

private fun scheduleRowCount(options: Bundle): Int {
    val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)
    return when {
        height >= 430 -> 14
        height >= 380 -> 12
        height >= 320 -> 10
        height >= 260 -> 8
        height >= 210 -> 6
        else -> 4
    }
}

private fun firstScheduleDate(state: WidgetState): LocalDate {
    val today = LocalDate.now()
    val allDates = state.shifts.map { it.date } + state.daysOff
    return allDates.filter { !it.isBefore(today) }.minOrNull()
        ?: allDates.minOrNull()
        ?: today
}

private object WidgetStore {
    fun load(context: Context): WidgetState {
        val root = context.getSharedPreferences("planner", Context.MODE_PRIVATE)
            .getString("state", null)
            ?.let(::JSONObject)
            ?: return WidgetState()
        return WidgetState(
            tasks = root.optJSONArray("tasks").toTasks(),
            notes = root.optJSONArray("notes").toNotes(),
            shifts = root.optJSONArray("shifts").toShifts(),
            daysOff = root.optJSONArray("daysOff").toDates(),
            darkMode = root.optBoolean("darkMode", false),
            accentStyle = root.optString("accentStyle", "Classic"),
            widgetLayoutMode = runCatching {
                WidgetLayoutMode.valueOf(root.optString("widgetLayoutMode", WidgetLayoutMode.Standard.name))
            }.getOrDefault(WidgetLayoutMode.Standard)
        )
    }

    fun toggleTask(context: Context, taskId: String) {
        val prefs = context.getSharedPreferences("planner", Context.MODE_PRIVATE)
        val root = prefs.getString("state", null)?.let(::JSONObject) ?: return
        val tasks = root.optJSONArray("tasks") ?: return
        for (index in 0 until tasks.length()) {
            val task = tasks.getJSONObject(index)
            if (task.optString("id") == taskId) {
                task.put("completed", !task.optBoolean("completed"))
                break
            }
        }
        prefs.edit().putString("state", root.toString()).apply()
    }

    private fun JSONArray?.toTasks(): List<WidgetTask> {
        if (this == null) return emptyList()
        return List(length()) { index ->
            val json = getJSONObject(index)
            WidgetTask(
                id = json.optString("id"),
                title = json.optString("title"),
                completed = json.optBoolean("completed")
            )
        }
    }

    private fun JSONArray?.toShifts(): List<WidgetShift> {
        if (this == null) return emptyList()
        return List(length()) { index ->
            val json = getJSONObject(index)
            WidgetShift(
                date = LocalDate.parse(json.getString("date")),
                start = LocalTime.parse(json.getString("start")),
                end = LocalTime.parse(json.getString("end"))
            )
        }
    }

    private fun JSONArray?.toNotes(): List<WidgetNote> {
        if (this == null) return emptyList()
        return List(length()) { index ->
            val json = getJSONObject(index)
            WidgetNote(
                text = json.optString("text"),
                kind = json.optString("kind", "General"),
                createdAt = json.optString("createdAt").takeIf { it.isNotBlank() && it != "null" }
                    ?.let(LocalDateTime::parse)
                    ?: LocalDateTime.MIN
            )
        }
    }

    private fun JSONArray?.toDates(): Set<LocalDate> {
        if (this == null) return emptySet()
        return List(length()) { index -> LocalDate.parse(getString(index)) }.toSet()
    }
}

private data class WidgetState(
    val tasks: List<WidgetTask> = emptyList(),
    val notes: List<WidgetNote> = emptyList(),
    val shifts: List<WidgetShift> = emptyList(),
    val daysOff: Set<LocalDate> = emptySet(),
    val darkMode: Boolean = false,
    val accentStyle: String = "Classic",
    val widgetLayoutMode: WidgetLayoutMode = WidgetLayoutMode.Standard
)

private data class WidgetTask(
    val id: String,
    val title: String,
    val completed: Boolean
)

private data class WidgetShift(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime
)

private data class WidgetNote(
    val text: String,
    val kind: String,
    val createdAt: LocalDateTime
)

private enum class WidgetLayoutMode {
    Compact,
    Standard,
    Detailed
}

private data class TaskRow(
    val containerId: Int,
    val boxId: Int,
    val titleId: Int
)

private val widgetDate = DateTimeFormatter.ofPattern("EEE, MMM d")
private val widgetShortDate = DateTimeFormatter.ofPattern("EEE M/d")
private val widgetTime = DateTimeFormatter.ofPattern("h:mm a")

private fun WidgetNote.toWidgetText(): String {
    val firstLine = text.lineSequence().firstOrNull().orEmpty().ifBlank { "Latest note" }
    return "$kind: ${firstLine.take(42)}"
}

private data class WidgetPalette(
    val background: Int,
    val title: Int,
    val body: Int,
    val accent: Int,
    val success: Int
) {
    companion object {
        fun from(state: WidgetState): WidgetPalette {
            val accent = when (state.accentStyle) {
                "Classic" -> if (state.darkMode) 0xFFFFB36B.toInt() else 0xFF9B4A15.toInt()
                "Emerald" -> if (state.darkMode) 0xFFA7D98B.toInt() else 0xFF2D6B3F.toInt()
                "Sunrise" -> if (state.darkMode) 0xFFFFC477.toInt() else 0xFFB35C16.toInt()
                "Logo" -> if (state.darkMode) 0xFF35E0D4.toInt() else 0xFF008C92.toInt()
                else -> if (state.darkMode) 0xFFFFB36B.toInt() else 0xFF9B4A15.toInt()
            }
            return if (state.darkMode) {
                WidgetPalette(
                    background = 0xFF1C1916.toInt(),
                    title = 0xFFFFF7EF.toInt(),
                    body = 0xFFEADFD5.toInt(),
                    accent = accent,
                    success = 0xFFA7D98B.toInt()
                )
            } else {
                WidgetPalette(
                    background = 0xFFFFFCF8.toInt(),
                    title = 0xFF241914.toInt(),
                    body = 0xFF3B302A.toInt(),
                    accent = accent,
                    success = 0xFF4F7D2B.toInt()
                )
            }
        }
    }
}
