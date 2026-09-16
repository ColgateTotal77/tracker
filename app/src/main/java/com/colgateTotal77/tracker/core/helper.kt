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
