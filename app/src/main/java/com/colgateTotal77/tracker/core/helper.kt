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

fun formatMoney(minorUnits: Int): String {
    return BigDecimal(minorUnits)
        .divide(BigDecimal(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
}

fun String.filterDigits() = filter { it.isDigit() }
fun String.filterDecimal() = filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')

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