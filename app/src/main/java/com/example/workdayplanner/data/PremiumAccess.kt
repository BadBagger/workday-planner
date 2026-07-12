package com.example.workdayplanner.data

import java.time.YearMonth

object PremiumAccess {
    const val FREE_SCREENSHOT_IMPORTS_PER_MONTH = 6

    fun canUse(state: AppState, feature: PremiumFeature): Boolean {
        return state.premium.has(feature)
    }

    fun remainingScreenshotImports(state: AppState, month: YearMonth = YearMonth.now()): Int {
        if (state.premium.has(PremiumFeature.UnlimitedImports)) return Int.MAX_VALUE
        val used = if (state.premium.importMonth == month.toString()) state.premium.screenshotImportsThisMonth else 0
        return (FREE_SCREENSHOT_IMPORTS_PER_MONTH - used).coerceAtLeast(0)
    }

    fun canImportScreenshot(state: AppState, month: YearMonth = YearMonth.now()): Boolean {
        return remainingScreenshotImports(state, month) > 0
    }
}
