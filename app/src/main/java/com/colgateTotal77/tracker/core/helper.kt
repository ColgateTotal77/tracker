package com.colgateTotal77.tracker.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatMoney(minorUnits: Number): String {
    return BigDecimal(minorUnits.toLong())
        .divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        .toPlainString()
}

fun parseInputToMinor(input: String?): Long? {
    return runCatching {
        input?.toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.longValueExact()
    }.getOrNull()
}


fun String.filterDecimal() = filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')
fun String.filterDecimalWithMinus() = (if (startsWith("-")) "-" else "") + removePrefix("-").filterDecimal()

enum class MeasureUnit(val label: String) {
    PIECE("pcs"),
    KG("kg"),
    G("g")
}
private fun String.hasWeightMarker(): Boolean {
    val tokens = lowercase().split(Regex("""[^\p{L}\p{N}.]+"""))
    return tokens.any { it == "ваг" || it == "ваг." || it.startsWith("вагов") }
}

fun getMeasureUnit(quantity: Int, name: String?): MeasureUnit {
    val isWeight = quantity % 1000 != 0 || name?.hasWeightMarker() == true

    return when {
        !isWeight -> MeasureUnit.PIECE
        quantity >= 1000 -> MeasureUnit.KG
        else -> MeasureUnit.G
    }
}

fun convertQuantityString(currentValue: String, fromUnit: MeasureUnit, toUnit: MeasureUnit): String {
    if (fromUnit == toUnit) return currentValue
    val value = currentValue.toBigDecimalOrNull() ?: return currentValue

    val thousand = BigDecimal("1000")

    val newValue = when (fromUnit) {
        MeasureUnit.G -> value.divide(thousand)
        MeasureUnit.KG, MeasureUnit.PIECE -> if (toUnit == MeasureUnit.G) value * thousand else value
    }

    return newValue.stripTrailingZeros().toPlainString()
}