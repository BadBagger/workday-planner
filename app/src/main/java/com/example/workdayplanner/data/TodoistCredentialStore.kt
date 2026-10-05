package com.example.workdayplanner.data

import android.content.Context

class TodoistCredentialStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)?.takeIf { it.isNotBlank() }

    fun isConnected(): Boolean = accessToken() != null

    fun tokenSource(): String = prefs.getString(KEY_SOURCE, SOURCE_PERSONAL) ?: SOURCE_PERSONAL

    fun expiresAtEpochMs(): Long = prefs.getLong(KEY_EXPIRES, 0L)

    fun clientId(): String? = prefs.getString(KEY_CLIENT, null)?.takeIf { it.isNotBlank() }

    fun lastSyncedAtEpochMs(): Long = prefs.getLong(KEY_SYNCED, 0L)

    fun pendingDeletes(): Set<String> = prefs.getStringSet(KEY_DELETES, emptySet()).orEmpty()

    fun savePersonalToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS, token)
            .remove(KEY_REFRESH)
            .putString(KEY_SOURCE, SOURCE_PERSONAL)
            .putLong(KEY_EXPIRES, 0L)
            .apply()
    }

    fun saveOAuth(accessToken: String, refreshToken: String?, expiresAtEpochMs: Long) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .putString(KEY_SOURCE, SOURCE_OAUTH)
            .putLong(KEY_EXPIRES, expiresAtEpochMs)
            .remove(KEY_OAUTH_STATE)
            .remove(KEY_VERIFIER)
            .apply()
    }

    fun saveClientId(clientId: String) {
        prefs.edit().putString(KEY_CLIENT, clientId).apply()
    }

    fun saveOAuthRequest(state: String, verifier: String) {
        prefs.edit()
            .putString(KEY_OAUTH_STATE, state)
            .putString(KEY_VERIFIER, verifier)
            .apply()
    }

    fun oauthState(): String? = prefs.getString(KEY_OAUTH_STATE, null)

    fun codeVerifier(): String? = prefs.getString(KEY_VERIFIER, null)

    fun rememberUsedCode(code: String) {
        val recent = prefs.getStringSet(KEY_USED_CODES, emptySet()).orEmpty().toMutableSet()
        recent += code
        val trimmed = if (recent.size > 8) recent.toList().takeLast(8).toSet() else recent
        prefs.edit().putStringSet(KEY_USED_CODES, trimmed).apply()
    }

    fun wasCodeUsed(code: String): Boolean = code in prefs.getStringSet(KEY_USED_CODES, emptySet()).orEmpty()

    fun markSynced(epochMs: Long) {
        prefs.edit().putLong(KEY_SYNCED, epochMs).apply()
    }

    fun enqueueDelete(todoistId: String) {
        val next = prefs.getStringSet(KEY_DELETES, emptySet()).orEmpty() + todoistId
        prefs.edit().putStringSet(KEY_DELETES, next).apply()
    }

    fun removeDelete(todoistId: String) {
        val next = prefs.getStringSet(KEY_DELETES, emptySet()).orEmpty() - todoistId
        prefs.edit().putStringSet(KEY_DELETES, next).apply()
    }

    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)?.takeIf { it.isNotBlank() }

    fun disconnect() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_EXPIRES)
            .remove(KEY_OAUTH_STATE)
            .remove(KEY_VERIFIER)
            .remove(KEY_DELETES)
            .putString(KEY_SOURCE, SOURCE_PERSONAL)
            .apply()
    }

    companion object {
        const val SOURCE_PERSONAL = "personal"
        const val SOURCE_OAUTH = "oauth"
        private const val PREFS = "todoist_credentials"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES = "expires_at"
        private const val KEY_SOURCE = "source"
        private const val KEY_CLIENT = "client_id"
        private const val KEY_OAUTH_STATE = "oauth_state"
        private const val KEY_VERIFIER = "pkce_verifier"
        private const val KEY_DELETES = "pending_deletes"
        private const val KEY_SYNCED = "last_synced_at"
        private const val KEY_USED_CODES = "used_codes"
    }
}
