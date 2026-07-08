package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class PremiumAccessTest {
    @Test
    fun freePlanHasLimitedScreenshotImports() {
        val state = AppState(
            premium = PremiumEntitlement(
                importMonth = "2026-07",
                screenshotImportsThisMonth = 2
            )
        )

        assertEquals(1, PremiumAccess.remainingScreenshotImports(state, YearMonth.of(2026, 7)))
        assertTrue(PremiumAccess.canImportScreenshot(state, YearMonth.of(2026, 7)))
    }

    @Test
    fun freePlanBlocksAfterMonthlyLimit() {
        val state = AppState(
            premium = PremiumEntitlement(
                importMonth = "2026-07",
                screenshotImportsThisMonth = PremiumAccess.FREE_SCREENSHOT_IMPORTS_PER_MONTH
            )
        )

        assertEquals(0, PremiumAccess.remainingScreenshotImports(state, YearMonth.of(2026, 7)))
        assertFalse(PremiumAccess.canImportScreenshot(state, YearMonth.of(2026, 7)))
    }

    @Test
    fun mockPremiumUnlocksPremiumFeatures() {
        val state = AppState(premium = PremiumEntitlement(mockPremiumEnabled = true))

        assertTrue(PremiumAccess.canUse(state, PremiumFeature.PayEstimator))
        assertTrue(PremiumAccess.canImportScreenshot(state, YearMonth.of(2026, 7)))
    }
}
