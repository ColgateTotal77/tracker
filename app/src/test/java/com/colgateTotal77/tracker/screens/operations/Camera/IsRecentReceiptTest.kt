package com.colgateTotal77.tracker.screens.operations.Camera

import com.colgateTotal77.tracker.screens.operations.Camera.fiscal.FiscalQrParams
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class IsRecentReceiptTest {
    private val now = ZonedDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneId.of("Europe/Kyiv"))
        .toInstant().toEpochMilli()

    @Test
    fun rejectsMissingParams() {
        val input: FiscalQrParams? = null
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsRecentReceipt() {
        val input = FiscalQrParams("20260114", "120000", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun rejectsReceiptOlderThanTwoDays() {
        val input = FiscalQrParams("20260112", "120000", "123", "12.50", "3000000001")
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun rejectsFutureReceipt() {
        val input = FiscalQrParams("20260115", "120001", "123", "12.50", "3000000001")
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun rejectsInvalidCalendarDate() {
        val input = FiscalQrParams("20260230", "120000", "123", "12.50", "3000000001")
        val inputNow = Instant.parse("2026-03-01T10:00:00Z").toEpochMilli()
        val expected = false
        assertEquals(expected, isRecentReceipt(input, inputNow))
    }

    @Test
    fun rejectsInvalidHour() {
        val input = FiscalQrParams("20260114", "25:00", "123", "12.50", "3000000001")
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsHoursOnly() {
        val input = FiscalQrParams("20260115", "12", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsCompactHoursAndMinutes() {
        val input = FiscalQrParams("20260115", "1200", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsColonSeparatedHoursAndMinutes() {
        val input = FiscalQrParams("20260115", "12:00", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsHoursMinutesAndSeconds() {
        val input = FiscalQrParams("20260115", "12:00:00", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsMissingTimeAsMidnight() {
        val input = FiscalQrParams("20260115", null, "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsEmptyTimeAsMidnight() {
        val input = FiscalQrParams("20260115", "", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun rejectsInvalidDateLength() {
        val input = FiscalQrParams("202601", "1200", "123", "12.50", "3000000001")
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun rejectsInvalidTimeLength() {
        val input = FiscalQrParams("20260115", "120", "123", "12.50", "3000000001")
        val expected = false
        assertEquals(expected, isRecentReceipt(input, now))
    }

    @Test
    fun acceptsReceiptExactlyTwoDaysOld() {
        val input = FiscalQrParams("20260113", "120000", "123", "12.50", "3000000001")
        val expected = true
        assertEquals(expected, isRecentReceipt(input, now))
    }
}
