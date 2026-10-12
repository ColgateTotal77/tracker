package com.colgateTotal77.tracker.core

import com.colgateTotal77.tracker.core.ProductNameNormalizer.repairDisplay
import org.junit.Assert.assertEquals
import org.junit.Test


class RepairDisplayTest {
    @Test
    fun normalizesLatinLookalikes() {
        val input = "AaаCcсEeеIiіOoоPpрXxхYyу"
        val expected = "АааСссЕееІііОооРррХххУуу"
        assertEquals(expected, repairDisplay(input))
    }

    @Test
    fun normalizesApostrophes() {
        val input = "'`´‘’ʼ＇"
        val expected = "'''''''"
        assertEquals(expected, repairDisplay(input))
    }

    @Test
    fun trimsWhitespace() {
        val input = " мо ло ко "
        val expected = "мо ло ко"
        assertEquals(expected, repairDisplay(input))
    }

    @Test
    fun preservesUppercase() {
        val input = "МОЛОКО"
        val expected = "МОЛОКО"
        assertEquals(expected, repairDisplay(input))
    }

    @Test
    fun emptyString() {
        val input = ""
        val expected = ""
        assertEquals(expected, repairDisplay(input))
    }

    @Test
    fun whiteSpaceOnly() {
        val input = "   "
        val expected = ""
        assertEquals(expected, repairDisplay(input))
    }
}