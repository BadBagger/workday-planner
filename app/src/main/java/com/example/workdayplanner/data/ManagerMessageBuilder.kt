package com.example.workdayplanner.data

enum class ManagerMessageType(val label: String) {
    TimeOff("Time off"),
    ShiftSwap("Shift swap"),
    ScheduleQuestion("Schedule question"),
    PayMistake("Pay mistake"),
    Coverage("Need coverage"),
    HoursRequest("Hours request")
}

object ManagerMessageBuilder {
    fun build(type: ManagerMessageType, detail: String): String {
        val cleanDetail = detail.trim().ifBlank { "the details below" }
        return when (type) {
            ManagerMessageType.TimeOff ->
                "Hi, I wanted to ask about taking time off for $cleanDetail. Please let me know if there is anything else you need from me to request it."
            ManagerMessageType.ShiftSwap ->
                "Hi, I wanted to ask if it would be okay for me to swap $cleanDetail. I can confirm the coverage details before anything is changed."
            ManagerMessageType.ScheduleQuestion ->
                "Hi, I had a question about my schedule: $cleanDetail. Could you confirm when you get a chance?"
            ManagerMessageType.PayMistake ->
                "Hi, I think there may be a pay or punch issue with $cleanDetail. Could we review it when you have a chance?"
            ManagerMessageType.Coverage ->
                "Hi, I may need coverage for $cleanDetail. Please let me know the best way to handle it."
            ManagerMessageType.HoursRequest ->
                "Hi, I wanted to ask about $cleanDetail. Please let me know if more or fewer hours are possible on the upcoming schedule."
        }
    }
}
