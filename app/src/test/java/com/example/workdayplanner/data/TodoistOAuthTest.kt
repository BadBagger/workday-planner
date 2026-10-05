package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TodoistOAuthTest {
    @Test
    fun pkceChallengeIsUnpaddedBase64Url() {
        val challenge = TodoistOAuth.challenge("todoist-pkce-verifier-example-value")
        assertEquals(43, challenge.length)
        assertFalse(challenge.contains("+"))
        assertFalse(challenge.contains("/"))
        assertFalse(challenge.contains("="))
        assertEquals("workdayplanner://todoist/callback", TodoistOAuth.REDIRECT_URI)
    }
}
