package com.example.workdayplanner.data

import android.net.Uri
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class TodoistSyncOutcome(
    val connected: Boolean,
    val syncedAt: LocalDateTime? = null,
    val message: String? = null,
    val error: String? = null
)

class TodoistCoordinator(
    private val repository: PlannerRepository,
    private val credentials: TodoistCredentialStore,
    private val api: TodoistApi = TodoistApi(),
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val now: () -> Instant = Instant::now
) {
    fun isConnected(): Boolean = credentials.isConnected()

    fun lastSyncedAt(): LocalDateTime? {
        val epoch = credentials.lastSyncedAtEpochMs()
        if (epoch <= 0L) return null
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), zone)
    }

    fun savePersonalToken(raw: String) {
        val token = raw.trim().removePrefix("Bearer ").trim()
        if (token.length < 8 || token.any { it.isWhitespace() }) {
            throw IllegalArgumentException("Paste the API token from Todoist Settings, Integrations, Developer.")
        }
        credentials.savePersonalToken(token)
    }

    fun disconnect() {
        credentials.disconnect()
    }

    fun beginAuthorization(): Uri {
        val clientId = credentials.clientId() ?: api.registerPublicClient(TodoistOAuth.REDIRECT_URI).also {
            credentials.saveClientId(it)
        }
        val state = TodoistOAuth.state()
        val verifier = TodoistOAuth.verifier()
        credentials.saveOAuthRequest(state, verifier)
        return TodoistOAuth.authorizeUri(clientId, state, TodoistOAuth.challenge(verifier))
    }

    fun completeAuthorization(redirect: Uri): TodoistSyncOutcome {
        val error = redirect.getQueryParameter("error")
        if (!error.isNullOrBlank()) {
            return TodoistSyncOutcome(connected = credentials.isConnected(), error = "Todoist did not grant access ($error).")
        }
        val code = redirect.getQueryParameter("code").orEmpty()
        val state = redirect.getQueryParameter("state").orEmpty()
        if (code.isBlank()) return TodoistSyncOutcome(connected = credentials.isConnected(), error = "Todoist did not return a sign-in code.")
        if (state.isBlank() || state != credentials.oauthState()) {
            return TodoistSyncOutcome(connected = credentials.isConnected(), error = "Todoist sign-in could not be verified. Try connecting again.")
        }
        if (credentials.wasCodeUsed(code)) {
            return TodoistSyncOutcome(connected = credentials.isConnected(), message = "Todoist is already connected.")
        }
        val clientId = credentials.clientId() ?: return TodoistSyncOutcome(connected = false, error = "Todoist sign-in expired. Try connecting again.")
        val verifier = credentials.codeVerifier() ?: return TodoistSyncOutcome(connected = false, error = "Todoist sign-in expired. Try connecting again.")
        val token = api.exchangeCode(clientId, code, TodoistOAuth.REDIRECT_URI, verifier)
        credentials.rememberUsedCode(code)
        val expiresAt = token.expiresInSeconds?.takeIf { it in 1..(60L * 60 * 24 * 30) }?.let {
            now().plusSeconds(it).minusSeconds(60).toEpochMilli()
        } ?: 0L
        credentials.saveOAuth(token.accessToken, token.refreshToken, expiresAt)
        return sync()
    }

    fun sync(): TodoistSyncOutcome {
        if (!credentials.isConnected()) {
            return TodoistSyncOutcome(connected = false, message = "Todoist is not connected.")
        }
        return try {
            pushPending()
            val token = authorizedToken()
            val remote = api.activeTasks(token)
            val projects = runCatching { api.projects(token) }.getOrDefault(emptyList())
            repository.applyTodoistMerge(remote, zone)
            val syncedAt = now()
            credentials.markSynced(syncedAt.toEpochMilli())
            TodoistSyncOutcome(
                connected = true,
                syncedAt = LocalDateTime.ofInstant(syncedAt, zone),
                message = TodoistSync.projectSummary(projects, remote.count { !it.checked && !it.isDeleted })
            )
        } catch (error: TodoistHttpException) {
            if (error.code == 401 && credentials.tokenSource() == TodoistCredentialStore.SOURCE_PERSONAL) {
                TodoistSyncOutcome(connected = true, error = "That Todoist token was rejected. Paste a new one from Settings → Integrations → Developer.")
            } else {
                TodoistSyncOutcome(connected = credentials.isConnected(), error = error.message)
            }
        } catch (error: Exception) {
            TodoistSyncOutcome(connected = credentials.isConnected(), error = error.message ?: "Could not reach Todoist.")
        }
    }

    private fun pushPending() {
        credentials.pendingDeletes().forEach { id ->
            authorized { token -> api.deleteTask(token, id) }
            credentials.removeDelete(id)
        }
        val tasks = repository.state.value.tasks.filter { it.todoistPending != TodoistPendingAction.None }
        tasks.forEach { task -> pushTask(task) }
        val events = repository.state.value.events.filter { it.todoistPending != TodoistPendingAction.None }
        events.forEach { event -> pushEvent(event) }
    }

    private fun pushTask(original: TaskItem) {
        val task = repository.state.value.tasks.firstOrNull { it.id == original.id } ?: return
        if (!TodoistSync.canMirror(task) && task.todoistId == null) {
            repository.updateTask(task.id) { it.copy(todoistPending = TodoistPendingAction.None) }
            return
        }
        when (task.todoistPending) {
            TodoistPendingAction.None -> Unit
            TodoistPendingAction.Create -> {
                val draft = TodoistSync.draftFor(task, zone)
                val created = authorized { token -> api.createTask(token, draft) }
                val closed = if (task.completed) closeAndReload(created.id) else null
                repository.updateTask(task.id) { current ->
                    val unchanged = current.title == task.title &&
                        current.notes == task.notes &&
                        current.deadline == task.deadline &&
                        current.completed == task.completed
                    val applied = when {
                        !task.completed -> TodoistSync.applyRemote(created, current, zone)
                        closed != null -> TodoistSync.applyRemote(closed, current, zone)
                        else -> current.copy(
                            todoistId = created.id,
                            todoistProjectId = created.projectId,
                            completed = true,
                            todoistPending = TodoistPendingAction.None
                        )
                    }
                    if (unchanged) {
                        applied
                    } else {
                        applied.copy(
                            title = current.title,
                            notes = current.notes,
                            deadline = current.deadline,
                            completed = current.completed,
                            todoistPending = if (current.completed) TodoistPendingAction.Complete else TodoistPendingAction.Update
                        )
                    }
                }
            }
            TodoistPendingAction.Update -> {
                val id = task.todoistId ?: return
                val updated = authorized { token -> api.updateTask(token, id, TodoistSync.draftFor(task, zone)) }
                repository.updateTask(task.id) { current ->
                    if (current.todoistPending != TodoistPendingAction.Update) current
                    else TodoistSync.applyRemote(updated, current, zone)
                }
            }
            TodoistPendingAction.Complete -> {
                val id = task.todoistId ?: return
                if (TodoistSync.canMirror(task)) {
                    runCatching { authorized { token -> api.updateTask(token, id, TodoistSync.draftFor(task, zone)) } }
                }
                val reloaded = closeAndReload(id)
                repository.updateTask(task.id) { current ->
                    if (reloaded != null) TodoistSync.applyRemote(reloaded, current, zone)
                    else current.copy(completed = true, todoistPending = TodoistPendingAction.None)
                }
            }
            TodoistPendingAction.Reopen -> {
                val id = task.todoistId ?: return
                authorized { token -> api.reopenTask(token, id) }
                val reloaded = authorized { token -> api.task(token, id) }
                repository.updateTask(task.id) { current ->
                    if (reloaded != null) TodoistSync.applyRemote(reloaded, current, zone)
                    else current.copy(completed = false, todoistPending = TodoistPendingAction.None)
                }
            }
        }
    }

    private fun pushEvent(original: WorkEvent) {
        val event = repository.state.value.events.firstOrNull { it.id == original.id } ?: return
        when (event.todoistPending) {
            TodoistPendingAction.Create -> {
                val created = authorized { token -> api.createTask(token, TodoistSync.draftFor(event, zone)) }
                repository.updateEvent(event.id) { current ->
                    current.copy(todoistId = created.id, todoistPending = TodoistPendingAction.None)
                }
            }
            TodoistPendingAction.Update -> {
                val id = event.todoistId ?: return
                val updated = authorized { token -> api.updateTask(token, id, TodoistSync.draftFor(event, zone)) }
                repository.updateEvent(event.id) { current ->
                    if (current.todoistPending != TodoistPendingAction.Update) current
                    else TodoistSync.applyRemoteEvent(updated, current, zone)
                }
            }
            TodoistPendingAction.Complete, TodoistPendingAction.Reopen, TodoistPendingAction.None -> Unit
        }
    }

    private fun closeAndReload(todoistId: String): TodoistTask? {
        authorized { token -> api.closeTask(token, todoistId) }
        return authorized { token -> api.task(token, todoistId) }
    }

    private fun authorizedToken(): String {
        val expires = credentials.expiresAtEpochMs()
        val needsRefresh = credentials.tokenSource() == TodoistCredentialStore.SOURCE_OAUTH &&
            expires > 0L &&
            now().toEpochMilli() >= expires
        if (needsRefresh) refresh()
        return credentials.accessToken() ?: throw TodoistNotConnected()
    }

    private fun refresh() {
        val clientId = credentials.clientId() ?: throw TodoistHttpException(401, "Todoist sign-in expired. Connect again.")
        val refresh = credentials.refreshToken() ?: throw TodoistHttpException(401, "Todoist sign-in expired. Connect again.")
        val token = api.refresh(clientId, refresh)
        val expiresAt = token.expiresInSeconds?.let { now().plusSeconds(it).minusSeconds(60).toEpochMilli() } ?: 0L
        credentials.saveOAuth(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken ?: refresh,
            expiresAtEpochMs = expiresAt
        )
    }

    private fun <T> authorized(block: (String) -> T): T {
        return try {
            block(authorizedToken())
        } catch (error: TodoistHttpException) {
            if (error.code == 401 && credentials.tokenSource() == TodoistCredentialStore.SOURCE_OAUTH) {
                refresh()
                block(authorizedToken())
            } else {
                throw error
            }
        }
    }
}
