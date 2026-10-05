package com.example.workdayplanner.todo

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.workdayplanner.alarm.AlarmScheduler
import com.example.workdayplanner.data.PlannerRepository
import com.example.workdayplanner.data.ReminderType
import com.example.workdayplanner.data.TaskItem
import com.example.workdayplanner.data.ToDoCommands
import java.time.LocalDate

class ToDoProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val state = repository().state.value
        val cursor = MatrixCursor(arrayOf("id", "kind", "title"))
        state.tasks.forEach { cursor.addRow(arrayOf(it.id, "task", it.title)) }
        state.events.forEach { cursor.addRow(arrayOf(it.id, "event", it.title)) }
        state.goals.forEach { cursor.addRow(arrayOf(it.id, "goal", it.title)) }
        state.files.forEach { cursor.addRow(arrayOf(it.id, "file", it.title)) }
        state.images.forEach { cursor.addRow(arrayOf(it.id, "photo", it.title)) }
        cursor.addRow(arrayOf("deli-standards", "standards", "Deli Daily Standards"))
        return cursor
    }

    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.smithware.workdayplanner.todo"

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val fields = values.toFieldMap()
        val before = repository().state.value.tasks
        val result = repository().applyToDo(ToDoCommands.fromFields(fields), LocalDate.now(), fields)
        syncAlarms(before, repository().state.value.tasks)
        return Uri.parse("content://${uri.authority}/items/${result.id}")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val id = uri.lastPathSegment?.takeIf { it != "items" } ?: return 0
        val kind = uri.getQueryParameter("kind") ?: "task"
        val fields = mapOf("action" to "delete", "kind" to kind, "id" to id)
        val before = repository().state.value.tasks
        repository().applyToDo(ToDoCommands.fromFields(fields), LocalDate.now(), fields)
        syncAlarms(before, repository().state.value.tasks)
        return 1
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val fields = values.toFieldMap().toMutableMap()
        if (fields["id"].isNullOrBlank()) {
            fields["id"] = uri.lastPathSegment?.takeIf { it != "items" }
        }
        if (fields["action"].isNullOrBlank()) fields["action"] = "update"
        val before = repository().state.value.tasks
        repository().applyToDo(ToDoCommands.fromFields(fields), LocalDate.now(), fields)
        syncAlarms(before, repository().state.value.tasks)
        return 1
    }

    private fun repository(): PlannerRepository {
        val appContext = context?.applicationContext ?: throw IllegalStateException("To Do provider has no context.")
        return PlannerRepository.get(appContext)
    }

    private fun syncAlarms(before: List<TaskItem>, after: List<TaskItem>) {
        val appContext = context?.applicationContext ?: return
        val scheduler = AlarmScheduler(appContext)
        val previous = before.associateBy { it.id }
        after.forEach { task ->
            val old = previous[task.id]
            if (task.completed || task.reminderType == ReminderType.None || task.alarmAt == null) {
                if (old?.alarmAt != null) scheduler.cancel(task.id)
            } else if (old?.alarmAt != task.alarmAt || old.completed != task.completed) {
                scheduler.cancel(task.id)
                scheduler.schedule(task)
            }
        }
    }
}

private fun ContentValues?.toFieldMap(): Map<String, String?> {
    if (this == null) return emptyMap()
    return keySet().associateWith { key -> get(key)?.toString() }
}
