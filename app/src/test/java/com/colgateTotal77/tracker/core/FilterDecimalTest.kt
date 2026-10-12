package com.colgateTotal77.tracker.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FilterDecimalTest {
    @Test
    fun preservesDecimalPoint() {
        val input = "12.34"
        val expected = "12.34"
        assertEquals(expected, input.filterDecimal())
    }

    @Test
    fun replacesDecimalCommaWithPoint() {
        val input = "12,34"
        val expected = "12.34"
        assertEquals(expected, input.filterDecimal())
    }

    @Test
    fun removesWhitespaceAndCurrencyText() {
        val input = " 12,34 грн"
        val expected = "12.34"
        assertEquals(expected, input.filterDecimal())
    }

    @Test
    fun handlesEmptyInput() {
        val input = ""
        val expected = ""
        assertEquals(expected, input.filterDecimal())
    }

    @Test
    fun removesInputWithoutDigitsOrSeparators() {
        val input = "abc"
        val expected = ""
        assertEquals(expected, input.filterDecimal())
    }
}
