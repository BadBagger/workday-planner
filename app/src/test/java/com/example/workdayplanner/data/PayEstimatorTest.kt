package com.example.workdayplanner.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PayEstimatorTest {
    @Test
    fun deductsLunchAndEstimatesRegularPay() {
        val estimate = PayEstimator.estimate(
            shifts = listOf(
                WorkShift(date = LocalDate.of(2026, 7, 6), start = LocalTime.of(9, 0), end = LocalTime.of(17, 0))
            ),
            settings = PaySettings(hourlyRate = 20.0, unpaidLunchMinutes = 30)
        )

        assertEquals(8.0, estimate.scheduledHours, 0.01)
        assertEquals(7.5, estimate.paidHours, 0.01)
        assertEquals(150.0, estimate.regularPay, 0.01)
        assertEquals(0.0, estimate.overtimePay, 0.01)
        assertEquals(150.0, estimate.grossPay, 0.01)
    }

    @Test
    fun appliesOvertimeMultiplierAboveThreshold() {
        val shifts = (0L..4L).map { day ->
            WorkShift(
                date = LocalDate.of(2026, 7, 6).plusDays(day),
                start = LocalTime.of(8, 0),
                end = LocalTime.of(17, 0)
            )
        }

        val estimate = PayEstimator.estimate(
            shifts = shifts,
            settings = PaySettings(hourlyRate = 20.0, unpaidLunchMinutes = 0, overtimeThresholdHours = 40.0, overtimeMultiplier = 1.5)
        )

        assertEquals(45.0, estimate.paidHours, 0.01)
        assertEquals(5.0, estimate.overtimeHours, 0.01)
        assertEquals(800.0, estimate.regularPay, 0.01)
        assertEquals(150.0, estimate.overtimePay, 0.01)
        assertEquals(950.0, estimate.grossPay, 0.01)
    }

    @Test
    fun handlesOvernightShift() {
        val estimate = PayEstimator.estimate(
            shifts = listOf(
                WorkShift(date = LocalDate.of(2026, 7, 6), start = LocalTime.of(22, 0), end = LocalTime.of(6, 0))
            ),
            settings = PaySettings(hourlyRate = 15.0, unpaidLunchMinutes = 0)
        )

        assertEquals(8.0, estimate.scheduledHours, 0.01)
        assertEquals(120.0, estimate.grossPay, 0.01)
    }

    @Test
    fun appliesDailyOvertimeWhenEnabled() {
        val estimate = PayEstimator.estimate(
            shifts = listOf(
                WorkShift(date = LocalDate.of(2026, 7, 6), start = LocalTime.of(8, 0), end = LocalTime.of(18, 0))
            ),
            settings = PaySettings(hourlyRate = 20.0, unpaidLunchMinutes = 0, dailyOvertimeThresholdHours = 8.0, overtimeMultiplier = 1.5)
        )

        assertEquals(8.0, estimate.regularHours, 0.01)
        assertEquals(2.0, estimate.overtimeHours, 0.01)
        assertEquals(220.0, estimate.grossPay, 0.01)
    }

    @Test
    fun appliesNightWeekendAndCustomDifferentials() {
        val estimate = PayEstimator.estimate(
            shifts = listOf(
                WorkShift(date = LocalDate.of(2026, 7, 11), start = LocalTime.of(22, 0), end = LocalTime.of(6, 0), label = "Inventory")
            ),
            settings = PaySettings(
                hourlyRate = 20.0,
                unpaidLunchMinutes = 0,
                nightShiftExtraAmount = 1.0,
                weekendExtraAmount = 2.0,
                customShiftTypeLabel = "Inventory",
                customShiftTypeExtraAmount = 3.0
            )
        )

        assertEquals(48.0, estimate.differentialPay, 0.01)
        assertEquals(208.0, estimate.grossPay, 0.01)
    }

    @Test
    fun biweeklyPayPeriodSumsWeeklyOvertime() {
        val shifts = ((0L..4L) + (7L..11L)).map { day ->
            WorkShift(
                date = LocalDate.of(2026, 7, 6).plusDays(day),
                start = LocalTime.of(8, 0),
                end = LocalTime.of(17, 0)
            )
        }
        val state = AppState(
            shifts = shifts,
            paySettings = PaySettings(
                hourlyRate = 20.0,
                unpaidLunchMinutes = 0,
                payPeriodType = PayPeriodType.Biweekly,
                customPayPeriodStart = LocalDate.of(2026, 7, 6)
            )
        )

        val estimate = PayEstimator.estimatePayPeriod(state, LocalDate.of(2026, 7, 10))

        assertEquals(90.0, estimate.paidHours, 0.01)
        assertEquals(10.0, estimate.overtimeHours, 0.01)
        assertEquals(1900.0, estimate.grossPay, 0.01)
    }
}
