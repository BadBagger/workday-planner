package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class PremiumAccessTest {
    @Test
    fun freePlanHasEnoughScreenshotImportsForWeeklySchedules() {
        val month = YearMonth.of(2026, 7)
        val state = AppState(
            premium = PremiumEntitlement(
                importMonth = month.toString(),
                screenshotImportsThisMonth = 0
            )
        )

        assertEquals(6, PremiumAccess.remainingScreenshotImports(state, month))
        assertTrue(PremiumAccess.canImportScreenshot(state, month))
    }

    @Test
    fun premiumRemovesScreenshotImportLimit() {
        val month = YearMonth.of(2026, 7)
        val state = AppState(
            premium = PremiumEntitlement(
                isPremium = true,
                importMonth = month.toString(),
                screenshotImportsThisMonth = 100
            )
        )

        assertEquals(Int.MAX_VALUE, PremiumAccess.remainingScreenshotImports(state, month))
    }
}
