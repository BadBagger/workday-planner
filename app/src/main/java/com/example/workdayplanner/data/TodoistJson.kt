package com.example.workdayplanner.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

object TodoistJson {
    fun parseTaskPage(body: String): TodoistPage<TodoistTask> = parsePage(body, ::parseTask)

    fun parseProjectPage(body: String): TodoistPage<TodoistProject> = parsePage(body, ::parseProject)

    fun parseTask(body: String): TodoistTask? {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed == "null") return null
        return parseTask(JSONObject(trimmed))
    }

    fun taskBody(draft: TodoistTaskDraft): String {
        val json = JSONObject()
            .put("content", draft.content)
            .put("description", draft.description)
            .put("priority", draft.priority)
        draft.due.dueString?.let {
            json.put("due_string", it)
            json.put("due_lang", "en")
        }
        draft.due.dueDate?.let { json.put("due_date", it) }
        draft.due.dueDatetime?.let { json.put("due_datetime", it) }
        if (draft.duration != null && draft.durationUnit != null) {
            json.put("duration", draft.duration)
            json.put("duration_unit", draft.durationUnit)
        }
        return json.toString()
    }

    fun registrationBody(redirectUri: String): String {
        return JSONObject()
            .put("client_name", "Workday Planner")
            .put("redirect_uris", JSONArray().put(redirectUri))
            .put("scope", "data:read_write data:delete")
            .put("grant_types", JSONArray().put("authorization_code").put("refresh_token"))
            .put("response_types", JSONArray().put("code"))
            .put("token_endpoint_auth_method", "none")
            .toString()
    }

    fun parseClientId(body: String): String {
        val id = JSONObject(body).optString("client_id")
        if (id.isBlank()) throw TodoistHttpException(200, "Todoist did not return an app id.")
        return id
    }

    fun parseToken(body: String): TodoistTokenResponse {
        val json = JSONObject(body)
        val access = json.optString("access_token")
        if (access.isBlank()) {
            val message = json.optString("error_description").ifBlank { json.optString("error") }.ifBlank { "Todoist did not return a token." }
            throw TodoistHttpException(400, message)
        }
        val expiresIn = json.optLong("expires_in", 0L).takeIf { json.has("expires_in") && !json.isNull("expires_in") }
        return TodoistTokenResponse(
            accessToken = access,
            refreshToken = json.optString("refresh_token").takeIf { it.isNotBlank() && it != "null" },
            expiresInSeconds = expiresIn
        )
    }

    fun errorMessage(body: String, fallback: String): String {
        val trimmed = body.trim()
        if (trimmed.startsWith("{")) {
            val json = runCatching { JSONObject(trimmed) }.getOrNull() ?: return fallback
            return json.optString("error_description")
                .ifBlank { json.optString("error") }
                .ifBlank { fallback }
        }
        return trimmed.ifBlank { fallback }
    }

    private fun <T> parsePage(body: String, parse: (JSONObject) -> T?): TodoistPage<T> {
        val trimmed = body.trim()
        if (trimmed.startsWith("[")) {
            return TodoistPage(JSONArray(trimmed).toItems(parse), null)
        }
        val json = JSONObject(trimmed)
        val array = json.optJSONArray("results") ?: json.optJSONArray("items")
        if (array != null) {
            return TodoistPage(array.toItems(parse), cursor(json))
        }
        val single = parse(json)
        return TodoistPage(listOfNotNull(single), null)
    }

    private fun parseTask(json: JSONObject): TodoistTask? {
        val id = json.optString("id").takeIf { it.isNotBlank() } ?: return null
        val content = json.optString("content")
        if (content.isBlank() && !json.has("content")) return null
        val due = json.optJSONObject("due")?.let(::parseDue)
        return TodoistTask(
            id = id,
            content = content,
            description = json.optString("description"),
            projectId = json.optString("project_id").takeIf { it.isNotBlank() && it != "null" },
            priority = json.optInt("priority", 1),
            due = due,
            durationMinutes = durationMinutes(json.optJSONObject("duration")),
            checked = json.optBoolean("checked", false),
            isDeleted = json.optBoolean("is_deleted", false),
            addedAt = json.optString("added_at").takeIf { it.isNotBlank() && it != "null" },
            updatedAt = json.optString("updated_at").takeIf { it.isNotBlank() && it != "null" }
        )
    }

    private fun parseProject(json: JSONObject): TodoistProject? {
        val id = json.optString("id").takeIf { it.isNotBlank() } ?: return null
        val name = json.optString("name")
        if (name.isBlank()) return null
        return TodoistProject(id, name)
    }

    private fun parseDue(json: JSONObject): TodoistDue {
        val date = json.optString("date").takeIf { it.isNotBlank() && it != "null" }?.let {
            runCatching { LocalDate.parse(it.take(10)) }.getOrNull()
        }
        val dateTime = json.optString("datetime").takeIf { it.isNotBlank() && it != "null" }?.let(::parseUtcDateTime)
        return TodoistDue(
            date = date,
            dateTimeUtc = dateTime,
            timezone = json.optString("timezone").takeIf { it.isNotBlank() && it != "null" },
            isRecurring = json.optBoolean("is_recurring", false),
            string = json.optString("string")
        )
    }

    private fun parseUtcDateTime(value: String): LocalDateTime? {
        return runCatching { OffsetDateTime.parse(value).withOffsetSameInstant(java.time.ZoneOffset.UTC).toLocalDateTime() }
            .getOrElse { runCatching { LocalDateTime.parse(value) }.getOrNull() }
    }

    private fun durationMinutes(json: JSONObject?): Int? {
        if (json == null) return null
        val amount = json.optInt("amount", 0)
        if (amount <= 0) return null
        return when (json.optString("unit")) {
            "day" -> amount * 24 * 60
            else -> amount
        }
    }

    private fun cursor(json: JSONObject): String? {
        return json.optString("next_cursor").takeIf { it.isNotBlank() && it != "null" }
    }

    private fun <T> JSONArray.toItems(parse: (JSONObject) -> T?): List<T> {
        return List(length()) { index -> optJSONObject(index) }.mapNotNull { item -> item?.let(parse) }
    }
}

data class TodoistTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresInSeconds: Long?
)
