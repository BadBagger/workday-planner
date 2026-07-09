package com.example.workdayplanner.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

enum class RepeatRule {
    None,
    Daily,
    Weekdays,
    Weekly,
    CustomDays,
    EveryWorkday,
    OpeningShifts,
    ClosingShifts,
    TruckDays
}

enum class TaskTimingRule(val label: String) {
    AtTime("At a time"),
    BeforeNextShift("Before next shift"),
    DuringShift("During shift"),
    AfterShift("After shift"),
    WorkdaysOnly("On workdays only")
}

enum class LinkedShiftType(val label: String) {
    Any("Any shift"),
    Opening("Opening shift"),
    Closing("Closing shift"),
    Truck("Truck day"),
    Inventory("Inventory day")
}

enum class CarryOverBehavior(val label: String) {
    None("Do not roll over"),
    NextWorkday("Roll to next workday")
}

enum class AppThemeStyle(val label: String, val premium: Boolean) {
    Default("Workday Default", false),
    GraphitePro("Graphite Pro", true),
    NightShift("Night Shift", true),
    PayrollGreen("Payroll Green", true),
    SteelBlueCollar("Steel Blue Collar", true),
    MinimalInk("Minimal Ink", true),
    SunriseShift("Sunrise Shift", true),
    DeliBoard("Deli Board", true),
    FocusPlum("Focus Plum", true);

    companion object {
        fun fromStored(value: String): AppThemeStyle {
            return when (value) {
                "Classic", "Emerald", "Logo" -> Default
                "Sunrise" -> SunriseShift
                else -> runCatching { valueOf(value) }.getOrDefault(Default)
            }
        }
    }
}

typealias AccentStyle = AppThemeStyle

enum class AppearanceMode(val label: String) {
    Light("Light"),
    Dark("Dark"),
    System("Follow system");

    companion object {
        fun fromStored(value: String?, legacyDarkMode: Boolean): AppearanceMode {
            return runCatching { valueOf(value.orEmpty()) }
                .getOrDefault(if (legacyDarkMode) Dark else Light)
        }
    }
}

enum class TaskCategory(val label: String) {
    General("General"),
    Orders("Orders"),
    Cleaning("Cleaning"),
    Prep("Prep"),
    Admin("Admin"),
    Personal("Personal")
}

enum class TaskPriority(val label: String, val sortWeight: Int) {
    Low("Low", 0),
    Normal("Normal", 1),
    High("High", 2),
    Critical("Critical", 3)
}

enum class WidgetLayoutMode(val label: String) {
    Compact("Compact"),
    Standard("Standard"),
    Detailed("Detailed")
}

enum class WorkNoteKind(val label: String) {
    General("General"),
    Order("Order"),
    Cleaning("Cleaning"),
    Customer("Customer"),
    Manager("Manager"),
    Issue("Issue"),
    FollowUp("Follow-up"),
    Meeting("Meeting")
}

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val category: TaskCategory = TaskCategory.General,
    val priority: TaskPriority = TaskPriority.Normal,
    val deadline: LocalDateTime? = null,
    val alarmAt: LocalDateTime? = null,
    val repeatRule: RepeatRule = RepeatRule.None,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val skipDaysOff: Boolean = true,
    val workRelated: Boolean = true,
    val linkedShiftId: String? = null,
    val linkedShiftType: LinkedShiftType = LinkedShiftType.Any,
    val timingRule: TaskTimingRule = TaskTimingRule.AtTime,
    val carryOverBehavior: CarryOverBehavior = CarryOverBehavior.None,
    val alarmOffsetMinutes: Long = 30,
    val completed: Boolean = false
)

data class WorkNote(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val text: String,
    val kind: WorkNoteKind = WorkNoteKind.General,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class WorkImage(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val title: String,
    val imagePath: String,
    val detectedText: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class TrainingItem(
    val id: String = UUID.randomUUID().toString(),
    val associateName: String,
    val trainingTitle: String,
    val dueDate: LocalDate? = null,
    val sourceText: String = "",
    val importedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class WorkShift(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val label: String = "Work",
    val location: String = "",
    val notes: String = "",
    val patternId: String? = null
)

enum class ShiftTemplateKind {
    Work,
    DayOff,
    Vacation,
    Sick
}

data class ShiftTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val label: String = "Work",
    val start: LocalTime = LocalTime.of(9, 0),
    val end: LocalTime = LocalTime.of(17, 0),
    val location: String = "",
    val notes: String = "",
    val kind: ShiftTemplateKind = ShiftTemplateKind.Work,
    val builtIn: Boolean = false
)

enum class ShiftPatternDayKind {
    Work,
    Off
}

data class ShiftPatternDay(
    val index: Int,
    val kind: ShiftPatternDayKind = ShiftPatternDayKind.Off,
    val label: String = "Work",
    val start: LocalTime = LocalTime.of(9, 0),
    val end: LocalTime = LocalTime.of(17, 0),
    val location: String = "",
    val notes: String = ""
)

data class ShiftPattern(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: LocalDate = LocalDate.now(),
    val cycleLength: Int = 7,
    val days: List<ShiftPatternDay> = emptyList(),
    val endDate: LocalDate? = null,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class ShiftPatternPreview(
    val shifts: List<WorkShift>,
    val daysOff: Set<LocalDate>
)

data class TaskTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val title: String,
    val notes: String = "",
    val category: TaskCategory = TaskCategory.General,
    val priority: TaskPriority = TaskPriority.Normal,
    val repeatRule: RepeatRule = RepeatRule.None,
    val reminderEnabled: Boolean = false,
    val workRelated: Boolean = true,
    val linkedShiftType: LinkedShiftType = LinkedShiftType.Any,
    val timingRule: TaskTimingRule = TaskTimingRule.AtTime,
    val carryOverBehavior: CarryOverBehavior = CarryOverBehavior.None,
    val alarmOffsetMinutes: Long = 30,
    val builtIn: Boolean = false
)

data class WorkEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val location: String = ""
)

data class PaySettings(
    val hourlyRate: Double = 0.0,
    val unpaidLunchMinutes: Int = 30,
    val overtimeThresholdHours: Double = 40.0,
    val overtimeMultiplier: Double = 1.5,
    val dailyOvertimeThresholdHours: Double = 0.0,
    val nightShiftExtraAmount: Double = 0.0,
    val weekendExtraAmount: Double = 0.0,
    val customShiftTypeLabel: String = "",
    val customShiftTypeExtraAmount: Double = 0.0,
    val payPeriodType: PayPeriodType = PayPeriodType.Weekly,
    val customPayPeriodStart: LocalDate = LocalDate.now(),
    val showPayOnDashboard: Boolean = false,
    val estimatedTaxRate: Double = 18.0,
    val estimatedDeductionRate: Double = 5.0
)

enum class PayPeriodType(val label: String) {
    Weekly("Weekly"),
    Biweekly("Biweekly"),
    SemiMonthly("Semi-monthly"),
    Custom("Custom start date")
}

enum class PremiumFeature(val title: String, val value: String) {
    UnlimitedImports("Unlimited screenshot imports", "Import as many schedule screenshots as you need."),
    AdvancedOcr("Advanced OCR correction", "Review, fix, and confirm messy schedule scans faster."),
    ShiftPatterns("Rotating shift patterns", "Generate 4-on/4-off, 5-on/2-off, and custom cycles."),
    PayEstimator("Pay and overtime estimator", "Estimate scheduled gross pay before payday."),
    CalendarSync("Calendar export/sync", "Copy your work schedule to your device calendar."),
    TaskTemplates("Task templates", "Reuse opening, closing, truck, and checklist routines."),
    AdvancedTaskRules("Advanced task rules", "Link tasks to shifts, workdays, and carry-over behavior."),
    Widgets("Widgets", "See workday details from your home screen."),
    BackupExport("Backup/export", "Export local data when you need a copy."),
    ThemeCustomization("Theme customization", "Adjust accent and widget display styles.")
}

data class PremiumEntitlement(
    val isPremium: Boolean = false,
    val mockPremiumEnabled: Boolean = false,
    val importMonth: String = "",
    val screenshotImportsThisMonth: Int = 0
) {
    fun has(feature: PremiumFeature): Boolean = isPremium || mockPremiumEnabled
}

data class TimecardEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate = LocalDate.now(),
    val clockIn: LocalDateTime? = null,
    val lunchStart: LocalDateTime? = null,
    val lunchEnd: LocalDateTime? = null,
    val clockOut: LocalDateTime? = null,
    val note: String = ""
)

data class AppState(
    val tasks: List<TaskItem> = emptyList(),
    val notes: List<WorkNote> = emptyList(),
    val images: List<WorkImage> = emptyList(),
    val events: List<WorkEvent> = emptyList(),
    val shifts: List<WorkShift> = emptyList(),
    val daysOff: Set<LocalDate> = emptySet(),
    val dayOffTypes: Map<LocalDate, ShiftTemplateKind> = emptyMap(),
    val defaultDaysOff: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    val appearanceMode: AppearanceMode = AppearanceMode.Light,
    val darkMode: Boolean = false,
    val accentStyle: AccentStyle = AccentStyle.Default,
    val widgetLayoutMode: WidgetLayoutMode = WidgetLayoutMode.Standard,
    val selectedCalendarId: Long? = null,
    val paySettings: PaySettings = PaySettings(),
    val timecards: List<TimecardEntry> = emptyList(),
    val trainingItems: List<TrainingItem> = emptyList(),
    val shiftTemplates: List<ShiftTemplate> = emptyList(),
    val taskTemplates: List<TaskTemplate> = emptyList(),
    val shiftPatterns: List<ShiftPattern> = emptyList(),
    val premium: PremiumEntitlement = PremiumEntitlement(),
    val onboardingCompleted: Boolean = false
)
