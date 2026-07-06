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
}
