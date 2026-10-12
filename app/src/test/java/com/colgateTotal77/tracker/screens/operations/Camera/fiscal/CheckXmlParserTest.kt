package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.xml.sax.SAXParseException
import java.io.IOException
import java.time.Instant
import java.util.TimeZone

class CheckXmlParserTest {
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun useKyivTimeZone() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun parsesPrroReceipt() {
        val input = fixture("prro.xml")
        val expected = FiscalCheck(
            fiscalNumber = "4000000001",
            tin = "12345678",
            receiptNumber = "10001",
            date = Instant.parse("2026-01-15T10:00:38Z").toEpochMilli(),
            amountMinor = 369900,
            items = listOf(
                FiscalItem(
                    position = 1,
                    name = "Термінал Mоtо G06 4/64 TЕNDRІL (РBА20000UА) (S/N)ІMЕІ 000000000000001",
                    barcode = null,
                    quantity = 1000,
                    unitPriceMinor = 369900,
                    totalMinor = 369900,
                ),
            ),
        )
        assertEquals(expected, CheckXmlParser.parse(input))
    }

    @Test
    fun parsesRroReceipt() {
        val input = fixture("rro.xml")
        val expected = FiscalCheck(
            fiscalNumber = "3000000001",
            tin = "123456780002",
            receiptNumber = "10003",
            date = Instant.parse("2026-01-15T09:51:09Z").toEpochMilli(),
            amountMinor = 14480,
            items = listOf(
                FiscalItem(
                    position = 4,
                    name = "Папір туалетний 1 шт Розумний вибір рожевий з ти",
                    barcode = "2999300057921",
                    quantity = 2000,
                    unitPriceMinor = 1490,
                    totalMinor = 2980,
                ),
                FiscalItem(
                    position = 5,
                    name = "Сметана 180 г ПростоНаше 15% п/стакан",
                    barcode = "4820226169031",
                    quantity = 1000,
                    unitPriceMinor = 3590,
                    totalMinor = 3590,
                ),
                FiscalItem(
                    position = 6,
                    name = "Батон 300 гр Ново-Баварський Студентський різани",
                    barcode = "4820117542547",
                    quantity = 2000,
                    unitPriceMinor = 1770,
                    totalMinor = 3540,
                ),
                FiscalItem(
                    position = 7,
                    name = "Соус 200 г Щедро Сирний д/пак",
                    barcode = "4823097408766",
                    quantity = 1000,
                    unitPriceMinor = 4370,
                    totalMinor = 4370,
                ),
            ),
        )
        assertEquals(expected, CheckXmlParser.parse(input))
    }

    @Test
    fun rejectsUnknownRootElement() {
        val input = "<UNKNOWN />".toByteArray()
        val expected = IOException::class.java
        assertThrows(expected) { CheckXmlParser.parse(input) }
    }

    @Test
    fun rejectsMalformedXml() {
        val input = "<CHECK><CHECKHEAD></CHECK>".toByteArray()
        val expected = SAXParseException::class.java
        assertThrows(expected) { CheckXmlParser.parse(input) }
    }

    private fun fixture(name: String): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/receipts/$name")) {
            "Missing receipt fixture: $name"
        }.use { it.readBytes() }
}
