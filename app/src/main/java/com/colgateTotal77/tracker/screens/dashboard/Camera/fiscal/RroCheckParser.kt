package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

import org.w3c.dom.Element
import com.colgateTotal77.tracker.core.ProductNameNormalizer

object RroCheckParser {

    fun parse(root: Element): FiscalCheck {
        val dat = firstElement(root, "DAT")
        val entry = firstElement(root, "E")

        return FiscalCheck(
            fiscalNumber = dat?.attr("FN"),
            tin = dat?.attr("TN")?.filter { it.isDigit() },
            receiptNumber = entry?.attr("NO"),
            date = entry?.attr("TS")?.toEpochMillis("yyyyMMddHHmmss") ?: System.currentTimeMillis(),
            amountMinor = entry?.attr("SM")?.toIntOrNull(),
            items = elements(root, "P").map(::parseItem),
        )
    }

    private fun parseItem(element: Element): FiscalItem {
        val totalMinor = element.attr("SM")?.toIntOrNull()
        val quantity = element.attr("Q")?.toIntOrNull() ?: 1000
        val unitPriceMinor = element.attr("PRC")?.toIntOrNull()
            ?: ((totalMinor ?: 0) * 1000L / quantity).toInt()
        return FiscalItem(
            position = element.attr("N")?.toIntOrNull(),
            name = element.attr("NM")?.let(ProductNameNormalizer::repairDisplay),
            barcode = element.attr("CD"),
            quantity = quantity,
            unitPriceMinor = unitPriceMinor,
            totalMinor = totalMinor,
            taxGroup = element.attr("TX"),
        )
    }
}
