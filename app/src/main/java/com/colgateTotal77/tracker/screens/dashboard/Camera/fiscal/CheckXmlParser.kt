package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

import javax.xml.parsers.DocumentBuilderFactory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.w3c.dom.Element
import java.io.IOException

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

        val document = factory.newDocumentBuilder().parse(xmlBytes.inputStream())
        document.documentElement.normalize()
        val root = document.documentElement

        return when {
            root.tagName.equals("CHECK", ignoreCase = true) -> PrroCheckParser.parse(root)
            root.tagName.equals("RQ", ignoreCase = true)    -> RroCheckParser.parse(root)
            else -> throw IOException("Unknown check format: root <${root.tagName}>")
        }
    }
}

internal fun elements(parent: Element, tag: String): List<Element> {
    val list = parent.getElementsByTagName(tag)
    return (0 until list.length).mapNotNull { list.item(it) as? Element }
}

internal fun firstElement(parent: Element, tag: String): Element? =
    elements(parent, tag).firstOrNull()

internal fun Element.textOf(tag: String): String? =
    elements(this, tag).firstOrNull()?.textContent

internal fun Element.attr(name: String): String? =
    getAttribute(name).takeIf { it.isNotBlank() }

internal fun String.toMinorMinor(): Int? =
    toBigDecimalOrNull()?.multiply(BigDecimal(100))?.toInt()

internal fun String.toEpochMillis(pattern: String): Long? = try {
    val format = SimpleDateFormat(pattern, Locale.US)
    format.timeZone = TimeZone.getDefault()
    format.parse(this)?.time
} catch (_: Exception) {
    null
}
