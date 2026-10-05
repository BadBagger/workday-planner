package com.example.workdayplanner.data

import android.net.Uri
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TodoistOAuth {
    const val REDIRECT_URI = "workdayplanner://todoist/callback"
    private const val AUTHORIZE_URL = "https://app.todoist.com/oauth/authorize"
    private val random = SecureRandom()

    fun verifier(): String = base64Url(randomBytes(32))

    fun challenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return base64Url(digest)
    }

    fun state(): String = base64Url(randomBytes(24))

    fun authorizeUri(clientId: String, state: String, codeChallenge: String): Uri {
        return Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("scope", "data:read_write,data:delete")
            .appendQueryParameter("state", state)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)

    private fun base64Url(bytes: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
