package com.example.workdayplanner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workdayplanner.data.DeliStandardsBook
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sheetDate = DateTimeFormatter.ofPattern("EEE MMM d", Locale.US)

@Composable
fun DeliStandardsSection(book: DeliStandardsBook, today: LocalDate) {
    val sheet = book.sheet(today)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(DeliStandardsBook.TITLE, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(DeliStandardsBook.SUBTITLE, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(DeliStandardsBook.GOAL, style = MaterialTheme.typography.bodyMedium)
            Text("To Do fills this in. Nothing here is typed by hand.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Text("Today, ${sheet.date.format(sheetDate)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Manager on open: ${sheet.managerOnOpen.ifBlank { "—" }}")
            Text("Manager on close: ${sheet.managerOnClose.ifBlank { "—" }}")
            sheet.lines.forEach { line ->
                Text(
                    "${line.number}. ${line.objective}. ${line.detail}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Owner ${line.owner.ifBlank { "—" }} · ${if (line.done) "Done" else "Not done"} · Time ${line.time.ifBlank { "—" }} · Initials ${line.initials.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!line.done && (line.whyNotDone.isNotBlank() || line.whoPicksItUp.isNotBlank())) {
                    Text(
                        "Why / who picks it up: ${line.whyNotDone.ifBlank { "—" }} / ${line.whoPicksItUp.ifBlank { "—" }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text("Stocking coverage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Stocker scheduled? ${yesNo(sheet.stockerScheduled)}")
            Text("Pulled from stocking? ${yesNo(sheet.pulledFromStocking)}")
            Text("If pulled, to do what? ${sheet.pulledToDoWhat.ifBlank { "—" }}")
            Text("Who covered stocking? ${sheet.whoCoveredStocking.ifBlank { "—" }}")
            Text("Stocking finished by close? ${yesNo(sheet.stockingFinishedByClose)}")
            Text("Product left in back room? ${yesNo(sheet.productLeftInBack)}")
            Text("Notes / handoff to next shift", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(sheet.handoff.ifBlank { "—" })

            Text("Our Working Agreements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Fill out once together, then revisit after the two-week tracker. Answers stay blank until To Do is told what was agreed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            book.agreements.forEach { item ->
                Text("${item.number}. ${item.prompt}", fontWeight = FontWeight.Medium)
                if (item.choices.isNotEmpty()) {
                    Text(item.choices.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(item.answer.ifBlank { "Not agreed yet." }, style = MaterialTheme.typography.bodyMedium)
            }
            Text("Department Manager: ${book.departmentManager.ifBlank { "—" }}  Date: ${book.departmentManagerDate.ifBlank { "—" }}")
            Text("Assistant Manager: ${book.assistantManager.ifBlank { "—" }}  Date: ${book.assistantManagerDate.ifBlank { "—" }}")

            Text("Two-Week Standards Tracker", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Mark Y or N. After 14 days, the N count is what gets brought to the weekly check-in.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Day", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    book.tracker.forEach { day ->
                        Text("${day.dayLabel} ${day.date.monthValue}/${day.date.dayOfMonth}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Total N", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
                DeliStandardsBook.TRACKER_COLUMNS.forEach { (key, label) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                        book.tracker.forEach { day ->
                            Text(day.marks[key] ?: "—", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(book.totalN(key).toString(), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Notes", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    book.tracker.forEach { day ->
                        Text(day.notes.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("—", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun yesNo(value: String): String = when (value.lowercase()) {
    "yes", "y", "true" -> "Yes"
    "no", "n", "false" -> "No"
    else -> "—"
}
