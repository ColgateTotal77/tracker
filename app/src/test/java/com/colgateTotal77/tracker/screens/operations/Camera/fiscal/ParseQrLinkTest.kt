package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import org.junit.Assert.assertEquals
import org.junit.Test

class ParseQrLinkTest {
    @Test
    fun acceptsMissingTime() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&id=123456&sm=12.50&fn=3000000001"
        val expected = FiscalQrParams("20260115", null, "123456", "12.50", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun acceptsEmptyTime() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=&id=123456&sm=12.50&fn=3000000001"
        val expected = FiscalQrParams("20260115", "", "123456", "12.50", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsMissingDate() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?time=1230&id=123456&sm=12.50&fn=3000000001"
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsMissingId() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=1230&sm=12.50&fn=3000000001"
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsMissingAmount() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=1230&id=123456&fn=3000000001"
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsMissingFiscalNumber() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=1230&id=123456&sm=12.50"
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsEmptyInput() {
        val input = ""
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun rejectsLinkWithoutQueryString() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check"
        val expected: FiscalQrParams? = null
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun extractsParamsRegardlessOfOrder() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?fn=3000000001&sm=12.50&id=123456&time=1230&date=20260115"
        val expected = FiscalQrParams("20260115", "1230", "123456", "12.50", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun decodesEncodedTime() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=17%3A20&id=123456&sm=12.50&fn=3000000001"
        val expected = FiscalQrParams("20260115", "17:20", "123456", "12.50", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun ignoresUnrelatedParams() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?extra=value&date=20260115&time=1230&id=123456&sm=12.50&fn=3000000001"
        val expected = FiscalQrParams("20260115", "1230", "123456", "12.50", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun extractsParamsFromTaxCabinetLink() {
        val input = "https://cabinet.tax.gov.ua/cashregs/check?date=20260115&time=1151&id=10003&sm=144.80&fn=3000000001"
        val expected = FiscalQrParams("20260115", "1151", "10003", "144.80", "3000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }

    @Test
    fun extractsParamsFromSfsCabinetLinkWithColonSeparatedTime() {
        val input = "https://cabinet.sfs.gov.ua/cashregs/check?date=20260115&time=12:00&id=1000000003&sm=3699.00&fn=4000000001"
        val expected = FiscalQrParams("20260115", "12:00", "1000000003", "3699.00", "4000000001")
        assertEquals(expected, TaxApi.parseQrLink(input))
    }
}
