package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiDateTimeTest {
    @Test
    fun formatsCompactHoursAndMinutes() {
        val input = FiscalQrParams("20261009", "1451", "123", "12.50", "3000000001")
        val expected = "2026-10-09 14:51:00"
        assertEquals(expected, input.apiDateTime)
    }

    @Test
    fun formatsColonSeparatedHoursAndMinutes() {
        val input = FiscalQrParams("20260209", "17:20", "123", "12.50", "3000000001")
        val expected = "2026-02-09 17:20:00"
        assertEquals(expected, input.apiDateTime)
    }

    @Test
    fun preservesSeconds() {
        val input = FiscalQrParams("20260115", "123045", "123", "12.50", "3000000001")
        val expected = "2026-01-15 12:30:45"
        assertEquals(expected, input.apiDateTime)
    }

    @Test
    fun padsMinutesAndSecondsForHoursOnly() {
        val input = FiscalQrParams("20260115", "12", "123", "12.50", "3000000001")
        val expected = "2026-01-15 12:00:00"
        assertEquals(expected, input.apiDateTime)
    }

    @Test
    fun usesMidnightForMissingTime() {
        val input = FiscalQrParams("20260115", null, "123", "12.50", "3000000001")
        val expected = "2026-01-15 00:00:00"
        assertEquals(expected, input.apiDateTime)
    }

    @Test
    fun usesMidnightForEmptyTime() {
        val input = FiscalQrParams("20260115", "", "123", "12.50", "3000000001")
        val expected = "2026-01-15 00:00:00"
        assertEquals(expected, input.apiDateTime)
    }
}
