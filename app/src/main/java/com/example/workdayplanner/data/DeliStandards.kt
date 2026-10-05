package com.example.workdayplanner.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DeliStandardLine(
    val number: Int,
    val objective: String,
    val detail: String,
    val owner: String = "",
    val done: Boolean = false,
    val time: String = "",
    val initials: String = "",
    val whyNotDone: String = "",
    val whoPicksItUp: String = ""
)

data class DeliDailySheet(
    val date: LocalDate,
    val managerOnOpen: String = "",
    val managerOnClose: String = "",
    val lines: List<DeliStandardLine> = blankLines(),
    val stockerScheduled: String = "",
    val pulledFromStocking: String = "",
    val pulledToDoWhat: String = "",
    val whoCoveredStocking: String = "",
    val stockingFinishedByClose: String = "",
    val productLeftInBack: String = "",
    val handoff: String = ""
) {
    companion object {
        fun blank(date: LocalDate) = DeliDailySheet(date = date, lines = blankLines())

        fun blankLines(): List<DeliStandardLine> = listOf(
            line(1, "Orders placed", "All orders in on time and reviewed."),
            line(2, "Counts complete", "Required counts entered."),
            line(3, "Truck received", "Checked in, cold chain maintained."),
            line(4, "Back stock worked", "Back stock to the floor before truck."),
            line(5, "Truck worked", "Truck product stocked and faced."),
            line(6, "Back room clear", "Nothing left that belongs on the floor."),
            line(7, "Floor full and faced", "Customer-facing shelves and cases stocked."),
            line(8, "Production complete", "Production plan hit for the day."),
            line(9, "Dept clean and organized", "Cleaning tasks done, workstations reset."),
            line(10, "Breaks and meals taken", "Everyone, per policy, timed to the flow of business."),
            line(11, "Safe close-out", "Slicer breakdown with correct PPE (cut gloves on both hands).")
        )

        private fun line(number: Int, objective: String, detail: String) = DeliStandardLine(
            number = number,
            objective = objective,
            detail = detail
        )
    }
}

data class WorkingAgreement(
    val number: Int,
    val prompt: String,
    val choices: List<String> = emptyList(),
    val answer: String = ""
)

data class StandardsTrackerDay(
    val date: LocalDate,
    val marks: Map<String, String> = emptyMap(),
    val notes: String = ""
) {
    val dayLabel: String
        get() = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
}

data class DeliStandardsBook(
    val agreements: List<WorkingAgreement> = agreementPrompts(),
    val departmentManager: String = "",
    val departmentManagerDate: String = "",
    val assistantManager: String = "",
    val assistantManagerDate: String = "",
    val tracker: List<StandardsTrackerDay> = trackerStarting(TRACKER_START),
    val sheets: Map<LocalDate, DeliDailySheet> = emptyMap()
) {
    fun sheet(date: LocalDate): DeliDailySheet = sheets[date] ?: DeliDailySheet.blank(date)

    fun totalN(column: String): Int = tracker.count { it.marks[column] == "N" }

    companion object {
        val TRACKER_START: LocalDate = LocalDate.of(2026, 10, 5)
        const val TITLE = "Deli Daily Standards"
        const val SUBTITLE = "Shared checklist for the department manager and assistant manager. One sheet per day."
        const val GOAL = "Every line checked before close, and anything not done is written down with the reason, so nothing gets silently passed to the next shift."
        val TRACKER_COLUMNS = listOf(
            "orders" to "Orders",
            "counts" to "Counts",
            "back_stock" to "Back stock worked",
            "truck" to "Truck worked",
            "back_room" to "Back room clear",
            "floor" to "Floor stocked",
            "production" to "Production",
            "clean" to "Clean",
            "breaks" to "Breaks on time",
            "stocker" to "Stocker kept in role"
        )

        fun seed(): DeliStandardsBook = DeliStandardsBook()

        fun agreementPrompts(): List<WorkingAgreement> = listOf(
            WorkingAgreement(
                number = 1,
                prompt = "When short-staffed, rank 1 (must happen) to 8 (can slide a day). Lowest rank is what drops, as an agreed decision.",
                choices = listOf(
                    "Orders / counts",
                    "Truck worked",
                    "Back stock worked",
                    "Floor full and faced",
                    "Production",
                    "Cleaning / organization",
                    "Resets / reorganizing projects",
                    "Cross-training"
                )
            ),
            WorkingAgreement(
                number = 2,
                prompt = "The stocking role: when is it OK to pull the stocker, and who covers?"
            ),
            WorkingAgreement(
                number = 3,
                prompt = "Breaks: how much flex is allowed to finish with a customer, within policy?"
            ),
            WorkingAgreement(
                number = 4,
                prompt = "Extra hours: when can a willing part-timer stay longer (no overtime), and who approves?"
            ),
            WorkingAgreement(
                number = 5,
                prompt = "Cross-training: who learns which role next, and how do we introduce the change?"
            ),
            WorkingAgreement(
                number = 6,
                prompt = "What we check together each week (day / time)."
            )
        )

        fun trackerStarting(start: LocalDate): List<StandardsTrackerDay> {
            val monday = start.with(DayOfWeek.MONDAY)
            return (0 until 14).map { StandardsTrackerDay(date = monday.plusDays(it.toLong())) }
        }
    }
}

object DeliStandardsPush {
    fun apply(book: DeliStandardsBook, fields: Map<String, String?>): DeliStandardsBook {
        return when (fields.value("page") ?: "daily") {
            "daily" -> applyDaily(book, fields)
            "agreements" -> applyAgreements(book, fields)
            "tracker" -> applyTracker(book, fields)
            else -> throw IllegalArgumentException("Standards page must be daily, agreements, or tracker.")
        }
    }

    private fun applyDaily(book: DeliStandardsBook, fields: Map<String, String?>): DeliStandardsBook {
        val date = fields.value("date")?.let(LocalDate::parse) ?: LocalDate.now()
        val current = book.sheet(date)
        val lineNumber = fields.value("line")?.toIntOrNull()
        val lines = if (lineNumber == null) {
            current.lines
        } else {
            if (current.lines.none { it.number == lineNumber }) {
                throw IllegalArgumentException("Daily line $lineNumber is not on the sheet.")
            }
            current.lines.map { line ->
                if (line.number != lineNumber) line else line.copy(
                    owner = fields.value("owner") ?: line.owner,
                    done = fields.yesNo("done") ?: line.done,
                    time = fields.value("time") ?: line.time,
                    initials = fields.value("initials") ?: line.initials,
                    whyNotDone = fields.value("why") ?: line.whyNotDone,
                    whoPicksItUp = fields.value("who") ?: line.whoPicksItUp
                )
            }
        }
        val updated = current.copy(
            managerOnOpen = fields.value("manager_open") ?: current.managerOnOpen,
            managerOnClose = fields.value("manager_close") ?: current.managerOnClose,
            lines = lines,
            stockerScheduled = fields.value("stocker_scheduled") ?: current.stockerScheduled,
            pulledFromStocking = fields.value("pulled_from_stocking") ?: current.pulledFromStocking,
            pulledToDoWhat = fields.value("pulled_to_do") ?: current.pulledToDoWhat,
            whoCoveredStocking = fields.value("who_covered_stocking") ?: current.whoCoveredStocking,
            stockingFinishedByClose = fields.value("stocking_finished") ?: current.stockingFinishedByClose,
            productLeftInBack = fields.value("product_left_in_back") ?: current.productLeftInBack,
            handoff = fields.value("handoff") ?: current.handoff
        )
        return book.copy(sheets = book.sheets + (date to updated))
    }

    private fun applyAgreements(book: DeliStandardsBook, fields: Map<String, String?>): DeliStandardsBook {
        val number = fields.value("item")?.toIntOrNull()
        val agreements = if (number == null) {
            book.agreements
        } else {
            if (book.agreements.none { it.number == number }) {
                throw IllegalArgumentException("Working agreement $number is not on the sheet.")
            }
            book.agreements.map { item ->
                if (item.number != number) item else item.copy(answer = fields.value("answer") ?: item.answer)
            }
        }
        return book.copy(
            agreements = agreements,
            departmentManager = fields.value("department_manager") ?: book.departmentManager,
            departmentManagerDate = fields.value("department_manager_date") ?: book.departmentManagerDate,
            assistantManager = fields.value("assistant_manager") ?: book.assistantManager,
            assistantManagerDate = fields.value("assistant_manager_date") ?: book.assistantManagerDate
        )
    }

    private fun applyTracker(book: DeliStandardsBook, fields: Map<String, String?>): DeliStandardsBook {
        val date = fields.value("date")?.let(LocalDate::parse)
            ?: throw IllegalArgumentException("Tracker updates need a date.")
        if (book.tracker.none { it.date == date }) {
            throw IllegalArgumentException("That date is outside the two-week tracker.")
        }
        val column = fields.value("column")
        val mark = fields.value("mark")?.uppercase()
        if (column != null && DeliStandardsBook.TRACKER_COLUMNS.none { it.first == column }) {
            throw IllegalArgumentException("Unknown tracker column \"$column\".")
        }
        if (mark != null && mark != "Y" && mark != "N" && mark != "CLEAR") {
            throw IllegalArgumentException("Tracker marks are Y or N.")
        }
        val tracker = book.tracker.map { day ->
            if (day.date != date) day else {
                val marks = if (column == null) day.marks else {
                    if (mark == null || mark == "CLEAR") day.marks - column else day.marks + (column to mark)
                }
                day.copy(marks = marks, notes = fields.value("notes") ?: day.notes)
            }
        }
        return book.copy(tracker = tracker)
    }

    private fun Map<String, String?>.value(key: String): String? =
        this[key]?.trim()?.takeIf { it.isNotEmpty() && it != "null" }

    private fun Map<String, String?>.yesNo(key: String): Boolean? {
        return when (value(key)?.lowercase()) {
            "true", "yes", "y", "done" -> true
            "false", "no", "n", "open" -> false
            null -> null
            else -> throw IllegalArgumentException("Use yes or no for $key.")
        }
    }
}

fun DeliStandardsBook.toJson(): JSONObject = JSONObject()
    .put("departmentManager", departmentManager)
    .put("departmentManagerDate", departmentManagerDate)
    .put("assistantManager", assistantManager)
    .put("assistantManagerDate", assistantManagerDate)
    .put("agreements", JSONArray(agreements.map { item ->
        JSONObject()
            .put("number", item.number)
            .put("prompt", item.prompt)
            .put("choices", JSONArray(item.choices))
            .put("answer", item.answer)
    }))
    .put("tracker", JSONArray(tracker.map { day ->
        JSONObject()
            .put("date", day.date.toString())
            .put("notes", day.notes)
            .put("marks", JSONObject().apply { day.marks.forEach { (key, value) -> put(key, value) } })
    }))
    .put("sheets", JSONArray(sheets.values.map { it.toJson() }))

fun deliStandardsFromJson(json: JSONObject): DeliStandardsBook {
    val seeded = DeliStandardsBook.seed()
    val agreements = json.optJSONArray("agreements")?.let { array ->
        seeded.agreements.map { prompt ->
            val stored = (0 until array.length()).map { array.getJSONObject(it) }
                .firstOrNull { it.optInt("number") == prompt.number }
            prompt.copy(answer = stored?.optString("answer").orEmpty())
        }
    } ?: seeded.agreements
    val tracker = json.optJSONArray("tracker")?.let { array ->
        val byDate = (0 until array.length()).associate { index ->
            val day = array.getJSONObject(index)
            val date = LocalDate.parse(day.getString("date"))
            val marksJson = day.optJSONObject("marks")
            val marks = marksJson?.keys()?.asSequence()?.associateWith { key -> marksJson.getString(key) }.orEmpty()
            date to StandardsTrackerDay(date, marks, day.optString("notes"))
        }
        seeded.tracker.map { day -> byDate[day.date] ?: day }
    } ?: seeded.tracker
    val sheets = json.optJSONArray("sheets")?.let { array ->
        (0 until array.length()).associate { index ->
            val sheet = sheetFromJson(array.getJSONObject(index))
            sheet.date to sheet
        }
    }.orEmpty()
    return DeliStandardsBook(
        agreements = agreements,
        departmentManager = json.optString("departmentManager"),
        departmentManagerDate = json.optString("departmentManagerDate"),
        assistantManager = json.optString("assistantManager"),
        assistantManagerDate = json.optString("assistantManagerDate"),
        tracker = tracker,
        sheets = sheets
    )
}

private fun DeliDailySheet.toJson(): JSONObject = JSONObject()
    .put("date", date.toString())
    .put("managerOnOpen", managerOnOpen)
    .put("managerOnClose", managerOnClose)
    .put("stockerScheduled", stockerScheduled)
    .put("pulledFromStocking", pulledFromStocking)
    .put("pulledToDoWhat", pulledToDoWhat)
    .put("whoCoveredStocking", whoCoveredStocking)
    .put("stockingFinishedByClose", stockingFinishedByClose)
    .put("productLeftInBack", productLeftInBack)
    .put("handoff", handoff)
    .put("lines", JSONArray(lines.map { line ->
        JSONObject()
            .put("number", line.number)
            .put("owner", line.owner)
            .put("done", line.done)
            .put("time", line.time)
            .put("initials", line.initials)
            .put("whyNotDone", line.whyNotDone)
            .put("whoPicksItUp", line.whoPicksItUp)
    }))

private fun sheetFromJson(json: JSONObject): DeliDailySheet {
    val date = LocalDate.parse(json.getString("date"))
    val stored = json.optJSONArray("lines")
    val lines = DeliDailySheet.blankLines().map { line ->
        val match = stored?.let { array ->
            (0 until array.length()).map { array.getJSONObject(it) }.firstOrNull { it.optInt("number") == line.number }
        }
        if (match == null) line else line.copy(
            owner = match.optString("owner"),
            done = match.optBoolean("done"),
            time = match.optString("time"),
            initials = match.optString("initials"),
            whyNotDone = match.optString("whyNotDone"),
            whoPicksItUp = match.optString("whoPicksItUp")
        )
    }
    return DeliDailySheet(
        date = date,
        managerOnOpen = json.optString("managerOnOpen"),
        managerOnClose = json.optString("managerOnClose"),
        lines = lines,
        stockerScheduled = json.optString("stockerScheduled"),
        pulledFromStocking = json.optString("pulledFromStocking"),
        pulledToDoWhat = json.optString("pulledToDoWhat"),
        whoCoveredStocking = json.optString("whoCoveredStocking"),
        stockingFinishedByClose = json.optString("stockingFinishedByClose"),
        productLeftInBack = json.optString("productLeftInBack"),
        handoff = json.optString("handoff")
    )
}
