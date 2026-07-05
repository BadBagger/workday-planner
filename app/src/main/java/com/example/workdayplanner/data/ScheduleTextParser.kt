package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.Year
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

data class ParsedSchedule(
    val shifts: List<WorkShift>,
    val daysOff: Set<LocalDate>,
    val unparsedLines: List<String>
)

object ScheduleTextParser {
    private val dateRegex = Regex("""\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b""")
    private val fullDateRegex = Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})\b""")
    private val dayHeaderRegex = Regex("""(?i)^\s*(sat|sun|mon|tue|wed|thu|fri)\b(?:\s+(\d{1,2})(?!\s*(?:a\.?m\.?|p\.?m\.?)))?(.*)$""")
    private val dayNumberRegex = Regex("""^\d{1,2}$""")
    private val leadingDayNumberRegex = Regex("""^(\d{1,2})\s+.+$""")
    private val hoursRegex = Regex("""(?i)^\d+(?:\.\d+)?\s+hours$""")
    private val roleLineRegex = Regex("""(?i)^\d{1,2}\s+(.+)$""")
    private val splitTwoDigitHourRegex = Regex("""(?i)^([0-2])\s*((?:a|p)\.?m\.?.*)$""")
    private val timeRangeRegex = Regex(
        """(?i)\b(\d{1,2})(?::(\d{2}))?\s*(a\.?m\.?|p\.?m\.?)?\s*(?:-|\u2013|to)\s*(\d{1,2})(?::(\d{2}))?\s*(a\.?m\.?|p\.?m\.?)\b"""
    )
    private val offRegex = Regex("""(?i)\b(off|pto|vacation|unavailable|day\s*off|not\s*scheduled)\b""")

    fun parse(rawText: String, currentYear: Int = Year.now().value): ParsedSchedule {
        val lines = preprocess(rawText)
        val baseDate = findBaseDate(lines, currentYear) ?: inferBaseDateFromWeekdayNumbers(lines, currentYear)
        val stateful = parseWithDayCards(lines, baseDate, currentYear)
        val sequential = baseDate?.let { parseSequentialSchedule(lines, it) }
        return if (sequential != null && shouldUseSequentialFallback(stateful, sequential)) {
            sequential
        } else {
            stateful
        }
    }

    private fun preprocess(rawText: String): List<String> {
        return rawText.lineSequence()
            .map(::normalizeOcrLine)
            .filter { it.isNotBlank() }
            .filterNot(::isJunkHeader)
            .toList()
    }

    private val ParsedSchedule.score: Int
        get() = shifts.size + daysOff.size

    private fun shouldUseSequentialFallback(stateful: ParsedSchedule, sequential: ParsedSchedule): Boolean {
        if (sequential.score == 0) return false
        if (sequential.score > stateful.score) return true
        if (sequential.score < stateful.score) return false
        val statefulDateCount = (stateful.shifts.map { it.date } + stateful.daysOff).distinct().size
        val sequentialDateCount = (sequential.shifts.map { it.date } + sequential.daysOff).distinct().size
        return sequentialDateCount > statefulDateCount
    }

    private fun parseWithDayCards(lines: List<String>, baseDate: LocalDate?, currentYear: Int): ParsedSchedule {
        val shifts = mutableListOf<WorkShift>()
        val daysOff = mutableSetOf<LocalDate>()
        val unparsed = mutableListOf<String>()
        var pendingDayName: String? = null
        var pendingDayRest: String? = null
        var currentDate: LocalDate? = null

        lines.forEach { originalLine ->
            val line = normalizeOcrLine(originalLine)
            val directDate = dateRegex.find(line)?.let { parseDate(it, currentYear) }
            if (directDate != null) {
                currentDate = directDate
                if (offRegex.containsMatchIn(line)) {
                    daysOff += directDate
                    return@forEach
                }
                parseShift(line, directDate)?.let {
                    shifts += it
                    return@forEach
                }
            }

            val dayHeader = dayHeaderRegex.matchEntire(line)
            if (dayHeader != null) {
                pendingDayName = dayHeader.groupValues[1]
                var dayNumber = dayHeader.groupValues[2].toIntOrNull()
                val rawRest = dayHeader.groupValues[3].trim()
                var rest = if (dayNumber != null && rawRest.startsWithPeriod()) "$dayNumber $rawRest" else rawRest
                val splitHour = splitTwoDigitHourRegex.matchEntire(rawRest)
                if (dayNumber == 1 && splitHour != null) {
                    rest = "1${splitHour.groupValues[1]} ${splitHour.groupValues[2]}"
                    dayNumber = null
                }
                pendingDayRest = rest.takeIf { it.isNotBlank() }
                if (dayNumber != null && baseDate != null) {
                    currentDate = dateNearBase(dayNumber, baseDate)
                } else if (baseDate != null) {
                    currentDate = dateForWeekday(dayHeader.groupValues[1], baseDate)
                } else {
                    currentDate = null
                }
                if (currentDate != null && rest.isNotBlank()) {
                    if (offRegex.containsMatchIn(rest)) {
                        daysOff += currentDate!!
                        pendingDayRest = null
                        return@forEach
                    }
                    parseShift(rest, currentDate!!, roleAndStoreNear(lines, originalLine))?.let {
                        shifts += it
                        pendingDayRest = null
                        return@forEach
                    }
                }
                return@forEach
            }

            if (pendingDayName != null && dayNumberRegex.matches(line) && baseDate != null) {
                currentDate = dateNearBase(line.toInt(), baseDate)
                pendingDayRest?.let { rest ->
                    parseShift(rest, currentDate!!, roleAndStoreNear(lines, originalLine))?.let {
                        shifts += it
                        pendingDayRest = null
                    }
                }
                pendingDayName = null
                return@forEach
            }

            val leadingDayNumber = leadingDayNumberRegex.matchEntire(line)?.groupValues?.get(1)?.toIntOrNull()
            if (pendingDayName != null && pendingDayRest != null && leadingDayNumber != null && baseDate != null) {
                currentDate = dateNearBase(leadingDayNumber, baseDate)
                parseShift(pendingDayRest!!, currentDate!!, roleAndStoreNear(lines, originalLine))?.let {
                    shifts += it
                    pendingDayRest = null
                    pendingDayName = null
                    return@forEach
                }
            }

            val activeDate = currentDate
            if (activeDate != null && offRegex.containsMatchIn(line)) {
                daysOff += activeDate
                return@forEach
            }

            if (activeDate != null) {
                parseShift(line, activeDate, roleAndStoreNear(lines, originalLine))?.let {
                    shifts += it
                    return@forEach
                }
            }

            if (!isNoise(line) && !isLikelyJobDetail(line)) unparsed += originalLine
        }

        return ParsedSchedule(
            shifts = shifts.distinctBy { "${it.date}-${it.start}-${it.end}" },
            daysOff = daysOff,
            unparsedLines = unparsed
        )
    }

    private fun parseSequentialSchedule(lines: List<String>, baseDate: LocalDate): ParsedSchedule {
        val shifts = mutableListOf<WorkShift>()
        val daysOff = mutableSetOf<LocalDate>()
        val unparsed = mutableListOf<String>()
        val knownDates = extractDayCardDates(lines, baseDate)
        var eventIndex = 0
        var lastEvent: String? = null

        lines.map(::normalizeOcrLine).forEach { line ->
            if (isNoise(line) || dateRegex.containsMatchIn(line) || dayHeaderRegex.matches(line)) return@forEach
            val eventKey = when {
                offRegex.containsMatchIn(line) -> "off"
                timeRangeRegex.containsMatchIn(line) -> line
                else -> null
            }
            if (eventKey == null) {
                if (!isLikelyJobDetail(line)) unparsed += line
                return@forEach
            }
            if (eventKey == lastEvent) return@forEach

            val date = knownDates.getOrNull(eventIndex) ?: baseDate.plusDays(eventIndex.toLong())
            if (eventKey == "off") {
                daysOff += date
            } else {
                parseShift(line, date, roleAndStoreNear(lines, line))?.let { shifts += it }
            }
            lastEvent = eventKey
            eventIndex += 1
        }

        return ParsedSchedule(
            shifts = shifts.distinctBy { "${it.date}-${it.start}-${it.end}" },
            daysOff = daysOff,
            unparsedLines = unparsed
        )
    }

    private fun extractDayCardDates(lines: List<String>, baseDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var pendingDayName: String? = null
        lines.map(::normalizeOcrLine).forEach { line ->
            val dayHeader = dayHeaderRegex.matchEntire(line)
            if (dayHeader != null) {
                pendingDayName = dayHeader.groupValues[1]
                dayHeader.groupValues[2].toIntOrNull()?.let { dayNumber ->
                    dates += dateNearBase(dayNumber, baseDate)
                    pendingDayName = null
                }
                return@forEach
            }
            if (pendingDayName != null && dayNumberRegex.matches(line)) {
                dates += dateNearBase(line.toInt(), baseDate)
                pendingDayName = null
            }
        }
        return if (dates.size >= 2) dates.distinct() else emptyList()
    }

    private fun findBaseDate(lines: List<String>, currentYear: Int): LocalDate? {
        val fullDate = lines.firstNotNullOfOrNull { line ->
            fullDateRegex.find(line)?.let { parseDate(it, currentYear) }
        }
        if (fullDate != null) return fullDate
        return lines.firstNotNullOfOrNull { line ->
            dateRegex.find(line)?.let { parseDate(it, currentYear) }
        }
    }

    private fun inferBaseDateFromWeekdayNumbers(lines: List<String>, currentYear: Int): LocalDate? {
        val pairs = weekdayNumberPairs(lines)
        if (pairs.isEmpty()) return null
        val likelyMonth = (1..12)
            .mapNotNull { month ->
                val score = pairs.count { (dayName, dayNumber) ->
                    runCatching {
                        LocalDate.of(currentYear, month, dayNumber).dayOfWeek == dayOfWeekFor(dayName)
                    }.getOrDefault(false)
                }
                val firstDay = pairs.minOf { it.second }
                val date = runCatching { LocalDate.of(currentYear, month, firstDay) }.getOrNull()
                date?.let { Triple(month, score, it) }
            }
            .filter { it.second > 0 }
            .minWithOrNull(
                compareByDescending<Triple<Int, Int, LocalDate>> { it.second }
                    .thenBy { monthDistanceFromToday(it.first) }
            )
            ?: return null
        val firstPair = pairs.minBy { it.second }
        return LocalDate.of(currentYear, likelyMonth.first, firstPair.second)
    }

    private fun weekdayNumberPairs(lines: List<String>): List<Pair<String, Int>> {
        val pairs = mutableListOf<Pair<String, Int>>()
        var pendingDayName: String? = null
        lines.forEach { line ->
            val dayHeader = dayHeaderRegex.matchEntire(normalizeOcrLine(line))
            if (dayHeader != null) {
                val dayName = dayHeader.groupValues[1]
                val inlineDayNumber = dayHeader.groupValues[2].toIntOrNull()
                if (inlineDayNumber != null) {
                    pairs += dayName to inlineDayNumber
                    pendingDayName = null
                } else {
                    pendingDayName = dayName
                }
                return@forEach
            }
            val dayNumber = dayNumberRegex.matchEntire(line)?.value?.toIntOrNull()
                ?: leadingDayNumberRegex.matchEntire(line)?.groupValues?.get(1)?.toIntOrNull()
            if (pendingDayName != null && dayNumber != null) {
                pairs += pendingDayName!! to dayNumber
                pendingDayName = null
            }
        }
        return pairs
    }

    private fun monthDistanceFromToday(month: Int): Int {
        val currentMonth = LocalDate.now().monthValue
        return minOf(abs(month - currentMonth), 12 - abs(month - currentMonth))
    }

    private fun parseDate(match: MatchResult, currentYear: Int): LocalDate? {
        val month = match.groupValues[1].toIntOrNull() ?: return null
        val day = match.groupValues[2].toIntOrNull() ?: return null
        val yearText = match.groupValues[3]
        val year = when {
            yearText.isBlank() -> currentYear
            yearText.length == 2 -> 2000 + yearText.toInt()
            else -> yearText.toInt()
        }
        return try {
            LocalDate.of(year, month, day)
        } catch (_: DateTimeParseException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun dateNearBase(dayNumber: Int, baseDate: LocalDate): LocalDate {
        val candidates = listOf(
            baseDate.withDayOfMonthSafe(dayNumber),
            baseDate.minusMonths(1).withDayOfMonthSafe(dayNumber),
            baseDate.plusMonths(1).withDayOfMonthSafe(dayNumber)
        ).filterNotNull()
        return candidates.minBy { abs(ChronoUnit.DAYS.between(baseDate, it)) }
    }

    private fun dateForWeekday(dayName: String, baseDate: LocalDate): LocalDate {
        val target = dayOfWeekFor(dayName)
        return (0L..6L)
            .map { baseDate.plusDays(it) }
            .firstOrNull { it.dayOfWeek == target }
            ?: baseDate
    }

    private fun dayOfWeekFor(dayName: String): DayOfWeek {
        return when (dayName.lowercase(Locale.US).take(3)) {
            "mon" -> DayOfWeek.MONDAY
            "tue" -> DayOfWeek.TUESDAY
            "wed" -> DayOfWeek.WEDNESDAY
            "thu" -> DayOfWeek.THURSDAY
            "fri" -> DayOfWeek.FRIDAY
            "sat" -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    private fun LocalDate.withDayOfMonthSafe(dayNumber: Int): LocalDate? {
        return runCatching { withDayOfMonth(dayNumber) }.getOrNull()
    }

    private fun normalizeOcrLine(line: String): String {
        return line
            .replace('\u2013', '-')
            .replace('\u2014', '-')
            .replace('\u00a0', ' ')
            .replace("a.m.", "AM", ignoreCase = true)
            .replace("p.m.", "PM", ignoreCase = true)
            .replace("a m", "AM", ignoreCase = true)
            .replace("p m", "PM", ignoreCase = true)
            .replace(Regex("""(?i)\b([ap])\s*\.?\s*m\.?\b""")) { "${it.groupValues[1].uppercase(Locale.US)}M" }
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun String.startsWithPeriod(): Boolean {
        return startsWith("am", ignoreCase = true) || startsWith("pm", ignoreCase = true)
    }

    private fun parseShift(line: String, date: LocalDate, label: String? = null): WorkShift? {
        val timeMatch = timeRangeRegex.find(line) ?: return null
        val endPeriod = timeMatch.groupValues[6]
        val startPeriod = timeMatch.groupValues[3].ifBlank {
            inferStartPeriod(timeMatch.groupValues[1], timeMatch.groupValues[4], endPeriod)
        }
        val start = parseTime(
            hour = timeMatch.groupValues[1],
            minute = timeMatch.groupValues[2],
            period = startPeriod
        )
        val end = parseTime(
            hour = timeMatch.groupValues[4],
            minute = timeMatch.groupValues[5],
            period = endPeriod
        )
        return if (start == null || end == null) {
            null
        } else {
            WorkShift(date = date, start = start, end = end, label = label?.takeIf { it.isNotBlank() } ?: "Work")
        }
    }

    private fun inferStartPeriod(startHourText: String, endHourText: String, endPeriod: String): String {
        val startHour = startHourText.toIntOrNull() ?: return endPeriod
        val endHour = endHourText.toIntOrNull() ?: return endPeriod
        return when {
            endPeriod.startsWith("p", ignoreCase = true) && startHour <= endHour -> "pm"
            endPeriod.startsWith("p", ignoreCase = true) && startHour > endHour -> "am"
            else -> endPeriod
        }
    }

    private fun parseTime(hour: String, minute: String, period: String): LocalTime? {
        val normalized = buildString {
            append(hour)
            append(":")
            append(minute.ifBlank { "00" })
            if (period.isNotBlank()) {
                append(" ")
                append(period.replace(".", "").uppercase(Locale.US))
            }
        }
        val formatters = listOf(
            DateTimeFormatter.ofPattern("h:mm a", Locale.US),
            DateTimeFormatter.ofPattern("H:mm", Locale.US)
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalTime.parse(normalized, formatter) }.getOrNull()
        }
    }

    private fun isNoise(line: String): Boolean {
        return line.contains("publix.org", ignoreCase = true) ||
            line.equals("MENU", ignoreCase = true) ||
            line.equals("Schedule", ignoreCase = true) ||
            line.startsWith("Net hours", ignoreCase = true) ||
            hoursRegex.matches(line) ||
            line.matches(Regex("""^\d+$"""))
    }

    private fun isJunkHeader(line: String): Boolean {
        return line.contains("publix.org", ignoreCase = true) ||
            line.equals("MENU", ignoreCase = true) ||
            line.equals("great", ignoreCase = true) ||
            line.equals("great place", ignoreCase = true) ||
            line.equals("Scheduling", ignoreCase = true) ||
            line.matches(Regex("""^\d+\s*$""")) && line.toIntOrNull()?.let { it > 31 } == true
    }

    private fun isLikelyJobDetail(line: String): Boolean {
        return line.contains("Manager", ignoreCase = true) ||
            line.contains("Clerk", ignoreCase = true) ||
            line.startsWith("Store #", ignoreCase = true) ||
            hoursRegex.matches(line)
    }

    private fun roleAndStoreNear(lines: List<String>, anchorLine: String): String? {
        val index = lines.indexOf(anchorLine)
        if (index < 0) return null
        val role = (listOf(lines[index]) + lines.drop(index + 1).take(4))
            .firstNotNullOfOrNull { line ->
                roleLineRegex.matchEntire(line)?.groupValues?.get(1)
                    ?: line.takeIf { isRoleLine(it) }
            }
        val store = lines.drop(index + 1).take(5).firstOrNull { it.startsWith("Store #", ignoreCase = true) }
        return listOfNotNull(role, store).joinToString(" - ").takeIf { it.isNotBlank() }
    }

    private fun isRoleLine(line: String): Boolean {
        return !isNoise(line) &&
            !offRegex.containsMatchIn(line) &&
            !timeRangeRegex.containsMatchIn(line) &&
            (line.contains("Clerk", ignoreCase = true) || line.contains("Manager", ignoreCase = true))
    }
}
