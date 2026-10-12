package com.colgateTotal77.tracker.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatMoneyTest {
    @Test
    fun formatsZero() {
        val input = 0
        val expected = "0.00"
        assertEquals(expected, formatMoney(input))
    }

    @Test
    fun formatsSingleMinorUnit() {
        val input = 1
        val expected = "0.01"
        assertEquals(expected, formatMoney(input))
    }

    @Test
    fun preservesTrailingZeroForAmountsBelowOneUnit() {
        val input = 10
        val expected = "0.10"
        assertEquals(expected, formatMoney(input))
    }

    @Test
    fun preservesTwoDecimalPlacesForWholeAmounts() {
        val input = 100
        val expected = "1.00"
        assertEquals(expected, formatMoney(input))
    }

    @Test
    fun formatsAmountWithFractionalPart() {
        val input = 12345
        val expected = "123.45"
        assertEquals(expected, formatMoney(input))
    }

    @Test
    fun formatsNegativeAmount() {
        val input = -123
        val expected = "-1.23"
        assertEquals(expected, formatMoney(input))
    }
}
