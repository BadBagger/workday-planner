package com.example.workdayplanner.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

object TrainingTextParser {
    private val datePatterns = listOf(
        Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})\b"""),
        Regex("""\b([A-Za-z]{3,9})\.?\s+(\d{1,2}),?\s+(\d{4})\b""")
    )
    private val compactDateFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMM d yyyy")
        .toFormatter(Locale.US)

    fun parse(text: String, today: LocalDate = LocalDate.now()): List<TrainingItem> {
        val rows = text.lineSequence()
            .map { it.trim().replace(Regex("""\s{2,}"""), " ") }
            .filter { it.length >= 6 }
            .filterNot { line -> line.lowercase().let { it == "associate" || it.contains("training due") } }
            .mapNotNull { line -> parseLine(line, today) }
            .toList()

        return rows.distinctBy { "${it.associateName.lowercase()}|${it.trainingTitle.lowercase()}|${it.dueDate}" }
    }

    private fun parseLine(line: String, today: LocalDate): TrainingItem? {
        val dateMatch = datePatterns.firstNotNullOfOrNull { it.find(line) }
        val dueDate = dateMatch?.let { parseDate(it.value, today) }
        val withoutDate = dateMatch?.let { line.removeRange(it.range).trim(' ', '-', '|', ',', '\t') } ?: line
        val parts = withoutDate.split(Regex("""\s[-|]\s|\t""")).map { it.trim() }.filter { it.isNotBlank() }

        val associate: String
        val training: String
        if (parts.size >= 2) {
            associate = parts.first()
            training = parts.drop(1).joinToString(" - ")
        } else {
            val tokens = withoutDate.split(" ").filter { it.isNotBlank() }
            if (tokens.size < 3) return null
            val nameTokenCount = if (tokens.size >= 4 && tokens[2].length == 1) 3 else 2
            associate = tokens.take(nameTokenCount).joinToString(" ")
            training = tokens.drop(nameTokenCount).joinToString(" ").ifBlank { "Training" }
        }

        if (!associate.any(Char::isLetter) || !training.any(Char::isLetter)) return null

        return TrainingItem(
            associateName = associate.cleanName(),
            trainingTitle = training.cleanTrainingTitle(),
            dueDate = dueDate,
            sourceText = line
        )
    }

    private fun parseDate(raw: String, today: LocalDate): LocalDate? {
        val normalized = raw.replace(",", "").replace(".", "")
        datePatterns[0].matchEntire(raw)?.let { match ->
            val month = match.groupValues[1].toIntOrNull() ?: return null
            val day = match.groupValues[2].toIntOrNull() ?: return null
            val yearRaw = match.groupValues[3].toIntOrNull() ?: return null
            val year = if (yearRaw < 100) 2000 + yearRaw else yearRaw
            return runCatching { LocalDate.of(year, month, day) }.getOrNull()
        }
        return try {
            LocalDate.parse(normalized, compactDateFormatter)
        } catch (_: DateTimeParseException) {
            parseMonthDay(normalized, today)
        }
    }

    private fun parseMonthDay(raw: String, today: LocalDate): LocalDate? {
        val formatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d")
            .parseDefaulting(ChronoField.YEAR, today.year.toLong())
            .toFormatter(Locale.US)
        return runCatching { LocalDate.parse(raw, formatter) }.getOrNull()
    }

    private fun String.cleanName(): String = split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.lowercase().replaceFirstChar { it.titlecase() } }

    private fun String.cleanTrainingTitle(): String = trim(' ', '-', '|', ',')
        .replace(Regex("""\s+"""), " ")
        .ifBlank { "Training" }
}
