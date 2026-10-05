package com.example.workdayplanner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workdayplanner.data.DeliDailySheet
import com.example.workdayplanner.data.DeliStandardLine
import com.example.workdayplanner.data.DeliStandardsBook
import com.example.workdayplanner.data.StandardsTrackerDay
import com.example.workdayplanner.data.WorkingAgreement
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sheetDate = DateTimeFormatter.ofPattern("EEE MMM d", Locale.US)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeliStandardsSection(
    book: DeliStandardsBook,
    today: LocalDate,
    onChange: (DeliStandardsBook) -> Unit
) {
    val sheet = book.sheet(today)
    fun saveSheet(updated: DeliDailySheet) {
        onChange(book.copy(sheets = book.sheets + (updated.date to updated)))
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(DeliStandardsBook.TITLE, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(DeliStandardsBook.SUBTITLE, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(DeliStandardsBook.GOAL, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Check each line during the day. To Do can also fill or update this sheet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Today, ${sheet.date.format(sheetDate)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = sheet.managerOnOpen,
                onValueChange = { saveSheet(sheet.copy(managerOnOpen = it)) },
                label = { Text("Manager on open") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sheet.managerOnClose,
                onValueChange = { saveSheet(sheet.copy(managerOnClose = it)) },
                label = { Text("Manager on close") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            sheet.lines.forEach { line ->
                StandardLineCard(line = line) { updated ->
                    saveSheet(sheet.copy(lines = sheet.lines.map { if (it.number == line.number) updated else it }))
                }
            }

            Text("Stocking coverage", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            YesNoField("Stocker scheduled?", sheet.stockerScheduled) { saveSheet(sheet.copy(stockerScheduled = it)) }
            YesNoField("Pulled from stocking?", sheet.pulledFromStocking) { saveSheet(sheet.copy(pulledFromStocking = it)) }
            OutlinedTextField(
                value = sheet.pulledToDoWhat,
                onValueChange = { saveSheet(sheet.copy(pulledToDoWhat = it)) },
                label = { Text("If pulled, to do what?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = sheet.whoCoveredStocking,
                onValueChange = { saveSheet(sheet.copy(whoCoveredStocking = it)) },
                label = { Text("Who covered stocking?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            YesNoField("Stocking finished by close?", sheet.stockingFinishedByClose) { saveSheet(sheet.copy(stockingFinishedByClose = it)) }
            YesNoField("Product left in back room?", sheet.productLeftInBack) { saveSheet(sheet.copy(productLeftInBack = it)) }

            Text("Notes / handoff to next shift", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = sheet.handoff,
                onValueChange = { saveSheet(sheet.copy(handoff = it)) },
                label = { Text("Handoff") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Our Working Agreements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Fill these in together, then revisit them after the two-week tracker.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            book.agreements.forEach { item ->
                AgreementField(item) { answer ->
                    onChange(book.copy(agreements = book.agreements.map { if (it.number == item.number) it.copy(answer = answer) else it }))
                }
            }
            OutlinedTextField(
                value = book.departmentManager,
                onValueChange = { onChange(book.copy(departmentManager = it)) },
                label = { Text("Department Manager") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = book.departmentManagerDate,
                onValueChange = { onChange(book.copy(departmentManagerDate = it)) },
                label = { Text("Department Manager date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = book.assistantManager,
                onValueChange = { onChange(book.copy(assistantManager = it)) },
                label = { Text("Assistant Manager") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = book.assistantManagerDate,
                onValueChange = { onChange(book.copy(assistantManagerDate = it)) },
                label = { Text("Assistant Manager date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Two-Week Standards Tracker", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                "Tap a cell to mark Y, then N, then clear it. Total N is what you bring to the weekly check-in.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            book.tracker.forEach { day ->
                TrackerDayCard(day) { updated ->
                    onChange(book.copy(tracker = book.tracker.map { if (it.date == day.date) updated else it }))
                }
            }
            Text(
                DeliStandardsBook.TRACKER_COLUMNS.joinToString(" · ") { (key, label) -> "$label ${book.totalN(key)} N" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StandardLineCard(line: DeliStandardLine, onChange: (DeliStandardLine) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChange(line.copy(done = !line.done)) }
            ) {
                Checkbox(checked = line.done, onCheckedChange = { onChange(line.copy(done = it)) })
                Column(Modifier.weight(1f)) {
                    Text("${line.number}. ${line.objective}", fontWeight = FontWeight.SemiBold)
                    Text(line.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(
                value = line.owner,
                onValueChange = { onChange(line.copy(owner = it)) },
                label = { Text("Owner") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = line.time,
                    onValueChange = { onChange(line.copy(time = it)) },
                    label = { Text("Time") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = line.initials,
                    onValueChange = { onChange(line.copy(initials = it)) },
                    label = { Text("Initials") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!line.done) {
                OutlinedTextField(
                    value = line.whyNotDone,
                    onValueChange = { onChange(line.copy(whyNotDone = it)) },
                    label = { Text("If not done, why") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = line.whoPicksItUp,
                    onValueChange = { onChange(line.copy(whoPicksItUp = it)) },
                    label = { Text("Who picks it up") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun YesNoField(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isYes(value),
                onClick = { onChange(if (isYes(value)) "" else "yes") },
                label = { Text("Yes") }
            )
            FilterChip(
                selected = isNo(value),
                onClick = { onChange(if (isNo(value)) "" else "no") },
                label = { Text("No") }
            )
        }
    }
}

@Composable
private fun AgreementField(item: WorkingAgreement, onAnswer: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text("${item.number}. ${item.prompt}", fontWeight = FontWeight.Medium)
        if (item.choices.isNotEmpty()) {
            Text(
                item.choices.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedTextField(
            value = item.answer,
            onValueChange = onAnswer,
            label = { Text("Answer") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackerDayCard(day: StandardsTrackerDay, onChange: (StandardsTrackerDay) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            "${day.dayLabel} ${day.date.monthValue}/${day.date.dayOfMonth}",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DeliStandardsBook.TRACKER_COLUMNS.forEach { (key, label) ->
                val mark = day.marks[key]
                FilterChip(
                    selected = mark == "Y" || mark == "N",
                    onClick = {
                        val next = when (mark) {
                            null, "" -> "Y"
                            "Y" -> "N"
                            else -> null
                        }
                        val marks = if (next == null) day.marks - key else day.marks + (key to next)
                        onChange(day.copy(marks = marks))
                    },
                    label = { Text(if (mark.isNullOrBlank()) label else "$label $mark") }
                )
            }
        }
        OutlinedTextField(
            value = day.notes,
            onValueChange = { onChange(day.copy(notes = it)) },
            label = { Text("Notes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun isYes(value: String) = value.equals("yes", true) || value.equals("y", true) || value.equals("true", true)

private fun isNo(value: String) = value.equals("no", true) || value.equals("n", true) || value.equals("false", true)
