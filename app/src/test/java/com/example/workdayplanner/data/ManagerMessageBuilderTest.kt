package com.example.workdayplanner.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ManagerMessageBuilderTest {
    @Test
    fun buildsTimeOffMessageWithDetail() {
        val message = ManagerMessageBuilder.build(ManagerMessageType.TimeOff, "July 12")

        assertTrue(message.contains("July 12"))
        assertTrue(message.contains("time off", ignoreCase = true))
    }

    @Test
    fun buildsPayMistakeMessageWithoutSoundingAccusatory() {
        val message = ManagerMessageBuilder.build(ManagerMessageType.PayMistake, "my lunch punch on Friday")

        assertTrue(message.contains("may be a pay or punch issue"))
        assertTrue(message.contains("review it"))
    }
}
