package com.example.workdayplanner.data

enum class ScheduleImportIssue(
    val title: String,
    val body: String,
    val tips: List<String>
) {
    NoTextDetected(
        title = "No text detected",
        body = "This screenshot was hard to read.",
        tips = listOf("Try cropping closer to the schedule.", "Make sure dates and shift times are visible.")
    ),
    ImageTooBlurry(
        title = "Image too blurry",
        body = "This screenshot was hard to read.",
        tips = listOf("Try cropping closer to the schedule.", "Make sure dates and shift times are visible.")
    ),
    ScheduleFormatUnclear(
        title = "Schedule format unclear",
        body = "The app found text, but it could not confidently turn it into shifts yet.",
        tips = listOf("Try cropping closer to the schedule.", "Review before saving.")
    ),
    DatesDetectedTimesMissing(
        title = "Dates detected, times missing",
        body = "The app can see dates, but shift start and end times are not clear.",
        tips = listOf("Make sure dates and shift times are visible.", "Review before saving.")
    ),
    TimesDetectedDatesMissing(
        title = "Times detected, dates missing",
        body = "The app can see shift times, but the matching dates are not clear.",
        tips = listOf("Make sure dates and shift times are visible.", "Try cropping closer to the schedule.")
    ),
    PermissionDenied(
        title = "Permission denied",
        body = "The app could not open that image.",
        tips = listOf("Choose the screenshot again if Android asks for access.", "Review before saving.")
    ),
    ImportCancelled(
        title = "Import cancelled",
        body = "No schedule was imported.",
        tips = listOf("Choose a screenshot when you are ready.", "Review before saving.")
    )
}

data class ScheduleImportGuidance(
    val issue: ScheduleImportIssue,
    val detail: String? = null
)

object ScheduleImportGuidanceClassifier {
    private val dateRegex = Regex("""\b(?:\d{1,2}[/-]\d{1,2}(?:[/-]\d{2,4})?|(?:mon|tue|wed|thu|fri|sat|sun)\w*)\b""", RegexOption.IGNORE_CASE)
    private val timeRegex = Regex("""\b\d{1,2}(?::\d{2})?\s*(?:a\.?m\.?|p\.?m\.?)\b|(?:\d{1,2}(?::\d{2})?\s*(?:-|to)\s*\d{1,2}(?::\d{2})?)""", RegexOption.IGNORE_CASE)

    fun fromRecognizedText(text: String): ScheduleImportGuidance? {
        if (text.isBlank()) return ScheduleImportGuidance(ScheduleImportIssue.NoTextDetected)
        return if (looksBlurry(text)) {
            ScheduleImportGuidance(ScheduleImportIssue.ImageTooBlurry)
        } else {
            null
        }
    }

    fun fromParsedText(text: String, parsed: ParsedSchedule): ScheduleImportGuidance? {
        if (text.isBlank()) return ScheduleImportGuidance(ScheduleImportIssue.NoTextDetected)
        if (parsed.shifts.isNotEmpty() || parsed.daysOff.isNotEmpty()) return null

        val hasDate = dateRegex.containsMatchIn(text)
        val hasTime = timeRegex.containsMatchIn(text)
        return when {
            hasDate && !hasTime -> ScheduleImportGuidance(ScheduleImportIssue.DatesDetectedTimesMissing)
            hasTime && !hasDate -> ScheduleImportGuidance(ScheduleImportIssue.TimesDetectedDatesMissing)
            looksBlurry(text) -> ScheduleImportGuidance(ScheduleImportIssue.ImageTooBlurry)
            else -> ScheduleImportGuidance(ScheduleImportIssue.ScheduleFormatUnclear)
        }
    }

    private fun looksBlurry(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 18) return true
        val lines = trimmed.lines().filter { it.isNotBlank() }
        val shortLineRatio = lines.count { it.trim().length <= 2 }.toDouble() / lines.size.coerceAtLeast(1)
        val letterOrDigitRatio = trimmed.count { it.isLetterOrDigit() || it.isWhitespace() }.toDouble() / trimmed.length.coerceAtLeast(1)
        return shortLineRatio > 0.45 || letterOrDigitRatio < 0.65
    }
}
