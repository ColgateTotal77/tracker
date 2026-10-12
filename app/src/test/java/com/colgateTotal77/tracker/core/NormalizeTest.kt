package com.colgateTotal77.tracker.core

import com.colgateTotal77.tracker.core.ProductNameNormalizer.normalize
import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeTest {
    @Test
    fun normalizesLatinLookalikes() {
        val input = "AaаCcсEeеIiіOoоPpрXxхYyу"
        val expected = "ааасссеееіііооорррхххууу"
        assertEquals(expected, normalize(input))
    }

    @Test
    fun normalizesApostrophes() {
        val input = "'`´‘’ʼ＇"
        val expected = "'''''''"
        assertEquals(expected, normalize(input))
    }

    @Test
    fun removesWhitespace() {
        val input = " мо ло ко "
        val expected = "молоко"
        assertEquals(expected, normalize(input))
    }

    @Test
    fun convertsToLowercase() {
        val input = "МОЛОКО"
        val expected = "молоко"
        assertEquals(expected, normalize(input))
    }

    @Test
    fun emptyString() {
        val input = ""
        val expected = ""
        assertEquals(expected, normalize(input))
    }

    @Test
    fun WhiteSpaceOnly() {
        val input = "   "
        val expected = ""
        assertEquals(expected, normalize(input))
    }
}