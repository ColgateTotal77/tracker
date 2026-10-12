package com.colgateTotal77.tracker.core

import org.junit.Assert.assertEquals
import org.junit.Test

class GetMeasureUnitTest {
    @Test
    fun treatsWholeQuantityWithoutNameAsPieces() {
        val inputQuantity = 1000
        val inputName: String? = null
        val expected = MeasureUnit.PIECE
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun treatsWholeQuantityWithoutWeightMarkerAsPieces() {
        val inputQuantity = 2000
        val inputName = "Молоко"
        val expected = MeasureUnit.PIECE
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun treatsFractionalQuantityBelowOneUnitAsGrams() {
        val inputQuantity = 500
        val inputName: String? = null
        val expected = MeasureUnit.G
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun treatsFractionalQuantityAboveOneUnitAsKilograms() {
        val inputQuantity = 1500
        val inputName: String? = null
        val expected = MeasureUnit.KG
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun recognizesAbbreviatedWeightMarkerAtKilogramBoundary() {
        val inputQuantity = 1000
        val inputName = "Цукор ваг."
        val expected = MeasureUnit.KG
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun recognizesWeightMarkerBelowOneKilogram() {
        val inputQuantity = 500
        val inputName = "Цукор ваговий"
        val expected = MeasureUnit.G
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun recognizesUppercaseWeightMarker() {
        val inputQuantity = 1000
        val inputName = "ЦУКОР ВАГОВИЙ"
        val expected = MeasureUnit.KG
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }

    @Test
    fun ignoresWeightMarkerInsideUnrelatedWord() {
        val inputQuantity = 1000
        val inputName = "Перевага"
        val expected = MeasureUnit.PIECE
        assertEquals(expected, getMeasureUnit(inputQuantity, inputName))
    }
}
