package com.example.workdayplanner.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class TodoistHttpRequest(
    val method: String,
    val url: String,
    val token: String? = null,
    val body: String? = null,
    val form: Boolean = false
)

data class TodoistHttpResult(
    val code: Int,
    val body: String
)

fun interface TodoistTransport {
    fun exchange(request: TodoistHttpRequest): TodoistHttpResult
}

class HttpTodoistTransport : TodoistTransport {
    override fun exchange(request: TodoistHttpRequest): TodoistHttpResult {
        val connection = (URL(request.url).openConnection() as HttpURLConnection).apply {
            requestMethod = request.method
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
            request.token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (request.body != null) {
                doOutput = true
                setRequestProperty(
                    "Content-Type",
                    if (request.form) "application/x-www-form-urlencoded" else "application/json; charset=utf-8"
                )
            }
        }
        try {
            if (request.body != null) {
                connection.outputStream.use { stream -> stream.write(request.body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            return TodoistHttpResult(code, body)
        } finally {
            connection.disconnect()
        }
    }
}

class TodoistApi(
    private val transport: TodoistTransport = HttpTodoistTransport(),
    private val apiRoot: String = "https://api.todoist.com/api/v1",
    private val oauthRoot: String = "https://api.todoist.com/oauth"
) {
    fun activeTasks(token: String): List<TodoistTask> {
        val tasks = mutableListOf<TodoistTask>()
        var cursor: String? = null
        var pages = 0
        do {
            val url = buildString {
                append("$apiRoot/tasks?limit=200")
                cursor?.let { append("&cursor=").append(URLEncoder.encode(it, Charsets.UTF_8.name())) }
            }
            val page = TodoistJson.parseTaskPage(call(TodoistHttpRequest("GET", url, token = token)))
            tasks += page.items
            cursor = page.nextCursor
            pages += 1
        } while (cursor != null && pages < 50)
        return tasks
    }

    fun projects(token: String): List<TodoistProject> {
        val projects = mutableListOf<TodoistProject>()
        var cursor: String? = null
        var pages = 0
        do {
            val url = buildString {
                append("$apiRoot/projects?limit=200")
                cursor?.let { append("&cursor=").append(URLEncoder.encode(it, Charsets.UTF_8.name())) }
            }
            val page = TodoistJson.parseProjectPage(call(TodoistHttpRequest("GET", url, token = token)))
            projects += page.items
            cursor = page.nextCursor
            pages += 1
        } while (cursor != null && pages < 20)
        return projects
    }

    fun task(token: String, taskId: String): TodoistTask? {
        val result = transport.exchange(TodoistHttpRequest("GET", "$apiRoot/tasks/$taskId", token = token))
        if (result.code == 404) return null
        val body = requireOk(result)
        return TodoistJson.parseTask(body)
    }

    fun createTask(token: String, draft: TodoistTaskDraft): TodoistTask {
        val body = call(
            TodoistHttpRequest(
                method = "POST",
                url = "$apiRoot/tasks",
                token = token,
                body = TodoistJson.taskBody(draft)
            )
        )
        return TodoistJson.parseTask(body) ?: throw TodoistHttpException(200, "Todoist did not return the new task.")
    }

    fun updateTask(token: String, taskId: String, draft: TodoistTaskDraft): TodoistTask {
        val body = call(
            TodoistHttpRequest(
                method = "POST",
                url = "$apiRoot/tasks/$taskId",
                token = token,
                body = TodoistJson.taskBody(draft)
            )
        )
        return TodoistJson.parseTask(body) ?: throw TodoistHttpException(200, "Todoist did not return the updated task.")
    }

    fun closeTask(token: String, taskId: String) {
        call(TodoistHttpRequest("POST", "$apiRoot/tasks/$taskId/close", token = token, body = "{}"))
    }

    fun reopenTask(token: String, taskId: String) {
        call(TodoistHttpRequest("POST", "$apiRoot/tasks/$taskId/reopen", token = token, body = "{}"))
    }

    fun deleteTask(token: String, taskId: String) {
        val result = transport.exchange(TodoistHttpRequest("DELETE", "$apiRoot/tasks/$taskId", token = token))
        if (result.code == 404) return
        requireOk(result)
    }

    fun registerPublicClient(redirectUri: String): String {
        val body = call(
            TodoistHttpRequest(
                method = "POST",
                url = "$oauthRoot/register",
                body = TodoistJson.registrationBody(redirectUri)
            )
        )
        return TodoistJson.parseClientId(body)
    }

    fun exchangeCode(clientId: String, code: String, redirectUri: String, codeVerifier: String): TodoistTokenResponse {
        return tokenRequest(
            mapOf(
                "client_id" to clientId,
                "code" to code,
                "redirect_uri" to redirectUri,
                "grant_type" to "authorization_code",
                "code_verifier" to codeVerifier
            )
        )
    }

    fun refresh(clientId: String, refreshToken: String): TodoistTokenResponse {
        return tokenRequest(
            mapOf(
                "client_id" to clientId,
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        )
    }

    private fun tokenRequest(fields: Map<String, String>): TodoistTokenResponse {
        val body = fields.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, Charsets.UTF_8.name())}=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
        val response = call(TodoistHttpRequest("POST", "$oauthRoot/access_token", body = body, form = true))
        return TodoistJson.parseToken(response)
    }

    private fun call(request: TodoistHttpRequest): String = requireOk(transport.exchange(request))

    private fun requireOk(result: TodoistHttpResult): String {
        if (result.code in 200..299) return result.body
        val fallback = when (result.code) {
            401 -> "Todoist rejected the connection. Connect again from Settings."
            403 -> "Todoist refused that change."
            else -> "Todoist request failed (${result.code})."
        }
        throw TodoistHttpException(result.code, TodoistJson.errorMessage(result.body, fallback))
    }
}
