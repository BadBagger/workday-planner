package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class VoiceTaskParseResult(
    val rawTranscript: String,
    val title: String,
    val dueAt: LocalDateTime?,
    val alarmAt: LocalDateTime?,
    val reminderOffsetMinutes: Long,
    val reminderType: ReminderType,
    val repeatRule: RepeatRule,
    val repeatDays: Set<DayOfWeek>,
    val priority: TaskPriority,
    val notes: String,
    val confidence: Double,
    val ambiguityReasons: List<String>,
    val timeZoneId: String
) {
    fun toTask(
        createdAt: LocalDateTime = LocalDateTime.now(),
        alarmDelivery: AlarmDelivery = AlarmDelivery.SystemClockAlarm
    ): TaskItem {
        val selectedDelivery = if (reminderType == ReminderType.None || alarmAt == null) {
            AlarmDelivery.WorkdayPlannerAlarm
        } else {
            alarmDelivery
        }
        return TaskItem(
            title = title.ifBlank { "Voice task" },
            notes = notes,
            priority = priority,
            deadline = dueAt,
            alarmAt = alarmAt,
            repeatRule = repeatRule,
            repeatDays = repeatDays,
            alarmOffsetMinutes = reminderOffsetMinutes,
            rawVoiceTranscript = rawTranscript,
            reminderType = reminderType,
            alarmDelivery = selectedDelivery,
            alarmSchedulingStatus = when {
                reminderType == ReminderType.None -> AlarmSchedulingStatus.NoAlarmRequested
                alarmAt != null || dueAt != null -> AlarmSchedulingStatus.Scheduled
                else -> AlarmSchedulingStatus.NotScheduled
            },
            alarmLabel = title.ifBlank { "Voice task" },
            parserConfidence = confidence,
            createdUsingVoice = true,
            timeZoneId = timeZoneId,
            createdAt = createdAt
        )
    }
}

object VoiceTaskParser {
    private val introPhrases = listOf(
        "remind me to",
        "i need to",
        "add a task to",
        "make a task for",
        "don't let me forget to",
        "dont let me forget to",
        "create a reminder to"
    )

    private val fillerWords = setOf("uh", "um", "please", "like", "hey")

    private val numberWords = mapOf(
        "zero" to 0,
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
        "ten" to 10,
        "eleven" to 11,
        "twelve" to 12,
        "thirteen" to 13,
        "fourteen" to 14,
        "fifteen" to 15,
        "sixteen" to 16,
        "seventeen" to 17,
        "eighteen" to 18,
        "nineteen" to 19,
        "twenty" to 20,
        "thirty" to 30,
        "forty" to 40,
        "fourty" to 40,
        "fifty" to 50
    )

    private data class TimeMatch(
        val time: LocalTime,
        val range: IntRange,
        val explicitMeridiem: Boolean,
        val ambiguous: Boolean
    )

    fun parse(
        raw: String,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        defaultTaskTime: LocalTime? = null
    ): VoiceTaskParseResult {
        val original = raw.trim()
        if (original.isBlank()) {
            return VoiceTaskParseResult(
                rawTranscript = raw,
                title = "",
                dueAt = null,
                alarmAt = null,
                reminderOffsetMinutes = 30,
                reminderType = ReminderType.None,
                repeatRule = RepeatRule.None,
                repeatDays = emptySet(),
                priority = TaskPriority.Normal,
                notes = "",
                confidence = 0.0,
                ambiguityReasons = listOf("No speech recognized"),
                timeZoneId = zoneId.id
            )
        }

        var working = normalize(original)
        val ambiguity = mutableListOf<String>()
        val notes = mutableListOf<String>()

        working = working.replace(Regex("^(?:uh|um|hey|please|like)\\s+", RegexOption.IGNORE_CASE), "")

        introPhrases.forEach { phrase ->
            working = working.replace(Regex("^${Regex.escape(phrase)}\\s+", RegexOption.IGNORE_CASE), "")
        }

        val priority = when {
            Regex("\\b(urgent|critical)\\b", RegexOption.IGNORE_CASE).containsMatchIn(working) -> TaskPriority.Critical
            Regex("\\bhigh priority\\b", RegexOption.IGNORE_CASE).containsMatchIn(working) -> TaskPriority.High
            else -> TaskPriority.Normal
        }
        working = working
            .replace(Regex("\\bhigh priority\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(urgent|critical)\\b", RegexOption.IGNORE_CASE), "")

        val noAlarm = Regex("\\bno alarm\\b", RegexOption.IGNORE_CASE).containsMatchIn(working)
        working = working.replace(Regex("\\bno alarm\\b", RegexOption.IGNORE_CASE), "")

        val alarmAtDueTime = Regex("\\b(alarm|remind me) at (the )?(due )?time\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(working)
        working = working.replace(Regex("\\b(alarm|remind me) at (the )?(due )?time\\b", RegexOption.IGNORE_CASE), "")

        var reminderOffset = 30L
        var explicitAlarmPhrase = ""
        Regex("\\b(?:remind me|alarm)\\s+(.+?)\\s+before\\b", RegexOption.IGNORE_CASE)
            .find(working)
            ?.let { match ->
                reminderOffset = parseDurationMinutes(match.groupValues[1]).coerceAtLeast(0)
                working = working.removeRange(match.range)
            }
        Regex("\\balarm\\s+at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE)
            .find(working)
            ?.let { match ->
                explicitAlarmPhrase = match.value
                working = working.removeRange(match.range)
            }

        var repeatRule = RepeatRule.None
        var repeatDays = emptySet<DayOfWeek>()
        val everyDay = Regex("\\bevery\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", RegexOption.IGNORE_CASE)
            .find(working)
        val weekdays = Regex("\\bweekdays\\b", RegexOption.IGNORE_CASE).find(working)
        if (everyDay != null) {
            repeatRule = RepeatRule.Weekly
            repeatDays = setOf(dayOfWeek(everyDay.groupValues[1]))
        } else if (weekdays != null) {
            repeatRule = RepeatRule.Weekdays
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        }

        val relativeDue = Regex("\\bin\\s+(.+?)\\s+(minutes?|hours?)\\b", RegexOption.IGNORE_CASE)
            .find(working)
        var dueDate: LocalDate? = null
        var dueTime: LocalTime? = null
        var timeMatchRange: IntRange? = null
        var dateRange: IntRange? = null

        if (relativeDue != null) {
            val amount = wordsToNumber(relativeDue.groupValues[1])
            val due = when {
                relativeDue.groupValues[2].startsWith("hour", ignoreCase = true) -> now.plusHours(amount.toLong())
                else -> now.plusMinutes(amount.toLong())
            }
            dueDate = due.toLocalDate()
            dueTime = due.toLocalTime().withSecond(0).withNano(0)
            working = working.removeRange(relativeDue.range)
        } else {
            val dateMatch = findDateMatch(working, now.toLocalDate(), repeatDays)
            dueDate = dateMatch?.first
            dateRange = dateMatch?.second

            val timeMatch = findTimeMatch(working, now)
            if (timeMatch != null) {
                dueTime = timeMatch.time
                timeMatchRange = timeMatch.range
                if (timeMatch.ambiguous) {
                    ambiguity += "AM/PM unclear"
                }
            }
        }

        if (dueDate == null && dueTime != null) {
            dueDate = if (dueTime.isAfter(now.toLocalTime())) now.toLocalDate() else now.toLocalDate().plusDays(1)
        }
        if (dueDate != null && dueDate.isAfter(now.toLocalDate()) && dueTime != null && "AM/PM unclear" in ambiguity && dueTime.hour >= 13) {
            dueTime = dueTime.minusHours(12)
        }
        if (dueDate != null && dueTime == null) {
            dueTime = defaultTaskTime
            if (dueTime == null) ambiguity += "Missing time"
        }

        if (Regex("\\bbefore work tomorrow\\b", RegexOption.IGNORE_CASE).containsMatchIn(working) && dueDate == null) {
            dueDate = now.toLocalDate().plusDays(1)
            dueTime = LocalTime.of(8, 0)
            notes += "Interpreted before work tomorrow as 8:00 AM because no shift context was available."
            working = working.replace(Regex("\\bbefore work tomorrow\\b", RegexOption.IGNORE_CASE), "")
        }
        if (Regex("\\bafter lunch\\b", RegexOption.IGNORE_CASE).containsMatchIn(working) && dueDate == null) {
            dueDate = now.toLocalDate()
            dueTime = LocalTime.of(13, 0)
            working = working.replace(Regex("\\bafter lunch\\b", RegexOption.IGNORE_CASE), "")
        }

        val dueAt = if (dueDate != null && dueTime != null) LocalDateTime.of(dueDate, dueTime) else null
        val reminderType = if (noAlarm || dueAt == null) ReminderType.None else ReminderType.FullAlarm
        val explicitAlarmAt = dueAt?.let { due -> explicitAlarmAt(explicitAlarmPhrase, due) }
        val alarmAt = when {
            reminderType == ReminderType.None -> null
            alarmAtDueTime -> dueAt
            explicitAlarmAt != null -> explicitAlarmAt
            dueAt != null -> dueAt.minusMinutes(reminderOffset)
            else -> null
        }
        val effectiveOffset = when {
            reminderType == ReminderType.None -> 0
            alarmAtDueTime -> 0
            explicitAlarmAt != null -> Duration.between(explicitAlarmAt, dueAt).toMinutes().coerceAtLeast(0)
            else -> reminderOffset
        }

        val title = buildTitle(
            original = working,
            dateRange = dateRange,
            timeRange = timeMatchRange,
            repeatRule = repeatRule
        )
        if (dueAt == null && !noAlarm) {
            ambiguity += "No date or time detected"
        }

        val confidence = when {
            title.isBlank() -> 0.2
            dueAt == null -> 0.55
            ambiguity.any { it == "AM/PM unclear" } -> 0.75
            ambiguity.isNotEmpty() -> 0.65
            else -> 0.95
        }

        return VoiceTaskParseResult(
            rawTranscript = raw,
            title = title.ifBlank { cleanTitle(working) },
            dueAt = dueAt,
            alarmAt = alarmAt,
            reminderOffsetMinutes = effectiveOffset,
            reminderType = reminderType,
            repeatRule = repeatRule,
            repeatDays = repeatDays,
            priority = priority,
            notes = notes.joinToString("\n"),
            confidence = confidence,
            ambiguityReasons = ambiguity.distinct(),
            timeZoneId = zoneId.id
        )
    }

    private fun normalize(value: String): String {
        return value
            .replace("a.m.", "am", ignoreCase = true)
            .replace("p.m.", "pm", ignoreCase = true)
            .replace(Regex("[,.;]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun findDateMatch(text: String, today: LocalDate, repeatDays: Set<DayOfWeek>): Pair<LocalDate, IntRange>? {
        Regex("\\btoday\\b", RegexOption.IGNORE_CASE).find(text)?.let { return today to it.range }
        Regex("\\btomorrow\\b", RegexOption.IGNORE_CASE).find(text)?.let { return today.plusDays(1) to it.range }
        Regex("\\bnext\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", RegexOption.IGNORE_CASE)
            .find(text)
            ?.let { return nextDay(today, dayOfWeek(it.groupValues[1]), includeToday = false) to it.range }
        Regex("\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|tues|wed|thu|thur|thurs|fri|sat|sun)\\b", RegexOption.IGNORE_CASE)
            .find(text)
            ?.let { return nextDay(today, dayOfWeek(it.groupValues[1]), includeToday = true) to it.range }
        repeatDays.firstOrNull()?.let { return nextDay(today, it, includeToday = true) to IntRange.EMPTY }
        return null
    }

    private fun findTimeMatch(text: String, now: LocalDateTime): TimeMatch? {
        Regex("\\bnoon\\b", RegexOption.IGNORE_CASE).find(text)?.let {
            return TimeMatch(LocalTime.NOON, it.range, explicitMeridiem = true, ambiguous = false)
        }
        Regex("\\bmidnight\\b", RegexOption.IGNORE_CASE).find(text)?.let {
            return TimeMatch(LocalTime.MIDNIGHT, it.range, explicitMeridiem = true, ambiguous = false)
        }

        Regex("\\b(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE)
            .find(text)
            ?.let { match ->
                val hourRaw = match.groupValues[1].toInt()
                val minute = match.groupValues[2].takeIf { it.isNotBlank() }?.toInt() ?: 0
                val meridiem = match.groupValues[3].lowercase(Locale.US)
                val explicit = meridiem.isNotBlank()
                val hour = resolveHour(hourRaw, meridiem, now.toLocalTime())
                return TimeMatch(
                    time = LocalTime.of(hour, minute),
                    range = match.range,
                    explicitMeridiem = explicit,
                    ambiguous = !explicit
                )
            }

        Regex("\\bat\\s+([a-z]+)(?:\\s+([a-z]+))?\\b", RegexOption.IGNORE_CASE).find(text)?.let { match ->
            val hour = numberWords[match.groupValues[1].lowercase(Locale.US)] ?: return@let
            val minute = numberWords[match.groupValues[2].lowercase(Locale.US)] ?: 0
            return TimeMatch(
                time = LocalTime.of(resolveHour(hour, "", now.toLocalTime()), minute),
                range = match.range,
                explicitMeridiem = false,
                ambiguous = true
            )
        }
        return null
    }

    private fun resolveHour(rawHour: Int, meridiem: String, now: LocalTime): Int {
        return when (meridiem.lowercase(Locale.US)) {
            "am" -> if (rawHour == 12) 0 else rawHour.coerceIn(0, 23)
            "pm" -> if (rawHour == 12) 12 else (rawHour + 12).coerceAtMost(23)
            else -> {
                val am = if (rawHour == 12) 0 else rawHour
                val pm = if (rawHour == 12) 12 else rawHour + 12
                when {
                    rawHour > 12 -> rawHour.coerceAtMost(23)
                    LocalTime.of(am, 0).isAfter(now) -> am
                    pm <= 23 -> pm
                    else -> am
                }
            }
        }
    }

    private fun explicitAlarmAt(phrase: String, dueAt: LocalDateTime): LocalDateTime? {
        if (phrase.isBlank()) return null
        val match = Regex("\\balarm\\s+at\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE)
            .find(phrase) ?: return null
        val rawHour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].takeIf { it.isNotBlank() }?.toInt() ?: 0
        val meridiem = match.groupValues[3].lowercase(Locale.US)
        val hour = when {
            meridiem.isNotBlank() -> resolveHour(rawHour, meridiem, LocalTime.MIDNIGHT)
            dueAt.hour >= 12 && rawHour in 1..11 -> rawHour + 12
            rawHour == 12 && dueAt.hour < 12 -> 0
            else -> rawHour
        }.coerceIn(0, 23)
        var alarmAt = dueAt.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (alarmAt.isAfter(dueAt)) alarmAt = alarmAt.minusHours(12)
        return alarmAt
    }

    private fun buildTitle(original: String, dateRange: IntRange?, timeRange: IntRange?, repeatRule: RepeatRule): String {
        var title = original
        if (repeatRule != RepeatRule.None) {
            title = title
                .replace(Regex("\\bevery\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\bweekdays\\b", RegexOption.IGNORE_CASE), "")
        }
        listOfNotNull(timeRange, dateRange)
            .filter { it != IntRange.EMPTY }
            .sortedByDescending { it.first }
            .forEach { range ->
                if (range.first >= 0 && range.last < title.length) {
                    title = title.removeRange(range)
                }
            }
        return cleanTitle(title)
    }

    private fun cleanTitle(value: String): String {
        val cleaned = value
            .replace(Regex("\\bat\\s+\\d{1,2}(?::\\d{2})?\\s*(am|pm)?\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\bat\\s+(noon|midnight|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)(\\s+(fifteen|thirty|forty five|fourty five))?\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(today|tomorrow|next\\s+)?(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|tues|wed|thu|thur|thurs|fri|sat|sun)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\bdue\\s*$", RegexOption.IGNORE_CASE), "")
            .split(" ")
            .filter { it.isNotBlank() && it.lowercase(Locale.US) !in fillerWords }
            .joinToString(" ")
            .replace(Regex("\\b(at|every)\\s*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '-', ':')
        return cleaned.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
    }

    private fun parseDurationMinutes(value: String): Long {
        val lower = value.lowercase(Locale.US).trim()
        return when {
            "hour" in lower -> wordsToNumber(lower.replace("an", "one").replace("hours", "").replace("hour", "")).toLong() * 60
            "minute" in lower -> wordsToNumber(lower.replace("minutes", "").replace("minute", "")).toLong()
            else -> wordsToNumber(lower).toLong()
        }
    }

    private fun wordsToNumber(value: String): Int {
        val trimmed = value.trim().lowercase(Locale.US)
        trimmed.toIntOrNull()?.let { return it }
        if (trimmed == "an" || trimmed == "a") return 1
        return trimmed.split(Regex("\\s+|-"))
            .filter { it.isNotBlank() }
            .sumOf { numberWords[it] ?: 0 }
            .takeIf { it > 0 } ?: 0
    }

    private fun dayOfWeek(value: String): DayOfWeek {
        return when (value.lowercase(Locale.US).take(3)) {
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    private fun nextDay(today: LocalDate, day: DayOfWeek, includeToday: Boolean): LocalDate {
        var offset = day.value - today.dayOfWeek.value
        if (offset < 0 || (offset == 0 && !includeToday)) offset += 7
        return today.plusDays(offset.toLong())
    }

    fun dayLabel(day: DayOfWeek): String {
        return day.getDisplayName(TextStyle.FULL, Locale.US)
    }
}
