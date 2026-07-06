package com.example.workdayplanner.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingTextParserTest {
    @Test
    fun parsesDelimitedTrainingRows() {
        val items = TrainingTextParser.parse(
            """
            Associate Training Due
            Katie Smith - CBT Food Safety - 07/15/2026
            Jordan Lee | Customer Service Basics | 7/20/26
            """.trimIndent(),
            today = LocalDate.of(2026, 7, 6)
        )

        assertEquals(2, items.size)
        assertEquals("Katie Smith", items[0].associateName)
        assertEquals("CBT Food Safety", items[0].trainingTitle)
        assertEquals(LocalDate.of(2026, 7, 15), items[0].dueDate)
        assertEquals("Jordan Lee", items[1].associateName)
        assertEquals(LocalDate.of(2026, 7, 20), items[1].dueDate)
    }

    @Test
    fun parsesOcrRowsWithoutDelimiters() {
        val items = TrainingTextParser.parse(
            """
            Alex Morgan Annual Safety Review Jul 9 2026
            Taylor Ray Fresh Department Training 08/01/2026
            """.trimIndent(),
            today = LocalDate.of(2026, 7, 6)
        )

        assertEquals(2, items.size)
        assertEquals("Alex Morgan", items[0].associateName)
        assertEquals("Annual Safety Review", items[0].trainingTitle)
        assertEquals(LocalDate.of(2026, 7, 9), items[0].dueDate)
        assertEquals("Taylor Ray", items[1].associateName)
        assertEquals("Fresh Department Training", items[1].trainingTitle)
    }
}
