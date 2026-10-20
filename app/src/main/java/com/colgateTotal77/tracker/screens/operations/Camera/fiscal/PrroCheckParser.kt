package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

import androidx.compose.runtime.toLong
import java.math.BigDecimal
import org.w3c.dom.Element
import com.colgateTotal77.tracker.core.ProductNameNormalizer

object PrroCheckParser {

    fun parse(root: Element): FiscalCheck {
        val head = firstElement(root, "CHECKHEAD")
        val total = firstElement(root, "CHECKTOTAL")
        val body = firstElement(root, "CHECKBODY")

        val orderDate = head?.textOf("ORDERDATE")
        val orderTime = head?.textOf("ORDERTIME")
        val dateTime = listOfNotNull(orderDate, orderTime).joinToString("").takeIf { it.isNotBlank() }

        return FiscalCheck(
            fiscalNumber = head?.textOf("CASHREGISTERNUM"),
            tin = head?.textOf("TIN"),
            receiptNumber = head?.textOf("ORDERNUM"),
            date = dateTime?.toEpochMillis("ddMMyyyyHHmmss") ?: System.currentTimeMillis(),
            amountMinor = total?.textOf("SUM")?.toMinorMinor(),
            items = elements(body ?: root, "ROW").map(::parseItem),
        )
    }

    private fun parseItem(element: Element): FiscalItem {
        val quantity = (element.textOf("AMOUNT")?.toBigDecimalOrNull()
            ?: BigDecimal.ONE).multiply(BigDecimal(1000)).toInt()
        val price = element.textOf("PRICE")?.toMinorMinor()
        val cost = element.textOf("COST")?.toMinorMinor()
        return FiscalItem(
            position = element.attr("ROWNUM")?.toIntOrNull(),
            name = element.textOf("NAME")?.let(ProductNameNormalizer::repairDisplay),
            barcode = null,
            quantity = quantity,
            unitPriceMinor = price ?: ((cost ?: 0L) * 1000L / quantity),
            totalMinor = cost,
        )
    }
}
