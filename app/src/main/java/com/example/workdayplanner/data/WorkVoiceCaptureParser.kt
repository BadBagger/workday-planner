package com.example.workdayplanner.data

import java.util.Locale

data class WorkVoiceCaptureResult(
    val title: String,
    val cleanedText: String,
    val kind: WorkNoteKind,
    val tags: List<String> = emptyList()
)

object WorkVoiceCaptureParser {
    fun format(rawTranscript: String, type: WorkVoiceCaptureType): WorkVoiceCaptureResult {
        val cleaned = normalizeTranscript(rawTranscript)
        if (cleaned.isBlank()) {
            return WorkVoiceCaptureResult(
                title = type.label,
                cleanedText = "",
                kind = type.noteKind
            )
        }

        return when (type) {
            WorkVoiceCaptureType.OrderList -> formatOrderList(cleaned)
            WorkVoiceCaptureType.TaskList -> formatTaskList(cleaned)
            WorkVoiceCaptureType.Reminder -> formatReminder(cleaned)
            WorkVoiceCaptureType.ManagerHandoff -> formatManagerHandoff(cleaned)
            WorkVoiceCaptureType.TruckNote -> formatSimple(cleaned, "Truck Note", type.noteKind, listOf("Truck", "Voice"))
            WorkVoiceCaptureType.InventoryNote -> formatSimple(cleaned, "Inventory Note", type.noteKind, listOf("Inventory", "Voice"))
            WorkVoiceCaptureType.ShiftNote -> formatSimple(cleaned, "Shift Note", type.noteKind, listOf("Shift note", "Voice"))
        }
    }

    private fun formatOrderList(raw: String): WorkVoiceCaptureResult {
        val lines = splitItems(raw)
            .mapNotNull(::formatOrderItem)
        val text = lines.ifEmpty { listOf("- ${raw.toSentenceCase()}") }.joinToString("\n")
        return WorkVoiceCaptureResult(
            title = "Order List",
            cleanedText = text,
            kind = WorkNoteKind.OrderNote,
            tags = listOf("Order", "Voice")
        )
    }

    private fun formatOrderItem(item: String): String? {
        val trimmed = item.trim().trimEnd('.')
        if (trimmed.isBlank()) return null
        val lower = trimmed.lowercase(Locale.US)
        if (lower.startsWith("check ")) {
            return "- Check ${trimmed.drop(6).toSentenceCase()} inventory"
        }

        val quantity = spokenQuantities.firstNotNullOfOrNull { (phrase, value) ->
            if (lower.contains(" $phrase ") || lower.endsWith(" $phrase")) value else null
        } ?: Regex("""\b(\d+(?:\.\d+)?)\b""").find(trimmed)?.groupValues?.getOrNull(1)

        val unit = when {
            lower.contains("gallon") || lower.contains(" gal") -> "gal"
            lower.contains("case") -> "case"
            lower.contains("box") -> "box"
            else -> ""
        }
        val product = removeQuantityAndUnit(trimmed)
        return if (quantity != null && unit.isNotBlank()) {
            "- ${product.toProductCase()} - $quantity $unit"
        } else if (quantity != null) {
            "- ${product.toProductCase()} - $quantity"
        } else {
            "- ${trimmed.toSentenceCase()}"
        }
    }

    private fun formatTaskList(raw: String): WorkVoiceCaptureResult {
        val lower = raw.lowercase(Locale.US)
        val title = when {
            lower.startsWith("closing task") || lower.startsWith("closing tasks") -> "Closing Tasks"
            lower.startsWith("opening task") || lower.startsWith("opening tasks") -> "Opening Tasks"
            lower.startsWith("truck task") || lower.startsWith("truck tasks") -> "Truck Tasks"
            lower.startsWith("inventory task") || lower.startsWith("inventory tasks") -> "Inventory Tasks"
            else -> "Task List"
        }
        val withoutLead = raw
            .replace(Regex("""(?i)^(closing|opening|truck|inventory)\s+tasks?\s*"""), "")
            .trim()
        val taskItems = mutableListOf<String>()
        val reminders = mutableListOf<String>()
        splitItems(withoutLead).forEach { item ->
            val normalized = item.trim().trimEnd('.')
            if (normalized.isBlank()) return@forEach
            val reminder = normalized.replace(Regex("""(?i)^(remind me to|reminder to|remember to)\s+"""), "")
            if (reminder != normalized || normalized.startsWith("follow up", ignoreCase = true)) {
                reminders += "- ${reminder.toSentenceCase()}"
            } else {
                taskItems += "- ${normalized.toTaskCase()}"
            }
        }
        val blocks = buildList {
            add(title)
            addAll(taskItems.ifEmpty { listOf("- ${withoutLead.toTaskCase()}") })
            if (reminders.isNotEmpty()) {
                add("")
                add("Reminder:")
                addAll(reminders)
            }
        }
        return WorkVoiceCaptureResult(
            title = title,
            cleanedText = blocks.joinToString("\n"),
            kind = WorkNoteKind.ShiftNote,
            tags = listOf("Tasks", "Voice")
        )
    }

    private fun formatReminder(raw: String): WorkVoiceCaptureResult {
        val reminder = raw.replace(Regex("""(?i)^(remind me to|reminder to|remember to|note to)\s+"""), "").trim()
        return WorkVoiceCaptureResult(
            title = "Reminder",
            cleanedText = "- ${reminder.toSentenceCase()}",
            kind = WorkNoteKind.ReminderNote,
            tags = listOf("Reminder", "Voice")
        )
    }

    private fun formatManagerHandoff(raw: String): WorkVoiceCaptureResult {
        val sections = handoffSections(raw)
        val text = if (sections.isEmpty()) {
            formatSimpleLines(raw)
        } else {
            buildList {
                add("What got done:")
                addAll(sections["done"].orEmpty().ifEmpty { listOf("- ") })
                add("")
                add("What still needs done:")
                addAll(sections["needs"].orEmpty().ifEmpty { listOf("- ") })
                add("")
                add("Issues:")
                addAll(sections["issues"].orEmpty().ifEmpty { listOf("- ") })
                add("")
                add("People notes:")
                addAll(sections["people"].orEmpty().ifEmpty { listOf("- ") })
                add("")
                add("Product/order notes:")
                addAll(sections["product"].orEmpty().ifEmpty { listOf("- ") })
                add("")
                add("Follow-up tasks:")
                addAll(sections["follow"].orEmpty().ifEmpty { listOf("- ") })
            }.joinToString("\n")
        }
        return WorkVoiceCaptureResult(
            title = "Manager Handoff",
            cleanedText = text,
            kind = WorkNoteKind.ManagerHandoff,
            tags = listOf("Manager handoff", "Follow-up", "Voice")
        )
    }

    private fun formatSimple(raw: String, title: String, kind: WorkNoteKind, tags: List<String>): WorkVoiceCaptureResult {
        return WorkVoiceCaptureResult(
            title = title,
            cleanedText = formatSimpleLines(raw),
            kind = kind,
            tags = tags
        )
    }

    private fun formatSimpleLines(raw: String): String {
        return splitItems(raw).joinToString("\n") { "- ${it.toTaskCase()}" }
    }

    private fun splitItems(raw: String): List<String> =
        raw.replace(Regex("""(?i)\b(and then|then|also|plus)\b"""), ",")
            .replace(
                Regex(
                    """(?i)\s+\band\b\s+(clean|check|fill|sweep|pull|order|count|stock|rotate|wipe|sanitize|take|call|finish|submit|review|print|label|remind|follow)\b"""
                ),
                ", $1"
            )
            .split(',', ';')
            .map { it.trim() }
            .map { it.removeListLeadWords() }
            .filter { it.isNotBlank() }

    private fun normalizeTranscript(raw: String): String {
        return raw.trim()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""(?i)^(work note|make a note|note that|add note|voice note)\s+"""), "")
            .replace("grab n go", "grab-and-go", ignoreCase = true)
            .replace("grab and go", "grab-and-go", ignoreCase = true)
            .replace("box meat", "boxmeat", ignoreCase = true)
            .replace("g g m", "GGM", ignoreCase = true)
            .replace("c b t", "CBT", ignoreCase = true)
            .trim()
    }

    private fun handoffSections(raw: String): Map<String, List<String>> {
        val lower = raw.lowercase(Locale.US)
        val markers = listOf(
            "done" to Regex("""(?i)\b(what got done|got done|completed|finished)\b"""),
            "needs" to Regex("""(?i)\b(what still needs done|still needs done|needs done|left to do)\b"""),
            "issues" to Regex("""(?i)\b(issues?|problems?)\b"""),
            "people" to Regex("""(?i)\b(people notes?|associate notes?|employee notes?)\b"""),
            "product" to Regex("""(?i)\b(product notes?|order notes?|truck notes?)\b"""),
            "follow" to Regex("""(?i)\b(follow[- ]?up tasks?|follow up|next shift)\b""")
        )
        val hits = markers.mapNotNull { (key, regex) ->
            regex.find(lower)?.let { Triple(key, it.range.first, it.range.last + 1) }
        }.sortedBy { it.second }
        if (hits.isEmpty()) return emptyMap()
        return hits.mapIndexed { index, hit ->
            val nextStart = hits.getOrNull(index + 1)?.second ?: raw.length
            val content = raw.substring(hit.third, nextStart).trim(' ', ':', '-', '.')
            hit.first to splitItems(content).map { "- ${it.toTaskCase()}" }
        }.toMap()
    }

    private fun removeQuantityAndUnit(value: String): String {
        var cleaned = value
        spokenQuantities.keys.sortedByDescending { it.length }.forEach { phrase ->
            cleaned = cleaned.replace(Regex("""(?i)\b${Regex.escape(phrase)}\b"""), "")
        }
        cleaned = cleaned
            .replace(Regex("""(?i)\b\d+(?:\.\d+)?\b"""), "")
            .replace(Regex("""(?i)\b(gallons?|gal|cases?|boxes?)\b"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return cleaned.ifBlank { value.trim() }
    }

    private fun String.removeListLeadWords(): String =
        replace(Regex("""(?i)^(and|also|then|plus)\s+"""), "")
            .replace(Regex("""(?i)^(tasks?|items?|note)\s*[:\-]\s*"""), "")
            .trim()

    private fun String.toSentenceCase(): String {
        val normalized = trim()
            .replace(Regex("""\s+"""), " ")
            .replace("grab and go", "grab-and-go", ignoreCase = true)
        return normalized.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
        }
    }

    private fun String.toTaskCase(): String = toSentenceCase()

    private fun String.toProductCase(): String =
        trim().replace(Regex("""\s+"""), " ")
            .split(' ')
            .joinToString(" ") { word ->
                if (word.length <= 2 && word.all { it.isUpperCase() }) {
                    word
                } else {
                    word.lowercase(Locale.US).replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
                    }
                }
            }

    private val spokenQuantities = linkedMapOf(
        "one and a half" to "1.5",
        "two and a half" to "2.5",
        "three and a half" to "3.5",
        "four and a half" to "4.5",
        "five and a half" to "5.5",
        "one" to "1",
        "two" to "2",
        "three" to "3",
        "four" to "4",
        "five" to "5",
        "six" to "6",
        "seven" to "7",
        "eight" to "8",
        "nine" to "9",
        "ten" to "10"
    )
}

object FutureAiFormatter {
    fun isAvailable(): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    fun format(
        rawTranscript: String,
        type: WorkVoiceCaptureType
    ): WorkVoiceCaptureResult? = null
}
