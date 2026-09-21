package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object CheckXmlParser {

    fun parse(xmlBytes: ByteArray): FiscalCheck {
        val factory = DocumentBuilderFactory.newInstance()
        listOf(
            "http://apache.org/xml/features/disallow-doctype-decl" to true,
            "http://xml.org/sax/features/external-general-entities" to false,
            "http://xml.org/sax/features/external-parameter-entities" to false,
        ).forEach { (feature, value) ->
            try {
                factory.setFeature(feature, value)
            } catch (_: Exception) {
            }
        }

        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(xmlBytes))
        document.documentElement.normalize()
        val root = document.documentElement

        val dat = firstElement(root, "DAT")
        val entry = firstElement(root, "E")
        val mac = firstElement(root, "MAC")
        val payments = elements(root, "M")

        return FiscalCheck(
            fiscalNumber = dat?.getAttribute("FN")?.takeIf { it.isNotBlank() },
            tin = dat?.getAttribute("TN")?.filter { it.isDigit() }?.takeIf { it.isNotBlank() },
            registrarSerial = dat?.getAttribute("ZN")?.takeIf { it.isNotBlank() },
            receiptNumber = entry?.getAttribute("NO")?.takeIf { it.isNotBlank() },
            date = entry?.getAttribute("TS")?.takeIf { it.isNotBlank() }?.toEpochMillis() ?: System.currentTimeMillis(),
            amountMinor = entry?.getAttribute("SM")?.toIntOrNull(),
            payments = payments.map(::parsePayment),
            items = elements(root, "P").map(::parseItem),
            mac = mac?.textContent?.trim()?.takeIf { it.isNotBlank() },
            macDi = mac?.getAttribute("DI")?.takeIf { it.isNotBlank() },
        )
    }

    private fun parseItem(element: Element): FiscalItem {
        val totalMinor = element.getAttribute("SM").toIntOrNull()
        val quantity = element.getAttribute("Q").toIntOrNull() ?: 1000
        val unitPriceMinor = element.getAttribute("PRC").toIntOrNull()
            ?: ((totalMinor ?: 0) * 1000L / quantity).toInt()
        return FiscalItem(
            position = element.getAttribute("N").toIntOrNull(),
            name = element.getAttribute("NM").takeIf { it.isNotBlank() },
            barcode = element.getAttribute("CD").takeIf { it.isNotBlank() },
            quantity = quantity,
            unitPriceMinor = unitPriceMinor,
            totalMinor = totalMinor,
            taxGroup = element.getAttribute("TX").takeIf { it.isNotBlank() },
        )
    }

    private fun parsePayment(element: Element) = FiscalPayment(
        name = element.getAttribute("NM").takeIf { it.isNotBlank() },
        description = element.getAttribute("PC").takeIf { it.isNotBlank() },
        cardNumber = element.getAttribute("PD").takeIf { it.isNotBlank() },
        systemName = element.getAttribute("PSNM").takeIf { it.isNotBlank() },
        rrn = element.getAttribute("RRN").takeIf { it.isNotBlank() },
        amountMinor = element.getAttribute("SM").toIntOrNull(),
    )

    private fun String.toEpochMillis(): Long? = try {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        format.timeZone = TimeZone.getDefault()
        format.parse(this)?.time
    } catch (_: Exception) {
        null
    }

    private fun elements(parent: Element, tag: String): List<Element> {
        val list = parent.getElementsByTagName(tag)
        return (0 until list.length).mapNotNull { list.item(it) as? Element }
    }

    private fun firstElement(parent: Element, tag: String): Element? =
        elements(parent, tag).firstOrNull()
}